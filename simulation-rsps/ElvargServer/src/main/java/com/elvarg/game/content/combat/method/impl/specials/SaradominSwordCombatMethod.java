package com.elvarg.game.content.combat.method.impl.specials;

import com.elvarg.game.content.combat.CombatSpecial;
import com.elvarg.game.content.combat.hit.PendingHit;
import com.elvarg.game.content.combat.method.impl.MeleeCombatMethod;
import com.elvarg.game.entity.impl.Mobile;
import com.elvarg.game.model.Animation;
import com.elvarg.game.model.Graphic;
import com.elvarg.game.model.Priority;

public class SaradominSwordCombatMethod extends MeleeCombatMethod {

    private static final Graphic ENMEMY_GRAPHIC = new Graphic(1196, Priority.HIGH);
    private static final Animation ANIMATION = new Animation(1132, Priority.HIGH);
    private static final Graphic GRAPHIC = new Graphic(1213, Priority.HIGH);

    @Override
    public PendingHit[] hits(Mobile character, Mobile target) {
        PendingHit hit = new PendingHit(character, target, this, true, 2, 0);
        hit.getHits()[1].setDamage(hit.isAccurate() ? hit.getHits()[0].getDamage() + 16 : 0);
        hit.updateTotalDamage();
        return new PendingHit[] { hit };
    }

    @Override
    public void start(Mobile character, Mobile target) {
        CombatSpecial.drain(character, CombatSpecial.SARADOMIN_SWORD.getDrainAmount());
        character.performAnimation(ANIMATION);
        character.performGraphic(GRAPHIC);
    }

    @Override
    public void handleAfterHitEffects(PendingHit hit) {
        hit.getTarget().performGraphic(ENMEMY_GRAPHIC);
    }
}