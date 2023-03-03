package com.elvarg.game.content.combat.method.impl.npcs;

import com.elvarg.game.content.PrayerHandler;
import com.elvarg.game.content.combat.CombatEquipment;
import com.elvarg.game.content.combat.CombatFactory;
import com.elvarg.game.content.combat.CombatType;
import com.elvarg.game.content.combat.hit.PendingHit;
import com.elvarg.game.content.combat.method.CombatMethod;
import com.elvarg.game.entity.impl.Mobile;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.Animation;
import com.elvarg.game.model.Projectile;
import com.elvarg.game.task.impl.CombatPoisonEffect.PoisonType;
import com.elvarg.util.Misc;

public class KingBlackDragonMethod extends CombatMethod {

    private CombatType currentAttackType = CombatType.MAGIC;
    private Breath currentBreath = Breath.DRAGON;

    @Override
    public void start(Mobile character, Mobile target) {
        if (currentAttackType == CombatType.MAGIC) {
            character.performAnimation(new Animation(84));
            Projectile.sendProjectile(character, target, currentBreath.projectile);
        } else if (currentAttackType == CombatType.MELEE) {
            character.performAnimation(new Animation(91));
        }
    }

    @Override
    public int attackSpeed(Mobile character) {
        return currentAttackType == CombatType.MAGIC ? 6 : 4;
    }

    @Override
    public int attackDistance(Mobile character) {
        return 8;
    }

    @Override
    public CombatType type() {
        return currentAttackType;
    }

    @Override
    public PendingHit[] hits(Mobile character, Mobile target) {
        PendingHit hit = new PendingHit(character, target, this, 1);
        if (target.isPlayer()) {
        	Player p = target.getAsPlayer();
            if (currentAttackType == CombatType.MAGIC && currentBreath == Breath.DRAGON) {
                if (PrayerHandler.isActivated(p, PrayerHandler.PROTECT_FROM_MAGIC) && CombatEquipment.hasDragonProtectionGear(p) && !p.getAsPlayer().getCombat().getFireImmunityTimer().finished()) {
                    target.getAsPlayer().getPacketSender().sendMessage("You're protected against the dragonfire breath.");
                    return new PendingHit[]{hit};
                }
                int extendedHit = 25;
                if (PrayerHandler.isActivated(p, PrayerHandler.PROTECT_FROM_MAGIC)) {
                    extendedHit -= 5;
                }
                if (!p.getAsPlayer().getCombat().getFireImmunityTimer().finished()) {
                    extendedHit -= 10;
                }
                if (CombatEquipment.hasDragonProtectionGear(p)) {
                    extendedHit -= 10;
                }
                p.getAsPlayer().getPacketSender().sendMessage("The dragonfire burns you.");
                hit.getHits()[0].incrementDamage(extendedHit);
            }
            if (currentAttackType == CombatType.MAGIC) {
                switch (currentBreath) {
                    case ICE:
                        CombatFactory.freeze(hit.getTarget().getAsPlayer(), 10);
                        break;
                    case POISON:
                        CombatFactory.poisonEntity(hit.getTarget().getAsPlayer(), PoisonType.SUPER);
                        break;
                    default:
                        break;

                }
            }
        }
        return new PendingHit[]{hit};
    }

    @Override
    public void finished(Mobile character, Mobile target) {
        if (character.getLocation().getDistance(target.getLocation()) <= 3) {
            if (Misc.randomInclusive(0, 2) == 0) {
                currentAttackType = CombatType.MAGIC;
            } else {
                currentAttackType = CombatType.MELEE;
            }
        } else {
            currentAttackType = CombatType.MAGIC;
        }

        if (currentAttackType == CombatType.MAGIC) {
            int random = Misc.randomInclusive(0, 10);
            if (random >= 0 && random <= 3) {
                currentBreath = Breath.DRAGON;
            } else if (random >= 4 && random <= 6) {
                currentBreath = Breath.SHOCK;
            } else if (random >= 7 && random <= 9) {
                currentBreath = Breath.POISON;
            } else {
                currentBreath = Breath.ICE;
            }
        }
    }

    private enum Breath {
        ICE(new Projectile(396, 31, 43, 40, 55)),
        POISON(new Projectile(394, 31, 43, 40, 55)),
        SHOCK(new Projectile(395, 31, 43, 40, 55)),
        DRAGON(new Projectile(393, 31, 43, 40, 55));

        private Projectile projectile;

        private Breath(Projectile projectile) {
            this.projectile = projectile;
        }
    }
}