package com.elvarg.net.packet.impl;

import com.elvarg.game.World;
import com.elvarg.game.content.combat.magic.CombatSpell;
import com.elvarg.game.content.combat.magic.CombatSpells;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.net.packet.Packet;
import com.elvarg.net.packet.PacketExecutor;

public class MagicOnPlayerPacketListener implements PacketExecutor {

    @Override
    public void execute(Player player, Packet packet) {
        int playerIndex = packet.readShortA();

        if (player == null || player.getHitpoints() <= 0) {
            return;
        }

        if (playerIndex < 0 || playerIndex > World.getPlayers().capacity())
            return;
        int spellId = packet.readLEShort();
        if (spellId < 0) {
            return;
        }

        Player attacked = World.getPlayers().get(playerIndex);

        if (attacked == null || attacked.equals(player)) {
            player.getMovementQueue().reset();
            return;
        }


        if (attacked.getHitpoints() <= 0) {
            player.getMovementQueue().reset();
            return;
        }

        CombatSpell spell = CombatSpells.getCombatSpell(spellId);

        if (spell == null) {
            player.getMovementQueue().reset();
            return;
        }

        player.setPositionToFace(attacked.getLocation());
        player.getCombat().setCastSpell(spell);

        player.getCombat().attack(attacked);
    }

}
