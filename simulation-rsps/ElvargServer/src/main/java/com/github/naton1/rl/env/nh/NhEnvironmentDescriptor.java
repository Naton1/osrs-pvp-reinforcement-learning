package com.github.naton1.rl.env.nh;

import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.entity.impl.playerbot.fightstyle.FighterPreset;
import com.github.naton1.rl.env.AgentEnvironment;
import com.github.naton1.rl.env.EnvironmentCallback;
import com.github.naton1.rl.env.EnvironmentDescriptor;
import java.util.Arrays;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NhEnvironmentDescriptor implements EnvironmentDescriptor<NhEnvironmentParams> {

    @Override
    public AgentEnvironment createEnvironment(
            final Player agent,
            final Player target,
            final EnvironmentCallback environmentCallback,
            final NhEnvironmentParams envParams) {
        return new NhEnvironment(agent, target, environmentCallback, getEnvironmentLoadout(envParams), envParams);
    }

    @Override
    public FighterPreset getBaselinePreset(NhEnvironmentParams envParams) {
        return new NhBaseline(getEnvironmentLoadout(envParams));
    }

    @Override
    public NhLoadout getEnvironmentLoadout(NhEnvironmentParams envParams) {
        NhEnvironmentParams.AccountBuild accountBuild = envParams.getAccountBuild();
        if (envParams.isRandomizeBuild()) {
            accountBuild = getRandomAccountBuild(envParams);
        }
        final NhLoadout nhLoadout =
                switch (accountBuild) {
                    case PURE -> new NhPureLoadout();
                    case ZERKER -> new NhZerkLoadout();
                    case MED -> new NhMedLoadout();
                    case MAXED -> new NhMaxLoadout();
                    case LMS_PURE -> new NhLmsPureLoadout();
                    case LMS_ZERKER -> new NhLmsZerkLoadout();
                    case LMS_MED -> new NhLmsMedLoadout();
                };
        if (envParams.isRandomizeGear()) {
            final String episodeId = envParams.getEpisodeId() != null
                            && !envParams.getEpisodeId().isEmpty()
                    ? envParams.getEpisodeId()
                    : UUID.randomUUID().toString();
            return nhLoadout.randomize(episodeId.hashCode());
        }
        return nhLoadout;
    }

    @Override
    public Class<NhEnvironmentParams> getEnvironmentParamsType() {
        return NhEnvironmentParams.class;
    }

    private NhEnvironmentParams.AccountBuild getRandomAccountBuild(NhEnvironmentParams envParams) {
        // Place the random account build by taking the lowest number name between agent vs target,
        // and modding it by the number of account builds, and the remainder will be the placement.
        // We can take advantage of the fact that env ids are sequential numbers.
        final int accountBuildIndex = Stream.of(envParams.getAgent(), envParams.getTarget())
                .filter(arg -> arg.matches("\\d+"))
                .mapToInt(Integer::parseInt)
                .min()
                .orElseGet(() -> Stream.of(envParams.getAgent(), envParams.getTarget())
                        .flatMap(arg -> Arrays.stream(arg.split(" ")))
                        .filter(part -> part.matches("\\d+"))
                        .mapToInt(Integer::parseInt)
                        .min()
                        .orElseGet(() -> {
                            log.warn(
                                    "Unable to find ID in name for {}, randomizing by string hash code",
                                    envParams.getAgent());
                            return Stream.of(envParams.getAgent(), envParams.getTarget())
                                    .sorted()
                                    .collect(Collectors.joining())
                                    .hashCode();
                        }));
        return envParams
                .getRandomBuildOptions()
                .get(Math.abs(accountBuildIndex)
                        % envParams.getRandomBuildOptions().size());
    }
}
