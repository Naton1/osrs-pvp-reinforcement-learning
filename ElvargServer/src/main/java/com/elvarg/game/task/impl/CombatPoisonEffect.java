package com.elvarg.game.task.impl;

import com.elvarg.game.content.combat.hit.HitDamage;
import com.elvarg.game.content.combat.hit.HitMask;
import com.elvarg.game.entity.impl.Mobile;
import com.elvarg.game.model.Item;
import com.elvarg.game.task.Task;
import com.elvarg.util.ItemIdentifiers;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * A {@link Task} implementation that handles the poisoning process.
 *
 * @author Professor Oak
 */
public class CombatPoisonEffect extends Task {

	/**
	 * The entity being inflicted with poison.
	 */
	private Mobile entity;

	private int tick;

	/**
	 * Create a new {@link CombatPoisonEffect}.
	 *
	 * @param entity
	 *            the entity being inflicted with poison.
	 */
	public CombatPoisonEffect(Mobile entity) {
		super(30, entity, false);
		this.entity = entity;
		this.tick = 0;
	}

	@Override
	public void execute() {
		this.tick++;

		// Stop the task if the entity is unregistered.
		if (!entity.isRegistered()) {
			this.stop();
			return;
		}

		// Stop the task if entity is no longer poisoned.
		if (!entity.isPoisoned()) {
			this.stop();
			return;
		}

		// Stop the task if entity is immune to poison..
		if (!entity.getCombat().getPoisonImmunityTimer().finished()) {
			this.stop();
			return;
		}

		if (entity.isImmuneToPoison()) {
			this.stop();
			return;
		}

		// Deal the damage, then try and decrement the damage count.
		int poisonDamage = (this.tick % 5 == 0) ? entity.getPoisonDamage() - 1 : entity.getPoisonDamage();
		entity.setPoisonDamage(poisonDamage);
		entity.getCombat().getHitQueue().addPendingDamage(new HitDamage(poisonDamage, HitMask.GREEN));

		if (poisonDamage <= 1) {
			this.stop();
			return;
		}
	}

	@Override
	public void stop() {
		entity.setPoisonDamage(0);

		// Reset client's poison type..
		if (entity.isPlayer()) {
			entity.getAsPlayer().getPacketSender().sendPoisonType(0);
		}

		super.stop();
	}

	/**
	 * Holds all of the different strengths of poisons.
	 *
	 * @author lare96
	 */
	public enum PoisonType {
		VERY_WEAK(2), WEAK(3), MILD(4), EXTRA(5), SUPER(6), VENOM(12);

		/**
		 * The starting damage for this poison type.
		 */
		private int damage;

		/**
		 * Create a new {@link PoisonType}.
		 *
		 * @param damage
		 *            the starting damage for this poison type.
		 */
		private PoisonType(int damage) {
			this.damage = damage;
		}

		/**
		 * Gets the starting damage for this poison type.
		 *
		 * @return the starting damage for this poison type.
		 */
		public int getDamage() {
			return damage;
		}
	}

	/**
	 * The small utility class that manages all of the combat poison data.
	 *
	 * @author lare96
	 * @author Advocatus
	 */
	public static final class CombatPoisonData {

		/**
		 * The map of all of the different weapons that poison.
		 */
		// Increase the capacity of the map as more elements are added.
		private static final Map<Integer, PoisonType> types = new HashMap<>(97);

		/**
		 * Default private constructor.
		 */
		private CombatPoisonData() {
		}

