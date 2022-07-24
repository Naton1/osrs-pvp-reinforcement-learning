package com.elvarg.game.entity.impl.playerbot.interaction;

import com.elvarg.game.GameConstants;
import com.elvarg.game.content.Food;
import com.elvarg.game.content.combat.CombatSpecial;
import com.elvarg.game.content.presets.Presetables;
import com.elvarg.game.entity.impl.Mobile;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.entity.impl.playerbot.PlayerBot;
import com.elvarg.game.model.Item;
import com.elvarg.game.model.Skill;
import com.elvarg.game.model.container.impl.Inventory;
import com.elvarg.game.model.teleportation.TeleportHandler;
import com.elvarg.game.model.teleportation.TeleportType;
import com.elvarg.game.task.Task;
import com.elvarg.game.task.TaskManager;
import com.elvarg.net.packet.impl.EquipPacketListener;
import com.elvarg.util.Misc;
import org.apache.commons.lang.ArrayUtils;

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

    // Called when the PlayerBot takes damage
    public void takenDamage(int damage, Mobile attacker) {
        int finalHitpoints = this.playerBot.getHitpoints() - damage;
        if (finalHitpoints <= 0) {
            // We're already gonna be dead XD
            return;
        }

        this.handleSpecialAttack();

        this.handleEating(finalHitpoints);
    }

    private void handleSpecialAttack() {
        if(this.playerBot.getCombatSpecial() != null && this.playerBot.getSpecialPercentage() >= playerBot.getCombatSpecial().getDrainAmount()) {
            // The weapon The PlayerBot is holding has a special attack
            CombatSpecial.activate(this.playerBot);
        } else if (this.playerBot.getCombatSpecial() == null && this.playerBot.getSpecialPercentage() > 40) {
            // Check if the player has a special item in their inventory to switch to
            int slot = specialAttackItemSlot();
            if (slot == -1) {
                return;
            }
            Item item =  this.playerBot.getInventory().get(slot);
            EquipPacketListener.equip(this.playerBot, item.getId(), slot, Inventory.INTERFACE_ID);
        }
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

    // Get the inventory slot of an item with special attack
    private int specialAttackItemSlot() {
        for (CombatSpecial c : CombatSpecial.values()) {

            int[] itemIds = this.playerBot.getInventory().getItemIdsArray();

            int slot = IntStream.range(0, itemIds.length)
                    .filter(i -> ArrayUtils.contains(c.getIdentifiers(), itemIds[i]))
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
            this.playerBot.sendChat("Gg " + killer.get().getUsername());
        }
    }

    // Called when the Player Bot has died
    public void handleDeath(Optional<Player> killer) {
        // For the most part, keep behaviour as Player-like as possible
        this.playerBot.getInventory().resetItems().refreshItems();
        this.playerBot.getEquipment().resetItems().refreshItems();

        this.playerBot.resetAttributes();
        this.playerBot.moveTo(GameConstants.DEFAULT_LOCATION);

        TaskManager.submit(new Task(Misc.randomInclusive(10,20), playerBot, false) {
            @Override
            protected void execute() {
                // Load this Bot's preset
                playerBot.setCurrentPreset(Presetables.GLOBAL_PRESETS[playerBot.getDefinition().getPresetIndex()]);
                Presetables.handleButton(playerBot, LOAD_PRESET_BUTTON_ID);

                // Teleport this bot back to their home location after some time
                TeleportHandler.teleport(playerBot, playerBot.getDefinition().getSpawnLocation(), TeleportType.NORMAL, false);
                stop();
            }
        });
    }
}
