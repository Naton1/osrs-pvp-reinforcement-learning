package com.elvarg.game.content.combat.magic;

import com.elvarg.game.entity.impl.Mobile;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.Item;

import java.util.Optional;

/**
 * A {@link Spell} implementation primarily used for spells that have effects
 * when they hit the player.
 *
 * @author lare96
 */
public abstract class CombatEffectSpell extends CombatSpell {

    @Override
    public int maximumHit() {

        // These types of spells don't have a 'hit'.
        return -1;
    }

    @Override
    public Optional<Item[]> equipmentRequired(Player player) {

        // These types of spells never require any equipment, although the
        // method can still be overridden if by some chance a spell does.
        return Optional.empty();
    }

    @Override
    public void finishCast(Mobile cast, Mobile castOn, boolean accurate,
                           int damage) {
        if (accurate) {
            spellEffect(cast, castOn);
        }
    }

    /**
     * The effect that will take place once the spell hits the target.
     *
     * @param cast   the entity casting the spell.
     * @param castOn the entity being hit by the spell.
     */
    public abstract void spellEffect(Mobile cast, Mobile castOn);
}
