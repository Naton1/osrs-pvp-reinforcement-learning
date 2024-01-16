package com.elvarg.game.content.combat.magic;

import com.elvarg.game.World;
import com.elvarg.game.content.combat.CombatFactory;
import com.elvarg.game.content.combat.hit.PendingHit;
import com.elvarg.game.content.combat.method.impl.MagicCombatMethod;
import com.elvarg.game.entity.impl.Mobile;
import com.elvarg.game.entity.impl.npc.NPC;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.Item;
import com.elvarg.game.model.MagicSpellbook;
import com.elvarg.game.model.areas.AreaManager;

import java.util.Iterator;
import java.util.Optional;

/**
 * A {@link CombatSpell} implementation that is primarily used for spells that
 * are a part of the ancients spellbook.
 *
 * @author lare96
 */
public abstract class CombatAncientSpell extends CombatSpell {

    @Override
    public MagicSpellbook getSpellbook() {
        return MagicSpellbook.ANCIENT;
    }

    @Override
    public void finishCast(Mobile cast, Mobile castOn, boolean accurate,
                           int damage) {

        // The spell wasn't accurate, so do nothing.
        if (!accurate || damage <= 0) {
            return;
        }

        // Do the spell effect here.
        spellEffect(cast, castOn, damage);
    }

    @Override
    public Optional<Item[]> equipmentRequired(Player player) {

        // Ancient spells never require any equipment, although the method can
        // still be overridden if by some chance a spell does.
        return Optional.empty();
    }

    /**
     * The effect this spell has on the target.
     *
     * @param cast   the entity casting this spell.
     * @param castOn the person being hit by this spell.
     * @param damage the damage inflicted.
     */
    public void spellEffect(Mobile cast, Mobile castOn, int damage) {};

    /**
     * The radius of this spell, only comes in effect when the victim is hit in
     * a multicombat area.
     *
     * @return how far from the target this spell can hit when targeting
     * multiple entities.
     */
    public abstract int spellRadius();
}
