package com.elvarg.game.model.commands.impl;

import com.elvarg.game.World;
import com.elvarg.game.content.combat.formula.DamageFormulas;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.commands.Command;

import java.util.Optional;

public class MaxHit implements Command {

    /**
     * This command can be used
     *
     * ::maxhit/mh {?player}
     *
     * This command will show you your max hit, or if you provide a player's name it will try to get their max hit.
     *
     * @param player
     * @param command
     * @param parts
     */
    @Override
    public void execute(Player player, String command, String[] parts) {
        String playerName = parts.length == 2 ? parts[1] : null;
        if (playerName != null) {
            Optional<Player> p2 = World.getPlayerByName(playerName);
            if (p2.isPresent()) {
                Player otherPlayer = p2.get();
                int maxHit = DamageFormulas.calculateMaxMeleeHit(otherPlayer);

                player.getPacketSender().sendMessage(playerName + "'s current max hit is: " + maxHit);
            } else {
                player.getPacketSender().sendMessage("Cannot find player: " + playerName);
            }

            return;
        }

        int maxHit = DamageFormulas.calculateMaxMeleeHit(player);

        player.getPacketSender().sendMessage("Your current max hit is: " + maxHit);
    }

    @Override
    public boolean canUse(Player player) {
        return true;
    }
}
