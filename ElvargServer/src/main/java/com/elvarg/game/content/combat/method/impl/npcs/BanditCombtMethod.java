package com.elvarg.game.content.combat.method.impl.npcs;

import com.elvarg.game.content.combat.method.impl.MeleeCombatMethod;
import com.elvarg.game.entity.impl.Mobile;
import com.elvarg.game.entity.impl.npc.NPC;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.container.impl.Equipment;

public class BanditCombtMethod extends MeleeCombatMethod {

    @Override
    public void onCombatBegan(Mobile character, Mobile target) {
        if (character == null || target == null) {
            return;
        }

        NPC npc = character.getAsNpc();
        Player player = target.getAsPlayer();

        if (npc == null || player == null) {
            return;
        }

        int zamorakItemCount = Equipment.getItemCount(player, "Zamorak", true);
        int saradominItemCount = Equipment.getItemCount(player, "Saradomin", true);

        if (saradominItemCount > 0) {
            npc.forceChat("Time to die, Saradominist filth!");
        } else if (zamorakItemCount > 0) {
            npc.forceChat("Prepare to suffer, Zamorakian scum!");
        } else {
            npc.forceChat("You chose the wrong place to start trouble!");
        }
    }

}
