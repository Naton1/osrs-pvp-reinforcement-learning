package com.elvarg.game.entity.impl.player;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.elvarg.game.GameConstants;
import com.elvarg.game.collision.RegionManager;
import com.elvarg.game.content.sound.Sound;
import com.elvarg.game.World;
import com.elvarg.game.content.*;
import com.elvarg.game.content.PrayerHandler.PrayerData;
import com.elvarg.game.content.clan.ClanChat;
import com.elvarg.game.content.clan.ClanChatManager;
import com.elvarg.game.content.combat.CombatFactory;
import com.elvarg.game.content.combat.CombatSpecial;
import com.elvarg.game.content.combat.CombatType;
import com.elvarg.game.content.combat.FightType;
import com.elvarg.game.content.combat.WeaponInterfaces;
import com.elvarg.game.content.combat.WeaponInterfaces.WeaponInterface;
import com.elvarg.game.content.combat.bountyhunter.BountyHunter;
import com.elvarg.game.content.combat.hit.PendingHit;
import com.elvarg.game.content.combat.magic.Autocasting;
import com.elvarg.game.content.minigames.impl.Barrows;
import com.elvarg.game.content.minigames.impl.Barrows.Brother;
import com.elvarg.game.content.presets.Presetable;
import com.elvarg.game.content.presets.Presetables;
import com.elvarg.game.content.skill.SkillManager;
import com.elvarg.game.content.skill.skillable.Skillable;
import com.elvarg.game.content.skill.skillable.impl.Runecrafting.Pouch;
import com.elvarg.game.content.skill.skillable.impl.Runecrafting.PouchContainer;
import com.elvarg.game.content.skill.slayer.ActiveSlayerTask;
import com.elvarg.game.definition.ItemDefinition;
import com.elvarg.game.definition.PlayerBotDefinition;
import com.elvarg.game.entity.impl.Mobile;
import com.elvarg.game.entity.impl.npc.NPC;
import com.elvarg.game.entity.impl.npc.NpcAggression;
import com.elvarg.game.entity.impl.playerbot.PlayerBot;
import com.elvarg.game.model.Animation;
import com.elvarg.game.model.Appearance;
import com.elvarg.game.model.ChatMessage;
import com.elvarg.game.model.EffectTimer;
import com.elvarg.game.model.EnteredAmountAction;
import com.elvarg.game.model.EnteredSyntaxAction;
import com.elvarg.game.model.Flag;
import com.elvarg.game.model.ForceMovement;
import com.elvarg.game.model.God;
import com.elvarg.game.model.Item;
import com.elvarg.game.model.Location;
import com.elvarg.game.model.MagicSpellbook;
import com.elvarg.game.model.PlayerInteractingOption;
import com.elvarg.game.model.PlayerRelations;
import com.elvarg.game.model.PlayerStatus;
import com.elvarg.game.model.SecondsTimer;
import com.elvarg.game.model.Skill;
import com.elvarg.game.model.SkullType;
import com.elvarg.game.model.areas.AreaManager;
import com.elvarg.game.model.container.impl.Bank;
import com.elvarg.game.model.container.impl.Equipment;
import com.elvarg.game.model.container.impl.Inventory;
import com.elvarg.game.model.container.impl.PriceChecker;
import com.elvarg.game.model.container.shop.Shop;
import com.elvarg.game.model.dialogues.DialogueManager;
import com.elvarg.game.model.equipment.BonusManager;
import com.elvarg.game.model.menu.CreationMenu;
import com.elvarg.game.model.movement.MovementQueue;
import com.elvarg.game.model.rights.DonatorRights;
import com.elvarg.game.model.rights.PlayerRights;
import com.elvarg.game.model.teleportation.TeleportButton;
import com.elvarg.game.task.Task;
import com.elvarg.game.task.TaskManager;
import com.elvarg.game.task.impl.CombatPoisonEffect;
import com.elvarg.game.task.impl.PlayerDeathTask;
import com.elvarg.game.task.impl.RestoreSpecialAttackTask;
import com.elvarg.net.PlayerSession;
import com.elvarg.net.channel.ChannelEventHandler;
import com.elvarg.net.packet.PacketSender;
import com.elvarg.util.FrameUpdater;
import com.elvarg.util.Misc;
import com.elvarg.util.NpcIdentifiers;
import com.elvarg.util.Stopwatch;
import com.elvarg.util.timers.TimerKey;

import io.netty.buffer.ByteBuf;

import static com.elvarg.game.GameConstants.PLAYER_PERSISTENCE;

public class Player extends Mobile {
	public final SecondsTimer increaseStats = new SecondsTimer();
	public final SecondsTimer decreaseStats = new SecondsTimer();
	private final List<Player> localPlayers = new LinkedList<Player>();
	private final List<NPC> localNpcs = new LinkedList<NPC>();
	private final PacketSender packetSender = new PacketSender(this);
	private final Appearance appearance = new Appearance(this);
	private final SkillManager skillManager = new SkillManager(this);
	private final PlayerRelations relations = new PlayerRelations(this);
	private final FrameUpdater frameUpdater = new FrameUpdater();
	private final BonusManager bonusManager = new BonusManager();
	private final QuickPrayers quickPrayers = new QuickPrayers(this);
	private final Inventory inventory = new Inventory(this);
	private final Equipment equipment = new Equipment(this);
	private final PriceChecker priceChecker = new PriceChecker(this);
	private final Stopwatch clickDelay = new Stopwatch();
	private final Stopwatch lastItemPickup = new Stopwatch();
	private final SecondsTimer yellDelay = new SecondsTimer();
	private final SecondsTimer aggressionTolerance = new SecondsTimer();
	// Delay for restoring special attack
	private final SecondsTimer specialAttackRestore = new SecondsTimer();
	/*
	 * Fields
	 */
	private final SecondsTimer targetSearchTimer = new SecondsTimer();
	private final List<String> recentKills = new ArrayList<String>(); // Contains ip addresses of recent kills
	private final Queue<ChatMessage> chatMessageQueue = new ConcurrentLinkedQueue<>();
	public boolean choosingMusic;
	private ChatMessage currentChatMessage;
	// Logout
	private final SecondsTimer forcedLogoutTimer = new SecondsTimer();
	// Trading
	private final Trading trading = new Trading(this);
	private final Dueling dueling = new Dueling(this);
	private final DialogueManager dialogueManager = new DialogueManager(this);
	// Presets
	private Presetable currentPreset;
	private Presetable[] presets = new Presetable[Presetables.MAX_PRESETS];
	private boolean openPresetsOnDeath = true;

