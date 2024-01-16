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

public class DragonWarhammerCombatMethod extends MeleeCombatMethod {

    private static final Animation ANIMATION = new Animation(1378, Priority.HIGH);
    private static final Graphic GRAPHIC = new Graphic(1292, Priority.HIGH);
    
    @Override
    public void start(Mobile character, Mobile target) {
        CombatSpecial.drain(character, CombatSpecial.DRAGON_WARHAMMER.getDrainAmount());
        character.performAnimation(ANIMATION);
        character.performGraphic(GRAPHIC);
    }

    @Override
    public void handleAfterHitEffects(PendingHit hit) {
        if (hit.isAccurate() && hit.getTarget().isPlayer()) {
            int damageDrain = (int) (hit.getTotalDamage() * 0.3);
            if (damageDrain < 0)
                return;
            Player player = hit.getAttacker().getAsPlayer();
            Player target = hit.getTarget().getAsPlayer();
            target.getSkillManager().decreaseCurrentLevel(Skill.DEFENCE, damageDrain, 1);
            player.getPacketSender().sendMessage("You've drained " + target.getUsername() + "'s Defence level by " + damageDrain + ".");
            target.getPacketSender().sendMessage("Your Defence level has been drained.");
        }
    }
}