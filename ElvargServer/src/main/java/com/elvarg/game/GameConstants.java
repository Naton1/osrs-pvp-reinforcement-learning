package com.elvarg.game;

import com.elvarg.game.model.Location;

/**
 * A class containing different attributes which affect the game in different
 * ways.
 *
 * @author Professor Oak
 */
public class GameConstants {

	/**
	 * The name of the game.
	 */
	public static final String NAME = "Elvarg";

	/**
	 * The secure game UID /Unique Identifier/
	 */
	public static final int CLIENT_UID = 8784521;

	/**
	 * The directory of the definition files.
	 */
	public static final String DEFINITIONS_DIRECTORY = "./data/definitions/";

	/**
	 * The directory of the clipping files.
	 */
	public static final String CLIPPING_DIRECTORY = "./data/clipping/";

	/**
	 * The flag that determines if processing should be parallelized, improving the
	 * performance of the server times {@code n} (where
	 * {@code n = Runtime.getRuntime().availableProcessors()}) at the cost of
	 * substantially more CPU usage.
	 */
	public static final boolean CONCURRENCY = (Runtime.getRuntime().availableProcessors() > 1);

	/**
	 * The game engine cycle rate in milliseconds.
	 */
	public static final int GAME_ENGINE_PROCESSING_CYCLE_RATE = 600;

	/**
	 * The maximum amount of iterations for a queue/list that should occur each
	 * cycle.
	 */
	public static final int QUEUED_LOOP_THRESHOLD = 45;

	/**
	 * The default position, where players will spawn upon logging in for the first
	 * time.
	 */
	public static final Location DEFAULT_LOCATION = new Location(3086, 3496);

	/**
	 * Should the inventory be refreshed immediately on switching items or should it
	 * be delayed until next game cycle?
	 */
	public static final boolean QUEUE_SWITCHING_REFRESH = false;

	/**
	 * The maximum amount of drops that can be rolled from the dynamic drop table.
	 */
	public static final int DROP_THRESHOLD = 2;

	/**
	 * Multiplies the experience gained.
	 */
	public static final double COMBAT_SKILLS_EXP_MULTIPLIER = 6;
	public static final double REGULAR_SKILLS_EXP_MULTIPLIER = 18;

	/**
	 * The gameframe's tab interface ids.
	 */
	public static final int TAB_INTERFACES[] = { 2423, 3917, 31000, 3213, 1644, 5608, -1, 37128, 5065, 5715, 2449,
			42500, 147, 32000 };

	/**
	 * Spawnable Items
	 */
	public static final int[] ALLOWED_SPAWNS = { 13441, 3144, 391, 397, 385, 7946, 2436, 145, 147, 149, 2440, 157, 159, 161,
			2442, 163, 165, 167, 9739, 2444, 169, 171, 173, // potions and food
			3040, 3042, 3044, 3046, 2452, 2454, 2456, 2458, 2448, 181, 183, 185, 6685, 6687, 6689, 6691, 2450, 189, 191, 193, 3024, 3026, 3028, 3030, 2434, // potions and food
			139, 141, 143, 4417, 4419, 4421, 4423, 229, // potions and food
			1149, 3140, 4087, 4585, 1187, 11840, // dragon
			1163, 1127, 1079, 1093, 1201, 4131, // rune
			1161, 1123, 1073, 1091, 1199, 4129, // addy
			1159, 1121, 1071, 1091, 1197, 4127, // mithril
			1165, 1125, 1077, 1089, 1195, 4125, // black
			1157, 1119, 1069, 1083, 1193, 4123, // steel
			1153, 1115, 1067, 1081, 1191, 4121, // iron
			1155, 1117, 1075, 1087, 1189, 4119, // bronze
			4587, 1333, 1331, 1329, 1327, 1325, 1323, 1321, // scimitars
			21009, 1289, 1287, 1285, 1283, 1281, 1279, 1277, // swords
			1305, 1303, 1301, 1299, 1297, 1295, 1293, 1291, // longswords
			7158, 1319, 1317, 1315, 1313, 1311, 1309, 1307, // 2hs
			1347, 1345, 1343, 1341, 1339, 1335, 1337, // warhammers
			5698, 1215, 1213, 1211, 1209, 1217, 1207, 1203, 1205, // daggers
			1434, 1432, 1430, 1428, 1426, 1424, 1420, 1422, // maces
			7462, 7461, 7460, 7459, 7458, 7457, 7456, 7455, 7454, // gloves
			11126, 2550, 4151, 4153, 10887, // special weapons
			6528, 6527, 6526, 6525, 6524, 6523, 6522, // obby items
			9747, 9748, 9750, 9751, 9753, 9754, 9756, 9757, 9759, 9760, 9762, 9763, 6568, 2412, 2413, 2414, // capes
			8850, 8849, 8848, 8847, 8846, 8845, 8844, 1540, 10828, 3755, 3753, 3751, 3749, 3748, 12831, 12829, 3842,
			3844, 12608, 12610, 12612, 11235, 859, 855, 851, 847, 845, 841, 861, 857, 853, 849, 843, 841, 9185, 9183,
			9181, 9179, 9177, 9174, 11212, 892, 890, 888, 886, 884, 882, 9245, 9244, 9243, 9242, 9241, 9240, 9239, 9238,
			9237, 9236, 9305, 9144, 9143, 9142, 9141, 9140, 877, 5667, 868, 867, 866, 869, 865, 863, 864, 19484, 5653,
			830, 829, 828, 827, 826, 825, 11230, 811, 810, 809, 808, 807, 806, 10368, 10370, 10372, 10374, 10376, 10378,
			10380, 10382, 10384, 10386, 10388, 10390, 12490, 12492, 12494, 12496, 12498, 12500, 12502, 12504, 12506,
			12508, 12510, 12512, 2503, 2497, 2491, 2501, 2495, 2489, 2499, 2493, 2487, 1135, 1099, 1065, 6322, 6324,
			6326, 6328, 6330, 10954, 10956, 10958, 6131, 6133, 6135, 1169, 1133, 1097, 1131, 1167, 1129, 1095, 10499,
			4675, 1381, 1383, 1385, 1387, 1379, 4089, 4091, 4093, 4095, 4097, 4099, 4101, 4103, 4105, 4107, 4109, 4111,
			4113, 4115, 4117, 7400, 7399, 7398, 6918, 6916, 6924, 6922, 6920, 6109, 6107, 6108, 6110, 6106, 3105, 6111,
			544, 542, 1035, 1033, 579, 577, 1011, 554, 555, 556, 557, 558, 559, 561, 563, 562, 560, 565, 566, 9075,
			1704, 1731, 1725, 1727, 1729, 8013, };
}
