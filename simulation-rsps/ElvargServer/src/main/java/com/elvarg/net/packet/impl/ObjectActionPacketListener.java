package com.elvarg.net.packet.impl;

import com.elvarg.Server;
import com.elvarg.game.collision.RegionManager;
import com.elvarg.game.content.DepositBox;
import com.elvarg.game.content.combat.CombatSpecial;
import com.elvarg.game.content.minigames.MinigameHandler;
import com.elvarg.game.content.minigames.impl.FightCaves;
import com.elvarg.game.content.skill.SkillManager;
import com.elvarg.game.content.skill.skillable.impl.Smithing;
import com.elvarg.game.content.skill.skillable.impl.Smithing.Bar;
import com.elvarg.game.content.skill.skillable.impl.Smithing.EquipmentMaking;
import com.elvarg.game.content.skill.skillable.impl.Thieving.StallThieving;
import com.elvarg.game.definition.ObjectDefinition;
import com.elvarg.game.entity.impl.object.GameObject;
import com.elvarg.game.entity.impl.object.MapObjects;
import com.elvarg.game.entity.impl.object.impl.WebHandler;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.*;
import com.elvarg.game.model.areas.impl.PrivateArea;
import com.elvarg.game.model.dialogues.builders.impl.SpellBookDialogue;
import com.elvarg.game.model.rights.PlayerRights;
import com.elvarg.game.model.teleportation.TeleportHandler;
import com.elvarg.game.model.teleportation.TeleportType;
import com.elvarg.game.task.Task;
import com.elvarg.game.task.TaskManager;
import com.elvarg.game.task.impl.ForceMovementTask;
import com.elvarg.game.task.impl.WalkToTask;
import com.elvarg.net.packet.Packet;
import com.elvarg.net.packet.PacketConstants;
import com.elvarg.net.packet.PacketExecutor;
import com.elvarg.util.ObjectIdentifiers;
import com.elvarg.game.entity.impl.object.ObjectManager;
import java.util.Objects;

/**
 * This packet listener is called when a player clicked on a game object.
 *
 * @author relex lawl
 */

public class ObjectActionPacketListener extends ObjectIdentifiers implements PacketExecutor {

	/**
	 * Handles the first click option on an object.
	 *
	 * @param player
	 *            The player that clicked on the object.
	 * @param object
	 *            The packet containing the object's information.
	 */
    private static void firstClick(Player player, GameObject object) {
        if(doorHandler(player, object)) {
	        return;
	    }

        // Skills..
        if (player.getSkillManager().startSkillable(object)) {
            return;
        }

        if (MinigameHandler.firstClickObject(player, object)) {
            // Player has clicked an object inside a minigame
            return;
        }

        final ObjectDefinition defs = object.getDefinition();
        if (defs != null) {
            if (defs.name != null && Objects.equals(defs.name, "Bank Deposit Box")) {
                DepositBox.open(player);
                return;
            }

        }

        if (com.elvarg.game.content.cannon.DwarfCannon.isObject(object)) {
            player.getDwarfCannon().handleInteraction(object, 1);
            return;
        }

        switch (object.getId()) {
            case WEB:
                if (!WebHandler.wieldingSharpItem(player)) {
                    player.getPacketSender().sendMessage("Only a sharp blade can cut through this sticky web.");
                    return;
                }
                WebHandler.handleSlashWeb(player, object, false);
                break;
        case KBD_LADDER_DOWN:
            TeleportHandler.teleport(player, new Location(3069, 10255), TeleportType.LADDER_DOWN, false);
            break;
        case KBD_LADDER_UP:
            TeleportHandler.teleport(player, new Location(3017, 3850), TeleportType.LADDER_UP, false);
            break;
        case KBD_ENTRANCE_LEVER:
            if (!player.getCombat().getTeleBlockTimer().finished()) {
                player.getPacketSender().sendMessage("A magical spell is blocking you from teleporting.");
                return;
            }
            TeleportHandler.teleport(player, new Location(2271, 4680), TeleportType.LEVER, false);
            break;
        case KBD_EXIT_LEVER:
            TeleportHandler.teleport(player, new Location(3067, 10253), TeleportType.LEVER, false);
            break;
        case FIGHT_CAVES_ENTRANCE:
            player.moveTo(FightCaves.ENTRANCE.clone());
            FightCaves.start(player);
            break;
        case FIGHT_CAVES_EXIT:
            player.moveTo(FightCaves.EXIT);
            break;
        case PORTAL_51:
            //DialogueManager.sendStatement(player, "Construction will be avaliable in the future.");
            break;
        case ANVIL:
            EquipmentMaking.openInterface(player);
            break;
        case ALTAR:
        case CHAOS_ALTAR_2:
            player.performAnimation(new Animation(645));
            if (player.getSkillManager().getCurrentLevel(Skill.PRAYER) < player.getSkillManager()
                    .getMaxLevel(Skill.PRAYER)) {
                player.getSkillManager().setCurrentLevel(Skill.PRAYER,
                        player.getSkillManager().getMaxLevel(Skill.PRAYER), true);
                player.getPacketSender().sendMessage("You recharge your Prayer points.");
            }
            break;

        case BANK_CHEST:
            player.getBank(player.getCurrentBankTab()).open();
            break;

        case DITCH_PORTAL:
            player.getPacketSender().sendMessage("You are teleported to the Wilderness ditch.");
            player.moveTo(new Location(3087, 3520));
            break;

        case WILDERNESS_DITCH:
            //player.getMovementQueue().reset();
            if (player.getForceMovement() == null && player.getClickDelay().elapsed(2000)) {
                final Location crossDitch = new Location(0, player.getLocation().getY() < 3522 ? 3 : -3);
                TaskManager.submit(new ForceMovementTask(player, 3, new ForceMovement(player.getLocation().clone(),
                        crossDitch, 0, 70, crossDitch.getY() == 3 ? 0 : 2, 6132)));
                player.getClickDelay().reset();
            }
            break;

        case ANCIENT_ALTAR:
            player.performAnimation(new Animation(645));
            player.getDialogueManager().start(new SpellBookDialogue());
            break;

        case ORNATE_REJUVENATION_POOL:
            player.getPacketSender().sendMessage("You feel slightly renewed.");
            player.performGraphic(new Graphic(683));
            // Restore special attack
            player.setSpecialPercentage(100);
            CombatSpecial.updateBar(player);

            // Restore run energy
            player.setRunEnergy(100);
            player.getPacketSender().sendRunEnergy();

            for (Skill skill : Skill.values()) {
                SkillManager skillManager = player.getSkillManager();
                if (skillManager.getCurrentLevel(skill) < skillManager.getMaxLevel(skill)) {
                    skillManager.setCurrentLevel(skill,  skillManager.getMaxLevel(skill));
                }
            }

            player.setPoisonDamage(0);


            player.getUpdateFlag().flag(Flag.APPEARANCE);
            break;

        }
    }

