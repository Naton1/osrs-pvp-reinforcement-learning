package com.elvarg.game.content.combat.bountyhunter;

import com.elvarg.game.entity.impl.npc.NPC;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.dialogues.builders.impl.ParduDialogue;
import com.elvarg.game.system.InteractIds;
import com.elvarg.game.system.NPCInteraction;

import static com.elvarg.util.NpcIdentifiers.PERDU;

@InteractIds(PERDU)
public class PerduInteraction extends NPCInteraction {

    @Override
    public void firstOptionClick(Player player, NPC npc) {
        player.getDialogueManager().start(new ParduDialogue());
    }
}
