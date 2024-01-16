package com.elvarg.game.content.combat.bountyhunter;

import com.elvarg.game.entity.impl.player.Player;

public class TargetPair {

    private final Player player1;
    private final Player player2;

    public TargetPair(Player player1, Player player2) {
        this.player1 = player1;
        this.player2 = player2;
    }

    public Player getPlayer1() {
        return player1;
    }

    public Player getPlayer2() {
        return player2;
    }
}