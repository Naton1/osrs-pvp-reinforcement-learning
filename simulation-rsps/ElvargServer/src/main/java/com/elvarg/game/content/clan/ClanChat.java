package com.elvarg.game.content.clan;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

import com.elvarg.game.World;
import com.elvarg.game.entity.impl.player.Player;

public class ClanChat {

	public static final int RANK_REQUIRED_TO_ENTER = 0, RANK_REQUIRED_TO_KICK = 1, RANK_REQUIRED_TO_TALK = 2;
	private final int index;
	private String name;
	private Player owner;
	private String ownerName;
	private boolean lootShare;
	private ClanChatRank[] rankRequirement = new ClanChatRank[3];
	private List<Player> members = new CopyOnWriteArrayList<>();
	private List<BannedMember> bannedMembers = new ArrayList<>();
	private Map<String, ClanChatRank> rankedNames = new HashMap<>();

	public ClanChat(Player owner, String name, int index) {
		this.owner = owner;
		this.name = name;
		this.index = index;
		this.ownerName = owner.getUsername();
	}

	public ClanChat(String ownerName, String name, int index) {
		Optional<Player> o = World.getPlayerByName(ownerName);
		this.owner = o.isPresent() ? o.get() : null;
		this.ownerName = ownerName;
		this.name = name;
		this.index = index;
	}

	public Player getOwner() {
		return owner;
	}

	public ClanChat setOwner(Player owner) {
		this.owner = owner;
		return this;
	}

	public String getOwnerName() {
		return ownerName;
	}

	public int getIndex() {
		return index;
	}

	public String getName() {
		return name;
	}

	public ClanChat setName(String name) {
		this.name = name;
		return this;
	}

	public boolean getLootShare() {
		return lootShare;
	}

	public void setLootShare(boolean lootShare) {
		this.lootShare = lootShare;
	}

	public ClanChat addMember(Player member) {
		members.add(member);
		return this;
	}

	public ClanChat removeMember(String name) {
		for (int i = 0; i < members.size(); i++) {
			Player member = members.get(i);
			if (member == null)
				continue;
			if (member.getUsername().equals(name)) {
				members.remove(i);
				break;
			}
		}
		return this;
	}

	public ClanChatRank getRank(Player player) {
		return getRank(player.getUsername());
	}

	public ClanChat giveRank(Player player, ClanChatRank rank) {
		return giveRank(player.getUsername(), rank);
	}

	public ClanChatRank getRank(String player) {
		return rankedNames.get(player);
	}

	public ClanChat giveRank(String player, ClanChatRank rank) {
		rankedNames.put(player, rank);
		return this;
	}

	public List<Player> getMembers() {
		return members;
	}

	public Map<String, ClanChatRank> getRankedNames() {
		return rankedNames;
	}

	public List<BannedMember> getBannedNames() {
		return bannedMembers;
	}

	public void addBannedName(String name) {
		bannedMembers.add(new BannedMember(name, 1800));
	}

	public boolean isBanned(String name) {
		Iterator<BannedMember> it = bannedMembers.iterator();
		while (it.hasNext()) {
			BannedMember b = it.next();
			if (b == null || b.getTimer().finished()) {
				it.remove();
				continue;
			}
			if (b.getName().equals(name)) {
				return true;
			}
		}
		return false;
	}

	public ClanChatRank[] getRankRequirement() {
		return rankRequirement;
	}

	public ClanChat setRankRequirements(int index, ClanChatRank rankRequirement) {
		this.rankRequirement[index] = rankRequirement;
		return this;
	}
}