package com.elvarg.game.model.teleportation;

import com.elvarg.game.GameConstants;
import com.elvarg.game.content.sound.Sound;
import com.elvarg.game.content.sound.SoundManager;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.EffectTimer;
import com.elvarg.game.model.Location;
import com.elvarg.game.model.areas.Area;
import com.elvarg.game.model.areas.AreaManager;
import com.elvarg.game.model.areas.impl.WildernessArea;
import com.elvarg.game.task.Task;
import com.elvarg.game.task.TaskManager;

public class TeleportHandler {

	/**
	 * Teleports a player to the target location.
	 *
	 * @param player
	 *            The player teleporting.
	 * @param targetLocation
	 *            The location to teleport to.
	 * @param teleportType
	 *            The type of teleport.
	 */
	public static void teleport(Player player, Location targetLocation, TeleportType teleportType,
			boolean wildernessWarning) {
		if (wildernessWarning) {
			StringBuilder warning = new StringBuilder();
			Area area = AreaManager.get(targetLocation);
			boolean wilderness = (area instanceof WildernessArea);
			int wildernessLevel = WildernessArea.getLevel(targetLocation.getY());
			if (wilderness) {
				warning.append("Are you sure you want to teleport there? ");
				if (wildernessLevel > 0) {
					warning.append("It's in level @red@" + wildernessLevel + "@bla@ wilderness! ");
					if (WildernessArea.multi(targetLocation.getX(), targetLocation.getY())) {
						warning.append(
								"Additionally, @red@it's a multi zone@bla@. Other players may attack you simultaneously.");
					} else {
						warning.append("Other players will be able to attack you.");
					}
				} else {
					warning.append("Other players will be able to attack you.");
				}
				/*player.setDialogueContinueAction(new Action() {
					@Override
					public void execute() {

						//DialogueManager.start(player, 7);
						player.setDialogueOptions(new DialogueOptions() {
							@Override
							public void handleOption(Player player, int option) {
								player.getPacketSender().sendInterfaceRemoval();
								if (option == 1) {
									teleport(player, targetLocation, teleportType, false);
								}
							}
						});
					}

				});
				//DialogueManager.sendStatement(player, warning.toString());*/
				return;
			}
		}
		player.getMovementQueue().setBlockMovement(true).reset();
		onTeleporting(player);
		player.performAnimation(teleportType.getStartAnimation());
		player.performGraphic(teleportType.getStartGraphic());
		player.setUntargetable(true);
		player.setTeleporting(true);
		SoundManager.sendSound(player, Sound.TELEPORT);
		TaskManager.submit(new Task(1, player, true) {
			int tick = 0;

			@Override
			public void execute() {
				if (tick == teleportType.getStartTick() - 2) {
					if (teleportType.getMiddleAnim() != null) {
						player.performAnimation(teleportType.getMiddleAnim());
					}
					if (teleportType.getMiddleGraphic() != null) {
						player.performGraphic(teleportType.getMiddleGraphic());
					}
				} else if (tick == teleportType.getStartTick()) {
					onTeleporting(player);
					player.performAnimation(teleportType.getEndAnimation());
					player.performGraphic(teleportType.getEndGraphic());
					player.moveTo(targetLocation);
				} else if (tick == teleportType.getStartTick() + 2) {
					player.getMovementQueue().setBlockMovement(false).reset();
					stop();
					return;
				}
				tick++;
			}

			@Override
			public void stop() {
				super.stop();
				player.getClickDelay().reset(0);
				player.setUntargetable(false);
				player.setTeleporting(false);
			}
		});
		player.getClickDelay().reset();
	}

	private static void onTeleporting(Player player) {
		player.getSkillManager().stopSkillable();
		player.getPacketSender().sendInterfaceRemoval();
		player.getCombat().reset();
	}

	public static boolean checkReqs(Player player, Location targetLocation) {
		if (player.busy()) {
			player.getPacketSender().sendMessage("You cannot do that right now.");
			return false;
		}

		if (!player.getCombat().getTeleBlockTimer().finished()) {
			if (player.getArea() instanceof WildernessArea) {
				player.getPacketSender().sendMessage("A magical spell is blocking you from teleporting.");
				return false;
			} else {
				player.getCombat().getTeleBlockTimer().stop();
				player.getPacketSender().sendEffectTimer(0, EffectTimer.TELE_BLOCK);
			}
		}

		if (player.getMovementQueue().isMovementBlocked()) {
			return false;
		}

		if (player.getArea() != null) {
			if (!player.getArea().canTeleport(player)) {
				return false;
			}
		}

		return true;
	}

	public static boolean handleButton(Player player, int buttonId, int menuId) {
		TeleportButton teleportButton = TeleportButton.get(buttonId);
		if (teleportButton != null) {
			if (player.getWildernessLevel() > 0) {
				player.getPacketSender().sendMessage("You can only use tablet to teleport out from wilderness.");
				return true;
			}
			switch (menuId) {
			case 0: // Click to teleport
				if (teleportButton == TeleportButton.HOME) {
					if (TeleportHandler.checkReqs(player, GameConstants.DEFAULT_LOCATION)) {
						TeleportHandler.teleport(player, GameConstants.DEFAULT_LOCATION,
								player.getSpellbook().getTeleportType(), false);
						player.getPreviousTeleports().put(teleportButton, GameConstants.DEFAULT_LOCATION);
					}
					return true;
				}
				player.getPacketSender().sendTeleportInterface(teleportButton.menu);
				return true;
			case 1: // Previous option on teleport
				if (player.getPreviousTeleports().containsKey(teleportButton)) {
					Location tele = player.getPreviousTeleports().get(teleportButton);
					if (TeleportHandler.checkReqs(player, tele)) {
						TeleportHandler.teleport(player, tele, player.getSpellbook().getTeleportType(), true);
					}
				} else {
					player.getPacketSender().sendMessage("Unable to find a previous teleport.");
				}
				player.getPacketSender().sendInterfaceRemoval();
				return true;
			}
		}
		return false;
	}
}
