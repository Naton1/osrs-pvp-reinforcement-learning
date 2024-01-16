package com.github.naton1.rl.env.dharok;

import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.entity.impl.playerbot.fightstyle.FighterPreset;
import com.github.naton1.rl.env.AgentEnvironment;
import com.github.naton1.rl.env.EnvironmentCallback;
import com.github.naton1.rl.env.EnvironmentDescriptor;

public class DharokEnvironmentDescriptor implements EnvironmentDescriptor<DharokEnvironmentParams> {

    @Override
    public AgentEnvironment createEnvironment(
            final Player agent,
            final Player target,
            final EnvironmentCallback environmentCallback,
            final DharokEnvironmentParams envParams) {
        return new DharokEnvironment(agent, target, environmentCallback, getEnvironmentLoadout(envParams), envParams);
    }

    @Override
    public FighterPreset getBaselinePreset(DharokEnvironmentParams envParams) {
        return new DharokBaseline(getEnvironmentLoadout(envParams));
    }

    @Override
    public DharokLoadout getEnvironmentLoadout(DharokEnvironmentParams envParams) {
        return new DharokLoadout();
    }

    @Override
    public Class<DharokEnvironmentParams> getEnvironmentParamsType() {
        return DharokEnvironmentParams.class;
    }
}
