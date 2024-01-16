package com.github.naton1.rl;

import com.elvarg.game.event.EventDispatcher;
import com.elvarg.game.event.events.ServerStartedEvent;
import com.elvarg.game.event.events.ServerStoppedEvent;
import com.elvarg.game.model.commands.CommandManager;
import com.elvarg.game.plugin.Plugin;
import com.github.naton1.rl.command.ApplyLoadoutCommand;
import com.github.naton1.rl.command.EnableAgentCommand;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ReinforcementLearningPlugin extends Plugin {

    private final List<Runnable> cleanupTasks = new ArrayList<>();

    public ReinforcementLearningPlugin() {
        EventDispatcher.getGlobal().add(ServerStartedEvent.class, e -> initialize());
        EventDispatcher.getGlobal().add(ServerStoppedEvent.class, e -> cleanup());
    }

    private void initialize() {
        if (EnvConfig.isTrainEnabled()) {
            final RemoteEnvironmentServer remoteEnvironmentServer = new RemoteEnvironmentServer();
            cleanupTasks.add(remoteEnvironmentServer::close);
        }
        if (EnvConfig.isEvalEnabled()) {
            final AgentBotLoader agentBotLoader = new AgentBotLoader();
            agentBotLoader.load();
            cleanupTasks.add(agentBotLoader::unload);
        }
        if (EnvConfig.isShowEnvDebugger()) {
            EnvironmentDebugger.initialize();
        }
        CommandManager.put(new EnableAgentCommand(), "enableagent", "disableagent");
        CommandManager.put(new ApplyLoadoutCommand(), "loadout");
        log.info("Initialized reinforcement learning plugin");
    }

    private void cleanup() {
        cleanupTasks.forEach(Runnable::run);
        cleanupTasks.clear();
    }
}
