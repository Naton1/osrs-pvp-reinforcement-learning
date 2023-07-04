package com.elvarg.game.model.areas.impl.pestcontrol;

import com.elvarg.game.content.PrayerHandler;
import com.elvarg.game.content.combat.hit.PendingHit;
import com.elvarg.game.content.minigames.MinigameHandler;
import com.elvarg.game.content.minigames.impl.pestcontrol.PestControl;
import com.elvarg.game.definition.ObjectDefinition;
import com.elvarg.game.entity.impl.Mobile;
import com.elvarg.game.entity.impl.npc.NPC;
import com.elvarg.game.entity.impl.object.GameObject;
import com.elvarg.game.entity.impl.object.ObjectManager;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.Boundary;
import com.elvarg.game.model.Location;
import com.elvarg.game.model.MagicSpellbook;
import com.elvarg.game.model.areas.Area;
import com.elvarg.game.model.areas.impl.PrivateArea;
import com.elvarg.net.packet.impl.EquipPacketListener;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static com.elvarg.util.ObjectIdentifiers.LADDER_174;

public class PestControlArea extends PrivateArea {

    private PestControl minigame;

    public static final Boundary LAUNCHER_BOAT_BOUNDARY = new Boundary(2656, 2659, 2609, 2614);

    /**
     * Returns the singleton instance of the Pest Control minigame.
     * <p>
     * Will fetch it if not alraedy populated.
     *
     * @return
     */
    private PestControl getMinigame() {
        if (this.minigame == null) {
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
    public boolean allowSummonPet(Player player) {
        player.sendMessage("The squire doesn't allow you to bring your pet with you.");
        return false;
    }

    @Override
    public boolean allowDwarfCannon(Player player) {
        player.sendMessage("Cannons are not allowed in pest control.");
        return false;
    }

    @Override
    public boolean isSpellDisabled(Player player, MagicSpellbook spellbook, int spellId) {
        boolean alch = spellbook == MagicSpellbook.NORMAL && Arrays.asList(1162, 1178).stream().anyMatch(a -> a.intValue() == spellId);
        if (alch) {
            player.getPacketSender().sendMessage("You cannot use this spell in Pest Control.");
            return true;
        }
        return false;
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
        player.setSpecialPercentage(100);
        player.setAttribute("pcDamage", 0);
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
        final String pcDamage = "pcDamage";
        int pendingDamage = hit.getTotalDamage();
        if (pendingDamage == 0)
            return;
        Integer damage = (Integer) player.getAttribute(pcDamage);
        if (damage == null) {
            player.setAttribute(pcDamage, pendingDamage);
            return;
        }
        player.setAttribute(pcDamage, damage + pendingDamage);

    }

    @Override
    public boolean handleDeath(Player player, Optional<Player> killer) {
        player.smartMove(LAUNCHER_BOAT_BOUNDARY);
        // Returning true means default death behavior is avoided.
        return true;
    }


    @Override
    public boolean handleObjectClick(Player player, GameObject object, int optionId) {
        Location objLoc = object.getLocation();
        int oX = objLoc.getX();
        int oY = objLoc.getY();
        int objectId = object.getId();
        int direction = object.getFace();
        int myX = player.getLocation().getX();
        int myY = player.getLocation().getY();
        switch (objectId) {

        }

        /**
         * Simple ladder formula
         */
        if (objectId == LADDER_174) {
            boolean down = direction == 1 && myX < oX || direction == 3 && myX > oX || direction == 0 && myY < oY;
            player.climb(down, down ? new Location((direction == 0 ? oX : direction == 1 ? oX + 1 : oX - 1), (direction == 1 ? oY : direction == 3 ? oY : oY + 1)) : new Location(direction == 1 ? oX - 1 : direction == 3 ? oX + 1 : oX, direction == 0 ? oY - 1 : oY));
            return true;
        }
        if (objectId >= 14233 && objectId <= 14236) {

            ObjectDefinition defs = ObjectDefinition.forId(objectId);

            if (defs == null) {
                System.err.println("no defs for objid="+objectId);
                return false;
            }

            boolean open = Arrays.stream(defs.interactions).filter(i -> i != null).anyMatch(d -> d.contains("Open"));

            boolean westernGate = oX == 2643;
            boolean southernGate = oY == 2585;
            boolean easternGate = oX == 2670;

            Location spawn = objLoc;
            GameObject gate = object;

            System.err.println("direction="+direction+" open="+open+" "+objLoc.toString()+" newOffset="+ getGateDirectionOffset(direction, objectId, open));

            if (open) {
                spawn = new Location(westernGate ? objLoc.getX() - 1 : easternGate ? objLoc.getX() + 1 : objLoc.getX(), southernGate ? objLoc.getY() - 1 : objLoc.getY());
                gate = new GameObject(objectId == 14233 ? 14234 : 14236, spawn, object.getType(), getGateDirectionOffset(direction, objectId, true), object.getPrivateArea());
            } else {
                spawn = new Location(oX == 2642 ? oX + 1 : oX == 2671 ? objLoc.getX() - 1 : objLoc.getX(), oY == 2584 ? objLoc.getY() + 1 : objLoc.getY());
                gate = new GameObject(objectId == 14234 ? 14233 : 14235, spawn, object.getType(), getGateDirectionOffset(direction, objectId, false), object.getPrivateArea());
            }
            ObjectManager.deregister(object, true);
            ObjectManager.register(gate, true);
            return true;
        }
        return false;
    }

    private int getGateDirectionOffset(int direction, int objectId, boolean opening) {
        if (opening) {
            if (direction == 0) {
                if (objectId == 14233) {
                    return 1;
                }
                if (objectId == 14235) {
                    return 3;
                }
            } else if (direction == 3) {
                if (objectId == 14233) {
                    return 4;
                }
                if (objectId == 14235) {
                    return 2;
                }
            } else if (direction == 2) {
                if (objectId == 14233) {
                    return 3;
                }
                if (objectId == 14235) {
                    return 1;
                }
            }
        } else {
            if (direction == 1) {
                if (objectId == 14234) {
                    return 0;
                }
                if (objectId == 14236) {
                    return 2;
                }
            } else if (direction == 2) {
                if (objectId == 14236) {
                    return 3;
                }
            } else if (direction == 3) {
                if (objectId == 14236) {
                    return 0;
                }
                if (objectId == 14234) {
                    return 2;
                }

            } else if (direction == 4) {
                if (objectId == 14234) {
                    return 3;
                }
            }
        }
        return -1;
    }
}
