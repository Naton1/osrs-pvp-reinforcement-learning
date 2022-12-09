package com.elvarg.game.entity.impl.npc.impl;

import com.elvarg.game.World;
import com.elvarg.game.collision.RegionManager;
import com.elvarg.game.entity.impl.npc.NPC;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.Item;
import com.elvarg.game.model.Location;
import io.netty.util.internal.ConcurrentSet;

import java.util.Arrays;
import java.util.Set;

/**
 * @author Ynneh | 06/12/2022 - 14:47
 * <https://github.com/drhenny>
 */
public class Barricades {

    /**
     * The NPC_ID of the barricade
     */
    public static final int NPC_ID = 5722;

    /**
     * The NPC_ID upon burning state
     */
    public static final int NPC_ID_BURNING = 5723;

    /**
     * The ITEM_ID for the barricade in inventory
     */
    public static final int BARRICADE_ITEM_ID = 4053;

    /**
     * A list used to the Tiles of the barricade clipping.
     */
    private static Set<Location> barricades = new ConcurrentSet<>();

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

    private static void handleTinderbox(Player player, NPC npc) {
        if (npc.barricadeOnFire) {
            player.getPacketSender().sendMessage("This barricade is already on fire!");
            return;
        }
        if (!player.getInventory().contains(590)) {
            player.getPacketSender().sendMessage("You need a tinderbox to set the barricade on fire.");
            return;
        }
        npc.setNpcTransformationId(NPC_ID_BURNING);
        npc.barricadeOnFire = true;
    }

    private static void handleBucketOfWater(Player player, NPC npc) {
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
        npc.setNpcTransformationId(NPC_ID);
        npc.barricadeOnFire = false;
        player.getPacketSender().sendMessage("You put out the fire!");
    }

    /**
     * Upon placing and passing successful checks.
     * @param player
     */
    private static void deploy(Player player) {
        Location tile = player.getLocation();
        /** Added clipping flag at the location **/
        RegionManager.addClipping(tile.getX(), tile.getY(), tile.getZ(), 0x200000, player.getPrivateArea());
        /** Deletes 1 of the Barricade ITEM **/
        player.getInventory().delete(BARRICADE_ITEM_ID, 1);
        /** Adds Tile to list. **/
        barricades.add(tile);
        /** Spawns the NPC **/
        World.getAddNPCQueue().add(new NPC(NPC_ID, tile.clone()));
    }

    public static boolean handleInteractiveOptions(Player player, NPC npc, int opcode) {
        boolean isBarricade = Arrays.asList(NPC_ID, NPC_ID_BURNING).stream().anyMatch(n -> n.intValue() == npc.getId());
        if (!isBarricade) {
            return false;
        }
        if (opcode == 17) {
            /**
             * Option 2 (BURN/EXTINGUISH)
             */
            if (npc.barricadeOnFire) {
                handleBucketOfWater(player, npc);
                return true;
            }
            handleTinderbox(player, npc);
            return true;
        }
        return false;
    }

    public static boolean itemOnBarricade(Player player, NPC npc, Item item) {
        switch (item.getId()) {
            case 590:
                handleTinderbox(player, npc);
                return true;
            case 1929:
                handleBucketOfWater(player, npc);
                return true;
            default:
                return false;
        }
    }
}
