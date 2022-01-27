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

    private static int maximumMeleeHitOsScape(Player player) {
        var strengthBonus = player.getBonusManager().getOtherBonus()[BonusManager.STRENGTH];

        double prayerBonus = 1;

        // Prayer additions
        if (PrayerHandler.isActivated(player, PrayerHandler.BURST_OF_STRENGTH)) {
            prayerBonus = 1.05;
        } else if (PrayerHandler.isActivated(player, PrayerHandler.SUPERHUMAN_STRENGTH)) {
            prayerBonus = 1.10;
        } else if (PrayerHandler.isActivated(player, PrayerHandler.ULTIMATE_STRENGTH)) {
            prayerBonus = 1.15;
        } else if (PrayerHandler.isActivated(player, PrayerHandler.CHIVALRY)) {
            prayerBonus = 1.18;
        } else if (PrayerHandler.isActivated(player, PrayerHandler.PIETY)) {
            prayerBonus = 1.23;
        }

        // Void set grants 10% bonus here
        double extraBonus = (CombatEquipment.wearingVoid(player, CombatType.MELEE)) ? 1.10 : 1.0;

        //If the player has a berserker neckalce & a toktz weapon
        double toktz_bonus = (CombatEquipment.wearingObsidian(player)) ? 1.20 : 1.0;
        // toktz_bonus *= (obbyArmour(player) && hasObbyWeapon(player) ? 1.10 : 1.0);

        FightStyle fightStyle = player.getFightType().getStyle();
        int style = fightStyle == FightStyle.AGGRESSIVE ? 3 : fightStyle == FightStyle.CONTROLLED ? 1 : 0;

        double effectiveStr = Math.ceil(player.getSkillManager().getCurrentLevel(Skill.STRENGTH) * prayerBonus * extraBonus * toktz_bonus) + style;

        //TODO effectiveStr depends on prayer and style and e.g. salve ammy
        double baseDamage = 1.3 + (effectiveStr / 10d) + (strengthBonus / 80d) + ((effectiveStr * strengthBonus) / 640d);

        if (CombatFactory.fullDharoks(player)) {
            double hp = player.getHitpoints();
            double max = player.getSkillManager().getMaxLevel(Skill.HITPOINTS);
            double mult = Math.max(0, ((max - hp) / max) * 100d) + 100d;
            baseDamage *= (mult / 100);
        }

        return (int) baseDamage;
    }

    private static float gearBonus(Player player)
    {
//        int salveLevel = salveLevel(input);
//        if (salveLevel == 2)
//            return 6f / 5f;
//        else if (salveLevel == 1 || blackMask(input))
//            return 7f / 6f;
//        else
        return 1f;
    }

    private static int effectiveStrengthLevel(Player player) {
        int str = player.getSkillManager().getCurrentLevel(Skill.STRENGTH);

        double prayerBonus = 1;

        // Prayer additions
        if (PrayerHandler.isActivated(player, PrayerHandler.BURST_OF_STRENGTH)) {
            prayerBonus = 1.05;
        } else if (PrayerHandler.isActivated(player, PrayerHandler.SUPERHUMAN_STRENGTH)) {
            prayerBonus = 1.10;
        } else if (PrayerHandler.isActivated(player, PrayerHandler.ULTIMATE_STRENGTH)) {
            prayerBonus = 1.15;
        } else if (PrayerHandler.isActivated(player, PrayerHandler.CHIVALRY)) {
            prayerBonus = 1.18;
        } else if (PrayerHandler.isActivated(player, PrayerHandler.PIETY)) {
            prayerBonus = 1.23;
        }

        str *= prayerBonus;

        FightStyle fightStyle = player.getFightType().getStyle();
        if (fightStyle == FightStyle.AGGRESSIVE)
            str += 3;
        else if (fightStyle == FightStyle.CONTROLLED)
            str += 1;
        str += 8;

        if (CombatEquipment.wearingVoid(player, CombatType.MELEE))
            str = (int) (str * 1.1f);

        if (CombatEquipment.wearingObsidian(player))
            str = (int) (str * 1.2f); // obisidian bonuses stack

        return str;
    }

    private static int maximumMeleeHitDpsCalc(Player player) {
        var strengthBonus = player.getBonusManager().getOtherBonus()[BonusManager.STRENGTH];
        int maxHit = effectiveStrengthLevel(player) * (strengthBonus + 64);
        maxHit += 320;
        maxHit /= 640;


        if (CombatFactory.fullDharoks(player)) {
            double hp = player.getHitpoints();
            double max = player.getSkillManager().getMaxLevel(Skill.HITPOINTS);
            double mult = Math.max(0, ((max - hp) / max) * 100d) + 100d;
            maxHit *= (mult / 100);
        }

        return (int) (maxHit * gearBonus(player));
    }

    public static int calculateMaxMeleeHit(Mobile entity) {
        if (entity.isPlayer()) {
            Player player = (Player) entity;
            return maximumMeleeHitDpsCalc(player);
        }
        NPC npc = (NPC) entity;
        double maxHit = npc.getDefinition().getMaxHit();

        // Dharoks effect
        if (CombatFactory.fullDharoks(entity)) {
            int hitpoints = entity.getHitpoints();
            maxHit += (int) ((entity.getAsNpc().getDefinition().getHitpoints() - hitpoints) * 0.35);
        }

        return (int) Math.floor(maxHit);
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
