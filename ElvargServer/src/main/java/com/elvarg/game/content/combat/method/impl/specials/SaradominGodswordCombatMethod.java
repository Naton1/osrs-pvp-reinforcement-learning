package com.elvarg.game.content.combat.method.impl.specials;

import com.elvarg.game.content.combat.CombatSpecial;
import com.elvarg.game.content.combat.hit.PendingHit;
import com.elvarg.game.content.combat.method.impl.MeleeCombatMethod;
import com.elvarg.game.entity.impl.Mobile;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.Animation;
import com.elvarg.game.model.Graphic;
import com.elvarg.game.model.Priority;
import com.elvarg.game.model.Skill;

public class SaradominGodswordCombatMethod extends MeleeCombatMethod {

    private static final Animation ANIMATION = new Animation(7640, Priority.HIGH);
    private static final Graphic GRAPHIC = new Graphic(1209, Priority.HIGH);

    @Override
    public void start(Mobile character, Mobile target) {
        CombatSpecial.drain(character, CombatSpecial.SARADOMIN_GODSWORD.getDrainAmount());
        character.performAnimation(ANIMATION);
        character.performGraphic(GRAPHIC);
    }

    @Override
    public void handleAfterHitEffects(PendingHit hit) {
        Player player = hit.getAttacker().getAsPlayer();
        int damage = hit.getTotalDamage();
        int damageHeal = (int) (damage * 0.5);
        int damagePrayerHeal = (int) (damage * 0.25);
        if (player.getSkillManager().getCurrentLevel(Skill.HITPOINTS) < player.getSkillManager()
                .getMaxLevel(Skill.HITPOINTS)) {
            int level = player.getSkillManager().getCurrentLevel(Skill.HITPOINTS) + damageHeal > player
                    .getSkillManager().getMaxLevel(Skill.HITPOINTS)
                            ? player.getSkillManager().getMaxLevel(Skill.HITPOINTS)
                            : player.getSkillManager().getCurrentLevel(Skill.HITPOINTS) + damageHeal;
            player.getSkillManager().setCurrentLevel(Skill.HITPOINTS, level);
        }
        if (player.getSkillManager().getCurrentLevel(Skill.PRAYER) < player.getSkillManager()
                .getMaxLevel(Skill.PRAYER)) {
            int level = player.getSkillManager().getCurrentLevel(Skill.PRAYER) + damagePrayerHeal > player
                    .getSkillManager().getMaxLevel(Skill.PRAYER) ? player.getSkillManager().getMaxLevel(Skill.PRAYER)
                            : player.getSkillManager().getCurrentLevel(Skill.PRAYER) + damagePrayerHeal;
            player.getSkillManager().setCurrentLevel(Skill.PRAYER, level);
        }
    }
}