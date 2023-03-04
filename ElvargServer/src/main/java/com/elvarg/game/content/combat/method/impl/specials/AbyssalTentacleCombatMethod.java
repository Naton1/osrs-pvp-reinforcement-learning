package com.elvarg.game.content.combat.method.impl.specials;

import com.elvarg.game.content.combat.CombatFactory;
import com.elvarg.game.content.combat.CombatSpecial;
import com.elvarg.game.content.combat.hit.PendingHit;
import com.elvarg.game.content.combat.method.impl.MeleeCombatMethod;
import com.elvarg.game.entity.impl.Mobile;
import com.elvarg.game.model.Animation;
import com.elvarg.game.model.Graphic;
import com.elvarg.game.model.GraphicHeight;
import com.elvarg.game.model.Priority;
import com.elvarg.game.task.impl.CombatPoisonEffect.PoisonType;
import com.elvarg.util.Misc;

public class AbyssalTentacleCombatMethod extends MeleeCombatMethod {

    private static final Animation ANIMATION = new Animation(1658, Priority.HIGH);
    private static final Graphic GRAPHIC = new Graphic(181, GraphicHeight.HIGH, Priority.HIGH);

    @Override
    public void start(Mobile character, Mobile target) {
        CombatSpecial.drain(character, CombatSpecial.ABYSSAL_TENTACLE.getDrainAmount());
        character.performAnimation(ANIMATION);
    }

    @Override
    public void handleAfterHitEffects(PendingHit hit) {
        Mobile target = hit.getTarget();

        if (target.getHitpoints() <= 0) {
            return;
        }
        
        target.performGraphic(GRAPHIC);
        CombatFactory.freeze(target, 8);
        if (Misc.getRandom(100) < 50) {
            CombatFactory.poisonEntity(target, PoisonType.EXTRA);
        }
    }
}