	/**
     * Handles the second click option on an object.
     *
     * @param player
     *            The player that clicked on the object.
     * @param object
     *            The packet containing the object's information.
     */
    private static void secondClick(Player player, GameObject object) {
        // Check thieving..
        if (StallThieving.init(player, object)) {
            return;
        }

        if (com.elvarg.game.content.cannon.DwarfCannon.isObject(object)) {
            player.getDwarfCannon().handleInteraction(object, 2);
            return;
        }

        switch (object.getId()) {
        case PORTAL_51:
            //DialogueManager.sendStatement(player, "Construction will be avaliable in the future.");
            break;
        case FURNACE_18:
            for (Bar bar : Smithing.Bar.values()) {
                player.getPacketSender().sendInterfaceModel(bar.getFrame(), bar.getBar(), 150);
            }
            player.getPacketSender().sendChatboxInterface(2400);
            break;
        case BANK_CHEST:
        case BANK:
        case BANK_BOOTH:
        case BANK_BOOTH_2:
        case BANK_BOOTH_3:
        case BANK_BOOTH_4:
            player.getBank(player.getCurrentBankTab()).open();
            break;
        case ANCIENT_ALTAR:
            player.getPacketSender().sendInterfaceRemoval();
            MagicSpellbook.changeSpellbook(player, MagicSpellbook.NORMAL);
            break;
        }
	}

	/**
	 * Handles the third click option on an object.
	 *
	 * @param player
	 *            The player that clicked on the object.
	 * @param object
	 *            The packet containing the object's information.
	 */
	private static void thirdClick(Player player, GameObject object) {
        if (com.elvarg.game.content.cannon.DwarfCannon.isObject(object)) {
            player.getDwarfCannon().handleInteraction(object, 3);
            return;
        }
		switch (object.getId()) {
        case PORTAL_51:
            //DialogueManager.sendStatement(player, "Construction will be avaliable in the future.");
            break;
        case ANCIENT_ALTAR:
            player.getPacketSender().sendInterfaceRemoval();
            MagicSpellbook.changeSpellbook(player, MagicSpellbook.ANCIENT);
            break;
        }
	}

	/**
	 * Handles the fourth click option on an object.
	 *
	 * @param player
	 *            The player that clicked on the object.
	 * @param object
	 *            The packet containing the object's information.
	 */
	private static void fourthClick(Player player, GameObject object) {
	    switch (object.getId()) {
        case PORTAL_51:
            //DialogueManager.sendStatement(player, "Construction will be avaliable in the future.");
            break;
        case ANCIENT_ALTAR:
            player.getPacketSender().sendInterfaceRemoval();
            MagicSpellbook.changeSpellbook(player, MagicSpellbook.LUNAR);
            break;
        }
	}

