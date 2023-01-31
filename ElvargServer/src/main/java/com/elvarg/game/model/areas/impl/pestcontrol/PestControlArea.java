package com.elvarg.game.model.areas.impl.pestcontrol;

import com.elvarg.game.content.PrayerHandler;
import com.elvarg.game.content.combat.hit.PendingHit;
import com.elvarg.game.content.minigames.MinigameHandler;
import com.elvarg.game.content.minigames.impl.PestControl;
import com.elvarg.game.entity.impl.Mobile;
import com.elvarg.game.entity.impl.npc.NPC;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.Boundary;
import com.elvarg.game.model.areas.Area;
import com.elvarg.net.packet.impl.EquipPacketListener;

import java.util.List;
import java.util.Optional;

import static com.elvarg.game.content.minigames.impl.PestControl.gameTimer;

public class PestControlArea extends Area {

    private PestControl minigame;

    public static final Boundary LAUNCHER_BOAT_BOUNDARY = new Boundary(2656, 2659, 2609, 2614);

    /**
     * Returns the singleton instance of the Pest Control minigame.
     *
     * Will fetch it if not alraedy populated.
     * @return
     */
    private PestControl getMinigame() {
        if(this.minigame == null) {
            this.minigame = (PestControl) MinigameHandler.Minigames.PEST_CONTROL.get();
        }

        return this.minigame;
    }

    public PestControlArea() {
        super(List.of(new Boundary(2616, 2691, 2556, 2624)));
    }

    @Override
    public void postEnter(Mobile character) {
        if (!character.isPlayer()) {
            return;
        }

        character.getAsPlayer().setWalkableInterfaceId(21100);
    }

    @Override
    public void process(Mobile character) {
        if (getMinigame().isActive()) {
            // Prevent any processing if the game is not actually underway.
            return;
        }

        if (character.isNpc()) {
            // Process npcs
            // TODO: Make brawlers path to void knight
            return;
        }

        if (character.isPlayerBot()) {
            // Handle player bots
            return;
        }

        if (character.isPlayer()) {
            Player player = character.getAsPlayer();
            // Handles player behaviour
            if (gameTimer > 60) {
                player.getPacketSender().sendString("Time remaining: " + gameTimer / 60 + " minutes", 21117);
            } else if (gameTimer < 60) {
                player.getPacketSender().sendString("Time remaining: " + gameTimer + " seconds", 21117);
            }
            //player.getPacketSender().sendMessage("The knights current health is " + KNIGHTS_HEALTH + ".");
            //player.getPacketSender().sendMessage("Your current pc damage is " + player.pcDamage + ".");

            /* TODO: Fix this interface
            for (j = 0; j < Server.npcHandler.npcs.length; j++) {
                if (Server.npcHandler.npcs[j] != null) {
                    if (Server.npcHandler.npcs[j].npcType == 3777)
                        c.getPA().sendFrame126("" + Server.npcHandler.npcs[j].HP + "", 21111);
                    if (Server.npcHandler.npcs[j].npcType == 3778)
                        c.getPA().sendFrame126("" + Server.npcHandler.npcs[j].HP + "", 21112);
                    if (Server.npcHandler.npcs[j].npcType == 3779)
                        c.getPA().sendFrame126("" + Server.npcHandler.npcs[j].HP + "", 21113);
                    if (Server.npcHandler.npcs[j].npcType == 3780)
                        c.getPA().sendFrame126("" + Server.npcHandler.npcs[j].HP + "", 21114);
                    if (Server.npcHandler.npcs[j].npcType == 3782)
                        c.getPA().sendFrame126("" + Server.npcHandler.npcs[j].HP + "", 21115);
                }
            }
            c.getPA().sendFrame126("0", 21116);
            c.getPA().sendFrame126("Time remaining: "+gameTimer+"", 21117);
 */
        }
    }

    @Override
    public void postLeave(Mobile character, boolean logout) {
        if (!character.isPlayer()) {
            return;
        }
        Player player = character.getAsPlayer();

        if (logout) {
            // If player has logged out, move them to gangplank
            player.moveTo(PestControl.GANG_PLANK_START);
        }

        player.setPoisonDamage(0);
        PrayerHandler.resetAll(player);
        player.getCombat().reset();
        player.getInventory().resetItems().refreshItems();
        player.resetAttributes();
        player.setSpecialPercentage(10);
        player.pcDamage = 0;
        EquipPacketListener.resetWeapon(player, true);
    }

    @Override
    public boolean canTeleport(Player player) {
        player.getPacketSender().sendMessage("You cannot teleport out of pest control!");
        return false;
    }

    @Override
    public boolean isMulti(Mobile character) {
        // Pest Control is multi combat
        return true;
    }

    @Override
    public boolean dropItemsOnDeath(Player player, Optional<Player> killer) {
        return false;
    }

    @Override
    public void onPlayerDealtDamage(Player player, Mobile target, PendingHit hit) {
        player.pcDamage += hit.getTotalDamage();
    }

    @Override
    public boolean handleDeath(Player player, Optional<Player> killer) {
        getMinigame().movePlayerToBoat(player);

        if (killer.isPresent()) {
            NPC npcKiller = killer.get().getAsNpc();
            player.getPacketSender().sendMessage("Oh no, you were killed by " + npcKiller.getDefinition().getName());
        }

        // Returning true means default death behavior is avoided.
        return true;
    }


    @Override
    public boolean handleObjectClick(Player player, int objectId, int type) {

        switch (objectId) {

            // Handle minigame objects here (fences and gates and shit)

        }

        return false;
    }
}
