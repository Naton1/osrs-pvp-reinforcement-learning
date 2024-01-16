package com.elvarg.game.model.areas.impl;

import java.util.Arrays;
import java.util.Optional;

import com.elvarg.game.content.minigames.impl.FightCaves;
import com.elvarg.game.entity.impl.Mobile;
import com.elvarg.game.entity.impl.npc.NPC;
import com.elvarg.game.entity.impl.object.GameObject;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.Boundary;
import com.elvarg.game.model.Item;
import com.elvarg.game.model.Location;
import com.elvarg.util.NpcIdentifiers;

public class FightCavesArea extends PrivateArea {

    public static final Boundary BOUNDARY = new Boundary(2368, 5056, 2431, 5119);

    public FightCavesArea() {
        super(Arrays.asList(BOUNDARY));
    }
    
    @Override
    public void postLeave(Mobile mobile, boolean logout) {
        if (mobile.isPlayer() && logout) {
            mobile.moveTo(FightCaves.EXIT);
        }
    }

    @Override
    public void process(Mobile mobile) {
    }

    @Override
    public boolean canTeleport(Player player) {
        return false;
    }

    @Override
    public boolean canTrade(Player player, Player target) {
        return false;
    }

    @Override
    public boolean isMulti(Mobile character) {
        return true;
    }

    @Override
    public boolean canEat(Player player, int itemId) {
        return true;
    }

    @Override
    public boolean canDrink(Player player, int itemId) {
        return true;
    }

    @Override
    public boolean dropItemsOnDeath(Player player, Optional<Player> killer) {
        return false;
    }

    @Override
    public boolean handleDeath(Player player, Optional<Player> killer) {
        player.moveTo(FightCaves.EXIT);
        //DialogueManager.start(player, 24);
        return true;
    }

    @Override
    public void onPlayerRightClick(Player player, Player rightClicked, int option) {
    }

    @Override
    public void defeated(Player player, Mobile character) {
        if (character.isNpc()) {
            NPC npc = character.getAsNpc();
            if (npc.getId() == NpcIdentifiers.TZTOK_JAD) {
                player.getInventory().forceAdd(player, new Item(6570, 1));
                player.resetAttributes();
                player.getCombat().reset();
                player.moveTo(FightCaves.EXIT);
                //DialogueManager.start(player, 25);
            }
        }
    }

    @Override
    public boolean overridesNpcAggressionTolerance(Player player, int npcId) {
        return true;
    }
    
    @Override
    public boolean handleObjectClick(Player player, GameObject object, int type) {
        return false;
    }
}
