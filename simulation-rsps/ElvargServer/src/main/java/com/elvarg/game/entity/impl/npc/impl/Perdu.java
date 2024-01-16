package com.elvarg.game.entity.impl.npc.impl;

import com.elvarg.game.entity.impl.npc.NPC;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.Ids;
import com.elvarg.game.model.Location;
import com.elvarg.game.model.dialogues.builders.impl.ParduDialogue;
import com.elvarg.game.entity.impl.npc.NPCInteraction;

import static com.elvarg.util.NpcIdentifiers.PERDU;

@Ids(PERDU)
public class Perdu extends NPC implements NPCInteraction {

    /**
     * Constructs a Perdu.
     *
     * @param id       The npc id.
     * @param position
     */
    public Perdu(int id, Location position) {
        super(id, position);
    }

    @Override
    public void firstOptionClick(Player player, NPC npc) {
        player.getDialogueManager().start(new ParduDialogue());
    }

    @Override
    public void secondOptionClick(Player player, NPC npc) {

    }

    @Override
    public void thirdOptionClick(Player player, NPC npc) {

    }

    @Override
    public void forthOptionClick(Player player, NPC npc) {

    }

    @Override
    public void useItemOnNpc(Player player, NPC npc, int itemId, int slot) {

    }
}
