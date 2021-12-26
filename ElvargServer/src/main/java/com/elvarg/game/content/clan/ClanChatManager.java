package com.elvarg.game.content.clan;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map.Entry;
import java.util.Optional;

import com.elvarg.game.GameLogic;
import com.elvarg.game.World;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.entity.impl.player.PlayerSaving;
import com.elvarg.game.model.rights.DonatorRights;
import com.elvarg.game.model.rights.PlayerRights;
import com.elvarg.util.Misc;
import com.elvarg.util.PlayerPunishment;

public class ClanChatManager {

	public static final int CLAN_CHAT_SETUP_INTERFACE_ID = 38300;
	private static final String FILE_DIRECTORY = "./data/saves/clans/";
	private static ClanChat[] clans = new ClanChat[3000];

	public static void init() {
		try {
			File dir = (new File(FILE_DIRECTORY));
			if (!dir.exists()) {
				dir.mkdir();
			}
			for (File file : dir.listFiles()) {
				if (!file.exists())
					continue;
				DataInputStream input = new DataInputStream(new FileInputStream(file));
				String name = input.readUTF();
				String owner = input.readUTF();
				int index = input.readShort();
				ClanChat clan = new ClanChat(owner, name, index);
				clan.setRankRequirements(ClanChat.RANK_REQUIRED_TO_ENTER, ClanChatRank.forId(input.read()));
				clan.setRankRequirements(ClanChat.RANK_REQUIRED_TO_KICK, ClanChatRank.forId(input.read()));
				clan.setRankRequirements(ClanChat.RANK_REQUIRED_TO_TALK, ClanChatRank.forId(input.read()));
				int totalRanks = input.readShort();
				for (int i = 0; i < totalRanks; i++) {
					clan.getRankedNames().put(input.readUTF(), ClanChatRank.forId(input.read()));
				}
				int totalBans = input.readShort();
				for (int i = 0; i < totalBans; i++) {
					clan.addBannedName(input.readUTF());
				}
				clans[index] = clan;
				input.close();
			}
		} catch (IOException exception) {
			exception.printStackTrace();
		}
	}

