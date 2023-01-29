package com.elvarg.game.entity.impl.npc.impl;

import com.elvarg.game.content.sound.Sound;
import com.elvarg.game.content.sound.SoundManager;
import com.elvarg.game.entity.impl.grounditem.ItemOnGroundManager;
import com.elvarg.game.entity.impl.npc.NPC;
import com.elvarg.game.entity.impl.npc.NPCInteraction;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.Animation;
import com.elvarg.game.model.Ids;
import com.elvarg.game.model.Item;
import com.elvarg.game.model.Location;
import com.elvarg.game.task.Task;
import com.elvarg.game.task.TaskManager;

import static com.elvarg.util.ItemIdentifiers.SHEARS;
import static com.elvarg.util.ItemIdentifiers.WOOL;
import static com.elvarg.util.NpcIdentifiers.*;

@Ids({SHEEP_FULL_BLACK_HEAD, SHEEP_FULL_GREY_HEAD, SHEEP_FULL_WHITE_HEAD, SHEEP_FULL_YELLOW_GREY_HEAD, SHEEP_FULL_YELLOW_BLACK_HEAD})
public class Sheep extends NPC implements NPCInteraction {

    private static final Animation SHEARING = new Animation(893);

    private static final Animation SHEEP_EATING = new Animation(5335);

    private static final Item ITEM_WOOL = new Item(WOOL);

    /**
     * Constructs a Sheep.
     *
     * @param id       The npc id.
     * @param position
     */
    public Sheep(int id, Location position) {
        super(id, position);
    }

    @Override
    public void firstOptionClick(Player player, NPC npc) {
        this.shear(player, npc);
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
        if (itemId != SHEARS) {
            return;
        }

        this.shear(player, npc);
    }

    /**
     * Function to handle shearing of sheep.
     *
     * @param player
     */
    public void shear(Player player, NPC npc) {
        if (!player.getInventory().contains(SHEARS)) {
            player.getPacketSender().sendMessage("You need a set of shears to do this.");
            return;
        }

        player.performAnimation(SHEARING);
        SoundManager.sendSound(player, Sound.CUTTING);

        // Shear the sheep and add the wool
        TaskManager.submit(new Task(3, npc, false) {
            @Override
            protected void execute() {
                npc.setNpcTransformationId(getSheepTransformId(npc));
                npc.forceChat("Baa!");

                if (player.getInventory().getFreeSlots() > 0) {
                    player.getInventory().add(ITEM_WOOL);
                } else {
                    ItemOnGroundManager.register(player, ITEM_WOOL);
                    player.getPacketSender().sendMessage("You did not have enough inventory space so the Wool was dropped on the ground.");
                }

                this.stop();
            }
        });

        // Ensure the sheep's coat grows back after 10 game ticks
        TaskManager.submit(new Task(13, npc, false) {
            @Override
            protected void execute() {
                npc.performAnimation(SHEEP_EATING);
                npc.setNpcTransformationId(npc.getRealId());
                this.stop();
            }
        });

    }

    /**
     * Finds the correct NPC ID for current sheep to transform into.
     *
     * @param npc
     * @return id
     */
    private int getSheepTransformId(NPC npc) {
        switch (npc.getId()) {
            case SHEEP_FULL_BLACK_HEAD -> {
                return SHEEP_BALD_BLACK_HEAD;
            }
            case SHEEP_BALD_BLACK_HEAD -> {
                return SHEEP_FULL_BLACK_HEAD;
            }

            case SHEEP_FULL_GREY_HEAD -> {
                return SHEEP_BALD_GREY_HEAD;
            }
            case SHEEP_BALD_GREY_HEAD -> {
                return SHEEP_FULL_GREY_HEAD;
            }

            case SHEEP_FULL_WHITE_HEAD -> {
                return SHEEP_BALD_WHITE_HEAD;
            }
            case SHEEP_BALD_WHITE_HEAD -> {
                return SHEEP_FULL_WHITE_HEAD;
            }

            case SHEEP_FULL_YELLOW_GREY_HEAD -> {
                return SHEEP_BALD_YELLOW_GREY_HEAD;
            }
            case SHEEP_FULL_YELLOW_BLACK_HEAD -> {
                return SHEEP_BALD_YELLOW_BLACK_HEAD;
            }
        }

        return -1;
    }
}
