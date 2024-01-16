package com.github.naton1.rl.util;

import com.elvarg.game.content.Food;
import com.elvarg.game.content.PrayerHandler;
import com.elvarg.game.content.combat.CombatType;
import com.elvarg.game.content.combat.hit.HitDamage;
import com.elvarg.game.content.skill.skillable.impl.Herblore;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.ItemInSlot;
import com.elvarg.game.model.Skill;
import java.util.Arrays;
import java.util.Objects;

public class GameDataUtil {

    public static CombatType getPrayerType(Player player) {
        return Arrays.stream(CombatType.values())
                .filter(c -> player.getPrayerActive()[PrayerHandler.getProtectingPrayer(c)])
                .findFirst()
                .orElse(null);
    }

    public static double getRemainingBrewScale(Player player) {
        final int brewHealAmount = getBrewHealAmount(player);
        return Arrays.stream(player.getInventory().getItems())
                        .mapToInt(i -> Herblore.PotionDose.SARADOMIN_BREW.getDoseForID(i.getId()))
                        .filter(i -> i > 0)
                        .map(dose -> dose * brewHealAmount)
                        .sum()
                / (double) player.getSkillManager().getMaxLevel(Skill.HITPOINTS);
    }

    public static int getBrewHealAmount(Player player) {
        return (int) Math.floor(2 + (0.15 * player.getSkillManager().getMaxLevel(Skill.HITPOINTS)));
    }

    public static int getRemainingFoodCount(Player player) {
        return (int) Arrays.stream(Food.Edible.values())
                .map(food -> ItemInSlot.getFromInventory(food.getItem().getId(), player.getInventory()))
                .filter(Objects::nonNull)
                .count();
    }

    public static double getRemainingFoodScale(Player player) {
        return Arrays.stream(Food.Edible.values())
                        .mapToInt(food ->
                                player.getInventory().getAmount(food.getItem().getId()) * food.getHeal())
                        .sum()
                / (double) player.getSkillManager().getMaxLevel(Skill.HITPOINTS);
    }

    public static double getHealthPercent(Player player) {
        return player.getHitpoints() / (double) player.getSkillManager().getMaxLevel(Skill.HITPOINTS);
    }

    public static int getDamageReceived(Player player, HitDamage previousPrimaryHit, HitDamage previousSecondaryHit) {
        final HitDamage currentPrimaryHit = player.getPrimaryHit();
        final HitDamage currentSecondaryHit = player.getSecondaryHit();
        int damage = 0;
        if (currentPrimaryHit != previousPrimaryHit && currentPrimaryHit != null) {
            damage += currentPrimaryHit.getDamage();
        }
        if (currentSecondaryHit != previousSecondaryHit && currentSecondaryHit != null) {
            damage += currentSecondaryHit.getDamage();
        }
        return damage;
    }

    public static boolean hasFood(Player player) {
        return Arrays.stream(Food.Edible.values())
                .map(food -> ItemInSlot.getFromInventory(food.getItem().getId(), player.getInventory()))
                .anyMatch(Objects::nonNull);
    }
}