		/**
		 * Load all of the poison data.
		 */
		public static void init() {
			types.put(ItemIdentifiers.BRONZE_DART_P_, PoisonType.VERY_WEAK);
			types.put(ItemIdentifiers.IRON_DART_P_, PoisonType.VERY_WEAK);
			types.put(ItemIdentifiers.STEEL_DART_P_, PoisonType.VERY_WEAK);
			types.put(ItemIdentifiers.MITHRIL_DART_P_, PoisonType.VERY_WEAK);
			types.put(ItemIdentifiers.ADAMANT_DART_P_, PoisonType.VERY_WEAK);
			types.put(ItemIdentifiers.RUNE_DART_P_, PoisonType.VERY_WEAK);
			types.put(ItemIdentifiers.BRONZE_JAVELIN_P_, PoisonType.VERY_WEAK);
			types.put(ItemIdentifiers.IRON_JAVELIN_P_, PoisonType.VERY_WEAK);
			types.put(ItemIdentifiers.STEEL_JAVELIN_P_, PoisonType.VERY_WEAK);
			types.put(ItemIdentifiers.MITHRIL_JAVELIN_P_, PoisonType.VERY_WEAK);
			types.put(ItemIdentifiers.ADAMANT_JAVELIN_P_, PoisonType.VERY_WEAK);
			types.put(ItemIdentifiers.RUNE_JAVELIN_P_, PoisonType.VERY_WEAK);
			types.put(ItemIdentifiers.BRONZE_KNIFE_P_, PoisonType.VERY_WEAK);
			types.put(ItemIdentifiers.IRON_KNIFE_P_, PoisonType.VERY_WEAK);
			types.put(ItemIdentifiers.STEEL_KNIFE_P_, PoisonType.VERY_WEAK);
			types.put(ItemIdentifiers.MITHRIL_KNIFE_P_, PoisonType.VERY_WEAK);
			types.put(ItemIdentifiers.BLACK_KNIFE_P_, PoisonType.VERY_WEAK);
			types.put(ItemIdentifiers.ADAMANT_KNIFE_P_, PoisonType.VERY_WEAK);
			types.put(ItemIdentifiers.RUNE_KNIFE_P_, PoisonType.VERY_WEAK);
			types.put(ItemIdentifiers.BRONZE_BOLTS_P_, PoisonType.VERY_WEAK);
			types.put(ItemIdentifiers.BRONZE_ARROW_P_, PoisonType.VERY_WEAK);
			types.put(ItemIdentifiers.IRON_ARROW_P_, PoisonType.VERY_WEAK);
			types.put(ItemIdentifiers.STEEL_ARROW_P_, PoisonType.VERY_WEAK);
			types.put(ItemIdentifiers.MITHRIL_ARROW_P_, PoisonType.VERY_WEAK);
			types.put(ItemIdentifiers.ADAMANT_ARROW_P_, PoisonType.VERY_WEAK);
			types.put(ItemIdentifiers.RUNE_ARROW_P_, PoisonType.VERY_WEAK);
			types.put(ItemIdentifiers.IRON_DAGGER_P_, PoisonType.MILD);
			types.put(ItemIdentifiers.BRONZE_DAGGER_P_, PoisonType.MILD);
			types.put(ItemIdentifiers.STEEL_DAGGER_P_, PoisonType.MILD);
			types.put(ItemIdentifiers.MITHRIL_DAGGER_P_, PoisonType.MILD);
			types.put(ItemIdentifiers.ADAMANT_DAGGER_P_, PoisonType.MILD);
			types.put(ItemIdentifiers.RUNE_DAGGER_P_, PoisonType.MILD);
			types.put(ItemIdentifiers.DRAGON_DAGGER_P_, PoisonType.MILD);
			types.put(ItemIdentifiers.BLACK_DAGGER_P_, PoisonType.MILD);
			types.put(ItemIdentifiers.POISONED_DAGGER_P_, PoisonType.MILD);
			types.put(ItemIdentifiers.BRONZE_SPEAR_P_, PoisonType.MILD);
			types.put(ItemIdentifiers.IRON_SPEAR_P_, PoisonType.MILD);
			types.put(ItemIdentifiers.STEEL_SPEAR_P_, PoisonType.MILD);
			types.put(ItemIdentifiers.MITHRIL_SPEAR_P_, PoisonType.MILD);
			types.put(ItemIdentifiers.ADAMANT_SPEAR_P_, PoisonType.MILD);
			types.put(ItemIdentifiers.RUNE_SPEAR_P_, PoisonType.MILD);
			types.put(ItemIdentifiers.DRAGON_SPEAR_P_, PoisonType.MILD);
			types.put(ItemIdentifiers.BLACK_DART_P_, PoisonType.VERY_WEAK);
			types.put(ItemIdentifiers.BLACK_SPEAR_P_, PoisonType.MILD);
			types.put(ItemIdentifiers.BRONZE_ARROW_P_PLUS_, PoisonType.WEAK);
			types.put(ItemIdentifiers.IRON_ARROW_P_PLUS_, PoisonType.WEAK);
			types.put(ItemIdentifiers.STEEL_ARROW_P_PLUS_, PoisonType.WEAK);
			types.put(ItemIdentifiers.MITHRIL_ARROW_P_PLUS_, PoisonType.WEAK);
			types.put(ItemIdentifiers.ADAMANT_ARROW_P_PLUS_, PoisonType.WEAK);
			types.put(ItemIdentifiers.RUNE_ARROW_P_PLUS_, PoisonType.WEAK);
			types.put(ItemIdentifiers.BRONZE_ARROW_P_PLUS_PLUS_, PoisonType.MILD);
			types.put(ItemIdentifiers.IRON_ARROW_P_PLUS_PLUS_, PoisonType.MILD);
			types.put(ItemIdentifiers.STEEL_ARROW_P_PLUS_PLUS_, PoisonType.MILD);
			types.put(ItemIdentifiers.MITHRIL_ARROW_P_PLUS_PLUS_, PoisonType.MILD);
			types.put(ItemIdentifiers.ADAMANT_ARROW_P_PLUS_PLUS_, PoisonType.MILD);
			types.put(ItemIdentifiers.RUNE_ARROW_P_PLUS_PLUS_, PoisonType.MILD);
			types.put(ItemIdentifiers.BRONZE_DART_P_PLUS_, PoisonType.WEAK);
			types.put(ItemIdentifiers.IRON_DART_P_PLUS_, PoisonType.WEAK);
			types.put(ItemIdentifiers.STEEL_DART_P_PLUS_, PoisonType.WEAK);
			types.put(ItemIdentifiers.BLACK_DART_P_PLUS_, PoisonType.WEAK);
			types.put(ItemIdentifiers.MITHRIL_DART_P_PLUS_, PoisonType.WEAK);
			types.put(ItemIdentifiers.ADAMANT_DART_P_PLUS_, PoisonType.WEAK);
			types.put(ItemIdentifiers.RUNE_DART_P_PLUS_, PoisonType.WEAK);
			types.put(ItemIdentifiers.BRONZE_DART_P_PLUS_PLUS_, PoisonType.MILD);
			types.put(ItemIdentifiers.IRON_DART_P_PLUS_PLUS_, PoisonType.MILD);
			types.put(ItemIdentifiers.STEEL_DART_P_PLUS_PLUS_, PoisonType.MILD);
			types.put(ItemIdentifiers.BLACK_DART_P_PLUS_PLUS_, PoisonType.MILD);
			types.put(ItemIdentifiers.MITHRIL_DART_P_PLUS_PLUS_, PoisonType.MILD);
			types.put(ItemIdentifiers.ADAMANT_DART_P_PLUS_PLUS_, PoisonType.MILD);
			types.put(ItemIdentifiers.RUNE_DART_P_PLUS_PLUS_, PoisonType.MILD);
			types.put(ItemIdentifiers.BRONZE_JAVELIN_P_PLUS_, PoisonType.WEAK);
			types.put(ItemIdentifiers.IRON_JAVELIN_P_PLUS_, PoisonType.WEAK);
			types.put(ItemIdentifiers.STEEL_JAVELIN_P_PLUS_, PoisonType.WEAK);
			types.put(ItemIdentifiers.MITHRIL_JAVELIN_P_PLUS_, PoisonType.WEAK);
			types.put(ItemIdentifiers.ADAMANT_JAVELIN_P_PLUS_, PoisonType.WEAK);
			types.put(ItemIdentifiers.RUNE_JAVELIN_P_PLUS_, PoisonType.WEAK);
			types.put(ItemIdentifiers.BRONZE_JAVELIN_P_PLUS_PLUS_, PoisonType.MILD);
			types.put(ItemIdentifiers.IRON_JAVELIN_P_PLUS_PLUS_, PoisonType.MILD);
			types.put(ItemIdentifiers.STEEL_JAVELIN_P_PLUS_PLUS_, PoisonType.MILD);
			types.put(ItemIdentifiers.MITHRIL_JAVELIN_P_PLUS_PLUS_, PoisonType.MILD);
			types.put(ItemIdentifiers.ADAMANT_JAVELIN_P_PLUS_PLUS_, PoisonType.MILD);
			types.put(ItemIdentifiers.RUNE_JAVELIN_P_PLUS_PLUS_, PoisonType.MILD);
			types.put(ItemIdentifiers.BRONZE_KNIFE_P_PLUS_, PoisonType.WEAK);
			types.put(ItemIdentifiers.IRON_KNIFE_P_PLUS_, PoisonType.WEAK);
			types.put(ItemIdentifiers.STEEL_KNIFE_P_PLUS_, PoisonType.WEAK);
			types.put(ItemIdentifiers.MITHRIL_KNIFE_P_PLUS_, PoisonType.WEAK);
			types.put(ItemIdentifiers.BLACK_KNIFE_P_PLUS_, PoisonType.WEAK);
			types.put(ItemIdentifiers.ADAMANT_KNIFE_P_PLUS_, PoisonType.WEAK);
			types.put(ItemIdentifiers.RUNE_KNIFE_P_PLUS_, PoisonType.WEAK);
			types.put(ItemIdentifiers.BRONZE_KNIFE_P_PLUS_PLUS_, PoisonType.MILD);
			types.put(ItemIdentifiers.IRON_KNIFE_P_PLUS_PLUS_, PoisonType.MILD);
			types.put(ItemIdentifiers.STEEL_KNIFE_P_PLUS_PLUS_, PoisonType.MILD);
			types.put(ItemIdentifiers.MITHRIL_KNIFE_P_PLUS_PLUS_, PoisonType.MILD);
			types.put(ItemIdentifiers.BLACK_KNIFE_P_PLUS_PLUS_, PoisonType.MILD);
			types.put(ItemIdentifiers.ADAMANT_KNIFE_P_PLUS_PLUS_, PoisonType.MILD);
			types.put(ItemIdentifiers.RUNE_KNIFE_P_PLUS_PLUS_, PoisonType.MILD);
			types.put(ItemIdentifiers.IRON_DAGGER_P_PLUS_, PoisonType.EXTRA);
			types.put(ItemIdentifiers.BRONZE_DAGGER_P_PLUS_, PoisonType.EXTRA);
			types.put(ItemIdentifiers.STEEL_DAGGER_P_PLUS_, PoisonType.EXTRA);
			types.put(ItemIdentifiers.MITHRIL_DAGGER_P_PLUS_, PoisonType.EXTRA);
			types.put(ItemIdentifiers.ADAMANT_DAGGER_P_PLUS_, PoisonType.EXTRA);
			types.put(ItemIdentifiers.RUNE_DAGGER_P_PLUS_, PoisonType.EXTRA);
			types.put(ItemIdentifiers.DRAGON_DAGGER_P_PLUS_, PoisonType.EXTRA);
			types.put(ItemIdentifiers.BLACK_DAGGER_P_PLUS_, PoisonType.EXTRA);
			types.put(ItemIdentifiers.POISON_DAGGER_P_PLUS_, PoisonType.EXTRA);
			types.put(ItemIdentifiers.IRON_DAGGER_P_PLUS_PLUS_, PoisonType.SUPER);
			types.put(ItemIdentifiers.BRONZE_DAGGER_P_PLUS_PLUS_, PoisonType.SUPER);
			types.put(ItemIdentifiers.STEEL_DAGGER_P_PLUS_PLUS_, PoisonType.SUPER);
			types.put(ItemIdentifiers.MITHRIL_DAGGER_P_PLUS_PLUS_, PoisonType.SUPER);
			types.put(ItemIdentifiers.ADAMANT_DAGGER_P_PLUS_PLUS_, PoisonType.SUPER);
			types.put(ItemIdentifiers.RUNE_DAGGER_P_PLUS_PLUS_, PoisonType.SUPER);
			types.put(ItemIdentifiers.DRAGON_DAGGER_P_PLUS_PLUS_, PoisonType.SUPER);
			types.put(ItemIdentifiers.BLACK_DAGGER_P_PLUS_PLUS_, PoisonType.SUPER);
			types.put(ItemIdentifiers.POISON_DAGGER_P_PLUS_PLUS_, PoisonType.SUPER);
			types.put(ItemIdentifiers.BRONZE_SPEAR_P_PLUS_, PoisonType.EXTRA);
			types.put(ItemIdentifiers.IRON_SPEAR_P_PLUS_, PoisonType.EXTRA);
			types.put(ItemIdentifiers.STEEL_SPEAR_P_PLUS_, PoisonType.EXTRA);
			types.put(ItemIdentifiers.MITHRIL_SPEAR_P_PLUS_, PoisonType.EXTRA);
			types.put(ItemIdentifiers.ADAMANT_SPEAR_P_PLUS_, PoisonType.EXTRA);
			types.put(ItemIdentifiers.RUNE_SPEAR_P_PLUS_, PoisonType.EXTRA);
			types.put(ItemIdentifiers.DRAGON_SPEAR_P_PLUS_, PoisonType.EXTRA);
			types.put(ItemIdentifiers.BRONZE_SPEAR_P_PLUS_PLUS_, PoisonType.SUPER);
			types.put(ItemIdentifiers.IRON_SPEAR_P_PLUS_PLUS_, PoisonType.SUPER);
			types.put(ItemIdentifiers.STEEL_SPEAR_P_PLUS_PLUS_, PoisonType.SUPER);
			types.put(ItemIdentifiers.MITHRIL_SPEAR_P_PLUS_PLUS_, PoisonType.SUPER);
			types.put(ItemIdentifiers.ADAMANT_SPEAR_P_PLUS_PLUS_, PoisonType.SUPER);
			types.put(ItemIdentifiers.RUNE_SPEAR_P_PLUS_PLUS_, PoisonType.SUPER);
			types.put(ItemIdentifiers.DRAGON_SPEAR_P_PLUS_PLUS_, PoisonType.SUPER);
			types.put(ItemIdentifiers.BLACK_SPEAR_P_PLUS_, PoisonType.EXTRA);
			types.put(ItemIdentifiers.BLACK_SPEAR_P_PLUS_PLUS_, PoisonType.SUPER);
			types.put(ItemIdentifiers.BRONZE_BOLTS_P_PLUS_, PoisonType.WEAK);
			types.put(ItemIdentifiers.BRONZE_BOLTS_P_PLUS_PLUS_, PoisonType.MILD);
			types.put(ItemIdentifiers.WHITE_DAGGER_P_, PoisonType.MILD);
			types.put(ItemIdentifiers.WHITE_DAGGER_P_PLUS_, PoisonType.EXTRA);
			types.put(ItemIdentifiers.WHITE_DAGGER_P_PLUS_PLUS_, PoisonType.SUPER);
			types.put(ItemIdentifiers.BONE_DAGGER_P_, PoisonType.MILD);
			types.put(ItemIdentifiers.BONE_DAGGER_P_PLUS_, PoisonType.EXTRA);
			types.put(ItemIdentifiers.BONE_DAGGER_P_PLUS_PLUS_, PoisonType.SUPER);
			types.put(ItemIdentifiers.BLURITE_BOLTS_P_, PoisonType.VERY_WEAK);
			types.put(ItemIdentifiers.IRON_BOLTS_P_, PoisonType.VERY_WEAK);
			types.put(ItemIdentifiers.STEEL_BOLTS_P_, PoisonType.VERY_WEAK);
			types.put(ItemIdentifiers.MITHRIL_BOLTS_P_, PoisonType.VERY_WEAK);
			types.put(ItemIdentifiers.ADAMANT_BOLTS_P_, PoisonType.VERY_WEAK);
			types.put(ItemIdentifiers.RUNITE_BOLTS_P_, PoisonType.VERY_WEAK);
			types.put(ItemIdentifiers.SILVER_BOLTS_P_, PoisonType.VERY_WEAK);
			types.put(ItemIdentifiers.BLURITE_BOLTS_P_PLUS_, PoisonType.WEAK);
			types.put(ItemIdentifiers.IRON_BOLTS_P_PLUS_, PoisonType.WEAK);
			types.put(ItemIdentifiers.STEEL_BOLTS_P_PLUS_, PoisonType.WEAK);
			types.put(ItemIdentifiers.MITHRIL_BOLTS_P_PLUS_, PoisonType.WEAK);
			types.put(ItemIdentifiers.ADAMANT_BOLTS_P_PLUS_, PoisonType.WEAK);
			types.put(ItemIdentifiers.RUNITE_BOLTS_P_PLUS_, PoisonType.WEAK);
			types.put(ItemIdentifiers.SILVER_BOLTS_P_PLUS_, PoisonType.WEAK);
			types.put(ItemIdentifiers.BLURITE_BOLTS_P_PLUS_PLUS_, PoisonType.MILD);
			types.put(ItemIdentifiers.IRON_BOLTS_P_PLUS_PLUS_, PoisonType.MILD);
			types.put(ItemIdentifiers.STEEL_BOLTS_P_PLUS_PLUS_, PoisonType.MILD);
			types.put(ItemIdentifiers.MITHRIL_BOLTS_P_PLUS_PLUS_, PoisonType.MILD);
			types.put(ItemIdentifiers.ADAMANT_BOLTS_P_PLUS_PLUS_, PoisonType.MILD);
			types.put(ItemIdentifiers.RUNITE_BOLTS_P_PLUS_PLUS_, PoisonType.MILD);
			types.put(ItemIdentifiers.SILVER_BOLTS_P_PLUS_PLUS_, PoisonType.MILD);
			types.put(ItemIdentifiers.KERIS_P_, PoisonType.MILD);
			types.put(ItemIdentifiers.KERIS_P_PLUS_, PoisonType.EXTRA);
			types.put(ItemIdentifiers.KERIS_P_PLUS_PLUS_, PoisonType.SUPER);
			types.put(ItemIdentifiers.DRAGON_ARROW_P_, PoisonType.VERY_WEAK);
			types.put(ItemIdentifiers.DRAGON_ARROW_P_PLUS_, PoisonType.WEAK);
			types.put(ItemIdentifiers.DRAGON_ARROW_P_PLUS_PLUS_, PoisonType.MILD);
			types.put(ItemIdentifiers.DRAGON_DART_P_, PoisonType.MILD);
			types.put(ItemIdentifiers.DRAGON_DART_P_PLUS_, PoisonType.EXTRA);
			types.put(ItemIdentifiers.DRAGON_DART_P_PLUS_PLUS_, PoisonType.SUPER);
			types.put(ItemIdentifiers.BRONZE_HASTA_P_, PoisonType.MILD);
			types.put(ItemIdentifiers.BRONZE_HASTA_P_PLUS_, PoisonType.EXTRA);
			types.put(ItemIdentifiers.BRONZE_HASTA_P_PLUS_PLUS_, PoisonType.SUPER);
			types.put(ItemIdentifiers.IRON_HASTA_P_, PoisonType.MILD);
			types.put(ItemIdentifiers.IRON_HASTA_P_PLUS_, PoisonType.EXTRA);
			types.put(ItemIdentifiers.IRON_HASTA_P_PLUS_PLUS_, PoisonType.SUPER);
			types.put(ItemIdentifiers.STEEL_HASTA_P_, PoisonType.MILD);
			types.put(ItemIdentifiers.STEEL_HASTA_P_PLUS_, PoisonType.EXTRA);
			types.put(ItemIdentifiers.STEEL_HASTA_P_PLUS_PLUS_, PoisonType.SUPER);
			types.put(ItemIdentifiers.MITHRIL_HASTA_P_, PoisonType.MILD);
			types.put(ItemIdentifiers.MITHRIL_HASTA_P_PLUS_, PoisonType.EXTRA);
			types.put(ItemIdentifiers.MITHRIL_HASTA_P_PLUS_PLUS_, PoisonType.SUPER);
			types.put(ItemIdentifiers.ADAMANT_HASTA_P_, PoisonType.MILD);
			types.put(ItemIdentifiers.ADAMANT_HASTA_P_PLUS_, PoisonType.EXTRA);
			types.put(ItemIdentifiers.ADAMANT_HASTA_P_PLUS_PLUS_, PoisonType.SUPER);
			types.put(ItemIdentifiers.RUNE_HASTA_P_, PoisonType.MILD);
			types.put(ItemIdentifiers.RUNE_HASTA_P_PLUS_, PoisonType.EXTRA);
			types.put(ItemIdentifiers.RUNE_HASTA_P_PLUS_PLUS_, PoisonType.SUPER);
			types.put(ItemIdentifiers.ABYSSAL_DAGGER_P_, PoisonType.MILD);
			types.put(ItemIdentifiers.ABYSSAL_DAGGER_P_PLUS_, PoisonType.EXTRA);
			types.put(ItemIdentifiers.ABYSSAL_DAGGER_P_PLUS_PLUS_, PoisonType.SUPER);
			types.put(ItemIdentifiers.DRAGON_JAVELIN_P_, PoisonType.VERY_WEAK);
			types.put(ItemIdentifiers.DRAGON_JAVELIN_P_PLUS_, PoisonType.WEAK);
			types.put(ItemIdentifiers.DRAGON_JAVELIN_P_PLUS_PLUS_, PoisonType.MILD);
			types.put(ItemIdentifiers.AMETHYST_JAVELIN_P_, PoisonType.VERY_WEAK);
			types.put(ItemIdentifiers.AMETHYST_JAVELIN_P_PLUS_, PoisonType.WEAK);
			types.put(ItemIdentifiers.AMETHYST_JAVELIN_P_PLUS_PLUS_, PoisonType.MILD);
			types.put(ItemIdentifiers.AMETHYST_ARROW_P_, PoisonType.VERY_WEAK);
			types.put(ItemIdentifiers.AMETHYST_ARROW_P_PLUS_, PoisonType.WEAK);
			types.put(ItemIdentifiers.AMETHYST_ARROW_P_PLUS_PLUS_, PoisonType.MILD);

			types.put(ItemIdentifiers.TOXIC_BLOWPIPE, PoisonType.VENOM);
			types.put(ItemIdentifiers.ABYSSAL_TENTACLE, PoisonType.VENOM);
			
			types.put(ItemIdentifiers.BRONZE_HASTA_KP_, PoisonType.SUPER);
			types.put(ItemIdentifiers.IRON_HASTA_KP_, PoisonType.SUPER);
			types.put(ItemIdentifiers.STEEL_HASTA_KP_, PoisonType.SUPER);
			types.put(ItemIdentifiers.MITHRIL_HASTA_KP_, PoisonType.SUPER);
			types.put(ItemIdentifiers.ADAMANT_HASTA_KP_, PoisonType.SUPER);
			types.put(ItemIdentifiers.RUNE_HASTA_KP_, PoisonType.SUPER);
			types.put(ItemIdentifiers.BRONZE_SPEAR_KP_, PoisonType.SUPER);
			types.put(ItemIdentifiers.IRON_SPEAR_KP_, PoisonType.SUPER);
			types.put(ItemIdentifiers.STEEL_SPEAR_KP_, PoisonType.SUPER);
			types.put(ItemIdentifiers.MITHRIL_SPEAR_KP_, PoisonType.SUPER);
			types.put(ItemIdentifiers.ADAMANT_SPEAR_KP_, PoisonType.SUPER);
			types.put(ItemIdentifiers.RUNE_SPEAR_KP_, PoisonType.SUPER);
			types.put(ItemIdentifiers.DRAGON_SPEAR_KP_, PoisonType.SUPER);
			types.put(ItemIdentifiers.DRAGON_HASTA_P_, PoisonType.MILD);
			types.put(ItemIdentifiers.DRAGON_HASTA_P_PLUS_, PoisonType.EXTRA);
			types.put(ItemIdentifiers.DRAGON_HASTA_P_PLUS_PLUS_, PoisonType.SUPER);
			types.put(ItemIdentifiers.DRAGON_HASTA_KP_, PoisonType.SUPER);
		}

		/**
		 * Gets the poison type of the specified item.
		 *
		 * @param item
		 *            the item to get the poison type of.
		 * @return the poison type of the specified item, or <code>null</code> if the
		 *         item is not able to poison the victim.
		 */
		public static Optional<PoisonType> getPoisonType(Item item) {
			if (item == null || item.getId() < 1 || item.getAmount() < 1)
				return Optional.empty();
			return Optional.ofNullable(types.get(item.getId()));
		}
	}
}
