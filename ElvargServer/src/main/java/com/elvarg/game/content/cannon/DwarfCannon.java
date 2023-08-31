package com.elvarg.game.content.cannon;

import com.elvarg.game.World;
import com.elvarg.game.collision.RegionManager;
import com.elvarg.game.content.combat.hit.HitDamage;
import com.elvarg.game.content.combat.hit.HitMask;
import com.elvarg.game.entity.impl.npc.NPC;
import com.elvarg.game.entity.impl.object.GameObject;
import com.elvarg.game.entity.impl.object.ObjectManager;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.*;
import com.elvarg.game.model.movement.path.PathFinder;
import com.elvarg.game.task.Task;
import com.elvarg.game.task.TaskManager;
import com.elvarg.util.Misc;
import com.google.common.collect.Maps;

import java.util.*;

import static com.elvarg.game.model.areas.impl.pestcontrol.PestControlArea.LAUNCHER_BOAT_BOUNDARY;
import static com.elvarg.util.ItemIdentifiers.CANNONBALL;

/**
 * @author Ynneh | 31/08/2023 - 16:21
 * <https://github.com/drhenny>
 */
public class DwarfCannon {

    public static Map<String, GameObject> active_cannons = Maps.newConcurrentMap();

    private Player player;

    private GameObject cannon;

    private int cannonBalls;

    private boolean firing;

    private List<Integer> cannonParts = Arrays.asList(DwarfCannonParts.BASE.itemId, DwarfCannonParts.STAND.itemId, DwarfCannonParts.BARRELS.itemId, DwarfCannonParts.FURNACE.itemId);

    private static final List<Boundary> CANNOT_SETUP_BOUNDS = Arrays.asList(LAUNCHER_BOAT_BOUNDARY);

    public DwarfCannon(Player player) {
        this.player = player;
    }

    private boolean notAllowedBounds() {
        return CANNOT_SETUP_BOUNDS.stream().anyMatch(b -> b.inside(player.getLocation()));
    }

    public boolean isOut() {
        return cannon != null;
    }

    public void setFiring(boolean firing) {
        this.firing = firing;
    }

    private int cannonRotationTick;

    public void setRotating(boolean toggle) {

        setFiring(toggle);

        if (!toggle) {
            return;
        }

        TaskManager.submit(new Task(0, player, true) {

            int baseAnim = 513, baseAnimMax = 521;

            @Override
            protected void execute() {

                cannonRotationTick++;

                if (!firing || cannon == null || cannonBalls == 0) {
                    if (cannonBalls == 0) {
                        player.getPacketSender().sendMessage("<col=ff0000>Your cannon is out of ammo.");
                    }
                    cannonRotationTick = cannon == null ? 0 : cannonRotationTick;
                    stop();
                    return;
                }

                int anim = baseAnim + cannonRotationTick;

                if (anim == baseAnimMax)
                    cannonRotationTick = 0;

                if (cannon != null) {
                    shootNPC();
                    cannon.performAnimation(new Animation(anim));
                }
            }
        });
    }

