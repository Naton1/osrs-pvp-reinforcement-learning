package com.elvarg.game.entity.impl.playerbot.interaction;

import com.elvarg.game.GameConstants;
import com.elvarg.game.content.combat.CombatSpecial;
import com.elvarg.game.content.presets.Presetables;
import com.elvarg.game.entity.impl.Mobile;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.entity.impl.playerbot.PlayerBot;
import com.elvarg.game.model.Item;
import com.elvarg.game.model.container.impl.Inventory;
import com.elvarg.game.task.Task;
import com.elvarg.game.task.TaskManager;
import com.elvarg.net.packet.impl.EquipPacketListener;
import org.apache.commons.lang.ArrayUtils;

import java.util.Optional;
import java.util.stream.IntStream;

import static com.elvarg.game.entity.impl.playerbot.commands.LoadPreset.LOAD_PRESET_BUTTON_ID;

public class CombatInteraction {

    // The PlayerBot this interaction belongs to
    PlayerBot playerBot;

    public CombatInteraction(PlayerBot _playerBot) {
        this.playerBot = _playerBot;
    }

    // Called when the PlayerBot takes damage
    public void takenDamage(int damage, Mobile attacker) {
        if ((this.playerBot.getHitpoints() - damage) <= 0) {
            // We're already gonna be dead XD
            return;
        }

        this.handleSpecialAttack();
    }

    private void handleSpecialAttack() {
        if(this.playerBot.getCombatSpecial() != null && this.playerBot.getSpecialPercentage() >= playerBot.getCombatSpecial().getDrainAmount()) {
            // The weapon The PlayerBot is holding has a special attack
            CombatSpecial.activate(this.playerBot);
        } else if (this.playerBot.getCombatSpecial() == null && this.playerBot.getSpecialPercentage() > 40) {
            // Check if the player has a special item in their inventory to switch to
            int slot = specialAttackItemSlot();
            Item item =  this.playerBot.getInventory().get(slot);
            EquipPacketListener.equip(this.playerBot, item.getId(), slot, Inventory.INTERFACE_ID);
        }
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
    public void handleDeath() {
        this.playerBot.moveTo(GameConstants.DEFAULT_LOCATION);

        // Load this Bot's preset
        this.playerBot.setCurrentPreset(Presetables.GLOBAL_PRESETS[this.playerBot.getDefinition().getPresetIndex()]);
        Presetables.handleButton(this.playerBot, LOAD_PRESET_BUTTON_ID);

        TaskManager.submit(new Task(10, playerBot, false) {
            @Override
            protected void execute() {
                // Teleport this bot back to their home location after some time
                playerBot.moveTo(playerBot.getDefinition().getSpawnLocation());
                stop();
            }
        });
    }
}
