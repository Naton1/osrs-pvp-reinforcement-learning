package com.elvarg.game.task.impl;

import com.elvarg.game.content.combat.CombatSpecial;
import com.elvarg.game.entity.impl.Mobile;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.task.Task;

/**
 * A {@link Task} implementation which handles
 * the regeneration of special attack.
 *
 * @author Professor Oak
 */
public class RestoreSpecialAttackTask extends Task {

    private final Mobile character;

    public RestoreSpecialAttackTask(Mobile character) {
        super(50, character, false);
        this.character = character;
        character.setRecoveringSpecialAttack(true);
    }

    @Override
    public void execute() {
        if (character == null || !character.isRegistered() || character.getSpecialPercentage() >= 100 || !character.isRecoveringSpecialAttack()) {
            character.setRecoveringSpecialAttack(false);
            stop();
            return;
        }
        int amount = character.getSpecialPercentage() + 10;
        if (amount >= 100) {
            amount = 100;
            character.setRecoveringSpecialAttack(false);
            stop();
        }
        character.setSpecialPercentage(amount);

        if (character.isPlayer()) {
            Player player = character.getAsPlayer();
            CombatSpecial.updateBar(player);
        }
    }
}