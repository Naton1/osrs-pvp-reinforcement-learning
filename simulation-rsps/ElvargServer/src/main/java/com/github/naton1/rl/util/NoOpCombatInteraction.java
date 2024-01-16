package com.github.naton1.rl.util;

import com.elvarg.game.entity.impl.Mobile;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.entity.impl.playerbot.PlayerBot;
import com.elvarg.game.entity.impl.playerbot.interaction.CombatInteraction;
import java.util.Optional;

public class NoOpCombatInteraction extends CombatInteraction {

    public NoOpCombatInteraction(final PlayerBot _playerBot) {
        super(_playerBot);
    }

    public void process() {}

    public void takenDamage(int damage, Mobile attacker) {}

    public void handleDying(Optional<Player> killer) {}

    public void handleDeath(Optional<Player> killer) {}

    public void targetAssigned(Player target) {}

    public void reset() {}
}
