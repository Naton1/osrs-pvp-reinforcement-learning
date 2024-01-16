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
import com.elvarg.util.ItemIdentifiers;

public class DamageFormulas {

    private static int effectiveStrengthLevel(Player player) {
        float str = player.getSkillManager().getCurrentLevel(Skill.STRENGTH);

        float prayerBonus = 1f;

        // Prayer additions
        if (PrayerHandler.isActivated(player, PrayerHandler.BURST_OF_STRENGTH)) {
            prayerBonus = 1.05f;
        }
        else if (PrayerHandler.isActivated(player, PrayerHandler.SUPERHUMAN_STRENGTH)) {
            prayerBonus = 1.10f;
        }
        else if (PrayerHandler.isActivated(player, PrayerHandler.ULTIMATE_STRENGTH)) {
            prayerBonus = 1.15f;
        }
        else if (PrayerHandler.isActivated(player, PrayerHandler.CHIVALRY)) {
            prayerBonus = 1.18f;
        }
        else if (PrayerHandler.isActivated(player, PrayerHandler.PIETY)) {
            prayerBonus = 1.23f;
        }

        str *= prayerBonus;
        str = (float) Math.floor(str);

        FightStyle fightStyle = player.getFightType().getStyle();
        if (fightStyle == FightStyle.AGGRESSIVE) {
            str += 3;
        }
        else if (fightStyle == FightStyle.CONTROLLED) {
            str += 1;
        }

        str += 8;

        if (CombatEquipment.wearingVoid(player, CombatType.MELEE)) {
            str *= 1.1f;
        }

        if (CombatEquipment.wearingObsidian(player)) {
            str *= 1.2f; // obisidian bonuses stack
        }

        return (int) Math.floor(str);
    }

    private static int maximumMeleeHitDpsCalc(Player player) {
        int strengthBonus = player.getBonusManager().getOtherBonus()[BonusManager.STRENGTH];
        float maxHit = effectiveStrengthLevel(player) * (strengthBonus + 64);
        maxHit += 320;
        maxHit /= 640;
        maxHit = (float) Math.floor(maxHit);

        if (CombatFactory.fullDharoks(player)) {
            float hp = player.getHitpoints();
            float max = player.getSkillManager().getMaxLevel(Skill.HITPOINTS);
            maxHit *= 1 + ((max - hp) / 100f) * (max / 100f);
        }

        if (player.isSpecialActivated()) {
            maxHit *= player.getCombatSpecial().getStrengthMultiplier();
        }

        return (int) Math.floor(maxHit);
    }

    public static int calculateMaxMeleeHit(Mobile entity) {
        if (entity.isPlayer()) {
            Player player = (Player) entity;
            return maximumMeleeHitDpsCalc(player);
        }
        NPC npc = (NPC) entity;
        float maxHit = npc.getCurrentDefinition().getMaxHit();

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
        float maxHit = 0;

        CombatSpell spell = c.getCombat().getSelectedSpell();

        if (spell != null) {
            if (spell.maximumHit() > 0) {
                maxHit = spell.maximumHit();
            }
            else if (c.isNpc()) {
                maxHit = c.getAsNpc().getDefinition().getMaxHit();
            }
            else {
                maxHit = 1;
            }
        }
        else if (c.isNpc()) {
            maxHit = c.getAsNpc().getDefinition().getMaxHit();
        }

        float equipmentBonus = c.isPlayer()
                               ? 1f + (c.getAsPlayer().getBonusManager().getOtherBonus()[BonusManager.MAGIC_STRENGTH] / 100f)
                               : 1f;

        maxHit *= equipmentBonus;

        // Add spell/target specific gear (tome of fire, salve amulet)

        return (int) Math.floor(maxHit);
    }

    private static float effectiveRangedStrength(Player player) {
        float rngStrength = player.getSkillManager().getCurrentLevel(Skill.RANGED);

        // Prayers
        float prayerMod = 1.0f;
        if (PrayerHandler.isActivated(player, PrayerHandler.SHARP_EYE)) {
            prayerMod = 1.05f;
        }
        else if (PrayerHandler.isActivated(player, PrayerHandler.HAWK_EYE)) {
            prayerMod = 1.10f;
        }
        else if (PrayerHandler.isActivated(player, PrayerHandler.EAGLE_EYE)) {
            prayerMod = 1.15f;
        }
        else if (PrayerHandler.isActivated(player, PrayerHandler.RIGOUR)) {
            prayerMod = 1.23f;
        }

        rngStrength *= prayerMod;
        rngStrength = (float) Math.floor(rngStrength);

        FightStyle fightStyle = player.getFightType().getStyle();
        if (fightStyle == FightStyle.ACCURATE) {
            rngStrength += 3;
        }

        rngStrength += 8;

        if (CombatEquipment.wearingVoid(player, CombatType.RANGED)) {
            // should be 1.125 if elite void
            rngStrength *= 1.1f;

        }

        return rngStrength;
    }

    private static int maximumRangeHitDpsCalc(Player player) {
        int strengthBonus = player.getBonusManager().getOtherBonus()[BonusManager.RANGED_STRENGTH];

        float maxHit = effectiveRangedStrength(player);
        maxHit *= (strengthBonus + 64);
        maxHit += 320;
        maxHit /= 640;

        // Extra gear bonus if/when supported (ex. tbow/salve)

        if (player.isSpecialActivated() && player.getCombatSpecial().getCombatMethod().type() == CombatType.RANGED) {
            maxHit *= player.getCombatSpecial().getStrengthMultiplier();
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
        if (entity.isNpc()) {
            NPC npc = (NPC) entity;
            return npc.getCurrentDefinition().getMaxHit();
        }

        Player player = (Player) entity;

        return maximumRangeHitDpsCalc(player);
    }
}
