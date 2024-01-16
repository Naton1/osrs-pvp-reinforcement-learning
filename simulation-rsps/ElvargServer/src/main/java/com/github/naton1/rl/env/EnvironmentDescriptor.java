package com.github.naton1.rl.env;

import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.entity.impl.playerbot.fightstyle.FighterPreset;
import com.github.naton1.rl.util.ContractLoader;
import java.util.Arrays;

public interface EnvironmentDescriptor<EnvParamsType> {

    AgentEnvironment createEnvironment(
            Player agent, Player target, EnvironmentCallback environmentCallback, EnvParamsType envParams);

    // Ideally, baselines are implemented as a plugin in the training system
    // and not hardcoded in the simulation. However, this works for now.
    FighterPreset getBaselinePreset(EnvParamsType envParams);

    Loadout getEnvironmentLoadout(EnvParamsType envParams);

    Class<EnvParamsType> getEnvironmentParamsType();

    default Loadout getDefaultLoadout() {
        return getEnvironmentLoadout(getDefaultConfig());
    }

    default EnvParamsType getDefaultConfig() {
        try {
            return getEnvironmentParamsType().getConstructor().newInstance();
        } catch (Exception e) {
            throw new IllegalStateException("Environment params must have default constructor", e);
        }
    }

    default ContractLoader.EnvironmentMeta getMeta() {
        return ContractLoader.getEnvironment(Arrays.stream(EnvironmentRegistry.values())
                .filter(e -> e.getEnvironmentDescriptor().getClass() == this.getClass())
                .findFirst()
                .orElseThrow()
                .getType());
    }
}
