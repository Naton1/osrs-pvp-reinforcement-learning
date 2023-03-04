package com.elvarg.game.content.combat.method.impl;

import com.elvarg.game.content.sound.SoundManager;
import com.elvarg.game.content.combat.CombatFactory;
import com.elvarg.game.content.combat.CombatType;
import com.elvarg.game.content.combat.WeaponInterfaces.WeaponInterface;
import com.elvarg.game.content.combat.formula.DamageFormulas;
import com.elvarg.game.content.combat.hit.PendingHit;
import com.elvarg.game.content.combat.method.CombatMethod;
import com.elvarg.game.entity.impl.Mobile;
import com.elvarg.game.model.Animation;
import com.elvarg.game.model.Graphic;
import com.elvarg.util.Misc;

public class MeleeCombatMethod extends CombatMethod {

    @Override
    public void start(Mobile character, Mobile target) {
        int animation = character.getAttackAnim();
        if (animation != -1) {
            character.performAnimation(new Animation(animation));
            SoundManager.sendSound(character.getAsPlayer(), character.getAttackSound());
        }
    }

    @Override
    public CombatType type() {
        return CombatType.MELEE;
    }

    @Override
    public PendingHit[] hits(Mobile character, Mobile target) {
        if (CombatFactory.fullVeracs(character) && Misc.getRandom(4) == 1) {
        	if(!character.isNpc()) //gfx does not play on npcs.
        		target.performGraphic(new Graphic(1041));
        	return new PendingHit[]{PendingHit.create(character, target, this,  Misc.inclusive(1, DamageFormulas.calculateMaxMeleeHit(character)), true)};
        }
        return new PendingHit[]{new PendingHit(character, target, this)};
    }

    @Override
    public int attackDistance(Mobile character) {
        if (character.isPlayer() && character.getAsPlayer().getWeapon() == WeaponInterface.HALBERD) {
            return 2;
        }
        return 1;
    }
}
