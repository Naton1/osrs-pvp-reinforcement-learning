package com.elvarg.game.content.combat.method.impl.specials;

import com.elvarg.game.content.combat.CombatFactory;
import com.elvarg.game.content.combat.CombatSpecial;
import com.elvarg.game.content.combat.hit.PendingHit;
import com.elvarg.game.content.combat.method.impl.RangedCombatMethod;
import com.elvarg.game.content.combat.ranged.RangedData.RangedWeapon;
import com.elvarg.game.entity.impl.Mobile;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.Animation;
import com.elvarg.game.model.Projectile;
import com.elvarg.game.model.Priority;

public class ArmadylCrossbowCombatMethod extends RangedCombatMethod {

    private static final Animation ANIMATION = new Animation(4230, Priority.HIGH);
    private static final Projectile PROJECTILE = new Projectile(301, 44, 35, 50, 70);

    @Override
    public PendingHit[] hits(Mobile character, Mobile target) {
        return new PendingHit[] { new PendingHit(character, target, this, 2) };
    }

    @Override
    public boolean canAttack(Mobile character, Mobile target) {
        Player player = character.getAsPlayer();
        if (player.getCombat().getRangedWeapon() != RangedWeapon.ARMADYL_CROSSBOW) {
            return false;
        }
        if (!CombatFactory.checkAmmo(player, 1)) {
            return false;
        }
        return true;
    }

    @Override
    public void start(Mobile character, Mobile target) {
        final Player player = character.getAsPlayer();        
        CombatSpecial.drain(player, CombatSpecial.ARMADYL_CROSSBOW.getDrainAmount());
        player.performAnimation(ANIMATION);
        Projectile.sendProjectile(character, target, PROJECTILE);
        CombatFactory.decrementAmmo(player, target.getLocation(), 1);
    }
}