    private static void objectInteract(Player player, int id, int x, int y, int clickType) {
        final Location location = new Location(x, y, player.getLocation().getZ());

        final GameObject object = MapObjects.get(player, id, location);

        if (player.getRights() == PlayerRights.DEVELOPER) {
            String typeFace = object != null ? "[F: " + object.getFace() + " T:" +object.getType() + "]" : "";
            player.getPacketSender().sendMessage(clickType + "-click object: " + id + ". " + location + " " + typeFace);
        }

        if (object == null) {
            return;
        }

        // Get object definition
        final ObjectDefinition def = ObjectDefinition.forId(id);

        if (def == null) {
            Server.getLogger().info("ObjectDefinition for object " + id + " is null.");
            return;
        }

        WalkToTask.submit(player, object, () -> {
            // Areas
            if (player.getArea() != null) {
                if (player.getArea().handleObjectClick(player, object, clickType)) {
                    return;
                }
            }

            switch (clickType) {
                case 1:
                    firstClick(player, object);
                    break;
                case 2:
                    secondClick(player, object);
                    break;
                case 3:
                    thirdClick(player, object);
                    break;
                case 4:
                    fourthClick(player, object);
                    break;
            }
        });
    }

    private static boolean atObject(int finalY, int finalX, int x, int height, int rotation, int width, int y, PrivateArea privateArea) {
        int maxX = (finalX + width) - 1;
        int maxY = (finalY + height) - 1;
        if (x >= finalX && x <= maxX && y >= finalY && y <= maxY)
            return true;
        if (x == finalX - 1 && y >= finalY && y <= maxY && (RegionManager.getClipping(x, y, height, privateArea) & 8) == 0
                && (rotation & 8) == 0)
            return true;
        if (x == maxX + 1 && y >= finalY && y <= maxY && (RegionManager.getClipping(x, y, height, privateArea) & 0x80) == 0
                && (rotation & 2) == 0)
            return true;
        return y == finalY - 1 && x >= finalX && x <= maxX && (RegionManager.getClipping(x, y, height, privateArea) & 2) == 0
                && (rotation & 4) == 0
                || y == maxY + 1 && x >= finalX && x <= maxX && (RegionManager.getClipping(x, y, height, privateArea) & 0x20) == 0
                        && (rotation & 1) == 0;
    }

    private static boolean doorHandler(Player player, GameObject object) {
        if (object.getDefinition().getName() != null && object.getDefinition().getName().contains("Door") ) {
            final int[][] openOffset = new int[][] { new int[] { -1, 0 }, new int[] { 0, 1 }, new int[] { 1, 0 }, new int[] { 0, -1 },
                    new int[] { 0, -1 } };
            final int[][] closeOffset = new int[][] { new int[] { 1, 0 }, new int[] { 1, 0 }, new int[] { 0, 1 }, new int[] { -1, 0 },
                    new int[] { 0, 1 } };
            /* check if its an open or closed door */
            boolean open = object.getDefinition().interactions[0].contains("Open");

            /* gets offset coords based on door face */
            int[] offset = open? openOffset[object.getFace()] :closeOffset[object.getFace()] ;
            /* adjust door direction based on if it needs to be opened or closed */
            int face = open? object.getFace() + 1 : object.getFace() - 1;

            Location loc =  new Location( object.getLocation().getX() + offset[0],object.getLocation().getY() + offset[1]   , object.getLocation().getZ());
            GameObject obj = new GameObject(open? object.getId() + 1 : object.getId() - 1, loc, object.getType(), face , null);

            /* spawns/despawns doors accordingly */

            if (open) {
                ObjectManager.register(new GameObject(-1, object.getLocation(), 0, 0, object.getPrivateArea()), true);
                ObjectManager.register(new GameObject(-1, loc, object.getType(), object.getFace(), object.getPrivateArea()), true);
            }

            ObjectManager.deregister(object, true);
            ObjectManager.register(obj, true);

            return true;
        }

	return false;
    }

	@Override
	public void execute(Player player, Packet packet) {

		if (player == null || player.getHitpoints() <= 0) {
			return;
		}

		// Make sure we aren't doing something else..
		if (player.busy()) {
			return;
		}
		
		int id, x, y;
		switch (packet.getOpcode()) {
		case PacketConstants.OBJECT_FIRST_CLICK_OPCODE:		    
		    x = packet.readLEShortA();
	        id = packet.readUnsignedShort();
	        y = packet.readUnsignedShortA();	        
			objectInteract(player, id, x, y, 1);
			break;
		case PacketConstants.OBJECT_SECOND_CLICK_OPCODE:		    
		    id = packet.readLEShortA();
	        y = packet.readLEShort();
	        x = packet.readUnsignedShortA();	        
	        objectInteract(player, id, x, y, 2);
			break;
		case PacketConstants.OBJECT_THIRD_CLICK_OPCODE:
		    x = packet.readLEShort();
	        y = packet.readShort();
	        id = packet.readLEShortA();
	        objectInteract(player, id, x, y, 3);
			break;
		case PacketConstants.OBJECT_FOURTH_CLICK_OPCODE:
		    x = packet.readLEShortA();
            id = packet.readUnsignedShortA();
            y = packet.readLEShortA();
            objectInteract(player, id, x, y, 4);
			break;
		case PacketConstants.OBJECT_FIFTH_CLICK_OPCODE:		    
			break;
		}
	}
}
