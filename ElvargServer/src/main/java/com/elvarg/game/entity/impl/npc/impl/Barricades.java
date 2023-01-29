package com.elvarg.game.entity.impl.npc.impl;

import com.elvarg.game.content.sound.Sound;
import com.elvarg.game.content.sound.SoundManager;
import com.elvarg.game.World;
import com.elvarg.game.collision.RegionManager;
import com.elvarg.game.content.skill.skillable.impl.Firemaking;
import com.elvarg.game.entity.impl.npc.NPC;
import com.elvarg.game.entity.impl.npc.NPCInteraction;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.*;
import com.elvarg.game.task.Task;
import com.elvarg.game.task.TaskManager;
import io.netty.util.internal.ConcurrentSet;

import java.util.Arrays;
import java.util.Set;

import static com.elvarg.util.ItemIdentifiers.BUCKET_OF_WATER;
import static com.elvarg.util.ItemIdentifiers.TINDERBOX;
import static com.elvarg.util.NpcIdentifiers.*;

/**
 * @author Ynneh | 06/12/2022 - 14:47
 * <https://github.com/drhenny>
 */
@Ids({BARRICADE, BARRICADE_BURNING, BARRICADE_3, BARRICADE_4})
public class Barricades extends NPC implements NPCInteraction {

    /**
     * The ITEM_ID for the barricade in inventory
     */
    public static final int ITEM_ID = 4053;

    /**
     * Amount of firemaking experience to gain for each barricade light.
     * (Normal log burn = 40)
     */
    public static final int FIREMAKING_EXPERIENCE = 10;

    /**
     * A list used to the Tiles of the barricade clipping.
     */
    private static Set<Location> barricades = new ConcurrentSet<>();

    public int barricadeFireTicks = 8;

    public boolean barricadeOnFire;

    /**
     * Constructs a Barricade.
     *
     * @param id       The npc id.
     * @param position
     */
    public Barricades(int id, Location position) {
        super(id, position);
    }

    private static boolean getBlackListedTiles(Player player, Location requestedTile) {
        return Arrays.asList(new Location(1, 1, 0)).stream().anyMatch(t -> t.equals(requestedTile));
    }

    /**
     * Checks the tile upon death OR pickup to remove the clipping.
     * @param tile
     */
    public static void checkTile(Location tile) {
        barricades.stream().filter(t -> t.equals(tile)).forEach(t -> {
            /** Removes clipping flag at the location **/
            RegionManager.removeClipping(t.getX(), t.getY(), t.getZ(), 0x200000, null);
            /** Removes tile from the list **/
            barricades.remove(t);
        });
    }

    /**
     * Upon placement checks IF they meet the requirements before adding physical clipping flag.
     *
     * @param player
     * @return
     */
    public static boolean canSetup(Player player) {
        Location tile = player.getLocation();
        boolean existsAtTile = barricades.stream().anyMatch(t -> t.equals(tile));
        if (existsAtTile) {
            player.getPacketSender().sendMessage("You can't set up a barricade here.");
            return true;
        }
        if (RegionManager.getClipping(tile.getX(), tile.getY(), tile.getZ(), player.getPrivateArea()) != 0) {
            player.getPacketSender().sendMessage("You can't set up a barricade here.");
            return true;
        }
        deploy(player);
        return true;
    }

    private static void handleTinderbox(Player player, Barricades npc) {
        if (npc.barricadeOnFire) {
            player.getPacketSender().sendMessage("This barricade is already on fire!");
            return;
        }
        if (!player.getInventory().contains(590)) {
            player.getPacketSender().sendMessage("You need a tinderbox to set the barricade on fire.");
            return;
        }

        player.performAnimation(Firemaking.LIGHT_FIRE);
        SoundManager.sendSound(player, Sound.FIRE_FIRST_ATTEMPT);

        TaskManager.submit(new Task(3, player, false) {
            @Override
            protected void execute() {
                npc.setNpcTransformationId(BARRICADE_BURNING);
                npc.barricadeOnFire = true;
                player.getSkillManager().addExperience(Skill.FIREMAKING, FIREMAKING_EXPERIENCE);
                player.performAnimation(Animation.DEFAULT_RESET_ANIMATION);
                this.stop();
            }
        });

    }

    private static void handleBucketOfWater(Player player, Barricades npc) {
        if (!npc.barricadeOnFire) {
            player.getPacketSender().sendMessage("This barricade is not on fire.");
            return;
        }
        if (!player.getInventory().contains(1929)) {
            player.getPacketSender().sendMessage("You need a bucket of water to extinguish the fire.");
            return;
        }
        player.getInventory().delete(new Item(1929, 1));
        player.getInventory().add(new Item(1925, 1));
        npc.setNpcTransformationId(BARRICADE);
        npc.barricadeOnFire = false;
        player.getPacketSender().sendMessage("You put out the fire!");
    }

    /**
     * Upon placing and passing successful checks.
     * @param player
     */
    private static void deploy(Player player) {
        Location tile = player.getLocation();
        RegionManager.addClipping(tile.getX(), tile.getY(), tile.getZ(), 0x200000, player.getPrivateArea());
        player.getInventory().delete(ITEM_ID, 1);
        barricades.add(tile);
        World.getAddNPCQueue().add(new NPC(BARRICADE, tile.clone()));
        SoundManager.sendSound(player, Sound.PICK_UP_ITEM);
    }

    @Override
    public void firstOptionClick(Player player, NPC npc) {

    }

    @Override
    public void secondOptionClick(Player player, NPC npc) {
        if (!(npc instanceof Barricades)) {
            return;
        }
        Barricades barricade = (Barricades) npc;

        if (barricade.barricadeOnFire) {
            handleBucketOfWater(player, barricade);
            return;
        }

        handleTinderbox(player, barricade);
    }

    @Override
    public void thirdOptionClick(Player player, NPC npc) {

    }

    @Override
    public void forthOptionClick(Player player, NPC npc) {

    }

    @Override
    public void useItemOnNpc(Player player, NPC npc, int itemId, int slot) {
        if (!(npc instanceof Barricades)) {
            return;
        }
        Barricades barricade = (Barricades) npc;

        switch (itemId) {
            case TINDERBOX:
                handleTinderbox(player, barricade);
                return;
            case BUCKET_OF_WATER:
                handleBucketOfWater(player, barricade);
                return;
        }
    }

    @Override
    public void process() {
        super.process();

        if (barricadeOnFire && barricadeFireTicks > 0) {
            barricadeFireTicks--;
            if (barricadeFireTicks == 0) {
                if (this.isBarricade()) {
                    Barricades.checkTile(this.getLocation());
                }
                barricadeOnFire = false;
                World.getRemoveNPCQueue().add(this);
            }
        }
    }
}
