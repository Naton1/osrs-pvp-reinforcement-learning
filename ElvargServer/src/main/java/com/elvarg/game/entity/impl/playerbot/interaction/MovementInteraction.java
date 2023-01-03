package com.elvarg.game.entity.impl.playerbot.interaction;

import com.elvarg.game.collision.RegionManager;
import com.elvarg.game.content.combat.CombatFactory;
import com.elvarg.game.entity.impl.playerbot.PlayerBot;
import com.elvarg.game.model.Location;
import com.elvarg.game.model.areas.impl.DuelArenaArea;
import com.elvarg.game.model.movement.MovementQueue;
import com.elvarg.game.model.teleportation.TeleportHandler;
import com.elvarg.game.model.teleportation.TeleportType;
import com.elvarg.util.Misc;

public class MovementInteraction {

    // The PlayerBot this interaction belongs to
    private PlayerBot playerBot;

    public MovementInteraction(PlayerBot _playerBot) {
        this.playerBot = _playerBot;
    }

    public void process() {
        if (!playerBot.getMovementQueue().getMobility().canMove() || playerBot.busy()) {
            return;
        }

        switch (playerBot.getCurrentState()) {
            case COMMAND:
                // Player Bot is currently busy, do nothing
                return;

            case IDLE:
                if (CombatFactory.inCombat(playerBot) || playerBot.getDueling().inDuel()) {
                    return;
                }

                // Player bot is idle, let it walk somewhere random
                if (!playerBot.getMovementQueue().isMoving()) {
                    if (Misc.getRandom(9) <= 1) {
                        Location pos = generateLocalPosition();
                        if (pos != null) {
                            MovementQueue.randomClippedStep(playerBot, 1);
                        }
                    }
                }

                if (playerBot.getArea() != null && playerBot.getArea().canPlayerBotIdle(playerBot)) {
                    break;
                }

                if (playerBot.getLocation().getDistance(playerBot.getDefinition().getSpawnLocation()) > 20) {
                    // Bot is far away, teleport back to original location
                    TeleportHandler.teleport(playerBot, playerBot.getDefinition().getSpawnLocation(), TeleportType.NORMAL, false);
                }
                break;
        }
    }

    private Location generateLocalPosition() {
        int dir = -1;
        int x = 0, y = 0;
        if (!RegionManager.blockedNorth(playerBot.getLocation(), playerBot.getPrivateArea())) {
            dir = 0;
        } else if (!RegionManager.blockedEast(playerBot.getLocation(), playerBot.getPrivateArea())) {
            dir = 4;
        } else if (!RegionManager.blockedSouth(playerBot.getLocation(), playerBot.getPrivateArea())) {
            dir = 8;
        } else if (!RegionManager.blockedWest(playerBot.getLocation(), playerBot.getPrivateArea())) {
            dir = 12;
        }
        int random = Misc.getRandom(3);

        boolean found = false;

        if (random == 0) {
            if (!RegionManager.blockedNorth(playerBot.getLocation(), playerBot.getPrivateArea())) {
                y = 1;
                found = true;
            }
        } else if (random == 1) {
            if (!RegionManager.blockedEast(playerBot.getLocation(), playerBot.getPrivateArea())) {
                x = 1;
                found = true;
            }
        } else if (random == 2) {
            if (!RegionManager.blockedSouth(playerBot.getLocation(), playerBot.getPrivateArea())) {
                y = -1;
                found = true;
            }
        } else if (random == 3) {
            if (!RegionManager.blockedWest(playerBot.getLocation(), playerBot.getPrivateArea())) {
                x = -1;
                found = true;
            }
        }
        if (!found) {
            if (dir == 0) {
                y = 1;
            } else if (dir == 4) {
                x = 1;
            } else if (dir == 8) {
                y = -1;
            } else if (dir == 12) {
                x = -1;
            }
        }
        if (x == 0 && y == 0)
            return null;
        int spawnX = playerBot.getSpawnPosition().getX();
        int spawnY = playerBot.getSpawnPosition().getY();
        if (x == 1) {
            if (playerBot.getLocation().getX() + x > spawnX + 1)
                return null;
        }
        if (x == -1) {
            if (playerBot.getLocation().getX() - x < spawnX - 1)
                return null;
        }
        if (y == 1) {
            if (playerBot.getLocation().getY() + y > spawnY + 1)
                return null;
        }
        if (y == -1) {
            if (playerBot.getLocation().getY() - y < spawnY - 1)
                return null;
        }
        return new Location(x, y);
    }
}
