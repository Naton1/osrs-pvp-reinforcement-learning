package com.elvarg.game.content.combat.method.impl.npcs;

import com.elvarg.game.content.combat.method.impl.MeleeCombatMethod;
import com.elvarg.game.entity.impl.Mobile;
import com.elvarg.game.entity.impl.npc.NPC;
import com.elvarg.game.entity.impl.npc.impl.RockCrab;

import java.util.Arrays;

public class RockCrabCombatMethod extends MeleeCombatMethod {

    @Override
    public void onCombatBegan(Mobile character, Mobile target) {
        NPC npc = character.getAsNpc();

        if (npc == null) {
            return;
        }

        if (npc.getNpcTransformationId() == -1 ||
                Arrays.stream(RockCrab.ROCK_IDS).anyMatch(id -> id == npc.getNpcTransformationId())) {
            // Transform into an actual rock crab when combat starts
            npc.setNpcTransformationId(RockCrab.getTransformationId(npc.getId()));
        }

    }

    @Override
    public void onCombatEnded(Mobile character, Mobile target) {
        NPC npc = character.getAsNpc();

        if (npc == null || npc.isDying()) {
            return;
        }

        int undoTransformId = RockCrab.getTransformationId(npc.getNpcTransformationId());
        npc.setNpcTransformationId(undoTransformId);
    }
}
