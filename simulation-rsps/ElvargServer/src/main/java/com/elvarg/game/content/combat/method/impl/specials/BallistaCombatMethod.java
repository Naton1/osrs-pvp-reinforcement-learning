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

public class BallistaCombatMethod extends RangedCombatMethod {

    private static final Animation ANIMATION = new Animation(7222, Priority.HIGH);
    private static final Projectile PROJECTILE = new Projectile(1301, 43, 31, 70, 30);

    @Override
    public PendingHit[] hits(Mobile character, Mobile target) {
        return new PendingHit[]{new PendingHit(character, target, this, 2)};
    }

    @Override
    public boolean canAttack(Mobile character, Mobile target) {
        if (!character.isPlayer()) {
            return false;
        }
        Player player = character.getAsPlayer();
        if (player.getCombat().getRangedWeapon() != RangedWeapon.BALLISTA) {
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
        CombatSpecial.drain(player, CombatSpecial.BALLISTA.getDrainAmount());
        character.performAnimation(ANIMATION);
        Projectile.sendProjectile(player, target, PROJECTILE);
        CombatFactory.decrementAmmo(player, target.getLocation(), 1);
    }
}