# Simulation RSPS

This contains a RuneScape Private Server (RSPS) specifically modified to facilitate training
reinforcement learning agents. It builds upon [Elvarg RSPS](https://github.com/RSPSApp/elvarg-rsps).

Key modifications in this project can be reviewed in the [rl](ElvargServer/src/main/java/com/github/naton1/rl) package.
For a comprehensive view of all changes, consider running a diff between this and the upstream repository.

#### Primary Modifications

* Development of a [plugin](ElvargServer/src/main/java/com/elvarg/game/plugin)
  and [event](ElvargServer/src/main/java/com/elvarg/game/event) system.
* Creation of
  a [reinforcement learning plugin](ElvargServer/src/main/java/com/github/naton1/rl/ReinforcementLearningPlugin.java).

#### Repository Synchronization

To synchronize with the [upstream repository](https://github.com/RSPSApp/elvarg-rsps), use:
`git subtree pull --prefix=simulation-rsps https://github.com/RSPSApp/elvarg-rsps.git master`

**Note: This repository has diverged from the upstream due to incompatible changes in the upstream (like their own
plugin system).**

#### Simulation Preview

![Simulation Gif](../assets/simulation.gif)

# How To Use

## ElvargServer

This contains the simulated game environment. By default, the reinforcement learning plugin will run, launching the
remote environment server required for training/evaluation (see [pvp-ml](../pvp-ml/README.md) for connection details).

It requires running with Java 17.

1. Navigate to the [ElvargServer](ElvargServer) directory.
2. Launch the server using gradle: `./gradlew run`.

**Note:** [pvp-ml](../pvp-ml/README.md#how-to-use) will install Java 17.

### Connect to Server via Client

Connect to the server with a RSPS client by cloning upstream, and launching the built-in client. This is useful for
testing and observing training.

It requires running with Java 11.

1. Clone the upstream repository: `git clone https://github.com/RSPSApp/elvarg-rsps`.
2. Change to the ElvargClient directory: `cd elvarg-rsps/ElvargClient`.
3. Start the client with gradle: `./gradlew run`.
4. Log in!

### Reinforcement Learning Plugin

This extends the game logic with a custom reinforcement learning plugin (`ReinforcementLearningPlugin`)
(note: the plugin/event systems themselves were added in as part of this as well to make the code cleaner).

#### Features:

* Launches a socket
  server ([RemoteEnvironmentServer](ElvargServer/src/main/java/com/github/naton1/rl/RemoteEnvironmentServer.java)) for
  API interactions. This exposes routes for logging in and out, and for step and reset requests (like the gym
  interface).
* Enables control over training agents via
  API ([RemoteEnvironmentPlayerBot](ElvargServer/src/main/java/com/github/naton1/rl/RemoteEnvironmentPlayerBot.java))
* Supports independent ML-driven
  agents ([AgentBotLoader](ElvargServer/src/main/java/com/github/naton1/rl/AgentBotLoader.java)) and player-controlled
  agents ([EnableAgentCommand](ElvargServer/src/main/java/com/github/naton1/rl/command/EnableAgentCommand.java)).

### Add New Environment

Adding a new environment requires a few simple steps, assuming the environment contract has already been created.

1) Implement an `AgentEnvironment` class for the new environment.
2) Implement an `EnvironmentDescriptor` class for the new environment, and associated classes (such as
   `EnvironmentParams`).
3) Add a new type to the `EnvironmentRegistry` enum. This type can now be used for training.

# Simulation Accuracy

The aim is to replicate OSRS combat as accurately as possible. Achieving this required extensive modifications to the
original RSPS, especially in combat mechanics (ex. food consumption and attack delays). Precision in simulating these
details is crucial to ensure that the agent's learned policies are applicable to the live game.

# Possible Enhancements

## Generalized RL Plugin

Future work could involve generalizing the plugin logic for broader applicability across various RSPS frameworks. This
would facilitate easier adaptation and training on different servers if needed.