	private String username;
	private String passwordHashWithSalt;
	private String hostAddress;
	private boolean isDiscordLogin;
	private String cachedDiscordAccessToken;
	private Long longUsername;
	private PlayerSession session;
	private PlayerInteractingOption playerInteractingOption = PlayerInteractingOption.NONE;
	private PlayerStatus status = PlayerStatus.NONE;
	private ClanChat currentClanChat;
	private String clanChatName;
	private Shop shop;
	private int interfaceId = -1, walkableInterfaceId = -1, multiIcon;
	private boolean isRunning = true;
	private int runEnergy = 100;
	private Stopwatch lastRunRecovery = new Stopwatch();
	private boolean isDying;
	private boolean allowRegionChangePacket;
	private boolean experienceLocked;
	private ForceMovement forceMovement;
	private NPC currentPet;
	private int skillAnimation;
	private boolean drainingPrayer;
	private double prayerPointDrain;
	private MagicSpellbook spellbook = MagicSpellbook.NORMAL;
	private final Map<TeleportButton, Location> previousTeleports = new HashMap<>();
	private boolean teleportInterfaceOpen;
	private int destroyItem = -1;
	private boolean updateInventory; // Updates inventory on next tick
	private boolean newPlayer;
	private boolean packetsBlocked = false;
	private int regionHeight;

	private int questPoints;
	private Map<Integer, Integer> questProgress = new HashMap<Integer, Integer>();
	// Skilling
	private Optional<Skillable> skill = Optional.empty();
	private CreationMenu creationMenu;
	// Entering data
	private EnteredAmountAction enteredAmountAction;
	private EnteredSyntaxAction enteredSyntaxAction;
	
	// Time the account was created
	private Timestamp creationDate;
	// RC
	private PouchContainer[] pouches = new PouchContainer[] { new PouchContainer(Pouch.SMALL_POUCH),
			new PouchContainer(Pouch.MEDIUM_POUCH), new PouchContainer(Pouch.LARGE_POUCH),
			new PouchContainer(Pouch.GIANT_POUCH), };
	// Slayer
	private ActiveSlayerTask slayerTask;
	private int slayerPoints;
	private int consecutiveTasks;
	
	// Combat
	private SkullType skullType = SkullType.WHITE_SKULL;
	private CombatSpecial combatSpecial;
	private int recoilDamage;
	private SecondsTimer vengeanceTimer = new SecondsTimer();
	private int wildernessLevel;
	private int skullTimer;
	private int points;
	private int amountDonated;
	// Blowpipe
	private int blowpipeScales;
	// Bounty hunter
	private int targetKills;
	private int normalKills;
	private int totalKills;
	private int killstreak;
	private int highestKillstreak;
	private int deaths;
	private int safeTimer = 180;
	public int pcPoints;
	// Barrows
	private int barrowsCrypt;
	private int barrowsChestsLooted;
	private boolean[] killedBrothers = new boolean[Brother.values().length];
	private NPC currentBrother;
	private boolean preserveUnlocked;
	private boolean rigourUnlocked;
	private boolean auguryUnlocked;
	private boolean targetTeleportUnlocked;
	// Banking
	private int currentBankTab;
	private Bank[] banks = new Bank[Bank.TOTAL_BANK_TABS]; // last index is for bank searches
	private boolean noteWithdrawal, insertMode, searchingBank;
	private String searchSyntax = "";
	private boolean placeholders = true;
    private boolean infiniteHealth;
    private FightType fightType = FightType.UNARMED_KICK;
    private WeaponInterface weapon;
    private boolean autoRetaliate = true;
    
	// GWD
	private int[] godwarsKillcount = new int[God.values().length];

	// Rights
	private PlayerRights rights = PlayerRights.NONE;
	private DonatorRights donatorRights = DonatorRights.NONE;
	/**
	 * The cached player update block for updating.
	 */
	private ByteBuf cachedUpdateBlock;
	private String loyaltyTitle = "empty";
	private boolean spawnedBarrows;
	private Location oldPosition;
	
	/**
	 * Creates this player.
	 *
	 * @param playerIO
	 */
	public Player(PlayerSession playerIO) {
		super(GameConstants.DEFAULT_LOCATION.clone());
		this.session = playerIO;
	}

	/**
	 * Creates this player with pre defined spawn location.
	 *
	 * @param playerIO
	 */
	public Player(PlayerSession playerIO, Location spawnLocation) {
		super(spawnLocation);
		this.session = playerIO;
	}

	/**
	 * Actions that should be done when this character is added to the world.
	 */
	@Override
	public void onAdd() {
		onLogin();
	}

	/**
	 * Actions that should be done when this character is removed from the world.
	 */
	@Override
	public void onRemove() {
		onLogout();
	}

	@Override
	public void appendDeath() {
		if (!isDying) {
			TaskManager.submit(new PlayerDeathTask(this));
			isDying = true;
		}
	}

	@Override
	public int getHitpoints() {
		return getSkillManager().getCurrentLevel(Skill.HITPOINTS);
	}

	@Override
	public int getAttackAnim() {
		return getFightType().getAnimation();
	}

	@Override
	public Sound getAttackSound() {
		return getFightType().getAttackSound();
	}

	@Override
	public int getBlockAnim() {
		final Item shield = getEquipment().getItems()[Equipment.SHIELD_SLOT];
		final Item weapon = getEquipment().getItems()[Equipment.WEAPON_SLOT];
		ItemDefinition definition = shield.getId() > 0 ? shield.getDefinition() : weapon.getDefinition();
		return definition.getBlockAnim();
	}

