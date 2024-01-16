package com.elvarg.game.content.combat.method.impl.npcs.pestcontrol;

import com.elvarg.game.content.combat.CombatType;
import com.elvarg.game.content.combat.hit.PendingHit;
import com.elvarg.game.content.combat.method.CombatMethod;
import com.elvarg.game.content.minigames.impl.pestcontrol.PestControl;
import com.elvarg.game.entity.impl.Mobile;
import com.elvarg.game.entity.impl.npc.NPC;
import com.elvarg.game.entity.impl.player.Player;

import java.util.Optional;

/**
 * @author Ynneh | 24/02/2023 - 11:09
 * <https://github.com/drhenny>
 */
public class PestControlPortalCombatMethod extends CombatMethod {

    @Override
    public CombatType type() {
        return CombatType.MELEE;
    }

    @Override
    public PendingHit[] hits(Mobile character, Mobile target) {
        return new PendingHit[0];
    }

    @Override
    public int attackDistance(Mobile character) {
        return 5;
    }

    @Override
    public boolean canAttack(Mobile character, Mobile target) {
        return PestControl.isPortal(character.getAsNpc().getId(), false);
    }

    @Override
    public void onDeath(NPC npc, Optional<Player> killer) {
        PestControl.healKnight(npc);
    }


}
