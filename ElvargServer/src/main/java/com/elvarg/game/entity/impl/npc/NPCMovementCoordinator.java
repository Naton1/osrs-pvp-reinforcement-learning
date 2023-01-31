package com.elvarg.game.entity.impl.npc;

import com.elvarg.game.collision.RegionManager;
import com.elvarg.game.content.combat.CombatFactory;
import com.elvarg.game.model.Location;
import com.elvarg.game.model.movement.path.PathFinder;
import com.elvarg.util.Misc;

/**
 * Will make all {@link NPC}s set to coordinate, pseudo-randomly move within a
 * specified radius of their original position.
 *
 * @author lare96
 */
public class NPCMovementCoordinator {

    /**
     * The npc we are coordinating movement for.
     */
    private NPC npc;

    /**
     * The coordinate state this npc is in.
     */
    private CoordinateState coordinateState;

    /**
     * The total radius a npc can move from spawn location before retreating
     **/
    private int radius;

    public NPCMovementCoordinator(NPC npc) {
        this.npc = npc;
        this.coordinateState = CoordinateState.HOME;
    }

    public void process() {

        // If walk radius is 0, that means the npc shouldn't walk around.
        // HOWEVER: Only if npc is home. Because the npc might be retreating
        // from a fight.
        if (radius == 0) {
            if (coordinateState == CoordinateState.HOME) {
                return;
            }
        }
        
        if (!npc.getMovementQueue().getMobility().canMove()) {
        	return;
        }

        updateCoordinator();

        switch (coordinateState) {
            case HOME:

                if (CombatFactory.inCombat(npc)) {
                    return;
                }

                if (npc.getInteractingMobile() != null) {
                    return;
                }

                if (!npc.getMovementQueue().isMoving()) {
                    if (Misc.getRandom(9) <= 1) {
                        Location pos = generateLocalPosition();
                        if (pos != null) {
                            npc.getMovementQueue().walkStep(pos.getX(), pos.getY());
                        }
                    }
                }

                break;
            case RETREATING:
            case AWAY:
                PathFinder.calculateWalkRoute(npc, npc.getSpawnPosition().getX(), npc.getSpawnPosition().getY());
                break;
        }
    }

    public void updateCoordinator() {

        /**
         * Handle retreating from combat.
         */

        if (CombatFactory.inCombat(npc)) {
            if (coordinateState == CoordinateState.AWAY) {
                coordinateState = CoordinateState.RETREATING;
            }
            if (coordinateState == CoordinateState.RETREATING) {
                if (npc.getLocation().equals(npc.getSpawnPosition())) {
                    coordinateState = CoordinateState.HOME;
                }
                npc.getCombat().reset();
            }
            return;
        }

        int deltaX;
        int deltaY;

        if (npc.getSpawnPosition().getX() > npc.getLocation().getX()) {
            deltaX = npc.getSpawnPosition().getX() - npc.getLocation().getX();
        } else {
            deltaX = npc.getLocation().getX() - npc.getSpawnPosition().getX();
        }

        if (npc.getSpawnPosition().getY() > npc.getLocation().getY()) {
            deltaY = npc.getSpawnPosition().getY() - npc.getLocation().getY();
        } else {
            deltaY = npc.getLocation().getY() - npc.getSpawnPosition().getY();
        }

        if ((deltaX > radius) || (deltaY > radius)) {
            coordinateState = CoordinateState.AWAY;
        } else {
            coordinateState = CoordinateState.HOME;
        }
    }

    private Location generateLocalPosition() {
        int dir = -1;
        int x = 0, y = 0;
        if (!RegionManager.blockedNorth(npc.getLocation(), npc.getPrivateArea())) {
            dir = 0;
        } else if (!RegionManager.blockedEast(npc.getLocation(), npc.getPrivateArea())) {
            dir = 4;
        } else if (!RegionManager.blockedSouth(npc.getLocation(), npc.getPrivateArea())) {
            dir = 8;
        } else if (!RegionManager.blockedWest(npc.getLocation(), npc.getPrivateArea())) {
            dir = 12;
        }
        int random = Misc.getRandom(3);

        boolean found = false;

        if (random == 0) {
            if (!RegionManager.blockedNorth(npc.getLocation(), npc.getPrivateArea())) {
                y = 1;
                found = true;
            }
        } else if (random == 1) {
            if (!RegionManager.blockedEast(npc.getLocation(), npc.getPrivateArea())) {
                x = 1;
                found = true;
            }
        } else if (random == 2) {
            if (!RegionManager.blockedSouth(npc.getLocation(), npc.getPrivateArea())) {
                y = -1;
                found = true;
            }
        } else if (random == 3) {
            if (!RegionManager.blockedWest(npc.getLocation(), npc.getPrivateArea())) {
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
        int spawnX = npc.getSpawnPosition().getX();
        int spawnY = npc.getSpawnPosition().getY();
        if (x == 1) {
            if (npc.getLocation().getX() + x > spawnX + 1)
                return null;
        }
        if (x == -1) {
            if (npc.getLocation().getX() - x < spawnX - 1)
                return null;
        }
        if (y == 1) {
            if (npc.getLocation().getY() + y > spawnY + 1)
                return null;
        }
        if (y == -1) {
            if (npc.getLocation().getY() - y < spawnY - 1)
                return null;
        }
        return new Location(x, y);
    }

    public CoordinateState getCoordinateState() {
        return coordinateState;
    }

    public void setCoordinateState(CoordinateState coordinateState) {
        this.coordinateState = coordinateState;
    }

    public int getRadius() {
        return radius;
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }

    public enum CoordinateState {
        HOME,
        AWAY,
        RETREATING;
    }
}