	@Override
	public Mobile setHitpoints(int hitpoints) {
		if (isDying) {
			return this;
		}

		if (infiniteHealth) {
			if (skillManager.getCurrentLevel(Skill.HITPOINTS) > hitpoints) {
				return this;
			}
		}

		skillManager.setCurrentLevel(Skill.HITPOINTS, hitpoints);
		packetSender.sendSkill(Skill.HITPOINTS);
		if (getHitpoints() <= 0 && !isDying)
			appendDeath();
		return this;
	}

	@Override
	public void heal(int amount) {
		int level = skillManager.getMaxLevel(Skill.HITPOINTS);
		if ((skillManager.getCurrentLevel(Skill.HITPOINTS) + amount) >= level) {
			setHitpoints(level);
		} else {
			setHitpoints(skillManager.getCurrentLevel(Skill.HITPOINTS) + amount);
		}
	}

	@Override
	public int getBaseAttack(CombatType type) {
		if (type == CombatType.RANGED)
			return skillManager.getCurrentLevel(Skill.RANGED);
		else if (type == CombatType.MAGIC)
			return skillManager.getCurrentLevel(Skill.MAGIC);
		return skillManager.getCurrentLevel(Skill.ATTACK);
	}

	@Override
	public int getBaseDefence(CombatType type) {
		if (type == CombatType.MAGIC)
			return skillManager.getCurrentLevel(Skill.MAGIC);
		return skillManager.getCurrentLevel(Skill.DEFENCE);
	}

	@Override
	public int getBaseAttackSpeed() {

		// Gets attack speed for player's weapon
		// If player is using magic, attack speed is
		// Calculated in the MagicCombatMethod class.

		int speed = getWeapon().getSpeed();

		if (getFightType().toString().toLowerCase().contains("rapid")) {
			speed--;
		}

		return speed;
	}