	public static void writeFile(ClanChat clan) {
		GameLogic.submit(new Runnable() {
			@Override
			public void run() {
				try {
					File file = new File(FILE_DIRECTORY + clan.getName());
					if (file.exists())
						file.createNewFile();
					DataOutputStream output = new DataOutputStream(new FileOutputStream(file));
					output.writeUTF(clan.getName());
					output.writeUTF(clan.getOwnerName());
					output.writeShort(clan.getIndex());
					output.write(clan.getRankRequirement()[ClanChat.RANK_REQUIRED_TO_ENTER] != null
							? clan.getRankRequirement()[ClanChat.RANK_REQUIRED_TO_ENTER].ordinal()
							: -1);
					output.write(clan.getRankRequirement()[ClanChat.RANK_REQUIRED_TO_KICK] != null
							? clan.getRankRequirement()[ClanChat.RANK_REQUIRED_TO_KICK].ordinal()
							: -1);
					output.write(clan.getRankRequirement()[ClanChat.RANK_REQUIRED_TO_TALK] != null
							? clan.getRankRequirement()[ClanChat.RANK_REQUIRED_TO_TALK].ordinal()
							: -1);
					output.writeShort(clan.getRankedNames().size());
					for (Entry<String, ClanChatRank> iterator : clan.getRankedNames().entrySet()) {
						String name = iterator.getKey();
						int rank = iterator.getValue().ordinal();
						output.writeUTF(name);
						output.write(rank);
					}
					output.writeShort(clan.getBannedNames().size());
					for (BannedMember ban : clan.getBannedNames()) {
						output.writeUTF(ban.getName());
					}
					output.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
	}

	public static void save() {
		for (ClanChat clan : clans) {
			if (clan != null) {
				writeFile(clan);
			}
		}
	}

	public static ClanChat create(Player player, String name) {
		int index = getIndex();
		if (index == -1) { // Too many clans
			player.getPacketSender().sendMessage("An error occured! Please contact an administrator and report this.");
			return null;
		}
		clans[index] = new ClanChat(player, name, index);
		clans[index].getRankedNames().put(player.getUsername(), ClanChatRank.OWNER);
		clans[index].setRankRequirements(ClanChat.RANK_REQUIRED_TO_KICK, ClanChatRank.OWNER);
		return clans[index];
	}

	public static void join(Player player, String channel) {
		if (channel == null || channel.equals("") || channel.equals("null")) {
			return;
		}
		if (player.getCurrentClanChat() != null) {
			player.getPacketSender().sendMessage("You are already in a clan channel.");
			return;
		}
		channel = channel.toLowerCase();
		for (ClanChat clan : clans) {
			if (clan == null) {
				continue;
			}
			if (clan.getName().toLowerCase().equals(channel)) {
				join(player, clan);
				return;
			}
		}

		player.getPacketSender().sendMessage("That channel does not exist.");
	}

	public static void updateList(ClanChat clan) {
		Collections.sort(clan.getMembers(), new Comparator<Player>() {
			@Override
			public int compare(Player o1, Player o2) {
				ClanChatRank rank1 = clan.getRank(o1);
				ClanChatRank rank2 = clan.getRank(o2);
				if (rank1 == null && rank2 == null) {
					return 1;
				}
				if (rank1 == null && rank2 != null) {
					return 1;
				} else if (rank1 != null && rank2 == null) {
					return -1;
				}
				if (rank1.ordinal() == rank2.ordinal()) {
					return 1;
				}
				if (rank1 == ClanChatRank.OWNER) {
					return -1;
				} else if (rank2 == ClanChatRank.OWNER) {
					return 1;
				}
				if (rank1.ordinal() > rank2.ordinal()) {
					return -1;
				}
				return 1;
			}
		});
		for (Player member : clan.getMembers()) {
			if (member != null) {
				int childId = 37144;
				for (Player others : clan.getMembers()) {
					if (others != null) {
						ClanChatRank rank = clan.getRank(others);

						int image = -1;
						if (rank != null) {
							image = rank.getSpriteId();
						}

						String prefix = image != -1 ? ("<img=" + (image) + ">") : "";
						member.getPacketSender().sendString(childId, prefix + others.getUsername());
						childId++;
					}
				}
				member.getPacketSender().clearInterfaceText(childId, 37243);
				ClanChatRank rank = clan.getRank(member);

				if (rank != null) {
					if (rank == ClanChatRank.OWNER || rank == ClanChatRank.STAFF
							|| clan.getRankRequirement()[ClanChat.RANK_REQUIRED_TO_KICK] == null
							|| rank.ordinal() >= clan.getRankRequirement()[ClanChat.RANK_REQUIRED_TO_KICK].ordinal()) {
						member.getPacketSender().sendShowClanChatOptions(true);
					} else {
						member.getPacketSender().sendShowClanChatOptions(false);
					}
				} else {
					if (clan.getRankRequirement()[ClanChat.RANK_REQUIRED_TO_KICK] == null) {
						member.getPacketSender().sendShowClanChatOptions(true);
					} else {
						member.getPacketSender().sendShowClanChatOptions(false);
					}
				}

			}
		}
	}

	public static void sendMessage(Player player, String message) {
		if (PlayerPunishment.muted(player.getUsername()) || PlayerPunishment.IPMuted(player.getHostAddress())) {
			player.getPacketSender().sendMessage("You are muted and cannot chat.");
			return;
		}
		ClanChat clan = player.getCurrentClanChat();
		if (clan == null) {
			player.getPacketSender().sendMessage("You're not in a clanchat channel.");
			return;
		}
		ClanChatRank rank = clan.getRank(player);
		if (clan.getRankRequirement()[ClanChat.RANK_REQUIRED_TO_TALK] != null) {
			if (rank == null || rank.ordinal() < clan.getRankRequirement()[ClanChat.RANK_REQUIRED_TO_TALK].ordinal()) {
				player.getPacketSender().sendMessage("You do not have the required rank to speak in this channel.");
				return;
			}
		}
		String bracketColor = "<col=16777215>";
		String clanNameColor = "<col=255>";
		String nameColor = "@bla@";
		String chatColor = "<col=993D00>";

		String clanPrefix = "" + bracketColor + "[" + clanNameColor + clan.getName() + bracketColor + "]";
		
		String rightsPrefix = "";
		if (player.getRights() != PlayerRights.NONE) {
			rightsPrefix = "<img=" + player.getRights().getSpriteId() + ">";
		} else if (player.getDonatorRights() != DonatorRights.NONE) {
			rightsPrefix = "<img=" + player.getDonatorRights().getSpriteId() + ">";
		}
		
		for (Player memberPlayer : clan.getMembers()) {
			if (memberPlayer != null) {
				if (memberPlayer.getRelations().getIgnoreList().contains(player.getLongUsername()))
					continue;

			memberPlayer.getPacketSender().sendSpecialMessage(player.getUsername(), 16, (clanPrefix + nameColor
					+ rightsPrefix + " " + Misc.capitalizeWords(player.getUsername()) + ": " + chatColor + Misc.capitalize(message)));
			}
		}
	}

	public static void sendMessage(ClanChat clan, String message) {
		for (Player member : clan.getMembers()) {
			if (member != null) {
				member.getPacketSender().sendMessage(message);
			}
		}
	}

	public static void leave(Player player, boolean kicked) {
		final ClanChat clan = player.getCurrentClanChat();
		if (clan == null) {
			return;
		}
		resetInterface(player);
		player.setCurrentClanChat(null);
		clan.removeMember(player.getUsername());
		player.getPacketSender().sendShowClanChatOptions(false);
		updateList(clan);
		if (kicked) {
			player.setClanChatName("");
		}
		player.getPacketSender()
				.sendMessage(kicked ? "You have been kicked from the channel." : "You have left the channel.");
	}

	private static void join(Player player, ClanChat clan) {
		if (clan.getOwnerName().equals(player.getUsername())) {
			if (clan.getOwner() == null) {
				clan.setOwner(player);
			}
			clan.giveRank(player, ClanChatRank.OWNER);
		}
		player.getPacketSender().sendMessage("Attempting to join channel...");
		if (clan.getMembers().size() >= 100) {
			player.getPacketSender().sendMessage("This clan channel is currently full.");
			return;
		}
		if (clan.isBanned(player.getUsername())) {
			player.getPacketSender()
					.sendMessage("You're currently banned from using this channel. Bans expire after 30 minutes.");
			return;
		}

		// updateRank(clan, player);

		ClanChatRank rank = clan.getRank(player);
		if (clan.getRankRequirement()[ClanChat.RANK_REQUIRED_TO_ENTER] != null) {
			if (rank == null || rank.ordinal() < clan.getRankRequirement()[ClanChat.RANK_REQUIRED_TO_ENTER].ordinal()) {
				player.getPacketSender().sendMessage("Your rank is not high enough to enter this channel.");
				return;
			}
		}

		player.setCurrentClanChat(clan);
		player.setClanChatName(clan.getName());
		String clanName = Misc.capitalizeWords(clan.getName());
		clan.addMember(player);
		player.getPacketSender().sendString(37139, "Talking in: @whi@" + clanName);
		player.getPacketSender().sendString(37140, "Owner: " + Misc.capitalizeWords(clan.getOwnerName()));
		player.getPacketSender().sendString(37135, "Leave Chat");

		// player.getPacketSender().sendString(29454, "Lootshare:
		// "+getLootshareStatus(clan));
		player.getPacketSender().sendMessage("Now talking in " + clan.getOwnerName() + "'s channel.");
		player.getPacketSender().sendMessage("To talk start each line of chat with the / symbol.");
		updateList(clan);
	}

	public static void delete(Player player) {
		ClanChat clan = getClanChat(player);
		if (getClanChat(player) == null) {
			player.getPacketSender().sendMessage("Your clanchat channel is already disabled.");
			return;
		}
		File file = new File(FILE_DIRECTORY + clan.getName());
		for (Player member : clan.getMembers()) {
			if (member != null) {
				leave(member, false);
			}
		}
		if (player.getClanChatName() != null && player.getClanChatName().equalsIgnoreCase(clan.getName())) {
			player.setClanChatName("");
		}
		clans[clan.getIndex()] = null;
		file.delete();
		if (player.getInterfaceId() == CLAN_CHAT_SETUP_INTERFACE_ID) {
			clanChatSetupInterface(player);
		}
	}

    public static void updateRank(ClanChat clan, Player player2) {
        if (clan == null || player2 == null) {
            return;
        }
        ClanChatRank rank = clan.getRank(player2);
        Player owner = clan.getOwner();
        if (owner != null) {
            if (owner.getRelations().isFriendWith(player2.getUsername())) {
                if (rank == null) {
                    clan.giveRank(player2, ClanChatRank.FRIEND);
                    updateList(clan);
                }
            } else {
                if (rank == ClanChatRank.FRIEND) {
                    clan.giveRank(player2, null);
                    updateList(clan);
                }
            }
        }
        if (player2.isStaff()) {
            if (rank == null) {
                clan.giveRank(player2, ClanChatRank.STAFF);
                updateList(clan);
            }
        } else {
            if (rank == ClanChatRank.STAFF) {
                clan.giveRank(player2, null);
                updateList(clan);
            }
        }
    }

	public static void setName(Player player, String newName) {
		if (PlayerSaving.playerExists(newName)) {
			player.getPacketSender().sendMessage("That clanchat name is already taken.");
			return;
		}

		newName = newName.toLowerCase();

		for (ClanChat c : clans) {
			if (c == null)
				continue;
			if (c.getName().toLowerCase().equals(newName)) {
				player.getPacketSender().sendMessage("That clanchat name is already taken.");
				return;
			}
		}

		ClanChat clan = getClanChat(player);
		if (clan == null) {
			clan = create(player, newName);
		}
		if (clan == null) {
			return;
		}

		if (clan.getName().toLowerCase().equals(newName)) {
			return;
		}

		new File(FILE_DIRECTORY + clan.getName()).delete();
		clan.setName(Misc.capitalizeWords(newName));
		for (Player member : clan.getMembers()) {
			if (member == null)
				continue;
			member.setClanChatName(clan.getName());
			member.getPacketSender().sendString(37139, "Talking in: @whi@" + clan.getName());
		}
		writeFile(clan);
		if (player.getCurrentClanChat() == null) {
			join(player, clan);
		}
		if (player.getInterfaceId() == CLAN_CHAT_SETUP_INTERFACE_ID) {
			clanChatSetupInterface(player);
		}
	}

	public static void kick(Player player, Player target) {
		ClanChat clan = player.getCurrentClanChat();
		if (clan == null) {
			player.getPacketSender().sendMessage("You're not in a clan channel.");
			return;
		}
		final ClanChatRank rank = clan.getRank(player);
		if (clan.getRankRequirement()[ClanChat.RANK_REQUIRED_TO_KICK] != null) {
			if (rank == null || rank.ordinal() < clan.getRankRequirement()[ClanChat.RANK_REQUIRED_TO_KICK].ordinal()) {
				player.getPacketSender().sendMessage("You do not have the required rank to kick this player.");
				return;
			}
		}
		for (Player member : clan.getMembers()) {
			if (member != null && member.equals(target)) {
				ClanChatRank memberRank = clan.getRank(member);
				if (memberRank != null) {
					if (memberRank == ClanChatRank.STAFF) {
						player.getPacketSender().sendMessage("That player cannot be kicked.");
						break;
					}
					if (rank == null || rank.ordinal() < memberRank.ordinal()) {
						player.getPacketSender()
								.sendMessage("You cannot kick a player who has a higher rank than you!");
						break;
					}
				}
				clan.addBannedName(member.getUsername());
				leave(member, true);
				sendMessage(player.getCurrentClanChat(),
						"<col=16777215>[<col=255>" + clan.getName() + "<col=16777215>]<col=3300CC> "
								+ member.getUsername() + " has been kicked from the channel by " + player.getUsername()
								+ ".");
				break;
			}
		}
	}

	public static void clanChatSetupInterface(Player player) {
		player.getPacketSender().clearInterfaceText(38752, 39551);

		ClanChat clan = getClanChat(player);

		// Update buttons..
		if (clan == null) {
			player.getPacketSender().sendString(38332, "Clan disabled");
			player.getPacketSender().sendString(38334, "Anyone");
			player.getPacketSender().sendString(38336, "Anyone");
			player.getPacketSender().sendString(38338, "Only me");
		} else {
			player.getPacketSender().sendString(38332, clan.getName());

			if (clan.getRankRequirement()[ClanChat.RANK_REQUIRED_TO_ENTER] == null) {
				player.getPacketSender().sendString(38334, "Anyone");
			} else {
				String rank = clan.getRankRequirement()[ClanChat.RANK_REQUIRED_TO_ENTER] == ClanChatRank.OWNER
						? "Only me"
						: Misc.ucFirst(
								clan.getRankRequirement()[ClanChat.RANK_REQUIRED_TO_ENTER].toString().toLowerCase())
								+ "+";
				player.getPacketSender().sendString(38334, rank);
			}

			if (clan.getRankRequirement()[ClanChat.RANK_REQUIRED_TO_TALK] == null) {
				player.getPacketSender().sendString(38336, "Anyone");
			} else {
				String rank = clan.getRankRequirement()[ClanChat.RANK_REQUIRED_TO_TALK] == ClanChatRank.OWNER
						? "Only me"
						: Misc.ucFirst(
								clan.getRankRequirement()[ClanChat.RANK_REQUIRED_TO_TALK].toString().toLowerCase())
								+ "+";
				player.getPacketSender().sendString(38336, rank);
			}

			if (clan.getRankRequirement()[ClanChat.RANK_REQUIRED_TO_KICK] == null) {
				player.getPacketSender().sendString(38338, "Anyone");
			} else {
				String rank = clan.getRankRequirement()[ClanChat.RANK_REQUIRED_TO_KICK] == ClanChatRank.OWNER
						? "Only me"
						: Misc.ucFirst(
								clan.getRankRequirement()[ClanChat.RANK_REQUIRED_TO_KICK].toString().toLowerCase())
								+ "+";
				player.getPacketSender().sendString(38338, rank);
			}
		}

		// Send friends list and ranks
		int nameInterfaceId = 38752;
		int rankInterfaceId = 38952;
		for (long friend : player.getRelations().getFriendList()) {
			String playerName = Misc.longToString(friend);
			if (playerName == null || playerName.isEmpty())
				continue;
			playerName = Misc.formatPlayerName(playerName);
			ClanChatRank rank = (clan == null ? null : clan.getRank(playerName));
			player.getPacketSender().sendString(nameInterfaceId++, playerName);
			player.getPacketSender().sendString(rankInterfaceId++,
					(rank == null ? "Friend" : Misc.ucFirst(rank.toString().toLowerCase())));
		}

		player.getPacketSender().sendInterface(CLAN_CHAT_SETUP_INTERFACE_ID);
	}

	public static boolean handleButton(Player player, int button, int menuId) {
		if (player.getInterfaceId() == CLAN_CHAT_SETUP_INTERFACE_ID) {
			ClanChat clan = getClanChat(player);
			switch (button) {
			case 38319:
				if (menuId == 0) {
					player.setEnteredSyntaxAction((input) -> {
					    if (input.length() > 12) {
				            input = input.substring(0, 11);
				        }
				        if (!Misc.isValidName(input)) {
				            player.getPacketSender().sendMessage("Invalid syntax entered. Please set a valid name.");
				            return;
				        }
				        ClanChatManager.setName(player, input);
					});
					player.getPacketSender().sendEnterInputPrompt("What should your clanchat channel's name be?");
				} else if (menuId == 1) {
					delete(player);
				}
				return true;
			case 38322:
			case 38325:
			case 38328:
				if (clan == null) {
					player.getPacketSender().sendMessage("Please enable your clanchat before changing this.");
					return true;
				}
				ClanChatRank rank = null;
				if (menuId == 0) {
					rank = ClanChatRank.OWNER;
				} else if (menuId == 1) {
					rank = ClanChatRank.GENERAL;
				} else if (menuId == 2) {
					rank = ClanChatRank.CAPTAIN;
				} else if (menuId == 3) {
					rank = ClanChatRank.LIEUTENANT;
				} else if (menuId == 4) {
					rank = ClanChatRank.SERGEANT;
				} else if (menuId == 5) {
					rank = ClanChatRank.CORPORAL;
				} else if (menuId == 6) {
					rank = ClanChatRank.RECRUIT;
				} else if (menuId == 7) {
					rank = ClanChatRank.FRIEND;
				}

				if (button == 38322) {
					if (clan.getRankRequirement()[ClanChat.RANK_REQUIRED_TO_ENTER] != null
							&& clan.getRankRequirement()[ClanChat.RANK_REQUIRED_TO_ENTER] == rank) {
						return true;
					}
					clan.setRankRequirements(ClanChat.RANK_REQUIRED_TO_ENTER, rank);
					player.getPacketSender().sendMessage("You have changed your clanchat channel's settings.");
					if (clan.getRankRequirement()[ClanChat.RANK_REQUIRED_TO_ENTER] != null) {
						for (Player member : clan.getMembers()) {
							if (member == null)
								continue;
							ClanChatRank memberRank = clan.getRank(member);
							if (memberRank == null || clan.getRankRequirement()[ClanChat.RANK_REQUIRED_TO_ENTER]
									.ordinal() > memberRank.ordinal()) {
								member.getPacketSender()
										.sendMessage("Your rank is not high enough to be in this channel.");
								leave(member, false);
								player.getPacketSender()
										.sendMessage("@red@Warning! Changing that setting kicked the player "
												+ member.getUsername() + " from the chat because")
										.sendMessage("@red@they do not have the required rank to be in the chat.");
								;
							}
						}
					}
					clanChatSetupInterface(player);
				} else if (button == 38325) {
					if (clan.getRankRequirement()[ClanChat.RANK_REQUIRED_TO_TALK] != null
							&& clan.getRankRequirement()[ClanChat.RANK_REQUIRED_TO_TALK] == rank) {
						return true;
					}
					clan.setRankRequirements(ClanChat.RANK_REQUIRED_TO_TALK, rank);
					player.getPacketSender().sendMessage("You have changed your clanchat channel's settings.");
					clanChatSetupInterface(player);
				} else if (button == 38328) {
					if (clan.getRankRequirement()[ClanChat.RANK_REQUIRED_TO_KICK] != null
							&& clan.getRankRequirement()[ClanChat.RANK_REQUIRED_TO_KICK] == rank) {
						return true;
					}
					clan.setRankRequirements(ClanChat.RANK_REQUIRED_TO_KICK, rank);
					player.getPacketSender().sendMessage("You have changed your clanchat channel's settings.");
					clanChatSetupInterface(player);
					updateList(clan);
				}

				return true;
			}
		}

		// Selecting a player in one of the lists to manage them
		String target = null;
		ClanChat clan = null;
		if (button >= 37144 && button <= 37243) {
			if ((player.getCurrentClanChat() == null
					|| !player.getCurrentClanChat().getOwnerName().equals(player.getUsername())) && menuId != 7) {
				player.getPacketSender().sendMessage("Only the clanchat owner can do that.");
				return true;
			}
			int index = (button - 37144);
			target = getPlayer(index, player.getCurrentClanChat()).getUsername();
			clan = player.getCurrentClanChat();
		} else if (button >= 38752 && button <= 38951) {
			int index = button - 38752;
			if (index < player.getRelations().getFriendList().size()) {
				target = Misc.formatPlayerName(Misc.longToString(player.getRelations().getFriendList().get(index)));
				clan = getClanChat(player);
				if (clan == null) {
					player.getPacketSender().sendMessage("Please enable your clanchat before changing ranks.");
					return true;
				}
			}
		}

		if (clan != null && target != null && !target.equals(player.getUsername())) {
			switch (menuId) {
			case 0:
			case 1:
			case 2:
			case 3:
			case 4:
			case 5:
				ClanChatRank rank = ClanChatRank.forMenuId(menuId);
				ClanChatRank targetRank = clan.getRank(target);
				if (targetRank != null) {
					if (targetRank == rank) {
						player.getPacketSender().sendMessage("That player already has that rank.");
						return true;
					}
					if (targetRank == ClanChatRank.STAFF) {
						player.getPacketSender().sendMessage("That player cannot be promoted or demoted.");
						return true;
					}
				}
				clan.giveRank(target, rank);
				Optional<Player> p2 = World.getPlayerByName(target);
				if (p2.isPresent()) {
					updateRank(clan, p2.get());
					if (clan.getRankRequirement()[ClanChat.RANK_REQUIRED_TO_ENTER] != null) {
						if (rank == null || clan.getRankRequirement()[ClanChat.RANK_REQUIRED_TO_ENTER].ordinal() > rank
								.ordinal()) {
							p2.get().getPacketSender()
									.sendMessage("Your rank is not high enough to be in this channel.");
							leave(p2.get(), false);
							player.getPacketSender()
									.sendMessage("@red@Warning! Changing that setting kicked the player "
											+ p2.get().getUsername() + " from the chat because")
									.sendMessage("@red@they do not have the required rank to be in the chat.");
							;
						}
					}
				}
				updateList(clan);
				if (player.getInterfaceId() == CLAN_CHAT_SETUP_INTERFACE_ID) {
					clanChatSetupInterface(player);
				}
				break;
			case 6:
				targetRank = player.getCurrentClanChat().getRank(target);
				if (targetRank == null) {
					player.getPacketSender().sendMessage("That player has no rank.");
					return true;
				}
				if (targetRank == ClanChatRank.STAFF) {
					player.getPacketSender().sendMessage("That player cannot be promoted or demoted.");
					return true;
				}
				clan.getRankedNames().remove(target);
				p2 = World.getPlayerByName(target);
				if (p2.isPresent()) {
					updateRank(clan, p2.get());
					if (clan.getRankRequirement()[ClanChat.RANK_REQUIRED_TO_ENTER] != null) {
						rank = clan.getRank(p2.get());
						if (rank == null || clan.getRankRequirement()[ClanChat.RANK_REQUIRED_TO_ENTER].ordinal() > rank
								.ordinal()) {
							p2.get().getPacketSender()
									.sendMessage("Your rank is not high enough to be in this channel.");
							leave(p2.get(), false);
							player.getPacketSender()
									.sendMessage("@red@Warning! Changing that setting kicked the player "
											+ p2.get().getUsername() + " from the chat because")
									.sendMessage("@red@they do not have the required rank to be in the chat.");
							;
						}
					}
				}
				updateList(clan);
				if (player.getInterfaceId() == CLAN_CHAT_SETUP_INTERFACE_ID) {
					clanChatSetupInterface(player);
				}
				break;
			case 7:
				Optional<Player> kick = World.getPlayerByName(target);
				kick.ifPresent(k -> kick(player, k));
				break;
			}
			return true;
		}

		// Other buttons..
		switch (button) {
		case 37132: // CC Setup
			if (player.busy()) {
				player.getPacketSender().sendInterfaceRemoval();
			}
			clanChatSetupInterface(player);
			return true;
		case 37129: // Join / Leave clan
			if (player.getCurrentClanChat() == null) {
				player.setEnteredSyntaxAction((input) -> {
				    ClanChatManager.join(player, input);
				});
				player.getPacketSender().sendEnterInputPrompt("Which clanchat channel would you like to join?");
			} else {
				leave(player, false);
				player.setClanChatName("");
			}
			return true;
		}
		return false;
	}

	public static void onLogin(Player player) {
		resetInterface(player);
		if (player.getClanChatName() != null && !player.getClanChatName().isEmpty()) {
			ClanChatManager.join(player, player.getClanChatName());
		}
	}

	public static void resetInterface(Player player) {
		player.getPacketSender().sendString(37139, "Talking in: N/A");
		player.getPacketSender().sendString(37140, "Owner: N/A");
		player.getPacketSender().sendString(37135, "Join Chat");
		// player.getPacketSender().sendString(29454, "Lootshare: N/A");
		player.getPacketSender().clearInterfaceText(37144, 37243);
	}

	private static int getIndex() {
		for (int i = 0; i < clans.length; i++) {
			if (clans[i] == null) {
				return i;
			}
		}
		return -1;
	}

	public static ClanChat[] getClans() {
		return clans;
	}

	public static ClanChat getClanChat(int index) {
		return clans[index];
	}

	public static ClanChat getClanChat(Player player) {
		for (ClanChat clan : clans) {
			if (clan == null || clan.getOwnerName() == null)
				continue;
			if (clan.getOwnerName().equals(player.getUsername())) {
				return clan;
			}
		}
		return null;
	}

	public static Player getPlayer(int index, ClanChat clan) {
		int clanIndex = 0;
		for (Player members : clan.getMembers()) {
			if (members != null) {
				if (clanIndex == index) {
					return members;
				}
				clanIndex++;
			}
		}
		return null;
	}
}