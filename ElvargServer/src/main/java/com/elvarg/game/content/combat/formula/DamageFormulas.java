package com.elvarg.game.content.combat.formula;

import com.elvarg.game.content.PrayerHandler;
import com.elvarg.game.content.combat.CombatEquipment;
import com.elvarg.game.content.combat.CombatFactory;
import com.elvarg.game.content.combat.CombatType;
import com.elvarg.game.content.combat.FightStyle;
import com.elvarg.game.content.combat.magic.CombatSpell;
import com.elvarg.game.entity.impl.Mobile;
import com.elvarg.game.entity.impl.npc.NPC;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.Skill;
import com.elvarg.game.model.container.impl.Equipment;
import com.elvarg.game.model.equipment.BonusManager;

public class DamageFormulas {

    private static float effectiveStrengthLevel(Player player) {
        float str = player.getSkillManager().getCurrentLevel(Skill.STRENGTH);

        float prayerBonus = 1;

        // Prayer additions
        if (PrayerHandler.isActivated(player, PrayerHandler.BURST_OF_STRENGTH)) {
            prayerBonus = 1.05f;
        } else if (PrayerHandler.isActivated(player, PrayerHandler.SUPERHUMAN_STRENGTH)) {
            prayerBonus = 1.10f;
        } else if (PrayerHandler.isActivated(player, PrayerHandler.ULTIMATE_STRENGTH)) {
            prayerBonus = 1.15f;
        } else if (PrayerHandler.isActivated(player, PrayerHandler.CHIVALRY)) {
            prayerBonus = 1.18f;
        } else if (PrayerHandler.isActivated(player, PrayerHandler.PIETY)) {
            prayerBonus = 1.23f;
        }

        str = (str * prayerBonus);

        FightStyle fightStyle = player.getFightType().getStyle();
        if (fightStyle == FightStyle.AGGRESSIVE)
            str += 3;
        else if (fightStyle == FightStyle.CONTROLLED)
            str += 1;
        str += 8;

        if (CombatEquipment.wearingVoid(player, CombatType.MELEE))
            str =  (str * 1.1f);

        if (CombatEquipment.wearingObsidian(player))
            str = (str * 1.2f); // obisidian bonuses stack

        return str;
    }

    private static int maximumMeleeHitDpsCalc(Player player) {
        int strengthBonus = player.getBonusManager().getOtherBonus()[BonusManager.STRENGTH];
        float maxHit = effectiveStrengthLevel(player) * (strengthBonus + 64);
        maxHit += 320;
        maxHit /= 640;

        if (CombatFactory.fullDharoks(player)) {
            float hp = player.getHitpoints();
            float max = player.getSkillManager().getMaxLevel(Skill.HITPOINTS);
            float mult = Math.max(0, ((max - hp) / max) * 100f) + 100f;
            maxHit *= (mult / 100);
        }

        if (player.isSpecialActivated()) {
            maxHit *= player.getCombatSpecial().getStrengthMultiplier();
        }

        return (int)maxHit;
    }

    public static int calculateMaxMeleeHit(Mobile entity) {
        if (entity.isPlayer()) {
            Player player = (Player) entity;
            return maximumMeleeHitDpsCalc(player);
        }
        NPC npc = (NPC) entity;
        float maxHit = npc.getDefinition().getMaxHit();

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

    private static float effectiveRangedStrength(Player player) {
        float rngStrength = player.getSkillManager().getCurrentLevel(Skill.RANGED);

        // Prayers
        float prayerMod = 1.0f;
        if (PrayerHandler.isActivated(player, PrayerHandler.SHARP_EYE)) {
            prayerMod = 1.05f;
        } else if (PrayerHandler.isActivated(player, PrayerHandler.HAWK_EYE)) {
            prayerMod = 1.10f;
        } else if (PrayerHandler.isActivated(player, PrayerHandler.EAGLE_EYE)) {
            prayerMod = 1.15f;
        } else if (PrayerHandler.isActivated(player, PrayerHandler.RIGOUR)) {
            prayerMod = 1.23f;
        }
        rngStrength = (rngStrength * prayerMod);

        FightStyle fightStyle = player.getFightType().getStyle();
        if (fightStyle == FightStyle.ACCURATE)
            rngStrength += 3;
        rngStrength += 8;

        if (CombatEquipment.wearingVoid(player, CombatType.RANGED)) {
            rngStrength = (rngStrength * 1.125f);
        }

//        if (dragonHunter(input))
//            rngStrength = (int) (rngStrength * 1.3f);

        return rngStrength;
    }

    private static int maximumRangeHitDpsCalc(Player player) {
        var strengthBonus = player.getBonusManager().getOtherBonus()[BonusManager.RANGED_STRENGTH];

        float maxHit = effectiveRangedStrength(player);
        maxHit *= (strengthBonus + 64);
        maxHit += 320;
        maxHit /= 640;

//            maxHit *= gearBonus(player);

//            if (EquipmentRequirement.TBOW.isSatisfied(input))
//                maxHit = (int) (maxHit * tbowDmgModifier(input));
//
//            NpcStats target = input.getNpcTarget();
//            if (target.getName() != null && target.getName().contains("Zulrah"))
//                maxHit = Math.min(maxHit, 50);

        if (player.isSpecialActivated() && player.getCombatSpecial().getCombatMethod().type() == CombatType.RANGED) {
            maxHit *= player.getCombatSpecial().getStrengthMultiplier();
        }

        return (int)maxHit;
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
        if (entity.isNpc()) {
            NPC npc = (NPC) entity;
            return npc.getDefinition().getMaxHit();
        }

        Player player = (Player) entity;

        return maximumRangeHitDpsCalc(player);
    }
}
