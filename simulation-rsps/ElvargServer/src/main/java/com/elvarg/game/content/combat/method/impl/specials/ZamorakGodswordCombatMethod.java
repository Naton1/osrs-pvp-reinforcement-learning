package com.elvarg.game.content.combat.method.impl.specials;

import com.elvarg.game.content.combat.CombatFactory;
import com.elvarg.game.content.combat.CombatSpecial;
import com.elvarg.game.content.combat.hit.PendingHit;
import com.elvarg.game.content.combat.method.impl.MeleeCombatMethod;
import com.elvarg.game.entity.impl.Mobile;
import com.elvarg.game.model.Animation;
import com.elvarg.game.model.Graphic;
import com.elvarg.game.model.Priority;

public class ZamorakGodswordCombatMethod extends MeleeCombatMethod {

    private static final Animation ANIMATION = new Animation(7638, Priority.HIGH);
    private static final Graphic GRAPHIC = new Graphic(1210, Priority.HIGH);

    @Override
    public void start(Mobile character, Mobile target) {
        CombatSpecial.drain(character, CombatSpecial.ZAMORAK_GODSWORD.getDrainAmount());
        character.performAnimation(ANIMATION);
    }

    @Override
    public void handleAfterHitEffects(PendingHit hit) {
        if (hit.isAccurate()) {
            hit.getTarget().performGraphic(GRAPHIC);
            CombatFactory.freeze(hit.getTarget(), 32);
        }
    }
}