package com.elvarg.game.entity.impl.playerbot.interaction;

import com.elvarg.game.GameConstants;
import com.elvarg.game.content.Food;
import com.elvarg.game.content.PotionConsumable;
import com.elvarg.game.content.combat.CombatSpecial;
import com.elvarg.game.content.combat.bountyhunter.BountyHunter;
import com.elvarg.game.content.presets.Presetables;
import com.elvarg.game.definition.ItemDefinition;
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
import com.elvarg.util.ItemIdentifiers;
import com.elvarg.util.Misc;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

import static com.elvarg.game.content.PotionConsumable.*;
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
            if (playerBot.getMovementQueue().size() == 0) {
                this.playerBot.getCombat().attack(attacker);
            }
        }
        var area = this.playerBot.getArea();
        if (area != null && !area.getPlayers().isEmpty()) {
            this.potUp();
        }

        if (area != null && area.getPlayers().isEmpty()) {
            boolean shouldReset = this.playerBot.getInventory().getFreeSlots() > 5 || this.playerBot.getSpecialPercentage() < 50;

            if (shouldReset) {
                this.reset();
            }
        }
    }

    private void potUp() {
        // Boost range
        if (!playerBot.getSkillManager().isBoosted(Skill.RANGED)) {
            var pot = Arrays.stream(RANGE_POTIONS.getIds())
                    .mapToObj(id -> ItemInSlot.getFromInventory(id, this.playerBot.getInventory()))
                    .filter(Objects::nonNull)
                    .findFirst();

            if (pot.isPresent()) {
                PotionConsumable.drink(playerBot, pot.get().getId(), pot.get().getSlot());
                return;
            }
        }
        // Boost strength
        if (!playerBot.getSkillManager().isBoosted(Skill.STRENGTH)) {
            var pot = Arrays.stream(SUPER_STRENGTH_POTIONS.getIds())
                    .mapToObj(id -> ItemInSlot.getFromInventory(id, this.playerBot.getInventory()))
                    .filter(Objects::nonNull)
                    .findFirst();

            if (pot.isPresent()) {
                PotionConsumable.drink(playerBot, pot.get().getId(), pot.get().getSlot());
                return;
            }
        }
        //Boost attack
        if (!playerBot.getSkillManager().isBoosted(Skill.ATTACK)) {
            var pot = Arrays.stream(SUPER_ATTACK_POTIONS.getIds())
                    .mapToObj(id -> ItemInSlot.getFromInventory(id, this.playerBot.getInventory()))
                    .filter(Objects::nonNull)
                    .findFirst();

            if (pot.isPresent()) {
                PotionConsumable.drink(playerBot, pot.get().getId(), pot.get().getSlot());
                return;
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
            var edible = edibleItemSlot();
            if (edible == null) {
                return;
            }
            Food.consume(this.playerBot, edible.getId(), edible.getSlot());
            if (edible.getId() != ItemIdentifiers.COOKED_KARAMBWAN) {
                var karambwan = ItemInSlot.getFromInventory(ItemIdentifiers.COOKED_KARAMBWAN, this.playerBot.getInventory());
                if (karambwan != null) {
                    Food.consume(this.playerBot, karambwan.getId(), karambwan.getSlot());
                }
            }
        }
    }

    private ItemInSlot edibleItemSlot() {
        var edible = Arrays.stream(Food.Edible.values())
                .map(food -> ItemInSlot.getFromInventory(food.getItem().getId(), this.playerBot.getInventory()))
                .filter(Objects::nonNull)
                .findFirst();

        return edible.orElse(null);
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
        this.playerBot.setFollowing(null);
        this.playerBot.moveTo(GameConstants.DEFAULT_LOCATION);

        TaskManager.submit(new Task(Misc.randomInclusive(10,20), playerBot, false) {
            @Override
            protected void execute() {
                reset();
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

    private void reset() {
        // Load this Bot's preset
        playerBot.setCurrentPreset(playerBot.getDefinition().getPreset());
        Presetables.handleButton(playerBot, LOAD_PRESET_BUTTON_ID);

        // Teleport this bot back to their home location after some time
        TeleportHandler.teleport(playerBot, playerBot.getDefinition().getSpawnLocation(), TeleportType.NORMAL, false);

    }
}
