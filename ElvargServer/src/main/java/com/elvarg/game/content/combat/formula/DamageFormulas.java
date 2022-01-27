package com.elvarg.game.content.combat.formula;

import com.elvarg.game.content.PrayerHandler;
import com.elvarg.game.content.combat.CombatEquipment;
import com.elvarg.game.content.combat.CombatFactory;
import com.elvarg.game.content.combat.CombatType;
import com.elvarg.game.content.combat.FightStyle;
import com.elvarg.game.content.combat.magic.CombatSpell;
import com.elvarg.game.content.combat.ranged.RangedData.RangedWeapon;
import com.elvarg.game.entity.impl.Mobile;
import com.elvarg.game.entity.impl.npc.NPC;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.Skill;
import com.elvarg.game.model.container.impl.Equipment;
import com.elvarg.game.model.equipment.BonusManager;

public class DamageFormulas {

    public static int calculateMaxMeleeHit(Mobile entity) {
        double maxHit = 0;

        if (entity.isNpc()) {
            NPC npc = (NPC) entity;
            maxHit = npc.getDefinition().getMaxHit();
        } else {
            Player player = (Player) entity;

            double base = 0;
            double effective = getEffectiveStr(player);
            double strengthBonus = player.getBonusManager().getOtherBonus()[BonusManager.STRENGTH];
            double specialBonus = 1;

            // Special effects also affect maxhit
            if (player.isSpecialActivated()
                    && player.getCombatSpecial().getCombatMethod().type() == CombatType.MELEE) {
                specialBonus = player.getCombatSpecial().getStrengthMultiplier();
            }

            // Use our multipliers to adjust the maxhit...
            base = (13 + effective + (strengthBonus / 8) + ((effective * strengthBonus) / 65)) / 11;

            if (specialBonus > 1) {
                base = (base * specialBonus);
            }

            // Obsidian effect is +10% damage
            if (CombatEquipment.wearingObsidian(player)) {
                base = (base * 1.2);
            }

            // Void melee is +10% damage
            if (CombatEquipment.wearingVoid(player, CombatType.MELEE)) {
                base = (base * 1.1);
            }

            maxHit = (base *= 1);

        }

        // Dharoks effect
        if (CombatFactory.fullDharoks(entity)) {
            int hitpoints = entity.getHitpoints();
            if (entity.isNpc()) {
                maxHit += (int) ((int) (entity.getAsNpc().getDefinition().getHitpoints() - hitpoints) * 0.35);
            } else {
                maxHit += (int) ((int) (entity.getAsPlayer().getSkillManager().getMaxLevel(Skill.HITPOINTS) - hitpoints)
                        * 0.45) + 1;
            }
        }

        return (int) Math.floor(maxHit);
    }

    public static double getEffectiveStr(Player plr) {
        int styleBonus = 0;
        FightStyle style = plr.getFightType().getStyle();

        if (style == FightStyle.AGGRESSIVE || style == FightStyle.ACCURATE) {
            styleBonus = 3;
        } else if (style == FightStyle.CONTROLLED) {
            styleBonus = 1;
        }

        double prayerMod = 1.0;

        if (PrayerHandler.isActivated(plr, PrayerHandler.BURST_OF_STRENGTH)) {
            prayerMod = 1.05;
        } else if (PrayerHandler.isActivated(plr, PrayerHandler.SUPERHUMAN_STRENGTH)) {
            prayerMod = 1.1;
        } else if (PrayerHandler.isActivated(plr, PrayerHandler.ULTIMATE_STRENGTH)) {
            prayerMod = 1.15;
        } else if (PrayerHandler.isActivated(plr, PrayerHandler.CHIVALRY)) {
            prayerMod = 1.18;
        } else if (PrayerHandler.isActivated(plr, PrayerHandler.PIETY)) {
            prayerMod = 1.23;
        }

        return ((plr.getSkillManager().getCurrentLevel(Skill.STRENGTH)) * prayerMod) + styleBonus;
    }

