package com.github.naton1.rl;

public class EnvConfig {

    private static final String TRAIN_KEY = "TRAIN";
    private static final String SYNC_KEY = "SYNC_TRAINING";
    private static final String EVAL_KEY = "RUN_EVAL_BOTS";
    private static final String REMOTE_ENV_PORT_KEY = "REMOTE_ENV_PORT";
    private static final String SHOW_ENV_DEBUGGER = "SHOW_ENV_DEBUGGER";
    private static final String PREDICTION_API_HOST = "PREDICTION_API_HOST";
    private static final String PREDICTION_API_PORT = "PREDICTION_API_PORT";

    public static boolean isShowEnvDebugger() {
        return getBoolean(SHOW_ENV_DEBUGGER, true);
    }

    public static boolean isTrainEnabled() {
        return getBoolean(TRAIN_KEY, true);
    }

    public static boolean isSyncEnabled() {
        return getBoolean(SYNC_KEY, true);
    }

    public static boolean isEvalEnabled() {
        return getBoolean(EVAL_KEY, true);
    }

    public static int getRemoteEnvPort() {
        return Integer.parseInt(System.getenv().getOrDefault(REMOTE_ENV_PORT_KEY, "7070"));
    }

    public static String getPredictionApiHost() {
        return System.getenv().getOrDefault(PREDICTION_API_HOST, "localhost");
    }

    public static int getPredictionApiPort() {
        return Integer.parseInt(System.getenv().getOrDefault(PREDICTION_API_PORT, "9999"));
    }

    private static boolean getBoolean(String key, boolean defaultValue) {
        return Boolean.parseBoolean(
                System.getenv().getOrDefault(key, Boolean.valueOf(defaultValue).toString()));
    }
}
