package com.github.naton1.rl.util;

import com.google.gson.Gson;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Value;

public class ContractLoader {

    private static final Map<String, EnvironmentMeta> cache = new HashMap<>();
    private static final Gson gson = new Gson();

    public static synchronized EnvironmentMeta getEnvironment(String environmentType) {
        return cache.computeIfAbsent(environmentType, ContractLoader::loadEnvironment);
    }

    private static EnvironmentMeta loadEnvironment(String environmentType) {
        try (final InputStream is = ContractLoader.class
                .getClassLoader()
                .getResourceAsStream("environments/" + environmentType + ".json")) {
            if (is == null) {
                throw new IllegalArgumentException(environmentType);
            }
            return gson.fromJson(new InputStreamReader(is), EnvironmentMeta.class);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Value
    public static class Observation {
        private final String id;
        private final String description;
        private final boolean partial;
        private final boolean constant;
    }

    @Value
    public static class ActionDependencies {
        private final List<String> requireAll;
        private final List<String> requireAny;
        private final List<String> requireNone;
    }

    @Value
    public static class Action {
        private final String id;
        private final String description;
        private final ActionDependencies dependencies;
    }

    @Value
    public static class ActionHead {
        private final String id;
        private final String description;
        private final List<Action> actions;
    }

    @Value
    public static class EnvironmentMeta {
        private final List<ActionHead> actions;
        private final List<Observation> observations;
    }
}