    /**
     * Calculates a player's magic max hit
     *
     * @param player The player to calculate magic max hit for
     * @return The player's magic max hit damage
     */
    public static int getMagicMaxhit(Mobile c) {
        int maxHit = 0;

        CombatSpell spell = c.getCombat().getSelectedSpell();

        if (spell != null) {

            if (spell.maximumHit() > 0) {

                maxHit = spell.maximumHit();

            } else {

                if (c.isNpc()) {
                    maxHit = c.getAsNpc().getDefinition().getMaxHit();
                } else {
                    maxHit = 1;
                }

            }
        } else {
            if (c.isNpc()) {
                maxHit = c.getAsNpc().getDefinition().getMaxHit();
            }
        }

        if (c.isPlayer()) {
            switch (c.getAsPlayer().getEquipment().getItems()[Equipment.WEAPON_SLOT].getId()) {
                case 4675:
                case 6914:
                case 21006:
                    maxHit *= 1.10;
                    break;
                case 11791:
                    maxHit *= 1.15;
                    break;
                case 12904:
                    maxHit *= 1.18;
                    break;
                case 12899:
                case 11905:
                    maxHit *= 1.16;
                    break;
            }
        }

        return (int) Math.floor(maxHit);
    }

    /**
     * Calculates the maximum ranged hit for the argued {@link Mobile} without
     * taking the victim into consideration.
     *
     * @param entity the entity to calculate the maximum hit for.
     * @param victim the victim being attacked.
     * @return the maximum ranged hit that this entity can deal.
     */
    public static int calculateMaxRangedHit(Mobile entity) {
        double maxHit = 0;

        if (entity.isNpc()) {
            NPC npc = (NPC) entity;
            maxHit = npc.getDefinition().getMaxHit();
        } else {
            Player player = (Player) entity;

            double prayerMultiplier = 1;
            int combatStyleBonus = 2;
            int rangeLevel = player.getSkillManager().getCurrentLevel(Skill.RANGED);
            int rangedStrength = ((int) player.getBonusManager().getAttackBonus()[4] / 10);
            // Include the arrows strength..

            if (player.getCombat().getAmmunition() != null) {
                rangedStrength += (player.getCombat().getAmmunition().getStrength());
            }

            // Boost blowpipe
            if (player.getCombat().getRangedWeapon() != null) {
                if (player.getCombat().getRangedWeapon() == RangedWeapon.TOXIC_BLOWPIPE) {
                    rangedStrength += 35;
                }
            }

            // Include attack style... Accurate/long range hits harder.
            if (player.getFightType().getStyle() == FightStyle.ACCURATE) {
                combatStyleBonus = 3;
            } else if (player.getFightType().getStyle() == FightStyle.DEFENSIVE) {
                combatStyleBonus = 1;
            }

            // Do calculations of maxhit...
            int effectiveRangeDamage = (int) ((rangeLevel * prayerMultiplier) + combatStyleBonus);
            maxHit = 1.3 + (effectiveRangeDamage / 10) + (rangedStrength / 80)
                    + ((effectiveRangeDamage * rangedStrength) / 640);

            // We now have the maxhit and can increase its damage...

            // Void hits 20% more
            if (CombatEquipment.wearingVoid(player, CombatType.RANGED)) {
                maxHit *= 1.2;
            }

            // Prayers
            double prayerMod = 1.0;
            if (PrayerHandler.isActivated(player, PrayerHandler.SHARP_EYE)) {
                prayerMod = 1.05;
            } else if (PrayerHandler.isActivated(player, PrayerHandler.HAWK_EYE)) {
                prayerMod = 1.10;
            } else if (PrayerHandler.isActivated(player, PrayerHandler.EAGLE_EYE)) {
                prayerMod = 1.15;
            } else if (PrayerHandler.isActivated(player, PrayerHandler.RIGOUR)) {
                prayerMod = 1.23;
            }
            maxHit *= prayerMod;

            // Special attacks!
            if (player.isSpecialActivated()
                    && player.getCombatSpecial().getCombatMethod().type() == CombatType.RANGED) {
                maxHit *= player.getCombatSpecial().getStrengthMultiplier();
            }

        }

        return (int) Math.floor(maxHit);
    }
}
