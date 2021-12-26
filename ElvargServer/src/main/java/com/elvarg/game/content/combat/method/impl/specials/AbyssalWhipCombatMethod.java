package com.elvarg.game.content.combat.method.impl.specials;

import com.elvarg.game.content.combat.CombatSpecial;
import com.elvarg.game.content.combat.hit.PendingHit;
import com.elvarg.game.content.combat.method.impl.MeleeCombatMethod;
import com.elvarg.game.entity.impl.Mobile;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.Animation;
import com.elvarg.game.model.Graphic;
import com.elvarg.game.model.GraphicHeight;
import com.elvarg.game.model.Priority;

public class AbyssalWhipCombatMethod extends MeleeCombatMethod {

    private static final Animation ANIMATION = new Animation(1658, Priority.HIGH);
    private static final Graphic GRAPHIC = new Graphic(341, GraphicHeight.HIGH, Priority.HIGH);

    @Override
    public void start(Mobile character, Mobile target) {
        CombatSpecial.drain(character, CombatSpecial.ABYSSAL_WHIP.getDrainAmount());
        character.performAnimation(ANIMATION);
    }

    @Override
    public void handleAfterHitEffects(PendingHit hit) {
        Mobile target = hit.getTarget();

        if (target.getHitpoints() <= 0) {
            return;
        }

        target.performGraphic(GRAPHIC);
        if (target.isPlayer()) {
            Player p = (Player) target;
            int totalRunEnergy = p.getRunEnergy() - 25;
            if (totalRunEnergy < 0) {
                totalRunEnergy = 0;
            }
            p.setRunEnergy(totalRunEnergy);
            p.getPacketSender().sendRunEnergy();
            if (totalRunEnergy == 0) {
                p.setRunning(false);
                p.getPacketSender().sendRunStatus();
            }
        }
    }
}