    private void shootNPC() {
        for (NPC npc : World.getNpcs()) {
            if (npc == null)
                continue;
            if (npc.getDefinition() == null)
                continue;
            if (npc.getDefinition().getCombatLevel() > 0 && npc.getDefinition().getMaxHit() > 1) {
                int distanceX = npc.getLocation().getX() - cannon.getLocation().getX();
                int distanceY = npc.getLocation().getY() - cannon.getLocation().getY();
                boolean canDamage = false;
                switch (cannonRotationTick) {
                    case 0 -> {
                        if ((distanceY <= 1 && distanceY >= -1) && (distanceX <= 8 && distanceX >= 0)) {
                            canDamage = true;
                        }
                    }
                    case 1 -> {
                        if ((distanceY >= -8 && distanceY <= 0) && (distanceX <= 8 && distanceX >= 0)) {
                            canDamage = true;
                        }
                    }
                    case 2 -> {
                        if ((distanceY >= -8 && distanceY <= 0) && (distanceX <= 1 && distanceX >= -1)) {
                            canDamage = true;
                        }
                    }
                    case 3 -> {
                        if ((distanceY >= -8 && distanceY <= 0) && (distanceX >= -8 && distanceX <= 0)) {
                            canDamage = true;
                        }
                    }
                    case 4 -> {
                        if ((distanceY >= -1 && distanceY <= 1) && (distanceX >= -8 && distanceX <= 0)) {
                            canDamage = true;
                        }
                    }

                    case 5 -> {
                        if ((distanceY <= 8 && distanceY >= 0) && (distanceX >= -8 && distanceX <= 0)) {
                            canDamage = true;
                        }
                    }

                    case 6 -> {//North
                        if ((distanceY <= 8 && distanceY >= 0) && (distanceX >= -1 && distanceX <= 1)) {
                            canDamage = true;
                        }
                    }
                    case 7 -> {
                        if ((distanceY <= 8 && distanceY >= 0) && (distanceX <= 8 && distanceX >= 0)) {
                            canDamage = true;
                        }
                    }
                }
                if (canDamage) {
                    if (cannonBalls > 0) {
                        cannonBalls--;
                        npc.getCombat().setTarget(player);
                        int damage = Misc.random(30);
                        npc.getCombat().getHitQueue().addPendingDamage(new HitDamage(1, HitMask.RED));
                        player.getSkillManager().addExperience(Skill.RANGED, (int) (damage / 2.5));
                    }
                }
            }

        }
    }

    private void cannon_projectile(NPC npc) {
        int npcX = cannon.getLocation().getX();
        int npcY = cannon.getLocation().getY();
        int offsetX = ((npcX - npc.getLocation().getX()) * -1);
        int offsetY = ((npcY - npc.getLocation().getY()) * -1);
        /**
         * TODO Projectile
         */
    }

    public void reload() {
        int inventoryAmount = player.getInventory().getAmount(CANNONBALL);

        if (inventoryAmount == 0)
            return;

        int remaining = 30 - cannonBalls;

        if (inventoryAmount < remaining) {
            remaining = inventoryAmount;
        }
        player.getPacketSender().sendMessage("You load the cannon with " + remaining + " cannonballs");
        cannonBalls += remaining;
        player.getInventory().delete(CANNONBALL, remaining);
    }

    public void pickup(GameObject object) {
        int index = getIndex(object);
        if (index == -1)
            return;

        int requiredSlot = 1 + index;
        if (cannonBalls > 0)
            requiredSlot++;

        if (player.getInventory().getFreeSlots() < requiredSlot) {
            player.getPacketSender().sendMessage("You need " + requiredSlot + " free inventory spaces to pick that up.");
            return;
        }
        setRotating(!firing);
        active_cannons.remove(player.getUsername());

        for (int i = 0; i <= index; i++) {
            int itemId = cannonParts.get(i);
            player.getInventory().add(itemId, 1);
        }
        if (cannonBalls > 0) {
            player.getInventory().add(CANNONBALL, cannonBalls);
        }

        ObjectManager.deregister(cannon, true);
        player.getPacketSender().sendMessage("You pick up the cannon. It's really heavy.");
        cannon = null;
        cannonBalls = 0;
    }

    private int getIndex(GameObject object) {
        Optional<DwarfCannonParts> part = Arrays.stream(DwarfCannonParts.values()).filter(p -> p.objectId == object.getId()).findFirst();
        return part.isPresent() ? part.get().ordinal() : -1;
    }

    /**
     * Summary.. it allows you to place 1 part.. but you'll need to add other parts otherwise
     *
     * @return
     */

    private boolean meetsRequirements() {
        final Location position = player.getLocation();
        if (cannon != null) {
            player.getPacketSender().sendMessage("You can only have 1 cannon setup at a time.");
            return false;
        }
        if (notAllowedBounds()) {
            player.getPacketSender().sendMessage("You can't set up a cannon here.");
            return false;
        }
        if (RegionManager.isBlockedTileSize(position, 2)) {
            player.sendMessage("There isn't enough space to set up here.");
            return false;
        }
        if (!player.getInventory().contains(DwarfCannonParts.BASE.getItemId())) {
            return false;
        }
        return true;
    }

