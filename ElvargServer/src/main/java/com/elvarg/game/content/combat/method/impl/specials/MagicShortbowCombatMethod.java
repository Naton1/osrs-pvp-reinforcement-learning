package com.elvarg.game.content.combat.method.impl.specials;

import com.elvarg.game.content.combat.CombatFactory;
import com.elvarg.game.content.combat.CombatSpecial;
import com.elvarg.game.content.combat.hit.PendingHit;
import com.elvarg.game.content.combat.method.impl.RangedCombatMethod;
import com.elvarg.game.content.combat.ranged.RangedData;
import com.elvarg.game.content.combat.ranged.RangedData.RangedWeapon;
import com.elvarg.game.content.combat.ranged.RangedData.RangedWeaponType;
import com.elvarg.game.entity.impl.Mobile;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.Animation;
import com.elvarg.game.model.Graphic;
import com.elvarg.game.model.GraphicHeight;
import com.elvarg.game.model.Projectile;
import com.elvarg.game.model.Priority;

public class MagicShortbowCombatMethod extends RangedCombatMethod {

    private static final Animation ANIMATION = new Animation(1074, Priority.HIGH);
    private static final Graphic GRAPHIC = new Graphic(250, GraphicHeight.HIGH, Priority.HIGH);
    private static final Projectile PROJECTILE1 = new Projectile(249, 43, 31, 40, 57);
    private static final Projectile PROJECTILE2 = new Projectile(249, 48, 31, 33, 57);

    @Override
    public PendingHit[] hits(Mobile character, Mobile target) {
        int distance = character.getLocation().getChebyshevDistance(target.getLocation());
        int delay = RangedData.hitDelay(distance, RangedWeaponType.SHORTBOW);
        return new PendingHit[]{new PendingHit(character, target, this, true, delay), new PendingHit(character, target, this, true, delay)};
    }

    @Override
    public boolean canAttack(Mobile character, Mobile target) {
        Player player = character.getAsPlayer();
        if (player.getCombat().getRangedWeapon() != RangedWeapon.MAGIC_SHORTBOW) {
            return false;
        }        
        if (!CombatFactory.checkAmmo(player, 2)) {
            return false;
        }
        return true;
    }

    @Override
    public void start(Mobile character, Mobile target) {
        final Player player = character.getAsPlayer();
        CombatSpecial.drain(player, CombatSpecial.MAGIC_SHORTBOW.getDrainAmount());
        player.performAnimation(ANIMATION);
        player.performGraphic(GRAPHIC);
        Projectile.sendProjectile(player, target, PROJECTILE1);
        Projectile.sendProjectile(character, target, PROJECTILE2);
        CombatFactory.decrementAmmo(player, target.getLocation(), 2);
    }

    @Override
    public int attackSpeed(Mobile character) {
        return super.attackSpeed(character) + 1;
    }
}