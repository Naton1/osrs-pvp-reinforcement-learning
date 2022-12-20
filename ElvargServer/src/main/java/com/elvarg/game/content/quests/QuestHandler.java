package com.elvarg.game.content.quests;

import com.elvarg.game.content.quests.impl.CooksAssistant;
import com.elvarg.game.entity.impl.npc.NPC;
import com.elvarg.game.entity.impl.player.Player;

public class QuestHandler {

	public static final int NOT_STARTED = 0;

	public static void updateQuestTab(Player player) {
		player.getPacketSender().sendString("QP: " + player + " ", 3985);

		for (Quests questRecord : Quests.values()) {
			Quest quest = questRecord.get();

			player.getPacketSender().sendString(quest.questTabStringId(), questRecord.getProgressColor(player) + questRecord.getName());
		}
	}

	public static boolean firstClickNpc(Player player, NPC npc) {
		for (Quests questRecord : Quests.values()) {
			if (questRecord.quest.firstClickNpc(player, npc)) {
				return true;
			}
		}

		// Return false if no Quest handled this NPC click
		return false;
	}

	public enum Quests {
//		BLACK_KNIGHT(28164, "Black Knights' Fortress"),
		COOKS_ASSISTANT("Cook's Assistant", new CooksAssistant());
//		DEMON_SLAYER(28166, "Demon Slayer"),
//		DORICS_QUEST(28168, "Doric's Quest"),
//		DRAGON_SLAYER(28215, "Dragon Slayer"),
//		ERNEST(28171, "Ernest the Chicken"),
//		GOBLIN(28170, "Goblin Diplomacy"),
//		IMP_CATCHER(28172, "Imp Catcher"),
//		KNIGHTS_SWORD(28178, "The Knight's Sword"),
//		PIRATES_TREASURE(28173, "Pirates Treasure"),
//		PRINCE_RESCUE(28174, "Prince Ali Rescue"),
//		RESTLESS_GHOST(28169, "Restless Ghost"),
//		ROMEO_JULIET(28175, "Romeo Juliet"),
//		RUNE_MYSTERIES(28167, "Rune Mysteries"),
//		SHEEP_SHEARER(28176, "Sheep Shearer"),
//		SHIELD_OF_ARRAV(28177, "Shield of Arrav"),
//		VAMPYRE_SLAYER(28179, "Vampyre Slayer"),
//		WITCHS_POTION(28180, "Witchs Potion"),
//		BETWEEN_A_ROCK(49228, "Between A Rock"),
//		CHOMPY(2161, "Big Chompy Bird Hunting"),
//		BIOHAZARD(28124, "Biohazard"),
//		CABIN(68102, "Cabin Fever"),
//		CLOCK(28185, "Clock Tower"),
//		DEATH(32246, "Death Plateau"),
//		CREATURE(47097, "Creature of Fenkenstrain"),
//		DESERT_TREASURE(50052, "Desert Treasure"),
//		DRUDIC_RITUAL(28187, "Drudic Ritual"),
//		DWARF_CANNON(28188, "Dwarf Cannon"),
//		EADGARS_RUSE(33231, "Eadgars Ruse"),
//		DEVIOUS(61225, "Devious Minds"),
//		DIGSITE(28186, "Digsite Quest"),
//		ELEMENTAL(29035, "Elemental Workshop"),
//		ENAKHRA(63021, "Enakhra's Lamet"),
//		FAIRY1(27075, "A Fairy Tale Pt. 1"),
//		FAMILYCREST(28189, "Family Crest"),
//		FEUD(50036, "The Feud"),
//		FIGHT_ARENA(28190, "Fight Arena"),
//		FISHING_CONTEST(28191, "Fishing Contest"),
//		FORGETTABLE_TABLE(50089, "Forgettable Tale..."),
//		FREMMY_TRIALS(39131, "The Fremennik Trials"),
//		GARDEN(57012, "Garden of Tranquillity"),
//		GHOSTS(47250, "Ghosts Ahoy"),
//		GIANT_DWARF(53009, "The Giant Dwarf"),
//		GOLEM(50039, "The Golem"),
//		GRAND_TREE(28193, "The Grand Tree"),
//		HAND_IN_THE_SAND(63000, "The Hand in the Sand"),
//		HAUNTED_MINE(46081, "Haunted Mine"),
//		HAZEEL(28194, "Hazeel Cult"),
//		HEROES(28195, "Heroes Quest"),
//		HOLY(28196, "Holy Grail"),
//		HORROR(39151, "Horror from the Deep"),
//		ITCHLARIN(17156, "Itchlarin's Little Helper"),
//		AID_OF_MYREQUE(72085, "In Aid of the Myreque"),
//		SEARCH_OF_MYREQUE(46131, "In Search of the Myreque"),
//		JUNGLE_POTION(28197, "Jungle Potion"),
//		LEGENDS_QUEST(28198, "Legends Quest"),
//		LOST_CITY(28199, "Lost City"),
//		LOST_TRIBE(52077, "The Lost Tribe"),
//		MAKING_HISTORY(60127, "Making History"),
//		MONKEY_MADNESS(43124, "Monkey Madness"),
//		MERLINS_CRYSTAL(28200, "Merlins Crystal"),
//		MONKS_FRIEND(28201, "Monks Friend"),
//		MOUNTAIN_DAUGHTER(48101, "Mountain Daughter"),
//		MOURNINGS_END_1(54150, "Mourning's Ends Part 1"),
//		MOURNINGS_END_2(23139, "Mourning's Ends Part 2"),
//		MURDER_MYSTERY(28202, "Murder Mystery"),
//		NATURE_SPIRIT(31201, "Nature Spirit"),
//		OBSERVATORY(28203, "Observatory Quest"),
//		ONE_SMALL_FAVOUR(48057, "One Small Favour"),
//		PLAGUE_CITY(28204, "Plague City"),
//		PRIEST_IN_PERIL(31179, "Priest in Peril"),
//		RAG_AND_BONE_MAN(72252, "Rag and Bone Man"),
//		RAT_CATCHERS(60139, "Rat Catchers"),
//		RECIPE(71130, "Recipe for Disaster"),
//		RECRUITMENT_DRIVE(2156, "Recruitment Drive"),
//		REGICIDE(33128, "Regicide"),
//		ROVING_ELVES(47017, "Roving Elves"),
//		RUM_DEAL(58064, "Rum Deal"),
//		SCORPION_CATCHER(28205, "Scorpion Catcher"),
//		SEA_SLUG(28206, "Sea Slug Quest"),
//		SHADES_OF_MORTON(35009, "Shades of Mort'ton"),
//		SHADOW_OF_THE_STORM(59248, "Shadow of the Storm"),
//		SHEEP_HERDER(28207, "Sheep Herder"),
//		SHILO_VILLAGE(28208, "Shilo Village"),
//		SOULS_BANE(28250, "A Soul's Bane"),
//		SPIRITS_OF_THE_ELID(60232, "Spirits of The Elid"),
//		SWAN_SONG(249, "Swan Song"),
//		TAI_BWO(6204, "Tai Bwo Wannai Trio"),
//		TWO_CATS(59131, "A Tail of Two Cats"),
//		TEARS_OF_GUTHIX(12206, "Tears of Guthix"),
//		TEMPLE_OF_IKOV(28210, "Temple of Ikov"),
//		THRONE_OF_MISCELLANIA(25118, "Throne of Miscellania"),
//		TOURIST_TRAP(28211, "The Tourist Trap"),
//		TREE_GNOME_VILLAGE(28212, "Tree Gnome Village"),
//		TRIBAL_TOTEM(28213, "Tribal Totem"),
//		TROLL_ROMANCE(46082, "Troll Romance"),
//		TROLL_STRONGHOLD(191, "Troll Stronghold"),
//		UNDERGROUND_PASS(38199, "Underground Pass"),
//		WANTED(23136, "Wanted"),
//		WATCHTOWER(28181, "Watch Tower"),
//		WATERFALL(28182, "Waterfall Quest"),
//		WITCH(28183, "Witch's House"),
//		ZOGRE(52044, "Zogre Flesh Eaters");

