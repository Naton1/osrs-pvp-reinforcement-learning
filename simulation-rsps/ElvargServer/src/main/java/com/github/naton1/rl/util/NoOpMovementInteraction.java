package com.github.naton1.rl.util;

import com.elvarg.game.entity.impl.playerbot.PlayerBot;
import com.elvarg.game.entity.impl.playerbot.interaction.MovementInteraction;

public class NoOpMovementInteraction extends MovementInteraction {

    public NoOpMovementInteraction(final PlayerBot _playerBot) {
        super(_playerBot);
    }

    @Override
    public void process() {
        // Do nothing
    }
}
