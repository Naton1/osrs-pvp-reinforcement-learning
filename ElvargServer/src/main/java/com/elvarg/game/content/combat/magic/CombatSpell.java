package com.elvarg.game.content.combat.magic;

import com.elvarg.game.entity.impl.Mobile;
import com.elvarg.game.entity.impl.npc.NPC;
import com.elvarg.game.model.Animation;
import com.elvarg.game.model.Graphic;
import com.elvarg.game.model.Projectile;
import com.elvarg.game.task.Task;
import com.elvarg.game.task.TaskManager;

import java.util.Optional;

/**
 * A {@link Spell} implementation used for combat related spells.
 *
 * @author lare96
 */
public abstract class CombatSpell extends Spell {

    @Override
    public void startCast(Mobile cast, Mobile castOn) {

        int castAnimation = -1;

        NPC npc = cast.isNpc() ? ((NPC) cast) : null;
        /*if(npc != null) {
			if(npc.getId() == 3496 || npc.getId() == 6278 || npc.getId() == 2000 || npc.getId() == 109 || npc.getId() == 3580 || npc.getId() == 2007) {
				castAnimation = npc.getDefinition().getAttackAnim();
			}
		}*/
        
        if (castAnimation().isPresent() && castAnimation == -1) {
            castAnimation().ifPresent(cast::performAnimation);
        } else {
            cast.performAnimation(new Animation(castAnimation));
        }

        // Then send the starting graphic.
        if (npc != null) {
            if (npc.getId() != 2000 && npc.getId() != 109 && npc.getId() != 3580 && npc.getId() != 2007) {
                startGraphic().ifPresent(cast::performGraphic);
            }
        } else {
            startGraphic().ifPresent(cast::performGraphic);
        }

        // Finally send the projectile after two ticks.
        castProjectile(cast, castOn).ifPresent(g -> {
            //g.sendProjectile();
            TaskManager.submit(new Task(2, cast, false) {
                @Override
                public void execute() {
                    g.sendProjectile();
                    this.stop();
                }
            });
        });
    }

    public int getAttackSpeed() {
        int speed = 5;
        final CombatSpell spell = this;
        if (spell instanceof CombatAncientSpell) {

            if (spell == CombatSpells.SMOKE_RUSH.getSpell() || spell == CombatSpells.SHADOW_RUSH.getSpell()
                    || spell == CombatSpells.BLOOD_RUSH.getSpell() || spell == CombatSpells.ICE_RUSH.getSpell()
                    || spell == CombatSpells.SMOKE_BLITZ.getSpell() || spell == CombatSpells.SHADOW_BLITZ.getSpell()
                    || spell == CombatSpells.BLOOD_BLITZ.getSpell() || spell == CombatSpells.ICE_BLITZ.getSpell()) {
                speed = 4;
            }

        }
        return speed;
    }

    /**
     * The fixed ID of the spell implementation as recognized by the protocol.
     *
     * @return the ID of the spell, or <tt>-1</tt> if there is no ID for this
     * spell.
     */
    public abstract int spellId();

    /**
     * The maximum hit an {@link Mobile} can deal with this spell.
     *
     * @return the maximum hit able to be dealt with this spell implementation.
     */
    public abstract int maximumHit();

    /**
     * The animation played when the spell is cast.
     *
     * @return the animation played when the spell is cast.
     */
    public abstract Optional<Animation> castAnimation();

    /**
     * The starting graphic played when the spell is cast.
     *
     * @return the starting graphic played when the spell is cast.
     */
    public abstract Optional<Graphic> startGraphic();

    /**
     * The projectile played when this spell is cast.
     *
     * @param cast   the entity casting the spell.
     * @param castOn the entity targeted by the spell.
     * @return the projectile played when this spell is cast.
     */
    public abstract Optional<Projectile> castProjectile(Mobile cast,
                                                        Mobile castOn);

    /**
     * The ending graphic played when the spell hits the victim.
     *
     * @return the ending graphic played when the spell hits the victim.
     */
    public abstract Optional<Graphic> endGraphic();

    /**
     * Fired when the spell hits the victim.
     *
     * @param cast     the entity casting the spell.
     * @param castOn   the entity targeted by the spell.
     * @param accurate if the spell was accurate.
     * @param damage   the amount of damage inflicted by this spell.
     */
    public abstract void finishCast(Mobile cast, Mobile castOn,
                                    boolean accurate, int damage);
}