		private final String name;
		private final Quest quest;

		private Quests(final String name, final Quest quest) {
			this.name = name;
			this.quest = quest;
		}

		public String getName() {
			return name;
		}

		public Quest get() {
			return quest;
		}

		/**
		 * Returns an integer to represent the player's progress on this quest.
		 *
		 * @param player
		 * @return progress
		 */
		public int getProgress(Player player) {
			if (!player.getQuestProgress().containsKey(this.ordinal())) {
				return 0;
			}

			return player.getQuestProgress().get(this.ordinal());
		}

		/**
		 * Sets the progress for the given quest and updates the quest tab.
		 *
		 * @param player
		 * @param progress
		 */
		public void setProgress(Player player, int progress) {
			player.getQuestProgress().put(this.ordinal(), progress);
			QuestHandler.updateQuestTab(player);
		}

		/**
		 * Gets the progress colour for the Quest tab for the given quest.
		 *
		 * @param player The player to check status for
		 * @return progressColor The status colour prefix, e.g. "@red@"
		 */
		public String getProgressColor(Player player) {
			int questProgress = this.getProgress(player);
			if (questProgress == 0) {
				return "@red@";
			}

			int completeProgress = this.get().completeStatus();
			if (questProgress < completeProgress) {
				return "@yel@";
			}

			return "@gre@";
		}

		public static Quests forButton(int button) {
			for (Quests q : Quests.values()) {
				if (q.get().questTabButtonId() == button) {
					return q;
				}
			}
			return null;
		}

		public static int getOrdinal(Quest quest) {
			for (Quests q : Quests.values()) {
				if (q.get() == quest) {
					return q.ordinal();
				}
			}
			return -1;
		}

		public void showRewardInterface(Player player, String[] lines, int itemID) {
			String questName = this.getName();

			player.getPacketSender().sendString("You have completed " + questName + "!", 12144);
			player.getPacketSender().sendString("" + this.get().questPointsReward(), 12147);

			for (int i = 0; i < 5; i++) {
				player.getPacketSender().sendString(lines[i], 12150 + i);
			}

			if (itemID > 0) {
				player.getPacketSender().sendInterfaceModel(12145, itemID, 250);
			}

			player.getPacketSender().sendInterface(12140);
		}
	}

	public static boolean handleQuestButtonClick(Player player, int buttonId) {
		Quests quest = Quests.forButton(buttonId);
		if (quest == null) {
			// There is no quest for this button ID
			return false;
		}

		int status = player.getQuestProgress().get(quest.ordinal());
		quest.get().showQuestLog(player, status);
		return true;
	}

	/**
	 * This function blanks out all lines on the Quest log interface.
	 *
	 * @param player
	 */
	public static void clearQuestLogInterface(Player player) {
		for (int i = 8144; i < 8195; i++) {
			player.getPacketSender().sendString("", i);
		}
	}
}
