package com.elvarg.game.entity.impl.player.persistence;

import com.elvarg.game.content.PrayerHandler;
import com.elvarg.game.content.combat.FightType;
import com.elvarg.game.content.presets.Presetable;
import com.elvarg.game.content.skill.SkillManager;
import com.elvarg.game.content.skill.skillable.impl.Runecrafting;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.Item;
import com.elvarg.game.model.Location;
import com.elvarg.game.model.MagicSpellbook;
import com.elvarg.game.model.SkullType;
import com.elvarg.game.model.container.impl.Bank;
import com.elvarg.game.model.rights.DonatorRights;
import com.elvarg.game.model.rights.PlayerRights;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlayerSave {
    private String passwordHashWithSalt;
    private boolean isDiscordLogin;
    private String cachedDiscordAccessToken;
    private String title;
    private PlayerRights rights;
    private DonatorRights donatorRights;
    private Location position;
    private MagicSpellbook spellBook;
    private FightType fightType;
    private boolean autoRetaliate;
    private boolean xpLocked;
    private String clanChat;
    private boolean targetTeleportUnlocked;
    private boolean preserveUnlocked;
    private boolean rigourUnlocked;
    private boolean auguryUnlocked;
    private boolean hasVengeance;
    private int lastVengeanceTimer;
    private int specPercentage;
    private int recoilDamage;
    private int poisonDamage;
    private int blowpipeScales;
    private int barrowsCrypt;
    private int barrowsChests;
    private boolean[] killedBrothers;
    private int[] gwdKills;
    private int poisonImmunityTimer;
    private int fireImmunityTimer;
    private int teleblockTimer;
    private int targetSearchTimer;
    private int specialAttackRestoreTimer;
    private int skullTimer;
    private SkullType skullType;
    private boolean running;
    private int runEnergy;
    private int totalKills;
    private int targetKills;
    private int normalKills;
    private int killstreak;
    private int highestKillstreak;
    private List<String> recentKills;
    private int deaths;
    private int points;
    private Runecrafting.PouchContainer[] pouches;
    private Item[] inventory;
    private Item[] equipment;
    private int[] appearance;
    private SkillManager.Skills skills;
    private PrayerHandler.PrayerData[] quickPrayers;
    private List<Long> friends;
    private List<Long> ignores;
    private Map<Integer, List<Item>> banks;
    private Presetable[] presets;
    private int questPoints;
    private Map<Integer, Integer> questProgress;

    public String getPasswordHashWithSalt() {
        return passwordHashWithSalt;
    }

    public void setPasswordHashWithSalt(String passwordHashWithSalt) {
        this.passwordHashWithSalt = passwordHashWithSalt;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public PlayerRights getRights() {
        return rights;
    }

    public void setRights(PlayerRights rights) {
        this.rights = rights;
    }

    public DonatorRights getDonatorRights() {
        return donatorRights;
    }

    public void setDonatorRights(DonatorRights donatorRights) {
        this.donatorRights = donatorRights;
    }

    public Location getPosition() {
        return position;
    }

    public void setPosition(Location position) {
        this.position = position;
    }

    public MagicSpellbook getSpellBook() {
        return spellBook;
    }

    public void setSpellBook(MagicSpellbook spellBook) {
        this.spellBook = spellBook;
    }

    public FightType getFightType() {
        return fightType;
    }

    public void setFightType(FightType fightType) {
        this.fightType = fightType;
    }

    public boolean isAutoRetaliate() {
        return autoRetaliate;
    }

    public void setAutoRetaliate(boolean autoRetaliate) {
        this.autoRetaliate = autoRetaliate;
    }

    public boolean isXpLocked() {
        return xpLocked;
    }

    public void setXpLocked(boolean xpLocked) {
        this.xpLocked = xpLocked;
    }

    public String getClanChat() {
        return clanChat;
    }

    public void setClanChat(String clanChat) {
        this.clanChat = clanChat;
    }

    public boolean isTargetTeleportUnlocked() {
        return targetTeleportUnlocked;
    }

    public void setTargetTeleportUnlocked(boolean targetTeleportUnlocked) {
        this.targetTeleportUnlocked = targetTeleportUnlocked;
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

    public boolean isHasVengeance() {
        return hasVengeance;
    }

    public void setHasVengeance(boolean hasVengeance) {
        this.hasVengeance = hasVengeance;
    }

    public int getLastVengeanceTimer() {
        return lastVengeanceTimer;
    }

    public void setLastVengeanceTimer(int lastVengeanceTimer) {
        this.lastVengeanceTimer = lastVengeanceTimer;
    }

    public int getSpecPercentage() {
        return specPercentage;
    }

    public void setSpecPercentage(int specPercentage) {
        this.specPercentage = specPercentage;
    }

    public int getRecoilDamage() {
        return recoilDamage;
    }

    public void setRecoilDamage(int recoilDamage) {
        this.recoilDamage = recoilDamage;
    }

    public int getPoisonDamage() {
        return poisonDamage;
    }

    public void setPoisonDamage(int poisonDamage) {
        this.poisonDamage = poisonDamage;
    }

    public int getBlowpipeScales() {
        return blowpipeScales;
    }

    public void setBlowpipeScales(int blowpipeScales) {
        this.blowpipeScales = blowpipeScales;
    }

    public int getBarrowsCrypt() {
        return barrowsCrypt;
    }

    public void setBarrowsCrypt(int barrowsCrypt) {
        this.barrowsCrypt = barrowsCrypt;
    }

    public int getBarrowsChests() {
        return barrowsChests;
    }

    public void setBarrowsChests(int barrowsChests) {
        this.barrowsChests = barrowsChests;
    }

    public boolean[] getKilledBrothers() {
        return killedBrothers;
    }

    public void setKilledBrothers(boolean[] killedBrothers) {
        this.killedBrothers = killedBrothers;
    }

    public int[] getGwdKills() {
        return gwdKills;
    }

    public void setGwdKills(int[] gwdKills) {
        this.gwdKills = gwdKills;
    }

    public int getPoisonImmunityTimer() {
        return poisonImmunityTimer;
    }

    public void setPoisonImmunityTimer(int poisonImmunityTimer) {
        this.poisonImmunityTimer = poisonImmunityTimer;
    }

    public int getFireImmunityTimer() {
        return fireImmunityTimer;
    }

    public void setFireImmunityTimer(int fireImmunityTimer) {
        this.fireImmunityTimer = fireImmunityTimer;
    }

    public int getTeleblockTimer() {
        return teleblockTimer;
    }

    public void setTeleblockTimer(int teleblockTimer) {
        this.teleblockTimer = teleblockTimer;
    }

    public int getTargetSearchTimer() {
        return targetSearchTimer;
    }

    public void setTargetSearchTimer(int targetSearchTimer) {
        this.targetSearchTimer = targetSearchTimer;
    }

    public int getSpecialAttackRestoreTimer() {
        return specialAttackRestoreTimer;
    }

    public void setSpecialAttackRestoreTimer(int specialAttackRestoreTimer) {
        this.specialAttackRestoreTimer = specialAttackRestoreTimer;
    }

    public int getSkullTimer() {
        return skullTimer;
    }

    public void setSkullTimer(int skullTimer) {
        this.skullTimer = skullTimer;
    }

    public SkullType getSkullType() {
        return skullType;
    }

    public void setSkullType(SkullType skullType) {
        this.skullType = skullType;
    }

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    public int getRunEnergy() {
        return runEnergy;
    }

    public void setRunEnergy(int runEnergy) {
        this.runEnergy = runEnergy;
    }

    public int getTotalKills() {
        return totalKills;
    }

    public void setTotalKills(int totalKills) {
        this.totalKills = totalKills;
    }

    public int getTargetKills() {
        return targetKills;
    }

    public void setTargetKills(int targetKills) {
        this.targetKills = targetKills;
    }

    public int getNormalKills() {
        return normalKills;
    }

    public void setNormalKills(int normalKills) {
        this.normalKills = normalKills;
    }

    public int getKillstreak() {
        return killstreak;
    }

    public void setKillstreak(int killstreak) {
        this.killstreak = killstreak;
    }

    public int getHighestKillstreak() {
        return highestKillstreak;
    }

    public void setHighestKillstreak(int highestKillstreak) {
        this.highestKillstreak = highestKillstreak;
    }

    public List<String> getRecentKills() {
        return recentKills;
    }

    public void setRecentKills(List<String> recentKills) {
        this.recentKills = recentKills;
    }

    public int getDeaths() {
        return deaths;
    }

    public void setDeaths(int deaths) {
        this.deaths = deaths;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public Runecrafting.PouchContainer[] getPouches() {
        return pouches;
    }

    public void setPouches(Runecrafting.PouchContainer[] pouches) {
        this.pouches = pouches;
    }

    public Item[] getInventory() {
        return inventory;
    }

    public void setInventory(Item[] inventory) {
        this.inventory = inventory;
    }

    public Item[] getEquipment() {
        return equipment;
    }

    public void setEquipment(Item[] equipment) {
        this.equipment = equipment;
    }

    public int[] getAppearance() {
        return appearance;
    }

    public void setAppearance(int[] appearance) {
        this.appearance = appearance;
    }

    public SkillManager.Skills getSkills() {
        return skills;
    }

    public void setSkills(SkillManager.Skills skills) {
        this.skills = skills;
    }

    public PrayerHandler.PrayerData[] getQuickPrayers() {
        return quickPrayers;
    }

    public void setQuickPrayers(PrayerHandler.PrayerData[] quickPrayers) {
        this.quickPrayers = quickPrayers;
    }

    public List<Long> getFriends() {
        return friends;
    }

    public void setFriends(List<Long> friends) {
        this.friends = friends;
    }

    public List<Long> getIgnores() {
        return ignores;
    }

    public void setIgnores(List<Long> ignores) {
        this.ignores = ignores;
    }

    public Map<Integer, List<Item>> getBanks() {
        return banks;
    }

    public void setBanks(Map<Integer, List<Item>> banks) {
        this.banks = banks;
    }

    public Presetable[] getPresets() {
        return presets;
    }

    public void setPresets(Presetable[] presets) {
        this.presets = presets;
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

    public int getQuestPoints() {
        return questPoints;
    }

    public void setQuestPoints(int questPoints) {
        this.questPoints = questPoints;
    }

    public Map<Integer, Integer> getQuestProgress() {
        return questProgress;
    }

    public void setQuestProgress(Map<Integer, Integer> questProgress) {
        this.questProgress = questProgress;
    }

    public void applyToPlayer(Player player) {
        player.setPasswordHashWithSalt(this.passwordHashWithSalt);
        player.setDiscordLogin(this.isDiscordLogin);
        player.setCachedDiscordAccessToken(this.cachedDiscordAccessToken);
        player.setLoyaltyTitle(this.title);

        player.setLoyaltyTitle(this.title);
        player.setRights(this.rights);
        player.setDonatorRights(this.donatorRights);
        player.setLocation(this.position);
        player.setSpellbook(this.spellBook);
        player.setFightType(this.fightType);
        player.setAutoRetaliate(this.autoRetaliate);
        player.setExperienceLocked(this.xpLocked);
        player.setClanChatName(this.clanChat);
        player.setTargetTeleportUnlocked(this.targetTeleportUnlocked);
        player.setPreserveUnlocked(this.preserveUnlocked);
        player.setRigourUnlocked(this.rigourUnlocked);
        player.setAuguryUnlocked(this.auguryUnlocked);
        player.setHasVengeance(this.hasVengeance);
        player.getVengeanceTimer().start(this.lastVengeanceTimer);
        player.setRunning(this.running);
        player.setRunEnergy(this.runEnergy);
        player.setSpecialPercentage(this.specPercentage);
        player.setRecoilDamage(this.recoilDamage);
        player.setPoisonDamage(this.poisonDamage);

        player.getCombat().getPoisonImmunityTimer().start(this.poisonImmunityTimer);
        player.getCombat().getFireImmunityTimer().start(this.fireImmunityTimer);
        player.getCombat().getTeleBlockTimer().start(this.teleblockTimer);
        player.getTargetSearchTimer().start(this.targetSearchTimer);
        player.getSpecialAttackRestore().start(this.specialAttackRestoreTimer);

        player.setSkullTimer(this.skullTimer);
        player.setSkullType(this.skullType);

        player.setTotalKills(this.totalKills);
        player.setTargetKills(this.targetKills);
        player.setNormalKills(this.normalKills);
        player.setKillstreak(this.killstreak);
        player.setHighestKillstreak(this.highestKillstreak);
        player.setDeaths(this.deaths);
        player.setPoints(this.points);
        player.setPoisonDamage(this.poisonDamage);
        player.setBlowpipeScales(this.blowpipeScales);

        player.setBarrowsCrypt(this.barrowsCrypt);
        player.setBarrowsChestsLooted(this.barrowsChests);
        player.setKilledBrothers(this.killedBrothers);

        player.setGodwarsKillcount(this.gwdKills);

        // RC pouches
        player.setPouches(this.pouches);

        player.getInventory().setItems(this.inventory);
        player.getEquipment().setItems(this.equipment);
        player.getAppearance().set(this.appearance);
        player.getSkillManager().setSkills(this.skills);
        player.getQuickPrayers().setPrayers(this.quickPrayers);
        player.setQuestPoints(this.questPoints);
        player.setQuestProgress(this.questProgress);

        if (this.presets != null) {
            player.setPresets(this.presets);
        }

        for (long l : this.friends) {
            player.getRelations().getFriendList().add(l);
        }

        for (long l : this.ignores) {
            player.getRelations().getIgnoreList().add(l);
        }

        for (int i = 0; i < player.getBanks().length; i++) {
            if (i == Bank.BANK_SEARCH_TAB_INDEX) {
                continue;
            }
            var bankItems = this.banks.get(i);
            if (bankItems != null) {
                player.setBank(i, new Bank(player)).getBank(i).addItems(bankItems, false);
            }
        }
    }

    public static PlayerSave fromPlayer(Player player) {
        var playerSave = new PlayerSave();

        playerSave.passwordHashWithSalt = player.getPasswordHashWithSalt().trim();
        playerSave.isDiscordLogin = player.isDiscordLogin();
        playerSave.cachedDiscordAccessToken = player.getCachedDiscordAccessToken();
        playerSave.title = player.getLoyaltyTitle();
        playerSave.rights = player.getRights();
        playerSave.donatorRights = player.getDonatorRights();
        playerSave.position = player.getLocation();
        playerSave.spellBook = player.getSpellbook();
        playerSave.fightType = player.getFightType();
        playerSave.autoRetaliate = player.autoRetaliate();
        playerSave.xpLocked = player.experienceLocked();
        playerSave.clanChat = player.getClanChatName();
        playerSave.targetTeleportUnlocked = player.isTargetTeleportUnlocked();
        playerSave.preserveUnlocked = player.isPreserveUnlocked();
        playerSave.rigourUnlocked = player.isRigourUnlocked();
        playerSave.auguryUnlocked = player.isAuguryUnlocked();
        playerSave.hasVengeance = player.hasVengeance();
        playerSave.lastVengeanceTimer = player.getVengeanceTimer().secondsRemaining();
        playerSave.running = player.isRunning();
        playerSave.runEnergy = player.getRunEnergy();
        playerSave.specPercentage = player.getSpecialPercentage();
        playerSave.recoilDamage = player.getRecoilDamage();
        playerSave.poisonDamage = player.getPoisonDamage();

        playerSave.poisonImmunityTimer = player.getCombat().getPoisonImmunityTimer().secondsRemaining();
        playerSave.fireImmunityTimer = player.getCombat().getFireImmunityTimer().secondsRemaining();
        playerSave.teleblockTimer = player.getCombat().getTeleBlockTimer().secondsRemaining();
        playerSave.targetSearchTimer = player.getTargetSearchTimer().secondsRemaining();
        playerSave.specialAttackRestoreTimer = player.getSpecialAttackRestore().secondsRemaining();

        playerSave.skullTimer = player.getSkullTimer();
        playerSave.skullType = player.getSkullType();

        playerSave.totalKills = player.getTotalKills();
        playerSave.targetKills = player.getTargetKills();
        playerSave.normalKills = player.getNormalKills();
        playerSave.killstreak = player.getKillstreak();
        playerSave.highestKillstreak = player.getHighestKillstreak();
        playerSave.recentKills = player.getRecentKills();
        playerSave.deaths = player.getDeaths();
        playerSave.points = player.getPoints();
        playerSave.poisonDamage = player.getPoisonDamage();
        playerSave.blowpipeScales = player.getBlowpipeScales();

        playerSave.barrowsCrypt = player.getBarrowsCrypt();
        playerSave.barrowsChests = player.getBarrowsChestsLooted();
        playerSave.killedBrothers = player.getKilledBrothers();

        playerSave.gwdKills = player.getGodwarsKillcount();

        // RC pouches
        playerSave.pouches = player.getPouches();

        playerSave.inventory = player.getInventory().getItems();
        playerSave.equipment = player.getEquipment().getItems();
        playerSave.appearance = player.getAppearance().getLook();
        playerSave.skills = player.getSkillManager().getSkills();
        playerSave.quickPrayers = player.getQuickPrayers().getPrayers();
        playerSave.questPoints = player.getQuestPoints();
        playerSave.questProgress = player.getQuestProgress();

        playerSave.friends = player.getRelations().getFriendList();
        playerSave.ignores = player.getRelations().getIgnoreList();

        playerSave.presets = player.getPresets();

        var banks = new HashMap<Integer, List<Item>>();

        /** BANK **/
        for (int i = 0; i < player.getBanks().length; i++) {
            if (i == Bank.BANK_SEARCH_TAB_INDEX) {
                continue;
            }
            if (player.getBank(i) != null) {
                banks.put(i, player.getBank(i).getValidItems());
            }
        }
        playerSave.banks = banks;

        return playerSave;
    }
}
