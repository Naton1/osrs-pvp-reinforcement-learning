package com.elvarg.game.model.commands.impl;

import com.elvarg.game.content.combat.CombatFactory;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.Graphic;
import com.elvarg.game.model.Location;
import com.elvarg.game.model.commands.Command;
import com.elvarg.game.model.rights.PlayerRights;
import java.util.LinkedHashSet;

public class AttackRange implements Command {

    public static final Graphic PURPLE_GLOW = new Graphic(332);

    /**
     * This command can be used
     *
     * ::atkrange/attackrange {?distance}
     *
     * This command will show you your attack distance in a visual circumference of portal graphics.
     * If a developer runs the command, it will print the circumference delta to the console/standard output stream.
     * @param player
     * @param command
     * @param parts
     */
    @Override
    public void execute(Player player, String command, String[] parts) {
        // Player can type a fixed distance or use their current weapon's distance.
        int distance = parts.length == 2 ? Integer.parseInt(parts[1]) : CombatFactory.getMethod(player).attackDistance(player);

        Location playerLocation = player.getLocation().clone();

        Location startingLocation = player.getLocation().clone().translate(-(distance + 5), -(distance + 5));

        Location endingLocation = player.getLocation().clone().translate((distance + 5), (distance + 5));

        LinkedHashSet<Location> deltas = new LinkedHashSet<Location>();

        for (int x = startingLocation.getX(); x <= endingLocation.getX(); x++) {
            for (int y = startingLocation.getY(); y <= endingLocation.getY(); y++) {
                Location currentTile = new Location(x, y);
                if (currentTile.getDistance(playerLocation) != distance) {
                    continue;
                }

                Location delta = Location.delta(playerLocation, currentTile);

                // This tile happens to be exactly {distance} squares from the player, add it.
                deltas.add(delta);
                player.getPacketSender().sendGraphic(PURPLE_GLOW, currentTile);
            }
        }

        if (player.getRights() == PlayerRights.DEVELOPER) {
            System.out.println("Deltas for distance of " + distance + ":");
            System.out.println(deltas);
        }
    }

    @Override
    public boolean canUse(Player player) {
        PlayerRights rights = player.getRights();
        return (rights == PlayerRights.OWNER || rights == PlayerRights.DEVELOPER);
    }
}
