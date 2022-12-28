package com.elvarg.game.entity.impl.playerbot;

import com.elvarg.game.GameConstants;
import com.elvarg.game.World;
import com.elvarg.game.content.presets.Presetables;
import com.elvarg.game.definition.PlayerBotDefinition;
import com.elvarg.game.entity.impl.Mobile;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.entity.impl.playerbot.commands.*;
import com.elvarg.game.entity.impl.playerbot.interaction.*;
import com.elvarg.game.entity.updating.PlayerUpdating;
import com.elvarg.game.model.ChatMessage;
import com.elvarg.game.model.Location;
import com.elvarg.net.PlayerBotSession;
import com.elvarg.util.Misc;

public class PlayerBot extends Player {

    public enum InteractionState {
        IDLE,

        // Performing a job for a player
        COMMAND;
    }

    private static final BotCommand[] CHAT_COMMANDS = new BotCommand[]{
        new FollowPlayer(), new HoldItems(), new LoadPreset(), new FightCommand(), new PlayCastleWars(),
    };

    private final Location spawnPosition = GameConstants.DEFAULT_LOCATION;

    // The current interaction of this PlayerBot
    private InteractionState currentState = InteractionState.IDLE;


    private BotCommand activeCommand;

    private final PlayerBotDefinition definition;

    private Player interactingWith;

    public Player getInteractingWith() {
        return this.interactingWith;
    }

    public void setInteractingWith(Player _interact) {
        this.interactingWith = _interact;
    }

    private final MovementInteraction movementInteraction;

    private final ChatInteraction chatInteraction;

    private final TradingInteraction tradingInteraction;

    private final CombatInteraction combatInteraction;

    /**
     * Creates this player bot from a given definition.
     */
    public PlayerBot(PlayerBotDefinition definition) {
        super(new PlayerBotSession(), definition.getSpawnLocation());

        this.setUsername(definition.getUsername()).setLongUsername(Misc.stringToLong(definition.getUsername()))
                .setPasswordHashWithSalt(GameConstants.PLAYER_BOT_PASSWORD).setHostAddress("127.0.0.1");

        this.definition = definition;
        this.tradingInteraction = new TradingInteraction(this);
        this.chatInteraction = new ChatInteraction(this);
        this.movementInteraction = new MovementInteraction(this);
        this.combatInteraction = new CombatInteraction(this);

        this.setRigourUnlocked(true);
        this.setAuguryUnlocked(true);
        this.setAutoRetaliate(true);

        if (!World.getAddPlayerQueue().contains(this)) {
            World.getAddPlayerQueue().add(this);
        }
    }

    public PlayerBotDefinition getDefinition() {
        return this.definition;
    }

    public InteractionState getCurrentState() {
        return this.currentState;
    }

    public void setCurrentState(InteractionState interactionState) {
        this.currentState = interactionState;
    }

    public BotCommand[] getChatCommands() {
        return CHAT_COMMANDS;
    }

    public ChatInteraction getChatInteraction() {
        return this.chatInteraction;
    }

    public TradingInteraction getTradingInteraction() {
        return this.tradingInteraction;
    }

    public MovementInteraction getMovementInteraction() {
        return this.movementInteraction;
    }

    public CombatInteraction getCombatInteraction() {
        return this.combatInteraction;
    }

    public Location getSpawnPosition() {
        return this.spawnPosition;
    }

    public BotCommand getActiveCommand() {
        return this.activeCommand;
    }

    public void stopCommand() {
        if (this.getActiveCommand() != null) {
            this.getActiveCommand().stop(this);
        }

        this.setInteractingWith(null);
        this.activeCommand = null;
        this.setCurrentState(PlayerBot.InteractionState.IDLE);
    }

    public void startCommand(BotCommand _command, Player _player, String[] args) {
        this.setInteractingWith(_player);
        this.activeCommand = _command;
        this.setCurrentState(InteractionState.COMMAND);
        _command.start(this, args);
    }

    // Send a regular chat from this PlayerBot
    public void sendChat(String message) {
        this.getChatMessageQueue().add(new ChatMessage(0, 0, Misc.textPack(message)));
    }

    // Manually update the players local to this PlayerBot
    public void updateLocalPlayers() {
        if (this.getLocalPlayers().size() == 0) {
            return;
        }

        for (Player localPlayer : this.getLocalPlayers()) {
            PlayerUpdating.update(localPlayer);
        }
    }

    @Override
    public void process() {
        this.combatInteraction.process();
        super.process();
    }

    @Override
    public void onLogin() {
        super.onLogin();

        Presetables.load(this, this.getDefinition().getFighterPreset().getItemPreset());
    }

    @Override
    public void resetAttributes() {
        super.resetAttributes();

        stopCommand();
    }
}
