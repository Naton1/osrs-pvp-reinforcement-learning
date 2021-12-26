package com.elvarg.game.content.combat.method.impl;

import com.elvarg.game.content.combat.CombatType;
import com.elvarg.game.content.combat.WeaponInterfaces.WeaponInterface;
import com.elvarg.game.content.combat.hit.PendingHit;
import com.elvarg.game.content.combat.method.CombatMethod;
import com.elvarg.game.entity.impl.Mobile;
import com.elvarg.game.model.Animation;

public class MeleeCombatMethod extends CombatMethod {

    @Override
    public void start(Mobile character, Mobile target) {
        int animation = character.getAttackAnim();
        if (animation != -1) {
            character.performAnimation(new Animation(animation));
        }
    }
    
	@Override
	public CombatType type() {
		return CombatType.MELEE;
	}

	@Override
	public PendingHit[] hits(Mobile character, Mobile target) {
        return new PendingHit[] { new PendingHit(character, target, this) };
	}

	@Override
	public int attackDistance(Mobile character) {
        if (character.isPlayer() && character.getAsPlayer().getWeapon() == WeaponInterface.HALBERD) {
            return 2;
        }
		return 1;
	}
}
