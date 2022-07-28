package com.elvarg.game.entity.impl.playerbot.interaction;

import com.elvarg.game.GameConstants;
import com.elvarg.game.content.Food;
import com.elvarg.game.content.combat.CombatSpecial;
import com.elvarg.game.content.combat.bountyhunter.BountyHunter;
import com.elvarg.game.content.presets.Presetables;
import com.elvarg.game.entity.impl.Mobile;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.entity.impl.playerbot.PlayerBot;
import com.elvarg.game.entity.impl.playerbot.fightstyle.PlayerBotFightLogic;
import com.elvarg.game.entity.impl.playerbot.fightstyle.WeaponSwitch;
import com.elvarg.game.model.Item;
import com.elvarg.game.model.ItemInSlot;
import com.elvarg.game.model.Skill;
import com.elvarg.game.model.container.impl.Equipment;
import com.elvarg.game.model.teleportation.TeleportHandler;
import com.elvarg.game.model.teleportation.TeleportType;
import com.elvarg.game.task.Task;
import com.elvarg.game.task.TaskManager;
import com.elvarg.net.packet.impl.EquipPacketListener;
import com.elvarg.util.Misc;

import java.util.Optional;
import java.util.stream.IntStream;

import static com.elvarg.game.entity.impl.playerbot.commands.LoadPreset.LOAD_PRESET_BUTTON_ID;

public class CombatInteraction {

    // The PlayerBot this interaction belongs to
    PlayerBot playerBot;

    // The percentage of health a Player Bot will eat at (and below)
    private static final int HEAL_AT_HEALTH_PERCENT = 40;

    public CombatInteraction(PlayerBot _playerBot) {
        this.playerBot = _playerBot;
    }

    public void process() {
        PlayerBotFightLogic fightStyle = this.playerBot.getDefinition().getFightLogic();
        var attacker = this.playerBot.getCombat().getAttacker();
        if (fightStyle != null && attacker != null) {
            boolean shouldSwitchBackToMainWeapon = true;

            for (WeaponSwitch weaponSwitch : fightStyle.getWeaponSwitches()) {
                if (!weaponSwitch.shouldUse(this.playerBot, attacker)) {
                    continue;
                }

                weaponSwitch.beforeUse(playerBot);

                var switchWeapon = ItemInSlot.getFromInventory(weaponSwitch.getItemId(), this.playerBot.getInventory());

                if (switchWeapon != null) {
                    EquipPacketListener.equipFromInventory(this.playerBot, switchWeapon);
                }
                if (weaponSwitch.getCombatSpell() != null) {
                    this.playerBot.getCombat().setCastSpell(weaponSwitch.getCombatSpell());
                }

                weaponSwitch.afterUse(playerBot);

                this.playerBot.getCombat().attack(attacker);

                // We are switching to a new weapon so don't switch back
                shouldSwitchBackToMainWeapon = false;
                break; // No need to process any more weapon switches
            }

            // Switch back to main weapon
            Item equippedWeapon = this.playerBot.getEquipment().getItems()[Equipment.WEAPON_SLOT];
            var mainWeaponInInventory = ItemInSlot.getFromInventory(fightStyle.getMainWeaponId(), this.playerBot.getInventory());
            if (mainWeaponInInventory != null && shouldSwitchBackToMainWeapon && equippedWeapon.getId() != fightStyle.getMainWeaponId()) {
                EquipPacketListener.equipFromInventory(this.playerBot, mainWeaponInInventory);
                // Deactivate special attack
                if (playerBot.isSpecialActivated()) {
                    CombatSpecial.activate(playerBot);
                }
            }
        }
    }

    // Called when the PlayerBot takes damage
    public void takenDamage(int damage, Mobile attacker) {
        int finalHitpoints = this.playerBot.getHitpoints() - damage;
        if (finalHitpoints <= 0 || attacker == null) {
            // We're already gonna be dead XD
            return;
        }

        this.handleEating(finalHitpoints);
    }

    private void handleEating(int finalHitpoints) {
        float max = this.playerBot.getSkillManager().getMaxLevel(Skill.HITPOINTS);
        if (finalHitpoints <= ((max / 100) * CombatInteraction.HEAL_AT_HEALTH_PERCENT)) {
            // Player Bot needs to eat
            int slot = edibleItemSlot();
            if (slot == -1) {
                return;
            }
            Item item =  this.playerBot.getInventory().get(slot);
            Food.consume(this.playerBot, item.getId(), slot);
        }
    }

    private int edibleItemSlot() {
        for (Food.Edible f : Food.Edible.values()) {

            int[] itemIds = this.playerBot.getInventory().getItemIdsArray();

            int slot = IntStream.range(0, itemIds.length)
                    .filter(i -> f.getItem().getId() == itemIds[i])
                    .findFirst()
                    .orElse(-1);

            if (slot > -1) {
                // We've found an item with a special attack
                return slot;
            }
        }

        return -1;
    }

    // Called when the Player Bot is just about to die
    public void handleDying(Optional<Player> killer) {
        if (killer.isPresent()) {
            this.playerBot.sendChat("Gf " + killer.get().getUsername());
        }
    }

    // Called when the Player Bot has died
    public void handleDeath(Optional<Player> killer) {
        if (killer.isPresent()) {
            BountyHunter.onDeath(killer.get(), this.playerBot, false, 80);
        }
        // For the most part, keep behaviour as Player-like as possible
        this.playerBot.getInventory().resetItems().refreshItems();
        this.playerBot.getEquipment().resetItems().refreshItems();

        this.playerBot.resetAttributes();
        this.playerBot.moveTo(GameConstants.DEFAULT_LOCATION);

        TaskManager.submit(new Task(Misc.randomInclusive(10,20), playerBot, false) {
            @Override
            protected void execute() {
                // Load this Bot's preset
                playerBot.setCurrentPreset(playerBot.getDefinition().getPreset());
                Presetables.handleButton(playerBot, LOAD_PRESET_BUTTON_ID);

                // Teleport this bot back to their home location after some time
                TeleportHandler.teleport(playerBot, playerBot.getDefinition().getSpawnLocation(), TeleportType.NORMAL, false);
                stop();
            }
        });
    }

    // Called when this bot is assigned a Player target in the wilderness
    public void targetAssigned(Player target) {
        if (this.playerBot.getArea() == null || this.playerBot.getArea().getPlayers().size() > 1 || Misc.randomInclusive(1,3) != 1) {
            // Don't attack if there's another real player in the same area, and attack 1/3 times
            return;
        }

        this.playerBot.getCombat().attack(target);
    }
}
