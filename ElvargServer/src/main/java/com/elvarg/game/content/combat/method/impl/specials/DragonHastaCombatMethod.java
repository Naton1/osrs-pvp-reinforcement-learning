package com.elvarg.game.content.combat.method.impl.specials;

import com.elvarg.game.content.combat.CombatFactory;
import com.elvarg.game.content.combat.CombatSpecial;
import com.elvarg.game.content.combat.CombatType;
import com.elvarg.game.content.combat.formula.AccuracyFormulasDpsCalc;
import com.elvarg.game.content.combat.formula.DamageFormulas;
import com.elvarg.game.content.combat.hit.PendingHit;
import com.elvarg.game.content.combat.method.CombatMethod;
import com.elvarg.game.entity.impl.Mobile;
import com.elvarg.game.model.Animation;
import com.elvarg.game.model.Graphic;
import com.elvarg.game.model.GraphicHeight;
import com.elvarg.game.model.Priority;
import com.elvarg.util.Misc;
import com.elvarg.util.timers.TimerKey;

/**
 * 
 * The dragon hasta has a special attack called Unleash, which drains the
 * player's special attack bar and attacks with a 5% boost in accuracy and a
 * 2.5% boost in damage for every 5% of special attack energy used. 
 * 
 * For example, a player with 100% special attack energy who performs the
 * special attack will have 100% increased accuracy (meaning the accuracy is
 * doubled) and a damage boost of 50%.
 * 
 *         Notes: IS WIP. USES SAME CB INTERFACE AS DSPEAR
 *         
 * @author Advocatus | https://www.rune-server.ee/members/119929-advocatus/
 *
 */
public class DragonHastaCombatMethod extends CombatMethod {

	private static final Animation ANIMATION = new Animation(7515, Priority.HIGH);
	private static final Graphic GRAPHIC = new Graphic(1369, GraphicHeight.HIGH, Priority.HIGH);// TODO check gfx height

	@Override
	public void start(Mobile character, Mobile target) {
		character.performAnimation(ANIMATION);
		character.performGraphic(GRAPHIC);

		int boosts = character.getSpecialPercentage() % 5;
		CombatSpecial.drain(character, boosts * 5);

		// TODO is there a sound?
		// SoundManager.sendSound(character.getAsPlayer(), 0);

		//multiply accuracy for the special attack.
		int attRoll = (int) (AccuracyFormulasDpsCalc.attackMeleeRoll(character) * (1.0 + (0.05 * boosts)));

		boolean accurate = AccuracyFormulasDpsCalc.rollMeleeAccuracy(character, target, attRoll);
		
		//multiply damage for the special attack.
		int damage = accurate ? (int) (Misc.inclusive(0, DamageFormulas.calculateMaxMeleeHit(character)) * (1.0 + (0.025 * boosts))) : 0;
		
		//PendingHit.create is used for bypassing protection prayers.
		//doing it this way bypasses spirit shields.
		CombatFactory.addPendingHit(PendingHit.create(character, target, this, damage, accurate));

		/*
		 * Generic information to add combat delay and turn off the special attack bar.
		 */
		character.getTimers().register(TimerKey.COMBAT_ATTACK, character.getBaseAttackSpeed());
		character.setSpecialActivated(false);
		if (character.isPlayer()) {
			CombatSpecial.updateBar(character.getAsPlayer());
		}
	}

	@Override
	public CombatType type() {
		return CombatType.MELEE;
	}

	@Override
	public PendingHit[] hits(Mobile character, Mobile target) {
		//return null so that the custom logic above can run.
		return null;
	}
}
