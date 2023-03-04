package com.elvarg.game.content.combat.method.impl;

import com.elvarg.game.content.sound.Sound;
import com.elvarg.game.content.sound.SoundManager;
import com.elvarg.game.content.combat.CombatFactory;
import com.elvarg.game.content.combat.CombatType;
import com.elvarg.game.content.combat.FightType;
import com.elvarg.game.content.combat.hit.PendingHit;
import com.elvarg.game.content.combat.method.CombatMethod;
import com.elvarg.game.content.combat.ranged.RangedData;
import com.elvarg.game.content.combat.ranged.RangedData.Ammunition;
import com.elvarg.game.content.combat.ranged.RangedData.RangedWeapon;
import com.elvarg.game.content.combat.ranged.RangedData.RangedWeaponType;
import com.elvarg.game.entity.impl.Mobile;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.Animation;
import com.elvarg.game.model.Location;
import com.elvarg.game.model.Projectile;

public class RangedCombatMethod extends CombatMethod {

    @Override
    public CombatType type() {
        return CombatType.RANGED;
    }

    @Override
    public PendingHit[] hits(Mobile character, Mobile target) {
        int distance = character.getLocation().getChebyshevDistance(target.getLocation());
        RangedWeaponType type = character.getCombat().getRangedWeapon().getType();
        int delay = RangedData.hitDelay(distance, type);
        
        if (character.getCombat().getRangedWeapon() == RangedWeapon.DARK_BOW) {
            return new PendingHit[] { new PendingHit(character, target, this, delay), new PendingHit(character, target, this, RangedData.dbowArrowDelay(distance)) };
        }

        return new PendingHit[]{new PendingHit(character, target, this, delay)};
    }

    @Override
    public boolean canAttack(Mobile character, Mobile target) {
        if (character.isNpc()) {
            return true;
        }

        Player p = character.getAsPlayer();

        int ammoRequired = 1;
        if (p.getCombat().getRangedWeapon() == RangedWeapon.DARK_BOW) {
            ammoRequired = 2;
        }
        if (!CombatFactory.checkAmmo(p, ammoRequired)) {
            return false;
        }
        return true;
    }

    @Override
    public void start(Mobile character, Mobile target) {
        final Ammunition ammo = character.getCombat().getAmmunition();
        final RangedWeapon rangedWeapon = character.getCombat().getRangedWeapon();
        final int animation = character.getAttackAnim();

        if (animation != -1) {
            character.performAnimation(new Animation(animation));
        }

        if (ammo != null && ammo.getStartGraphic() != null) {

            // Check toxic blowpipe, it shouldn't have any start gfx.
            if (character.getCombat().getRangedWeapon() != null) {
                if (character.getCombat().getRangedWeapon() == RangedWeapon.TOXIC_BLOWPIPE) {
                    return;
                }
            }

            // Perform start gfx for ammo
            character.performGraphic(ammo.getStartGraphic());
        }

        if (ammo == null || rangedWeapon == null) {
            return;
        }

        int projectileId = ammo.getProjectileId();
        int delay = 40;
        int speed = 57;
        int heightEnd = 31;
        int heightStart = 43;

        if (rangedWeapon.getType() == RangedWeaponType.CROSSBOW) {
            delay = 46;
            speed = 62;
            heightStart = 44;
            heightEnd = 35;
        } else if (rangedWeapon.getType() == RangedWeaponType.LONGBOW) {
            speed = 70;
        } else if (rangedWeapon.getType() == RangedWeaponType.BLOWPIPE) {
            speed = 60;
            heightStart = 40;
            heightEnd = 35;
        }
        if (ammo == Ammunition.TOKTZ_XIL_UL) {
            delay = 30;
            speed = 55;
        }

        // Fire projectile
        Projectile.sendProjectile(character, target, new Projectile(projectileId, heightStart, heightEnd, delay, speed));

        // Send sound
        SoundManager.sendSound(character.getAsPlayer(), Sound.SHOOT_ARROW);

        // Dark bow sends two arrows, so send another projectile and delete another
        // arrow.
        if (rangedWeapon == RangedWeapon.DARK_BOW) {
            Projectile.sendProjectile(character, target, new Projectile(ammo.getProjectileId(), heightStart + 5, heightEnd, delay - 7, speed + 4));

            // Decrement 2 ammo if d bow
            if (character.isPlayer()) {
                CombatFactory.decrementAmmo(character.getAsPlayer(), target.getLocation(), 2);
            }

        } else {

            // Decrement 1 ammo
            if (character.isPlayer()) {
                CombatFactory.decrementAmmo(character.getAsPlayer(), target.getLocation(), 1);
            }
        }
    }

    @Override
    public int attackDistance(Mobile character) {
    	final RangedWeapon bow = character.getCombat().getRangedWeapon();
        if (bow != null) {

            if (character.isNpc() || character.isPlayer()
                    && character.getAsPlayer().getFightType() == bow.getType().getLongRangeFightType()) {
                return bow.getType().getLongRangeDistance();
            }
		    
			return bow.getType().getDefaultDistance();
		}
		return 6;
    }
}
