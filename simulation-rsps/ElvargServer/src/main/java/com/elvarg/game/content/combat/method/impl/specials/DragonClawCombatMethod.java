package com.elvarg.game.content.combat.method.impl.specials;

import com.elvarg.game.content.PrayerHandler;
import com.elvarg.game.content.combat.CombatConstants;
import com.elvarg.game.content.combat.CombatSpecial;
import com.elvarg.game.content.combat.formula.DamageFormulas;
import com.elvarg.game.content.combat.hit.PendingHit;
import com.elvarg.game.content.combat.method.impl.MeleeCombatMethod;
import com.elvarg.game.entity.impl.Mobile;
import com.elvarg.game.model.Animation;
import com.elvarg.game.model.Graphic;
import com.elvarg.game.model.Priority;
import com.elvarg.util.Misc;

public class DragonClawCombatMethod extends MeleeCombatMethod {

    private static final Animation ANIMATION = new Animation(7527, Priority.HIGH);
    private static final Graphic GRAPHIC = new Graphic(1171, Priority.HIGH);

    @Override
    public PendingHit[] hits(Mobile character, Mobile target) {
        PendingHit hit = new PendingHit(character, target, this, true, 4, 0);
        // Modify the hits.. Claws have a unique maxhit formula
        // Each hit rolls independently, but as soon as one hits, the rest hit too in a specific pattern

        // Damage rolls occur from a range based around the max hit
        int maxHit = DamageFormulas.calculateMaxMeleeHit(character);
        if (target.getPrayerActive()[PrayerHandler.PROTECT_FROM_MELEE]) {
            final double damageMultiplier = target.isNpc() ? CombatConstants.PRAYER_DAMAGE_REDUCTION_AGAINST_NPCS :
                                            CombatConstants.PRAYER_DAMAGE_REDUCTION_AGAINST_PLAYERS;
            maxHit *= damageMultiplier;
        }

        final int first, second, third, fourth;
        // First roll hit
        if (hit.getHits()[0].getDamage() > 0) {
            first = Misc.randomInclusive((int) Math.round(maxHit * 0.5), maxHit - 1);
            second = first / 2;
            third = second / 2;
            fourth = third + Misc.random(1); // 50% chance to add 1
        }
        // Second roll hit
        else if (hit.getHits()[1].getDamage() > 0) {
            first = 0;
            second = Misc.randomInclusive((int) Math.round(maxHit * (3 / 8D)), (int) Math.round(maxHit * (7 / 8D)));
            third = second / 2;
            fourth = third + Misc.random(1); // 50% chance to add 1
        }
        // Third roll hit
        else if (hit.getHits()[2].getDamage() > 0) {
            first = 0;
            second = 0;
            third = Misc.randomInclusive((int) Math.round(maxHit * 0.25), (int) Math.round(maxHit * 0.75));
            fourth = third + Misc.random(1); // 50% chance to add 1
        }
        // Fourth roll hit
        else if (hit.getHits()[3].getDamage() > 0) {
            first = 0;
            second = 0;
            third = 0;
            fourth = Misc.randomInclusive((int) Math.round(maxHit * 0.25), (int) Math.round(maxHit * 1.25));
        }
        // No roll hit
        else {
            first = 0;
            second = 0;
            third = Misc.random(1); // 50% chance to hit 1 1
            fourth = third;
        }

        hit.getHits()[0].setDamage(first);
        hit.getHits()[1].setDamage(second);
        hit.getHits()[2].setDamage(third);
        hit.getHits()[3].setDamage(fourth);
        hit.updateTotalDamage();
        return new PendingHit[] { hit };
    }

    @Override
    public void start(Mobile character, Mobile target) {
        CombatSpecial.drain(character, CombatSpecial.DRAGON_CLAWS.getDrainAmount());
        character.performAnimation(ANIMATION);
        character.performGraphic(GRAPHIC);
    }
}