    public void setup() {
        if (!meetsRequirements()) {
            return;
        }
        /** OSRS uses 2 tile walk distance **/
        PathFinder.findWalkable(player, player.getLocation().getX(), player.getLocation().getY(), 2);

        cannon = new GameObject(DwarfCannonParts.BASE.getObjectId(), player.getLocation(), 10, 1, player.getUsername(), player.getPrivateArea());

        ObjectManager.register(cannon, true);

        player.getPacketSender().sendMessage("You place the cannon base on the ground.");

        active_cannons.put(player.getUsername(), cannon);

        player.setAttribute("cannon-setup", true);

        final int[] cannonIndex = {0};

        TaskManager.submit(new Task(1, player, false) {

            int ticks = 1;

            boolean forceStop = false;

            @Override
            protected void execute() {

                if (ticks == 2) {
                    player.setPositionToFace(cannon.getLocation());
                    if (cannonIndex[0] >= DwarfCannonParts.values().length) {
                        reload();
                        player.setAttribute("cannon-setup", false);
                        stop();
                        return;
                    }
                    DwarfCannonParts cannonData = DwarfCannonParts.values()[cannonIndex[0]];
                    int itemId = cannonData.getItemId();
                    int objectId = cannonData.getObjectId();
                    if (player.getInventory().contains(itemId)) {
                        player.performAnimation(new Animation(827));
                        player.getPacketSender().sendMessage("You add the " + cannonData.name().toLowerCase() + ".");
                        player.getInventory().delete(itemId, 1);
                        transformObject(cannon, objectId);
                        cannonIndex[0]++;
                        ticks = 0;
                    } else {
                        forceStop = true;
                    }
                }
                if (ticks > 2) {
                    forceStop = true;
                }

                if (forceStop) {
                    player.setAttribute("cannon-setup", false);
                    stop();
                }

                ticks++;
            }
        });
    }

    private void transformObject(GameObject object, int nextId) {
        GameObject to = new GameObject(nextId, object.getLocation(), 10, 1, player.getUsername(), player.getPrivateArea());
        if (object != null)
            ObjectManager.deregister(object, true);
        cannon = to;
        active_cannons.put(player.getUsername(), cannon);
        ObjectManager.register(to, true);
    }

    public static boolean isObject(GameObject object) {
        return Arrays.stream(DwarfCannonParts.values()).anyMatch(p -> p.objectId == object.getId());
    }

    public void handleInteraction(GameObject object, int option) {

        if (!isCannonOwner(object))
            return;

        switch (option) {
            case 1 -> {
                if (object.getId() != DwarfCannonParts.FURNACE.objectId) {
                    pickup(object);
                    return;
                }
                setRotating(!firing);
            }
            case 2 -> {//Pick-up
                pickup(object);
            }

            case 3 -> {//Empty
                if (cannonBalls > 0) {
                    if (player.getInventory().getFreeSlots() > 0) {
                        player.getPacketSender().sendMessage("You unload your cannon and receive Cannonball x "+cannonBalls+".");
                        player.getInventory().add(CANNONBALL, cannonBalls);
                        cannonBalls = 0;
                    } else {
                        player.getPacketSender().sendMessage("You need 1 free space for this.");
                    }
                }
            }

        }
    }

    private boolean isCannonOwner(GameObject object) {
        if (object.getOwner() != null && !Objects.equals(object.getOwner(), player.getUsername())) {
            player.getPacketSender().sendMessage("This isn't your cannon.");
            return false;
        }
        return true;
    }

    public void handleCannonBallOnCannon(GameObject object, Item item) {
        if (!isCannonOwner(object))
            return;
        if (item.getId() != CANNONBALL)
            return;
        int amount = player.getInventory().getAmount(CANNONBALL);
        int remaining = 30 - cannonBalls;
        if (amount > 30)
            amount = 30;
        if (amount > remaining)
            amount = remaining;
        if (remaining < 1) {
            player.getPacketSender().sendMessage("Your cannon is already full.");
            return;
        }
        player.getInventory().delete(CANNONBALL, amount);
        cannonBalls += amount;
        player.getPacketSender().sendMessage("You load your cannon with x"+cannonBalls+" cannonballs.");
    }
}