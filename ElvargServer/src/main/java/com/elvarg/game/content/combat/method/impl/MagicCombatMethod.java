package com.elvarg.game.content.combat.method.impl;

import com.elvarg.game.content.sound.Sound;
import com.elvarg.game.content.sound.SoundManager;
import com.elvarg.game.World;
import com.elvarg.game.content.combat.CombatFactory;
import com.elvarg.game.content.combat.CombatType;
import com.elvarg.game.content.combat.hit.PendingHit;
import com.elvarg.game.content.combat.magic.CombatAncientSpell;
import com.elvarg.game.content.combat.magic.CombatSpell;
import com.elvarg.game.content.combat.method.CombatMethod;
import com.elvarg.game.entity.impl.Mobile;
import com.elvarg.game.entity.impl.npc.NPC;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.Graphic;
import com.elvarg.game.model.GraphicHeight;
import com.elvarg.game.model.Location;
import com.elvarg.game.model.areas.AreaManager;
import com.google.common.collect.Streams;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Represents the combat method for magic attacks.
 *
 * @author Professor Oak
 */
public class MagicCombatMethod extends CombatMethod {

	public static final Graphic SPLASH_GRAPHIC = new Graphic(85, GraphicHeight.MIDDLE);

	@Override
	public CombatType type() {
		return CombatType.MAGIC;
	}

	@Override
	public PendingHit[] hits(Mobile character, Mobile target) {
		int delay =  1 + ((1 + character.getLocation().getChebyshevDistance(target.getLocation())) / 3);

		PendingHit[] hits = new PendingHit[]{new PendingHit(character, target, this, delay)};

		CombatSpell spell = character.getCombat().getSelectedSpell();

		if (spell == null) {
			return hits;
		}
		
		List<PendingHit> multiCombatHits = new ArrayList<>();

		for (PendingHit hit : hits) {
			spell.onHitCalc(hit);

			if (!hit.isAccurate()
					|| !(spell instanceof CombatAncientSpell combatAncientSpell)
					|| combatAncientSpell.spellRadius() <= 0) {
				continue;
			}

			// We passed the checks, so now we do multiple target stuff.
			Iterator<? extends Mobile> it = null;
			if (character.isPlayer() && target.isPlayer()) {
				it = ((Player) character).getLocalPlayers().iterator();
			} else if (character.isPlayer() && target.isNpc()) {
				it = ((Player) character).getLocalNpcs().iterator();
			} else if (character.isNpc() && target.isNpc()) {
				it = World.getNpcs().iterator();
			} else if (character.isNpc() && target.isPlayer()) {
				it = World.getPlayers().iterator();
			}

			List<PendingHit> pendingHits = Streams.stream(it).filter((next) -> {
				if (next == null) {
					return false;
				}

				if (next.isNpc()) {
					NPC n = (NPC) next;
					if (!n.getCurrentDefinition().isAttackable()) {
						return false;
					}
				} else {
					Player p = (Player) next;
					if (AreaManager.canAttack(character, p) != CombatFactory.CanAttackResponse.CAN_ATTACK || !AreaManager.inMulti(p)) {
						return false;
					}
				}
				return true;
			}).filter((next) ->
					next.getLocation().isWithinDistance(target.getLocation(), combatAncientSpell.spellRadius())
							&& !next.equals(character)
							&& !next.equals(target)
							&& next.getHitpoints() > 0)
			.map((next) -> new PendingHit(character, next, this, false, delay)).toList();

			for (PendingHit pendingHit : pendingHits) {
				multiCombatHits.add(pendingHit);
				spell.onHitCalc(pendingHit);
			}
		}
		if (multiCombatHits.size() > 0) {
			multiCombatHits.addAll(Arrays.asList(hits));
			PendingHit[] allHits = new PendingHit[multiCombatHits.size()];
			return multiCombatHits.toArray(allHits);
		}

		return hits;
	}

	@Override
	public boolean canAttack(Mobile character, Mobile target) {
		if (character.isNpc()) {
			return true;
		}

		// Set the current spell to the autocast spell if it's null.
		if (character.getCombat().getCastSpell() == null) {
			character.getCombat().setCastSpell(character.getCombat().getAutocastSpell());
		}

		// Character didn't have autocast spell either.
		if (character.getCombat().getCastSpell() == null) {
			return false;
		}

		return character.getCombat().getCastSpell().canCast(character.getAsPlayer(), true);
	}

	@Override
	public void start(Mobile character, Mobile target) {
		CombatSpell spell = character.getCombat().getSelectedSpell();

		if (spell != null) {
			spell.startCast(character, target);
		}
	}

	@Override
	public int attackSpeed(Mobile character) {

		if (character.getCombat().getPreviousCast() != null) {
			return character.getCombat().getPreviousCast().getAttackSpeed();
		}

		return super.attackSpeed(character);
	}

	@Override
	public int attackDistance(Mobile character) {
		return 10;
	}

	@Override
	public void finished(Mobile character, Mobile target) {

		// Reset the castSpell to autocastSpell
		// Update previousCastSpell so effects can be handled.

		final CombatSpell current = character.getCombat().getCastSpell();

		character.getCombat().setCastSpell(null);

		if (character.getCombat().getAutocastSpell() == null) {
			character.getCombat().reset();
			character.setMobileInteraction(target);
			character.getMovementQueue().reset();
		}

		character.getCombat().setPreviousCast(current);
	}

	@Override
	public void handleAfterHitEffects(PendingHit hit) {
		Mobile attacker = hit.getAttacker();
		Mobile target = hit.getTarget();
		boolean accurate = hit.isAccurate();
		int damage = hit.getTotalDamage();

		if (attacker.getHitpoints() <= 0 || target.getHitpoints() <= 0) {
			return;
		}

		CombatSpell previousSpell = attacker.getCombat().getPreviousCast();

		if (previousSpell != null) {

			if (accurate) {

				// Send proper end graphics for the spell because it was accurate
				previousSpell.endGraphic().ifPresent(target::performGraphic);
				SoundManager.sendSound(target.getAsPlayer(), previousSpell.impactSound());

			} else {

				// Send splash graphics for the spell because it wasn't accurate
				target.performGraphic(SPLASH_GRAPHIC);
				SoundManager.sendSound(attacker.getAsPlayer(), Sound.SPELL_FAIL_SPLASH);

			}

			previousSpell.finishCast(attacker, target, accurate, damage);

		}
	}
}