	@Override
	public boolean isPlayer() {
		return true;
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Player)) {
			return false;
		}
		Player p = (Player) o;
		return p.getUsername().equals(username);
	}

	@Override
	public int size() {
		return 1;
	}

	public void process() {
		// Timers
		getTimers().process();

		// Process incoming packets...
		PlayerSession session = getSession();
		if (session != null) {
			session.processPackets();
		}

		// Process walking queue..
		getMovementQueue().process();

		// Process combat
		getCombat().process();

		// Process aggression
		NpcAggression.process(this);

		// Process areas..
		AreaManager.process(this);

		// Process Bounty Hunter
		BountyHunter.process(this);

		// Updates inventory if an update
		// has been requested
		if (isUpdateInventory()) {
			getInventory().refreshItems();
			setUpdateInventory(false);
		}

		// Updates appearance if an update
		// has been requested
		// or if skull timer hits 0.
		if (isSkulled() && getAndDecrementSkullTimer() == 0) {
			getUpdateFlag().flag(Flag.APPEARANCE);
		}

		// Send queued chat messages
		if (!getChatMessageQueue().isEmpty()) {
			setCurrentChatMessage(getChatMessageQueue().poll());
			getUpdateFlag().flag(Flag.CHAT);
		} else {
			setCurrentChatMessage(null);
		}

		// Increase run energy
		if (runEnergy < 100 && (!getMovementQueue().isMoving() || !isRunning)) {
			if (lastRunRecovery.elapsed(MovementQueue.runEnergyRestoreDelay(this))) {
				runEnergy++;
				getPacketSender().sendRunEnergy();
				lastRunRecovery.reset();
			}
		}

		if (this instanceof PlayerBot) {
			((PlayerBot) this).getMovementInteraction().process();
		}

		/**
		 * Decrease boosted stats Increase lowered stats
		 */
		if (getHitpoints() > 0) {
			if (increaseStats.finished() || decreaseStats
					.secondsElapsed() >= (PrayerHandler.isActivated(this, PrayerHandler.PRESERVE) ? 72 : 60)) {
				for (Skill skill : Skill.values()) {
					int current = getSkillManager().getCurrentLevel(skill);
					int max = getSkillManager().getMaxLevel(skill);

					// Should lowered stats be increased?
					if (current < max) {
						if (increaseStats.finished()) {
							int restoreRate = 1;

							// Rapid restore effect - 2x restore rate for all stats except hp/prayer
							// Rapid heal - 2x restore rate for hitpoints
							if (skill != Skill.HITPOINTS && skill != Skill.PRAYER) {
								if (PrayerHandler.isActivated(this, PrayerHandler.RAPID_RESTORE)) {
									restoreRate = 2;
								}
							} else if (skill == Skill.HITPOINTS) {
								if (PrayerHandler.isActivated(this, PrayerHandler.RAPID_HEAL)) {
									restoreRate = 2;
								}
							}

							getSkillManager().increaseCurrentLevel(skill, restoreRate, max);
						}
					} else if (current > max) {

						// Should boosted stats be decreased?
						if (decreaseStats
								.secondsElapsed() >= (PrayerHandler.isActivated(this, PrayerHandler.PRESERVE) ? 72
										: 60)) {

							// Never decrease Hitpoints / Prayer
							if (skill != Skill.HITPOINTS && skill != Skill.PRAYER) {
								getSkillManager().decreaseCurrentLevel(skill, 1, 1);
							}

						}
					}
				}

				// Reset timers
				if (increaseStats.finished()) {
					increaseStats.start(60);
				}
				if (decreaseStats
						.secondsElapsed() >= (PrayerHandler.isActivated(this, PrayerHandler.PRESERVE) ? 72 : 60)) {
					decreaseStats.start((PrayerHandler.isActivated(this, PrayerHandler.PRESERVE) ? 72 : 60));
				}
			}
		}
	}

	// Construction
	/*
	 * public boolean loadingHouse; public int portalSelected; public boolean
	 * inBuildingMode; public int[] toConsCoords; public int buildFurnitureId,
	 * buildFurnitureX, buildFurnitureY; public Room[][][] houseRooms = new
	 * Room[5][13][13]; public ArrayList<PlayerFurniture> playerFurniture = new
	 * ArrayList<PlayerFurniture>(); public ArrayList<Portal> portals = new
	 * ArrayList<>();
	 */

	/**
	 * Can the player logout?
	 *
	 * @return Yes if they can logout, false otherwise.
	 */
	public boolean canLogout() {
		if (CombatFactory.isBeingAttacked(this)) {
			getPacketSender().sendMessage("You must wait a few seconds after being out of combat before doing this.");
			return false;
		}
		if (busy()) {
			getPacketSender().sendMessage("You cannot log out at the moment.");
			return false;
		}
		return true;
	}

	/**
	 * Requests a logout by sending the logout packet to the client. This leads to
	 * the connection being closed. The {@link ChannelEventHandler} will then add
	 * the player to the remove characters queue.
	 */
	public void requestLogout() {
		getPacketSender().sendLogout();
	}

	/**
	 * Handles the actual logging out from the game.
	 */
	public void onLogout() {
		// Notify us
		System.out.println(
				"[World] Deregistering player - [username, host] : [" + getUsername() + ", " + getHostAddress() + "]");

		getPacketSender().sendInterfaceRemoval();

		// Leave area
		if (getArea() != null) {
			getArea().leave(this, true);
			getArea().postLeave(this, true);
		}

		// Do stuff...
		Barrows.brotherDespawn(this);
		PetHandler.pickup(this, getCurrentPet());
		getRelations().updateLists(false);
		BountyHunter.unassign(this);
		ClanChatManager.leave(this, false);
		TaskManager.cancelTasks(this);
		PLAYER_PERSISTENCE.save(this);

		if (getSession() != null && getSession().getChannel().isOpen()) {
			getSession().getChannel().close();
		}
	}

	/**
	 * Called by the world's login queue!
	 */
	public void onLogin() {
		// Attempt to register the player..
		System.out.println(
				"[World] Registering player - [username, host] : [" + getUsername() + ", " + getHostAddress() + "]");

		setNeedsPlacement(true);
		getPacketSender().sendMapRegion().sendDetails(); // Map region, player index and player rights
		getPacketSender().sendTabs(); // Client sideicons
		getPacketSender().sendMessage("Welcome to @red@" + GameConstants.NAME + ".");
		if (this.isDiscordLogin()) {
			getPacketSender().sendMessage(":discordtoken:" + this.getCachedDiscordAccessToken());
		}

		long totalExp = 0;
		for (Skill skill : Skill.values()) {
			getSkillManager().updateSkill(skill);
			totalExp += getSkillManager().getExperience(skill);
		}
		getPacketSender().sendTotalExp(totalExp);

		// Send friends and ignored players lists...
		getRelations().setPrivateMessageId(1).onLogin(this).updateLists(true);

		// Reset prayer configs...
		PrayerHandler.resetAll(this);
		getPacketSender().sendConfig(709, PrayerHandler.canUse(this, PrayerData.PRESERVE, false) ? 1 : 0);
		getPacketSender().sendConfig(711, PrayerHandler.canUse(this, PrayerData.RIGOUR, false) ? 1 : 0);
		getPacketSender().sendConfig(713, PrayerHandler.canUse(this, PrayerData.AUGURY, false) ? 1 : 0);

		// Refresh item containers..
		getInventory().refreshItems();
		getEquipment().refreshItems();

		// Interaction options on right click...
		getPacketSender().sendInteractionOption("Follow", 3, false);
		getPacketSender().sendInteractionOption("Trade With", 4, false);

		// Sending run energy attributes...
		getPacketSender().sendRunStatus();
		getPacketSender().sendRunEnergy();

		// Sending player's rights..
		getPacketSender().sendRights();

		// Close all interfaces, just in case...
		getPacketSender().sendInterfaceRemoval();

		// Update weapon data and interfaces..
		WeaponInterfaces.assign(this);
		// Update weapon interface configs
		getPacketSender().sendConfig(getFightType().getParentId(), getFightType().getChildId())
				.sendConfig(172, autoRetaliate() ? 1 : 0).updateSpecialAttackOrb();

		// Reset autocasting
		Autocasting.setAutocast(this, null);

		// Send pvp stats..
		getPacketSender().sendString(52029, "@or1@Killstreak: " + getKillstreak())
				.sendString(52030, "@or1@Kills: " + getTotalKills()).sendString(52031, "@or1@Deaths: " + getDeaths())
				.sendString(52033, "@or1@K/D Ratio: " + getKillDeathRatio())
				.sendString(52034, "@or1@Donated: " + getAmountDonated());

		// Join clanchat
		ClanChatManager.onLogin(this);

		// Handle timers and run tasks
		if (isPoisoned()) {
			getPacketSender().sendPoisonType(1);
			TaskManager.submit(new CombatPoisonEffect(this));
		}
		if (getSpecialPercentage() < 100) {
			TaskManager.submit(new RestoreSpecialAttackTask(this));
		}

		if (!getVengeanceTimer().finished()) {
			getPacketSender().sendEffectTimer(getVengeanceTimer().secondsRemaining(), EffectTimer.VENGEANCE);
		}
		if (!getCombat().getFireImmunityTimer().finished()) {
			getPacketSender().sendEffectTimer(getCombat().getFireImmunityTimer().secondsRemaining(),
					EffectTimer.ANTIFIRE);
		}
		if (!getCombat().getTeleBlockTimer().finished()) {
			getPacketSender().sendEffectTimer(getCombat().getTeleBlockTimer().secondsRemaining(),
					EffectTimer.TELE_BLOCK);
		}

		decreaseStats.start(60);
		increaseStats.start(60);

		getUpdateFlag().flag(Flag.APPEARANCE);

		if (this.newPlayer) {
			int presetIndex = Misc.randomInclusive(0, Presetables.GLOBAL_PRESETS.length-1);
			Presetables.load(this, Presetables.GLOBAL_PRESETS[presetIndex]);
		}

		if (!(this instanceof PlayerBot)) {
			// Spawn player bots when a real player logs in
			for (PlayerBotDefinition definition : GameConstants.PLAYER_BOTS) {
				if (World.getPlayerBots().containsKey(definition.getUsername())) {
					continue;
				}

				PlayerBot playerBot = new PlayerBot(definition);

				World.getPlayerBots().put(definition.getUsername(), playerBot);
			}

			System.out.println(GameConstants.PLAYER_BOTS.length + " player bots now online.");
		}
	}

	/**
	 * Resets the player's attributes to default.
	 */
	public void resetAttributes() {
		performAnimation(Animation.DEFAULT_RESET_ANIMATION);
		setSpecialActivated(false);
		CombatSpecial.updateBar(this);
		setHasVengeance(false);
		getCombat().getFireImmunityTimer().stop();
		getCombat().getPoisonImmunityTimer().stop();
		getCombat().getTeleBlockTimer().stop();
		getTimers().cancel(TimerKey.FREEZE);
		getCombat().getPrayerBlockTimer().stop();
		setPoisonDamage(0);
		setWildernessLevel(0);
		setRecoilDamage(0);
		setSkullTimer(0);
		setSkullType(SkullType.WHITE_SKULL);
		WeaponInterfaces.assign(this);
		BonusManager.update(this);
		PrayerHandler.deactivatePrayers(this);
		getEquipment().refreshItems();
		getInventory().refreshItems();
		for (Skill skill : Skill.values())
			getSkillManager().setCurrentLevel(skill, getSkillManager().getMaxLevel(skill));
		setRunEnergy(100);
		getPacketSender().sendRunEnergy();
		getMovementQueue().setBlockMovement(false).reset();
		getPacketSender().sendEffectTimer(0, EffectTimer.ANTIFIRE).sendEffectTimer(0, EffectTimer.FREEZE)
				.sendEffectTimer(0, EffectTimer.VENGEANCE).sendEffectTimer(0, EffectTimer.TELE_BLOCK);
		getPacketSender().sendPoisonType(0);
		getPacketSender().sendSpecialAttackState(false);
		setUntargetable(false);
		isDying = false;

		getUpdateFlag().flag(Flag.APPEARANCE);
	}

	/**
	 * Checks if a player is busy.
	 *
	 * @return
	 */
	public boolean busy() {
		if (interfaceId > 0) {
			return true;
		}
		if (getHitpoints() <= 0) {
			return true;
		}
		if (isNeedsPlacement() || isTeleporting()) {
			return true;
		}
		if (status != PlayerStatus.NONE) {
			return true;
		}
		if (forceMovement != null) {
			return true;
		}
		return false;
	}

	public boolean isStaff() {
		return (rights != PlayerRights.NONE);
	}

	public boolean isDonator() {
		return (donatorRights != DonatorRights.NONE);
	}

	public boolean isPacketsBlocked() {
		return packetsBlocked;
	}

	public void setPacketsBlocked(boolean blocked) {
		this.packetsBlocked = blocked;
	}

	/*
	 * Getters/Setters
	 */

	public Timestamp getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Timestamp timestamp) {
		creationDate = timestamp;
	}

	public PlayerSession getSession() {
		return session;
	}

	public String getUsername() {
		return username;
	}

	public Player setUsername(String username) {
		this.username = username;
		return this;
	}

	public Long getLongUsername() {
		return longUsername;
	}

	public Player setLongUsername(Long longUsername) {
		this.longUsername = longUsername;
		return this;
	}

	public String getPasswordHashWithSalt() {
		return passwordHashWithSalt;
	}

	public Player setPasswordHashWithSalt(String passwordHashWithSalt) {
		this.passwordHashWithSalt = passwordHashWithSalt;
		return this;
	}

	public String getHostAddress() {
		return hostAddress;
	}

	public Player setHostAddress(String hostAddress) {
		this.hostAddress = hostAddress;
		return this;
	}

	public PlayerRights getRights() {
		return rights;
	}

	public Player setRights(PlayerRights rights) {
		this.rights = rights;
		return this;
	}

	public PacketSender getPacketSender() {
		return packetSender;
	}

	public SkillManager getSkillManager() {
		return skillManager;
	}

	public Appearance getAppearance() {
		return appearance;
	}

	public SecondsTimer getForcedLogoutTimer() {
		return forcedLogoutTimer;
	}

	public boolean isDying() {
		return isDying;
	}

	public List<Player> getLocalPlayers() {
		return localPlayers;
	}

	public List<NPC> getLocalNpcs() {
		return localNpcs;
	}

	public int getInterfaceId() {
		return interfaceId;
	}

	public Player setInterfaceId(int interfaceId) {
		this.interfaceId = interfaceId;
		return this;
	}

	public boolean experienceLocked() {
		return experienceLocked;
	}

	public void setExperienceLocked(boolean experienceLocked) {
		this.experienceLocked = experienceLocked;
	}

	public PlayerRelations getRelations() {
		return relations;
	}

	public boolean isAllowRegionChangePacket() {
		return allowRegionChangePacket;
	}

	public void setAllowRegionChangePacket(boolean allowRegionChangePacket) {
		this.allowRegionChangePacket = allowRegionChangePacket;
	}

	public int getWalkableInterfaceId() {
		return walkableInterfaceId;
	}

	public void setWalkableInterfaceId(int interfaceId2) {
		this.walkableInterfaceId = interfaceId2;
	}

	public boolean isRunning() {
		return isRunning;
	}

	public Player setRunning(boolean isRunning) {
		this.isRunning = isRunning;
		return this;
	}

	public PlayerInteractingOption getPlayerInteractingOption() {
		return playerInteractingOption;
	}

	public Player setPlayerInteractingOption(PlayerInteractingOption playerInteractingOption) {
		this.playerInteractingOption = playerInteractingOption;
		return this;
	}

	public FrameUpdater getFrameUpdater() {
		return frameUpdater;
	}

	public BonusManager getBonusManager() {
		return bonusManager;
	}

	public int getMultiIcon() {
		return multiIcon;
	}

	public Player setMultiIcon(int multiIcon) {
		this.multiIcon = multiIcon;
		return this;
	}

	public Inventory getInventory() {
		return inventory;
	}

	public Equipment getEquipment() {
		return equipment;
	}

	public ForceMovement getForceMovement() {
		return forceMovement;
	}

	public Player setForceMovement(ForceMovement forceMovement) {
		this.forceMovement = forceMovement;
		if (this.forceMovement != null) {
			getUpdateFlag().flag(Flag.FORCED_MOVEMENT);
		}
		return this;
	}

	public int getSkillAnimation() {
		return skillAnimation;
	}

	public Player setSkillAnimation(int animation) {
		this.skillAnimation = animation;
		return this;
	}

	public int getRunEnergy() {
		return runEnergy;
	}

	public void setRunEnergy(int runEnergy) {
		this.runEnergy = runEnergy;
	}

	public boolean isDrainingPrayer() {
		return drainingPrayer;
	}

	public void setDrainingPrayer(boolean drainingPrayer) {
		this.drainingPrayer = drainingPrayer;
	}

	public double getPrayerPointDrain() {
		return prayerPointDrain;
	}

	public void setPrayerPointDrain(double prayerPointDrain) {
		this.prayerPointDrain = prayerPointDrain;
	}

	public Stopwatch getLastItemPickup() {
		return lastItemPickup;
	}

	public CombatSpecial getCombatSpecial() {
		return combatSpecial;
	}

	public void setCombatSpecial(CombatSpecial combatSpecial) {
		this.combatSpecial = combatSpecial;
	}

	public int getRecoilDamage() {
		return recoilDamage;
	}

	public void setRecoilDamage(int recoilDamage) {
		this.recoilDamage = recoilDamage;
	}

	public MagicSpellbook getSpellbook() {
		return spellbook;
	}

	public void setSpellbook(MagicSpellbook spellbook) {
		this.spellbook = spellbook;
	}

	public SecondsTimer getVengeanceTimer() {
		return vengeanceTimer;
	}

	public int getWildernessLevel() {
		return wildernessLevel;
	}

	public void setWildernessLevel(int wildernessLevel) {
		this.wildernessLevel = wildernessLevel;
	}

	public boolean isSpawnedBarrows() {
		return spawnedBarrows;
	}

	public void setSpawnedBarrows(boolean spawnedBarrows) {
		this.spawnedBarrows = spawnedBarrows;
	}

	public int getDestroyItem() {
		return destroyItem;
	}

	public void setDestroyItem(int destroyItem) {
		this.destroyItem = destroyItem;
	}

	public boolean isSkulled() {
		return skullTimer > 0;
	}

	public int getAndDecrementSkullTimer() {
		return this.skullTimer--;
	}

	public int getSkullTimer() {
		return this.skullTimer;
	}

	public void setSkullTimer(int skullTimer) {
		this.skullTimer = skullTimer;
	}

	public int getPoints() {
		return points;
	}

	public void setPoints(int points) {
		this.points = points;
	}

	public void incrementPoints(int points) {
		this.points += points;
	}

	public boolean isUpdateInventory() {
		return updateInventory;
	}

	public void setUpdateInventory(boolean updateInventory) {
		this.updateInventory = updateInventory;
	}

	public Stopwatch getClickDelay() {
		return clickDelay;
	}

	public Shop getShop() {
		return shop;
	}

	public Player setShop(Shop shop) {
		this.shop = shop;
		return this;
	}

	public PlayerStatus getStatus() {
		return status;
	}

	public Player setStatus(PlayerStatus status) {
		this.status = status;
		return this;
	}

	public int getCurrentBankTab() {
		return currentBankTab;
	}

	public Player setCurrentBankTab(int tab) {
		this.currentBankTab = tab;
		return this;
	}

	public void setNoteWithdrawal(boolean noteWithdrawal) {
		this.noteWithdrawal = noteWithdrawal;
	}

	public boolean withdrawAsNote() {
		return noteWithdrawal;
	}

	public void setInsertMode(boolean insertMode) {
		this.insertMode = insertMode;
	}

	public boolean insertMode() {
		return insertMode;
	}

	public Bank[] getBanks() {
		return banks;
	}

	public Bank getBank(int index) {
		if (banks[index] == null) {
			banks[index] = new Bank(this);
		}
		return banks[index];
	}

	public Player setBank(int index, Bank bank) {
		this.banks[index] = bank;
		return this;
	}

	public boolean isNewPlayer() {
		return newPlayer;
	}

	public void setNewPlayer(boolean newPlayer) {
		this.newPlayer = newPlayer;
	}

	public boolean isSearchingBank() {
		return searchingBank;
	}

	public void setSearchingBank(boolean searchingBank) {
		this.searchingBank = searchingBank;
	}

	public String getSearchSyntax() {
		return searchSyntax;
	}

	public void setSearchSyntax(String searchSyntax) {
		this.searchSyntax = searchSyntax;
	}

	public boolean isPreserveUnlocked() {
		return preserveUnlocked;
	}

	public void setPreserveUnlocked(boolean preserveUnlocked) {
		this.preserveUnlocked = preserveUnlocked;
	}

	public boolean isRigourUnlocked() {
		return rigourUnlocked;
	}

	public void setRigourUnlocked(boolean rigourUnlocked) {
		this.rigourUnlocked = rigourUnlocked;
	}

	public boolean isAuguryUnlocked() {
		return auguryUnlocked;
	}

	public void setAuguryUnlocked(boolean auguryUnlocked) {
		this.auguryUnlocked = auguryUnlocked;
	}

	public PriceChecker getPriceChecker() {
		return priceChecker;
	}

	public ClanChat getCurrentClanChat() {
		return currentClanChat;
	}

	public void setCurrentClanChat(ClanChat currentClanChat) {
		this.currentClanChat = currentClanChat;
	}

	public String getClanChatName() {
		return clanChatName;
	}

	public void setClanChatName(String clanChatName) {
		this.clanChatName = clanChatName;
	}

	public Trading getTrading() {
		return trading;
	}

	public QuickPrayers getQuickPrayers() {
		return quickPrayers;
	}

	public boolean isTargetTeleportUnlocked() {
		return targetTeleportUnlocked;
	}

	public void setTargetTeleportUnlocked(boolean targetTeleportUnlocked) {
		this.targetTeleportUnlocked = targetTeleportUnlocked;
	}

	public SecondsTimer getYellDelay() {
		return yellDelay;
	}

	public int getAmountDonated() {
		return amountDonated;
	}

	public void setAmountDonated(int amountDonated) {
		this.amountDonated = amountDonated;
	}

	public void incrementAmountDonated(int amountDonated) {
		this.amountDonated += amountDonated;
	}

	public void incrementTargetKills() {
		targetKills++;
	}

	public int getTargetKills() {
		return targetKills;
	}

	public void setTargetKills(int targetKills) {
		this.targetKills = targetKills;
	}

	public void incrementKills() {
		normalKills++;
	}

	public int getNormalKills() {
		return normalKills;
	}

	public void setNormalKills(int normalKills) {
		this.normalKills = normalKills;
	}

	public int getTotalKills() {
		return totalKills;
	}

	public void setTotalKills(int totalKills) {
		this.totalKills = totalKills;
	}

	public void incrementTotalKills() {
		this.totalKills++;
	}

	public void incrementDeaths() {
		deaths++;
	}

	public int getDeaths() {
		return deaths;
	}

	public void setDeaths(int deaths) {
		this.deaths = deaths;
	}

	public void resetSafingTimer() {
		this.setSafeTimer(180);
	}

	public int getHighestKillstreak() {
		return highestKillstreak;
	}

	public void setHighestKillstreak(int highestKillstreak) {
		this.highestKillstreak = highestKillstreak;
	}

	public int getKillstreak() {
		return killstreak;
	}

	public void setKillstreak(int killstreak) {
		this.killstreak = killstreak;
	}

	public void incrementKillstreak() {
		this.killstreak++;
	}

	public String getKillDeathRatio() {
		double kc = 0;
		if (deaths == 0) {
			kc = totalKills / 1;
		} else {
			kc = ((double) totalKills / deaths);
		}
		return Misc.FORMATTER.format(kc);
	}

	public List<String> getRecentKills() {
		return recentKills;
	}

	public int getSafeTimer() {
		return safeTimer;
	}

	public void setSafeTimer(int safeTimer) {
		this.safeTimer = safeTimer;
	}

	public int decrementAndGetSafeTimer() {
		return this.safeTimer--;
	}

	public SecondsTimer getTargetSearchTimer() {
		return targetSearchTimer;
	}

	public SecondsTimer getSpecialAttackRestore() {
		return specialAttackRestore;
	}

	public SkullType getSkullType() {
		return skullType;
	}

	public void setSkullType(SkullType skullType) {
		this.skullType = skullType;
	}

	public Dueling getDueling() {
		return dueling;
	}

	public int getBlowpipeScales() {
		return blowpipeScales;
	}

	public void setBlowpipeScales(int blowpipeScales) {
		this.blowpipeScales = blowpipeScales;
	}

	public void incrementBlowpipeScales(int blowpipeScales) {
		this.blowpipeScales += blowpipeScales;
	}

	public int decrementAndGetBlowpipeScales() {
		return this.blowpipeScales--;
	}

	public NPC getCurrentPet() {
		return currentPet;
	}

	public void setCurrentPet(NPC currentPet) {
		this.currentPet = currentPet;
	}

	public SecondsTimer getAggressionTolerance() {
		return aggressionTolerance;
	}

	public ByteBuf getCachedUpdateBlock() {
		return cachedUpdateBlock;
	}

	public void setCachedUpdateBlock(ByteBuf cachedUpdateBlock) {
		this.cachedUpdateBlock = cachedUpdateBlock;
	}

	public int getRegionHeight() {
		return regionHeight;
	}

	public int getRegionId() {
		return RegionManager.calculateRegionId(getLocation().getX(), getLocation().getY());
	}

	public void setRegionHeight(int regionHeight) {
		this.regionHeight = regionHeight;
	}

	public Optional<Skillable> getSkill() {
		return skill;
	}

	public void setSkill(Optional<Skillable> skill) {
		this.skill = skill;
	}

	public CreationMenu getCreationMenu() {
		return creationMenu;
	}

	public void setCreationMenu(CreationMenu creationMenu) {
		this.creationMenu = creationMenu;
	}

	public PouchContainer[] getPouches() {
		return pouches;
	}

	public void setPouches(PouchContainer[] pouches) {
		this.pouches = pouches;
	}

	public String getLoyaltyTitle() {
		return loyaltyTitle;
	}

	public void setLoyaltyTitle(String loyaltyTitle) {
		this.loyaltyTitle = loyaltyTitle;
		this.getUpdateFlag().flag(Flag.APPEARANCE);
	}
	
	public boolean hasInfiniteHealth() {
		return infiniteHealth;
	}

	public void setInfiniteHealth(boolean infiniteHealth) {
		this.infiniteHealth = infiniteHealth;
	}

	public DonatorRights getDonatorRights() {
		return donatorRights;
	}

	public void setDonatorRights(DonatorRights donatorPrivilege) {
		this.donatorRights = donatorPrivilege;
	}

	public NPC getCurrentBrother() {
		return currentBrother;
	}

	public void setCurrentBrother(NPC brother) {
		this.currentBrother = brother;
	}

	public int getBarrowsCrypt() {
		return barrowsCrypt;
	}

	public void setBarrowsCrypt(int crypt) {
		this.barrowsCrypt = crypt;
	}

	public boolean[] getKilledBrothers() {
		return killedBrothers;
	}

	public void setKilledBrothers(boolean[] killedBrothers) {
		this.killedBrothers = killedBrothers;
	}

	public void setKilledBrother(int index, boolean state) {
		this.killedBrothers[index] = state;
	}

	public int getBarrowsChestsLooted() {
		return barrowsChestsLooted;
	}

	public void setBarrowsChestsLooted(int chestsLooted) {
		this.barrowsChestsLooted = chestsLooted;
	}

	public boolean isPlaceholders() {
		return placeholders;
	}

	public void setPlaceholders(boolean placeholders) {
		this.placeholders = placeholders;
	}

	public Presetable[] getPresets() {
		return presets;
	}

	public void setPresets(Presetable[] sets) {
		this.presets = sets;
	}

	public boolean isOpenPresetsOnDeath() {
		return openPresetsOnDeath;
	}

	public void setOpenPresetsOnDeath(boolean openPresetsOnDeath) {
		this.openPresetsOnDeath = openPresetsOnDeath;
	}

	public Presetable getCurrentPreset() {
		return currentPreset;
	}

	public void setCurrentPreset(Presetable currentPreset) {
		this.currentPreset = currentPreset;
	}

	public Queue<ChatMessage> getChatMessageQueue() {
		return chatMessageQueue;
	}

	public ChatMessage getCurrentChatMessage() {
		return currentChatMessage;
	}

	public void setCurrentChatMessage(ChatMessage currentChatMessage) {
		this.currentChatMessage = currentChatMessage;
	}

	public Map<TeleportButton, Location> getPreviousTeleports() {
		return previousTeleports;
	}

	public boolean isTeleportInterfaceOpen() {
		return teleportInterfaceOpen;
	}

	public void setTeleportInterfaceOpen(boolean teleportInterfaceOpen) {
		this.teleportInterfaceOpen = teleportInterfaceOpen;
	}

	@Override
	public PendingHit manipulateHit(PendingHit hit) {
		Mobile attacker = hit.getAttacker();
		
		if (attacker.isNpc()) {
			NPC npc = attacker.getAsNpc();
			if (npc.getId() == NpcIdentifiers.TZTOK_JAD) {
				if (PrayerHandler.isActivated(this, PrayerHandler.getProtectingPrayer(hit.getCombatType()))) {
					hit.setTotalDamage(0);
				}
			}
		}
		
		return hit;
	}

	public Location getOldPosition() {
		return oldPosition;
	}

	public void setOldPosition(Location oldPosition) {
		this.oldPosition = oldPosition;
	}

	public int[] getGodwarsKillcount() {
		return godwarsKillcount;
	}

	public void setGodwarsKillcount(int[] godwarsKillcount) {
		this.godwarsKillcount = godwarsKillcount;
	}
	
	public void setGodwarsKillcount(int index, int value) {
		this.godwarsKillcount[index] = value;
	}

    public EnteredAmountAction getEnteredAmountAction() {
        return enteredAmountAction;
    }

    public void setEnteredAmountAction(EnteredAmountAction enteredAmountAction) {
        this.enteredAmountAction = enteredAmountAction;
    }

    public EnteredSyntaxAction getEnteredSyntaxAction() {
        return enteredSyntaxAction;
    }

    public void setEnteredSyntaxAction(EnteredSyntaxAction enteredSyntaxAction) {
        this.enteredSyntaxAction = enteredSyntaxAction;
    }

    public ActiveSlayerTask getSlayerTask() {
        return slayerTask;
    }

    public void setSlayerTask(ActiveSlayerTask slayerTask) {
        this.slayerTask = slayerTask;
    }

    public int getConsecutiveTasks() {
        return consecutiveTasks;
    }

    public void setConsecutiveTasks(int consecutiveTasks) {
        this.consecutiveTasks = consecutiveTasks;
    }

    public int getSlayerPoints() {
        return slayerPoints;
    }

    public void setSlayerPoints(int slayerPoints) {
        this.slayerPoints = slayerPoints;
    }
    
    public DialogueManager getDialogueManager() {
        return dialogueManager;
    }
    
    public WeaponInterface getWeapon() {
        return weapon;
    }

    public void setWeapon(WeaponInterface weapon) {
        this.weapon = weapon;
    }

    public FightType getFightType() {
        return fightType;
    }

    public void setFightType(FightType fightType) {
        this.fightType = fightType;
    }

    public boolean autoRetaliate() {
        return autoRetaliate;
    }

    public void setAutoRetaliate(boolean autoRetaliate) {
        this.autoRetaliate = autoRetaliate;
    }


	public boolean isDiscordLogin() {
		return isDiscordLogin;
	}

	public void setDiscordLogin(boolean discordLogin) {
		isDiscordLogin = discordLogin;
	}

	public String getCachedDiscordAccessToken() {
		return cachedDiscordAccessToken;
	}

	public void setCachedDiscordAccessToken(String cachedDiscordAccessToken) {
		this.cachedDiscordAccessToken = cachedDiscordAccessToken;
	}

	public Map<Integer, Integer> getQuestProgress() {
		return this.questProgress;
	}

	public int getQuestPoints() {
		return this.questPoints;
	}

	public void setQuestPoints(int questPoints) {
		this.questPoints = questPoints;
	}

	public void setQuestProgress(Map<Integer, Integer> questProgress) {
		if (questProgress == null) {
			return;
		}
		this.questProgress = questProgress;
	}

	public int castlewarsKills, castlewarsDeaths, castlewarsIdleTime;

	public void resetCastlewarsIdleTime() {
		this.castlewarsIdleTime = 200;
	}

	public void climb(boolean down, Location location) {
		this.performAnimation(new Animation(down ? 827 : 828));
		Task task = new Task(1, this.getIndex(), true) {
			int ticks = 0;

			@Override
			protected void execute() {
				ticks++;
				if (ticks == 2) {
					moveTo(location);
					stop();
				}
			}
		};
		TaskManager.submit(task);
	}

	public int currentInterfaceTabId;

	public int getCurrentInterfaceTabId() {
		return currentInterfaceTabId;
	}
    public void setCurrentInterfaceTab(int currentInterfaceTabId) {
		this.currentInterfaceTabId = currentInterfaceTabId;
    }
}
