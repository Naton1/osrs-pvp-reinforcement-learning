package com.github.naton1.rl;

import com.elvarg.game.definition.PlayerBotDefinition;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.entity.impl.playerbot.PlayerBot;
import com.elvarg.game.entity.impl.playerbot.fightstyle.FighterPreset;
import com.elvarg.game.model.Location;
import com.github.naton1.rl.env.EnvironmentDescriptor;
import com.github.naton1.rl.env.nh.NhBaseline;
import com.github.naton1.rl.env.nh.NhEnvironmentDescriptor;
import com.github.naton1.rl.env.nh.NhEnvironmentParams;
import com.github.naton1.rl.env.nh.NhMaxLoadout;
import com.github.naton1.rl.env.nh.NhMedLoadout;
import com.github.naton1.rl.env.nh.NhPureLoadout;
import com.github.naton1.rl.env.nh.NhZerkLoadout;
import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AgentBotLoader {

    private static final Location BASELINE_LOAD_TILE = new Location(3093, 3529);

    private static final List<AgentBotConfig<?>> agentBotConfigs = List.of(
            AgentBotConfig.<NhEnvironmentParams>builder()
                    .name("NhAgentPure")
                    .model("GeneralizedNh")
                    .frameStack(1)
                    .deterministic(false)
                    .environmentParams(new NhEnvironmentParams().setAccountBuild(NhEnvironmentParams.AccountBuild.PURE))
                    .environmentDescriptor(new NhEnvironmentDescriptor())
                    .build(),
            AgentBotConfig.<NhEnvironmentParams>builder()
                    .name("NhAgentZerk")
                    .model("GeneralizedNh")
                    .frameStack(1)
                    .deterministic(false)
                    .environmentParams(
                            new NhEnvironmentParams().setAccountBuild(NhEnvironmentParams.AccountBuild.ZERKER))
                    .environmentDescriptor(new NhEnvironmentDescriptor())
                    .build(),
            AgentBotConfig.<NhEnvironmentParams>builder()
                    .name("NhAgentMed")
                    .model("GeneralizedNh")
                    .frameStack(1)
                    .deterministic(false)
                    .environmentParams(new NhEnvironmentParams().setAccountBuild(NhEnvironmentParams.AccountBuild.MED))
                    .environmentDescriptor(new NhEnvironmentDescriptor())
                    .build());

    private static final List<BaselineConfig> baselineConfigs = List.of(
            BaselineConfig.builder()
                    .name("BaselineMed")
                    .preset(new NhBaseline(new NhMedLoadout()))
                    .build(),
            BaselineConfig.builder()
                    .name("BaselinePure")
                    .preset(new NhBaseline(new NhPureLoadout()))
                    .build(),
            BaselineConfig.builder()
                    .name("BaselineMax")
                    .preset(new NhBaseline(new NhMaxLoadout()))
                    .build(),
            BaselineConfig.builder()
                    .name("BaselineZerk")
                    .preset(new NhBaseline(new NhZerkLoadout()))
                    .build());

    private final List<Player> loadedBots = new ArrayList<>();

    public void load() {

        for (AgentBotConfig<?> agentBotConfig : agentBotConfigs) {
            loadedBots.add(new AgentPlayerBot(
                    EnvConfig.getPredictionApiHost(),
                    EnvConfig.getPredictionApiPort(),
                    agentBotConfig.getName(),
                    agentBotConfig.getModel(),
                    agentBotConfig.getFrameStack(),
                    agentBotConfig.getEnvironmentDescriptor(),
                    agentBotConfig.getEnvironmentParams(),
                    agentBotConfig.isDeterministic()));
        }

        for (BaselineConfig baseline : baselineConfigs) {
            loadedBots.add(new PlayerBot(
                    new PlayerBotDefinition(baseline.getName(), BASELINE_LOAD_TILE, baseline.getPreset())));
        }
    }

    public synchronized void unload() {
        loadedBots.forEach(Player::requestLogout);
        loadedBots.clear();
    }

    @Builder
    @Value
    private static class AgentBotConfig<T> {
        private final String name;
        private final String model;
        private final int frameStack;
        private final EnvironmentDescriptor<T> environmentDescriptor;
        private final T environmentParams;
        private final boolean deterministic;
    }

    @Builder
    @Value
    private static class BaselineConfig {
        private final String name;
        private final FighterPreset preset;
    }
}
