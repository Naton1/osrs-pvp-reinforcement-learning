package com.elvarg.game.content.combat.method.impl.specials;

import com.elvarg.game.content.combat.CombatSpecial;
import com.elvarg.game.content.combat.method.impl.MeleeCombatMethod;
import com.elvarg.game.entity.impl.Mobile;
import com.elvarg.game.model.Animation;
import com.elvarg.game.model.Graphic;
import com.elvarg.game.model.GraphicHeight;
import com.elvarg.game.model.Priority;

public class GraniteMaulCombatMethod extends MeleeCombatMethod {

    private static final Animation ANIMATION = new Animation(1667, Priority.HIGH);
    private static final Graphic GRAPHIC = new Graphic(340, GraphicHeight.HIGH, Priority.HIGH);
    
    @Override
    public void start(Mobile character, Mobile target) {
        CombatSpecial.drain(character, CombatSpecial.GRANITE_MAUL.getDrainAmount());
        character.performAnimation(ANIMATION);
        character.performGraphic(GRAPHIC);
    }
}
