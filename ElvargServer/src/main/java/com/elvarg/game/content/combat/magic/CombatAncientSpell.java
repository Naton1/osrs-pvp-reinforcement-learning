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

        // The spell doesn't support multiple targets or we aren't in a
        // multicombat zone, so do nothing.
        if (spellRadius() == 0/* || !Locations.Location.inMulti(castOn)*/) {
            return;
        }

        // We passed the checks, so now we do multiple target stuff.
        Iterator<? extends Mobile> it = null;
        if (cast.isPlayer() && castOn.isPlayer()) {
            it = ((Player) cast).getLocalPlayers().iterator();
        } else if (cast.isPlayer() && castOn.isNpc()) {
            it = ((Player) cast).getLocalNpcs().iterator();
        } else if (cast.isNpc() && castOn.isNpc()) {
            it = World.getNpcs().iterator();
        } else if (cast.isNpc() && castOn.isPlayer()) {
            it = World.getPlayers().iterator();
        }

        for (Iterator<? extends Mobile> $it = it; $it.hasNext(); ) {
            Mobile next = $it.next();

            if (next == null) {
                continue;
            }

            if (next.isNpc()) {
                NPC n = (NPC) next;
                if (!n.getDefinition().isAttackable()) {
                    continue;
                }
            } else {
                Player p = (Player) next;
                if (!(AreaManager.canAttack(cast, p)) || !AreaManager.inMulti(p)) {
                    continue;
                }
            }


            if (next.getLocation().isWithinDistance(castOn.getLocation(), spellRadius()) && !next.equals(cast) && !next.equals(castOn) && next.getHitpoints() > 0 && next.getHitpoints() > 0) {
                PendingHit hit = new PendingHit(cast, next, CombatFactory.MAGIC_COMBAT, true, 0).setHandleAfterHitEffects(false);
                if (hit.isAccurate()) {
                    endGraphic().ifPresent(next::performGraphic);
                    spellEffect(cast, next, hit.getTotalDamage());
                } else {
                    next.performGraphic(MagicCombatMethod.SPLASH_GRAPHIC);
                }
                CombatFactory.addPendingHit(hit);
            }
        }
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
    public abstract void spellEffect(Mobile cast, Mobile castOn, int damage);

    /**
     * The radius of this spell, only comes in effect when the victim is hit in
     * a multicombat area.
     *
     * @return how far from the target this spell can hit when targeting
     * multiple entities.
     */
    public abstract int spellRadius();
}
