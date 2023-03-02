package com.elvarg.game.content.combat.method.impl.npcs.pestcontrol;

import com.elvarg.game.content.combat.method.CombatMethod;
import com.elvarg.game.content.combat.method.impl.MeleeCombatMethod;
import com.elvarg.game.entity.impl.Mobile;
import com.elvarg.game.entity.impl.npc.NPC;

/**
 * @author Ynneh | 01/03/2023 - 04:17
 * <https://github.com/drhenny>
 */
public class SpinnerCombatMethod extends MeleeCombatMethod {


    @Override
    public void onTick(NPC npc, Mobile target) {

        System.err.println(target == null);
    }

}
