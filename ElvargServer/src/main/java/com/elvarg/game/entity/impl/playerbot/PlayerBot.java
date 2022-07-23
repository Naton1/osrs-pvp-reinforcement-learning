package com.elvarg.game.entity.impl.playerbot;

import com.elvarg.game.GameConstants;
import com.elvarg.game.World;
import com.elvarg.game.content.presets.Presetables;
import com.elvarg.game.definition.PlayerBotDefinition;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.entity.impl.playerbot.commands.BotCommand;
import com.elvarg.game.entity.impl.playerbot.commands.FollowPlayer;
import com.elvarg.game.entity.impl.playerbot.commands.HoldItems;
import com.elvarg.game.entity.impl.playerbot.commands.LoadPreset;
import com.elvarg.game.entity.impl.playerbot.interaction.*;
import com.elvarg.game.model.ChatMessage;
import com.elvarg.game.model.Location;
import com.elvarg.net.PlayerBotSession;
import com.elvarg.net.PlayerSession;
import com.elvarg.util.Misc;

import java.util.HashMap;

import static com.elvarg.game.entity.impl.playerbot.commands.LoadPreset.LOAD_PRESET_BUTTON_ID;

public class PlayerBot extends Player {

    private Location spawnPosition = GameConstants.DEFAULT_LOCATION;

    public Location getSpawnPosition() {
        return this.spawnPosition;
    }

    // The current interaction of this PlayerBot
    private InteractionState currentState = InteractionState.IDLE;

    private static final BotCommand[] chatCommands = new BotCommand[] {
            new FollowPlayer(), new HoldItems(), new LoadPreset()
    };

    private BotCommand activeCommand;


    public enum InteractionState {
        IDLE,

        // Performing a job for a player
        COMMAND;
    }

    public BotCommand getActiveCommand() { return this.activeCommand; }

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

    private PlayerBotDefinition definition;

    private Player interactingWith;

    public Player getInteractingWith() { return this.interactingWith; }

    public void setInteractingWith(Player _interact) { this.interactingWith = _interact; }

    private MovementInteraction movementInteraction;

    private ChatInteraction chatInteraction;

    private TradingInteraction tradingInteraction;

    private CombatInteraction combatInteraction;

    public PlayerBotDefinition getDefinition() { return this.definition; }
    public InteractionState getCurrentState() {
        return this.currentState;
    }

    public void setCurrentState(InteractionState interactionState) {
        this.currentState = interactionState;
    }

    public BotCommand[] getChatCommands() {
        return this.chatCommands;
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

    public PlayerBot(PlayerBotDefinition _definition) {
        super(new PlayerBotSession(), _definition.getSpawnLocation());

        this.setUsername(_definition.getUsername()).setLongUsername(Misc.stringToLong(_definition.getUsername()))
                .setPassword(GameConstants.PLAYER_BOT_PASSWORD).setHostAddress("127.0.0.1");

        this.definition = _definition;
        this.tradingInteraction = new TradingInteraction(this);
        this.chatInteraction = new ChatInteraction(this);
        this.movementInteraction = new MovementInteraction(this);
        this.combatInteraction = new CombatInteraction(this);

        if (!World.getAddPlayerQueue().contains(this)) {
            World.getAddPlayerQueue().add(this);
        }
    }

    /**
     * Creates this player bot.
     *
     */
    public PlayerBot(String username) {
        super(new PlayerBotSession());

        this.setUsername(username).setLongUsername(Misc.stringToLong(username))
                .setPassword(GameConstants.PLAYER_BOT_PASSWORD).setHostAddress("127.0.0.1");

        this.tradingInteraction = new TradingInteraction(this);
        this.chatInteraction = new ChatInteraction(this);
        this.movementInteraction = new MovementInteraction(this);
        this.combatInteraction = new CombatInteraction(this);

        if (!World.getAddPlayerQueue().contains(this)) {
            World.getAddPlayerQueue().add(this);
        }
    }

    // Send a regular chat from this PlayerBot
    public void sendChat(String message) {
        this.getChatMessageQueue().add(new ChatMessage(0, 0, Misc.textPack(message)));
    }

    @Override
    public boolean autoRetaliate() {
        // PlayerBots always retaliate
        return true;
    }

    @Override
    public void onLogin() {
        super.onLogin();

        this.setCurrentPreset(Presetables.GLOBAL_PRESETS[this.getDefinition().getPresetIndex()]);
        Presetables.handleButton(this, LOAD_PRESET_BUTTON_ID);
    }
}
