package com.elvarg.game.content.combat.method.impl.specials;

import com.elvarg.game.Sound;
import com.elvarg.game.Sounds;
import com.elvarg.game.content.combat.CombatSpecial;
import com.elvarg.game.content.combat.hit.PendingHit;
import com.elvarg.game.content.combat.method.impl.MeleeCombatMethod;
import com.elvarg.game.entity.impl.Mobile;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.Animation;
import com.elvarg.game.model.Graphic;
import com.elvarg.game.model.GraphicHeight;
import com.elvarg.game.model.Priority;

public class DragonDaggerCombatMethod extends MeleeCombatMethod {

    private static final Animation ANIMATION = new Animation(1062, Priority.HIGH);
    private static final Graphic GRAPHIC = new Graphic(252, GraphicHeight.HIGH, Priority.HIGH);

    @Override
    public PendingHit[] hits(Mobile character, Mobile target) {
        return new PendingHit[] { new PendingHit(character, target, this),
                new PendingHit(character, target, this, target.isNpc() ? 1 : 0) };
    }

    @Override
    public void start(Mobile character, Mobile target) {
        CombatSpecial.drain(character, CombatSpecial.DRAGON_DAGGER.getDrainAmount());
        character.performAnimation(ANIMATION);
        character.performGraphic(GRAPHIC);
        if (character.isPlayer()) {
            Sounds.sendSound((Player)character, Sound.DRAGON_DAGGER_SPECIAL);
        }
    }
}