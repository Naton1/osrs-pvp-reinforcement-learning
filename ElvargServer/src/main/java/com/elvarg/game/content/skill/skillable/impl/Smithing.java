package com.elvarg.game.content.skill.skillable.impl;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.Animation;
import com.elvarg.game.model.AnimationLoop;
import com.elvarg.game.model.Item;
import com.elvarg.game.model.RequiredItem;
import com.elvarg.game.model.Skill;
import com.elvarg.util.ItemIdentifiers;
import com.elvarg.util.Misc;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

/**
 * Handles the Smithing skill.
 *
 * @author Professor Oak
 */
//TODO: USE CONFIGS
public class Smithing extends ItemIdentifiers {

    /**
     * Handles buttons related to the Smithing skill.
     *
     * @param player
     * @param button
     * @return
     */
    public static boolean handleButton(Player player, int button) {
        //Handle bar creation interface..
        for (Bar bar : Bar.values()) {
            for (int[] b : bar.getButtons()) {
                if (b[0] == button) {
                    int amount = b[1];
                    if (amount == -1) {
                        player.setEnteredAmountAction((input) -> {
                            player.getSkillManager().startSkillable(new Smelting(bar, input)); 
                        });
                        player.getPacketSender().sendEnterAmountPrompt("Enter amount of bars to smelt:");
                    } else {
                        player.getSkillManager().startSkillable(new Smelting(bar, amount));
                    }
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * An emurated type containing data about all the
     * equipment which can be created using the Smithing skill
     * in the game.
     *
     * @author Professor Oak
     */
    public enum SmithableEquipment {
        BRONZE_DAGGER("Dagger", 2349, 1205, 1, 1119, 0, 1094, 1, 1, 1125),
        BRONZE_AXE("Axe", 2349, 1351, 1, 1120, 0, 1091, 1, 1, 1126),
        BRONZE_MACE("Mace", 2349, 1422, 1, 1120, 1, 1093, 2, 1, 1129),
        BRONZE_MED_HELM("Med helm", 2349, 1139, 1, 1122, 0, 1102, 3, 1, 1127),
        BRONZE_DART_TIPS("Dart tips", 2349, 819, 10, 1123, 0, 1107, 4, 1, 1128),
        BRONZE_SWORD("Sword", 2349, 1277, 1, 1119, 1, 1085, 4, 1, 1124),
        BRONZE_ARROWTIPS("Arrowtips", 2349, 39, 15, 1123, 1, 1108, 5, 1, 1130),
        BRONZE_SCIMITAR("Scimitar", 2349, 1321, 1, 1119, 2, 1087, 5, 2, 1116),
        BRONZE_LONG_SWORD("Long sword", 2349, 1291, 1, 1119, 3, 1086, 6, 2, 1089),
        BRONZE_THROWING_KNIVES("Throwing knives", 2349, 864, 5, 1123, 2, 1106, 7, 1, 1131),
        BRONZE_FULL_HELM("Full helm", 2349, 1155, 1, 1122, 1, 1103, 7, 2, 1113),
        BRONZE_SQUARE_SHIELD("Square shield", 2349, 1173, 1, 1122, 2, 1104, 8, 2, 1114),
        BRONZE_WARHAMMER("Warhammer", 2349, 1337, 1, 1120, 2, 1083, 9, 3, 1118),
        BRONZE_BATTLE_AXE("Battle axe", 2349, 1375, 1, 1120, 3, 1092, 10, 3, 1095),
        BRONZE_CHAINBODY("Chainbody", 2349, 1103, 1, 1121, 0, 1098, 11, 3, 1109),
        BRONZE_KITE_SHIELD("Kite shield", 2349, 1189, 1, 1122, 3, 1105, 12, 3, 1115),
        BRONZE_CLAWS("Claws", 2349, 3095, 1, 1120, 4, 8429, 13, 2, 8428),
        BRONZE_2_HAND_SWORD("2 hand sword", 2349, 1307, 1, 1119, 4, 1088, 14, 3, 1090),
        BRONZE_PLATESKIRT("Plate skirt", 2349, 1087, 1, 1121, 2, 1100, 16, 3, 1111),
        BRONZE_PLATELEGS("Plate legs", 2349, 1075, 1, 1121, 1, 1099, 16, 3, 1110),
        BRONZE_PLATEBODY("Plate body", 2349, 1117, 1, 1121, 3, 1101, 18, 5, 1112),
        BRONZE_NAILS("Nails", 2349, 4819, 15, 1122, 4, 13358, 4, 1, 13357),
        BRONZE_UNF_BOLTS("Bolts (unf)", 2349, 9375, 10, 1121, 4, 11461, 3, 1, 11459),

        IRON_DAGGER("Dagger", 2351, 1203, 1, 1119, 0, 1094, 15, 1, 1125),
        IRON_AXE("Axe", 2351, 1349, 1, 1120, 0, 1091, 16, 1, 1126),
        IRON_MACE("Mace", 2351, 1420, 1, 1120, 1, 1093, 17, 1, 1129),
        IRON_MED_HELM("Med helm", 2351, 1137, 1, 1122, 0, 1102, 18, 1, 1127),
        IRON_DART_TIPS("Dart tips", 2351, 820, 10, 1123, 0, 1107, 19, 1, 1128),
        IRON_SWORD("Sword", 2351, 1279, 1, 1119, 1, 1085, 19, 1, 1124),
        IRON_ARROWTIPS("Arrowtips", 2351, 40, 15, 1123, 1, 1108, 20, 1, 1130),
        IRON_SCIMITAR("Scimitar", 2351, 1323, 1, 1119, 2, 1087, 20, 2, 1116),
        IRON_LONG_SWORD("Long sword", 2351, 1293, 1, 1119, 3, 1086, 21, 2, 1089),
        IRON_THROWING_KNIVES("Throwing knives", 2351, 863, 5, 1123, 2, 1106, 22, 1, 1131),
        IRON_FULL_HELM("Full helm", 2351, 1153, 1, 1122, 1, 1103, 22, 2, 1113),
        IRON_SQUARE_SHIELD("Square shield", 2351, 1175, 1, 1122, 2, 1104, 23, 2, 1114),
        IRON_WARHAMMER("Warhammer", 2351, 1335, 1, 1120, 2, 1083, 24, 3, 1118),
        IRON_BATTLE_AXE("Battle axe", 2351, 1363, 1, 1120, 3, 1092, 25, 3, 1095),
        IRON_CHAINBODY("Chainbody", 2351, 1101, 1, 1121, 0, 1098, 26, 3, 1109),
        IRON_KITE_SHIELD("Kite shield", 2351, 1191, 1, 1122, 3, 1105, 27, 3, 1115),
        IRON_CLAWS("Claws", 2351, 3096, 1, 1120, 4, 8429, 28, 2, 8428),
        IRON_2_HAND_SWORD("2 hand sword", 2351, 1309, 1, 1119, 4, 1088, 29, 3, 1090),
        IRON_PLATESKIRT("Plate skirt", 2351, 1081, 1, 1121, 2, 1100, 31, 3, 1111),
        IRON_PLATELEGS("Plate legs", 2351, 1067, 1, 1121, 1, 1099, 31, 3, 1110),
        IRON_PLATEBODY("Plate body", 2351, 1115, 1, 1121, 3, 1101, 33, 5, 1112),
        IRON_NAILS("Nails", 2351, 4820, 15, 1122, 4, 13358, 19, 1, 13357),
        IRON_UNF_BOLTS("Bolts (unf)", 2351, 9377, 10, 1121, 4, 11461, 19, 1, 11459),

        STEEL_DAGGER("Dagger", 2353, 1207, 1, 1119, 0, 1094, 30, 1, 1125),
        STEEL_AXE("Axe", 2353, 1353, 1, 1120, 0, 1091, 31, 1, 1126),
        STEEL_MACE("Mace", 2353, 1424, 1, 1120, 1, 1093, 32, 1, 1129),
        STEEL_MED_HELM("Med helm", 2353, 1141, 1, 1122, 0, 1102, 33, 1, 1127),
        STEEL_DART_TIPS("Dart tips", 2353, 821, 10, 1123, 0, 1107, 34, 1, 1128),
        STEEL_SWORD("Sword", 2353, 1281, 1, 1119, 1, 1085, 34, 1, 1124),
        STEEL_ARROWTIPS("Arrowtips", 2353, 41, 15, 1123, 1, 1108, 35, 1, 1130),
        STEEL_SCIMITAR("Scimitar", 2353, 1325, 1, 1119, 2, 1087, 35, 2, 1116),
        STEEL_LONG_SWORD("Long sword", 2353, 1295, 1, 1119, 3, 1086, 36, 2, 1089),
        STEEL_THROWING_KNIVES("Throwing knives", 2353, 865, 5, 1123, 2, 1106, 37, 1, 1131),
        STEEL_FULL_HELM("Full helm", 2353, 1157, 1, 1122, 1, 1103, 37, 2, 1113),
        STEEL_SQUARE_SHIELD("Square shield", 2353, 1177, 1, 1122, 2, 1104, 38, 2, 1114),
        STEEL_WARHAMMER("Warhammer", 2353, 1339, 1, 1120, 2, 1083, 39, 3, 1118),
        STEEL_BATTLE_AXE("Battle axe", 2353, 1365, 1, 1120, 3, 1092, 40, 3, 1095),
        STEEL_CHAINBODY("Chainbody", 2353, 1105, 1, 1121, 0, 1098, 41, 3, 1109),
        STEEL_KITE_SHIELD("Kite shield", 2353, 1193, 1, 1122, 3, 1105, 42, 3, 1115),
        STEEL_CLAWS("Claws", 2353, 3097, 1, 1120, 4, 8429, 43, 2, 8428),
        STEEL_2_HAND_SWORD("2 hand sword", 2353, 1311, 1, 1119, 4, 1088, 44, 3, 1090),
        STEEL_PLATESKIRT("Plate skirt", 2353, 1083, 1, 1121, 2, 1100, 46, 3, 1111),
        STEEL_PLATELEGS("Plate legs", 2353, 1069, 1, 1121, 1, 1099, 46, 3, 1110),
        STEEL_PLATEBODY("Plate body", 2353, 1119, 1, 1121, 3, 1101, 48, 5, 1112),
        STEEL_NAILS("Nails", 2353, 1539, 15, 1122, 4, 13358, 34, 1, 13357),
        STEEL_UNF_BOLTS("Bolts (unf)", 2353, 9378, 10, 1121, 4, 11461, 33, 1, 11459),
        CANNONBALL("Cannon ball", 2353, 2, 4, 1123, 3, 1096, 35, 1, 1132),
        STEEL_STUDS("Studs", 2353, 2370, 1, 1123, 4, 1134, 36, 1, 1135),

        MITHRIL_DAGGER("Dagger", 2359, 1209, 1, 1119, 0, 1094, 50, 1, 1125),
        MITHRIL_AXE("Axe", 2359, 1355, 1, 1120, 0, 1091, 51, 1, 1126),
        MITHRIL_MACE("Mace", 2359, 1428, 1, 1120, 1, 1093, 52, 1, 1129),
        MITHRIL_MED_HELM("Med helm", 2359, 1143, 1, 1122, 0, 1102, 53, 1, 1127),
        MITHRIL_DART_TIPS("Dart tips", 2359, 822, 10, 1123, 0, 1107, 54, 1, 1128),
        MITHRIL_SWORD("Sword", 2359, 1285, 1, 1119, 1, 1085, 54, 1, 1124),
        MITHRIL_ARROWTIPS("Arrowtips", 2359, 42, 15, 1123, 1, 1108, 55, 1, 1130),
        MITHRIL_SCIMITAR("Scimitar", 2359, 1329, 1, 1119, 2, 1087, 55, 2, 1116),
        MITHRIL_LONG_SWORD("Long sword", 2359, 1299, 1, 1119, 3, 1086, 56, 2, 1089),
        MITHRIL_THROWING_KNIVES("Throwing knives", 2359, 866, 5, 1123, 2, 1106, 57, 1, 1131),
        MITHRIL_FULL_HELM("Full helm", 2359, 1159, 1, 1122, 1, 1103, 57, 2, 1113),
        MITHRIL_SQUARE_SHIELD("Square shield", 2359, 1181, 1, 1122, 2, 1104, 58, 2, 1114),
        MITHRIL_WARHAMMER("Warhammer", 2359, 1343, 1, 1120, 2, 1083, 59, 3, 1118),
        MITHRIL_BATTLE_AXE("Battle axe", 2359, 1369, 1, 1120, 3, 1092, 60, 3, 1095),
        MITHRIL_CHAINBODY("Chainbody", 2359, 1109, 1, 1121, 0, 1098, 61, 3, 1109),
        MITHRIL_KITE_SHIELD("Kite shield", 2359, 1197, 1, 1122, 3, 1105, 62, 3, 1115),
        MITHRIL_CLAWS("Claws", 2359, 3099, 1, 1120, 4, 8429, 63, 2, 8428),
        MITHRIL_2_HAND_SWORD("2 hand sword", 2359, 1315, 1, 1119, 4, 1088, 64, 3, 1090),
        MITHRIL_PLATESKIRT("Plate skirt", 2359, 1085, 1, 1121, 2, 1100, 66, 3, 1111),
        MITHRIL_PLATELEGS("Plate legs", 2359, 1071, 1, 1121, 1, 1099, 66, 3, 1110),
        MITHRIL_PLATEBODY("Plate body", 2359, 1121, 1, 1121, 3, 1101, 68, 5, 1112),
        MITHRIL_NAILS("Nails", 2359, 4822, 15, 1122, 4, 13358, 54, 1, 13357),
        MITHRIL_UNF_BOLTS("Bolts (unf)", 2359, 9379, 10, 1121, 4, 11461, 53, 1, 11459),

        ADAMANT_DAGGER("Dagger", 2361, 1211, 1, 1119, 0, 1094, 70, 1, 1125),
        ADAMANT_AXE("Axe", 2361, 1357, 1, 1120, 0, 1091, 71, 1, 1126),
        ADAMANT_MACE("Mace", 2361, 1430, 1, 1120, 1, 1093, 72, 1, 1129),
        ADAMANT_MED_HELM("Med helm", 2361, 1145, 1, 1122, 0, 1102, 73, 1, 1127),
        ADAMANT_DART_TIPS("Dart tips", 2361, 823, 10, 1123, 0, 1107, 74, 1, 1128),
        ADAMANT_SWORD("Sword", 2361, 1287, 1, 1119, 1, 1085, 74, 1, 1124),
        ADAMANT_ARROWTIPS("Arrowtips", 2361, 43, 15, 1123, 1, 1108, 75, 1, 1130),
        ADAMANT_SCIMITAR("Scimitar", 2361, 1331, 1, 1119, 2, 1087, 75, 2, 1116),
        ADAMANT_LONG_SWORD("Long sword", 2361, 1301, 1, 1119, 3, 1086, 76, 2, 1089),
        ADAMANT_THROWING_KNIVES("Throwing knives", 2361, 867, 5, 1123, 2, 1106, 77, 1, 1131),
        ADAMANT_FULL_HELM("Full helm", 2361, 1161, 1, 1122, 1, 1103, 77, 2, 1113),
        ADAMANT_SQUARE_SHIELD("Square shield", 2361, 1183, 1, 1122, 2, 1104, 78, 2, 1114),
        ADAMANT_WARHAMMER("Warhammer", 2361, 1345, 1, 1120, 2, 1083, 79, 3, 1118),
        ADAMANT_BATTLE_AXE("Battle axe", 2361, 1371, 1, 1120, 3, 1092, 80, 3, 1095),
        ADAMANT_CHAINBODY("Chainbody", 2361, 1111, 1, 1121, 0, 1098, 81, 3, 1109),
        ADAMANT_KITE_SHIELD("Kite shield", 2361, 1199, 1, 1122, 3, 1105, 82, 3, 1115),
        ADAMANT_CLAWS("Claws", 2361, 3100, 1, 1120, 4, 8429, 83, 2, 8428),
        ADAMANT_2_HAND_SWORD("2 hand sword", 2361, 1317, 1, 1119, 4, 1088, 84, 3, 1090),
        ADAMANT_PLATESKIRT("Plate skirt", 2361, 1091, 1, 1121, 2, 1100, 86, 3, 1111),
        ADAMANT_PLATELEGS("Plate legs", 2361, 1073, 1, 1121, 1, 1099, 86, 3, 1110),
        ADAMANT_PLATEBODY("Plate body", 2361, 1123, 1, 1121, 3, 1101, 88, 5, 1112),
        ADAMANT_NAILS("Nails", 2361, 4823, 15, 1122, 4, 13358, 74, 1, 13357),
        ADAMANT_UNF_BOLTS("Bolts (unf)", 2361, 9380, 10, 1121, 4, 11461, 73, 1, 11459),

        RUNE_DAGGER("Dagger", 2363, 1213, 1, 1119, 0, 1094, 85, 1, 1125),
        RUNE_AXE("Axe", 2363, 1359, 1, 1120, 0, 1091, 86, 1, 1126),
        RUNE_MACE("Mace", 2363, 1432, 1, 1120, 1, 1093, 87, 1, 1129),
        RUNE_MED_HELM("Med helm", 2363, 1147, 1, 1122, 0, 1102, 88, 1, 1127),
        RUNE_DART_TIPS("Dart tips", 2363, 824, 10, 1123, 0, 1107, 89, 1, 1128),
        RUNE_SWORD("Sword", 2363, 1289, 1, 1119, 1, 1085, 89, 1, 1124),
        RUNE_ARROWTIPS("Arrowtips", 2363, 44, 15, 1123, 1, 1108, 90, 1, 1130),
        RUNE_SCIMITAR("Scimitar", 2363, 1333, 1, 1119, 2, 1087, 90, 2, 1116),
        RUNE_LONG_SWORD("Long sword", 2363, 1303, 1, 1119, 3, 1086, 91, 2, 1089),
        RUNE_THROWING_KNIVES("Throwing knives", 2363, 868, 5, 1123, 2, 1106, 92, 1, 1131),
        RUNE_FULL_HELM("Full helm", 2363, 1163, 1, 1122, 1, 1103, 92, 2, 1113),
        RUNE_SQUARE_SHIELD("Square shield", 2363, 1185, 1, 1122, 2, 1104, 93, 2, 1114),
        RUNE_WARHAMMER("Warhammer", 2363, 1347, 1, 1120, 2, 1083, 94, 3, 1118),
        RUNE_BATTLE_AXE("Battle axe", 2363, 1373, 1, 1120, 3, 1092, 95, 3, 1095),
        RUNE_CHAINBODY("Chainbody", 2363, 1113, 1, 1121, 0, 1098, 96, 3, 1109),
        RUNE_KITE_SHIELD("Kite shield", 2363, 1201, 1, 1122, 3, 1105, 97, 3, 1115),
        RUNE_CLAWS("Claws", 2363, 3101, 1, 1120, 4, 8429, 98, 2, 8428),
        RUNE_2_HAND_SWORD("2 hand sword", 2363, 1319, 1, 1119, 4, 1088, 99, 3, 1090),
        RUNE_PLATESKIRT("Plate skirt", 2363, 1093, 1, 1121, 2, 1100, 99, 3, 1111),
        RUNE_PLATELEGS("Plate legs", 2363, 1079, 1, 1121, 1, 1099, 99, 3, 1110),
        RUNE_PLATEBODY("Plate body", 2363, 1127, 1, 1121, 3, 1101, 99, 5, 1112),
        RUNE_NAILS("Nails", 2363, 4824, 15, 1122, 4, 13358, 89, 1, 13357),
        RUNE_UNF_BOLTS("Bolts (unf)", 2363, 9381, 10, 1121, 4, 11461, 88, 1, 11459),;

        public static final ImmutableSet<SmithableEquipment> RUNE_ITEMS = Sets.immutableEnumSet(RUNE_DAGGER, RUNE_AXE, RUNE_MACE, RUNE_MED_HELM, RUNE_DART_TIPS, RUNE_SWORD, RUNE_ARROWTIPS, RUNE_SCIMITAR, RUNE_LONG_SWORD,
                RUNE_THROWING_KNIVES, RUNE_FULL_HELM, RUNE_SQUARE_SHIELD, RUNE_WARHAMMER, RUNE_BATTLE_AXE, RUNE_CHAINBODY, RUNE_KITE_SHIELD,
                RUNE_CLAWS, RUNE_2_HAND_SWORD, RUNE_PLATESKIRT, RUNE_PLATELEGS, RUNE_PLATEBODY, RUNE_NAILS, RUNE_UNF_BOLTS);
        public static final ImmutableSet<SmithableEquipment> ADAMANT_ITEMS = Sets.immutableEnumSet(ADAMANT_DAGGER, ADAMANT_AXE, ADAMANT_MACE, ADAMANT_MED_HELM, ADAMANT_DART_TIPS, ADAMANT_SWORD, ADAMANT_ARROWTIPS, ADAMANT_SCIMITAR, ADAMANT_LONG_SWORD,
                ADAMANT_THROWING_KNIVES, ADAMANT_FULL_HELM, ADAMANT_SQUARE_SHIELD, ADAMANT_WARHAMMER, ADAMANT_BATTLE_AXE, ADAMANT_CHAINBODY, ADAMANT_KITE_SHIELD,
                ADAMANT_CLAWS, ADAMANT_2_HAND_SWORD, ADAMANT_PLATESKIRT, ADAMANT_PLATELEGS, ADAMANT_PLATEBODY, ADAMANT_NAILS, ADAMANT_UNF_BOLTS);
        public static final ImmutableSet<SmithableEquipment> MITHRIL_ITEMS = Sets.immutableEnumSet(MITHRIL_DAGGER, MITHRIL_AXE, MITHRIL_MACE, MITHRIL_MED_HELM, MITHRIL_DART_TIPS, MITHRIL_SWORD, MITHRIL_ARROWTIPS, MITHRIL_SCIMITAR, MITHRIL_LONG_SWORD,
                MITHRIL_THROWING_KNIVES, MITHRIL_FULL_HELM, MITHRIL_SQUARE_SHIELD, MITHRIL_WARHAMMER, MITHRIL_BATTLE_AXE, MITHRIL_CHAINBODY, MITHRIL_KITE_SHIELD,
                MITHRIL_CLAWS, MITHRIL_2_HAND_SWORD, MITHRIL_PLATESKIRT, MITHRIL_PLATELEGS, MITHRIL_PLATEBODY, MITHRIL_NAILS, MITHRIL_UNF_BOLTS);
        public static final ImmutableSet<SmithableEquipment> STEEL_ITEMS = Sets.immutableEnumSet(STEEL_DAGGER, STEEL_AXE, STEEL_MACE, STEEL_MED_HELM, STEEL_DART_TIPS, STEEL_SWORD, STEEL_ARROWTIPS, STEEL_SCIMITAR, STEEL_LONG_SWORD,
                STEEL_THROWING_KNIVES, STEEL_FULL_HELM, STEEL_SQUARE_SHIELD, STEEL_WARHAMMER, STEEL_BATTLE_AXE, STEEL_CHAINBODY, STEEL_KITE_SHIELD,
                STEEL_CLAWS, STEEL_2_HAND_SWORD, STEEL_PLATESKIRT, STEEL_PLATELEGS, STEEL_PLATEBODY, STEEL_NAILS, STEEL_UNF_BOLTS, STEEL_STUDS, CANNONBALL);
        public static final ImmutableSet<SmithableEquipment> IRON_ITEMS = Sets.immutableEnumSet(IRON_DAGGER, IRON_AXE, IRON_MACE, IRON_MED_HELM, IRON_DART_TIPS, IRON_SWORD, IRON_ARROWTIPS, IRON_SCIMITAR, IRON_LONG_SWORD,
                IRON_THROWING_KNIVES, IRON_FULL_HELM, IRON_SQUARE_SHIELD, IRON_WARHAMMER, IRON_BATTLE_AXE, IRON_CHAINBODY, IRON_KITE_SHIELD,
                IRON_CLAWS, IRON_2_HAND_SWORD, IRON_PLATESKIRT, IRON_PLATELEGS, IRON_PLATEBODY, IRON_NAILS, IRON_UNF_BOLTS);
        public static final ImmutableSet<SmithableEquipment> BRONZE_ITEMS = Sets.immutableEnumSet(BRONZE_DAGGER, BRONZE_AXE, BRONZE_MACE, BRONZE_MED_HELM, BRONZE_DART_TIPS, BRONZE_SWORD, BRONZE_ARROWTIPS, BRONZE_SCIMITAR, BRONZE_LONG_SWORD,
                BRONZE_THROWING_KNIVES, BRONZE_FULL_HELM, BRONZE_SQUARE_SHIELD, BRONZE_WARHAMMER, BRONZE_BATTLE_AXE, BRONZE_CHAINBODY, BRONZE_KITE_SHIELD,
                BRONZE_CLAWS, BRONZE_2_HAND_SWORD, BRONZE_PLATESKIRT, BRONZE_PLATELEGS, BRONZE_PLATEBODY, BRONZE_NAILS, BRONZE_UNF_BOLTS);
        private final String name;
        private final int barId;
        private final int itemId;
        private final int amount;
        private final int itemFrame;
        private final int itemSlot;
        private final int nameFrame;
        private final int requiredLevel;
        private final int barsRequired;
        private final int barFrame;

        SmithableEquipment(String name, int barId, int itemId, int amount, int itemFrame, int itemSlot, int nameFrame, int requiredLevel, int barsRequired, int barFrame) {
            this.name = name;
            this.barId = barId;
            this.itemId = itemId;
            this.amount = amount;
            this.itemFrame = itemFrame;
            this.itemSlot = itemSlot;
            this.nameFrame = nameFrame;
            this.requiredLevel = requiredLevel;
            this.barsRequired = barsRequired;
            this.barFrame = barFrame;
        }

        public int getItemId() {
            return itemId;
        }

        public int getAmount() {
            return amount;
        }

        public int getItemFrame() {
            return itemFrame;
        }

        public int getItemSlot() {
            return itemSlot;
        }

        public int getNameFrame() {
            return nameFrame;
        }

        public int getRequiredLevel() {
            return requiredLevel;
        }

        public int getBarsRequired() {
            return barsRequired;
        }

        public int getBarFrame() {
            return barFrame;
        }

        public int getBarId() {
            return barId;
        }

        public String getName() {
            return name;
        }
    }

    /**
     * Represents a bar which can be created by using
     * the required ores with a furnace.
     */
    public enum Bar {
        BRONZE_BAR(2349, new RequiredItem[]{new RequiredItem(new Item(438), true), new RequiredItem(new Item(436), true)}, 1, 120, 2405, new int[][]{{3987, 1}, {3986, 5}, {2807, 10}, {2414, -1}}, Optional.of(SmithableEquipment.BRONZE_ITEMS)),
        IRON_BAR(2351, new RequiredItem[]{new RequiredItem(new Item(440), true)}, 15, 540, 2406, new int[][]{{3991, 1}, {3990, 5}, {3989, 10}, {3988, -1}}, Optional.of(SmithableEquipment.IRON_ITEMS)),
        SILVER_BAR(2355, new RequiredItem[]{new RequiredItem(new Item(442), true)}, 20, 725, 2407, new int[][]{{3995, 1}, {3994, 5}, {3993, 10}, {3992, -1}}, Optional.empty()),
        STEEL_BAR(2353, new RequiredItem[]{new RequiredItem(new Item(440), true), new RequiredItem(new Item(453, 2), true)}, 30, 1350, 2409, new int[][]{{3999, 1}, {3998, 5}, {3997, 10}, {3996, -1}}, Optional.of(SmithableEquipment.STEEL_ITEMS)),
        GOLD_BAR(2357, new RequiredItem[]{new RequiredItem(new Item(444), true)}, 40, 2400, 2410, new int[][]{{4003, 1}, {4002, 5}, {4001, 10}, {4000, -1}}, Optional.empty()),
        MITHRIL_BAR(2359, new RequiredItem[]{new RequiredItem(new Item(447), true), new RequiredItem(new Item(453, 4), true)}, 50, 3450, 2411, new int[][]{{7441, 1}, {7440, 5}, {6397, 10}, {4158, -1}}, Optional.of(SmithableEquipment.MITHRIL_ITEMS)),
        ADAMANTITE_BAR(2361, new RequiredItem[]{new RequiredItem(new Item(449), true), new RequiredItem(new Item(453, 6), true)}, 70, 4500, 2412, new int[][]{{7446, 1}, {7444, 5}, {7443, 10}, {7442, -1}}, Optional.of(SmithableEquipment.ADAMANT_ITEMS)),
        RUNITE_BAR(2363, new RequiredItem[]{new RequiredItem(new Item(451), true), new RequiredItem(new Item(453, 8), true)}, 85, 5560, 2413, new int[][]{{7450, 1}, {7449, 5}, {7448, 10}, {7447, -1}}, Optional.of(SmithableEquipment.RUNE_ITEMS)),;

        private static Map<Integer, Bar> smeltables = new HashMap<Integer, Bar>();

        static {
            for (Bar s : Bar.values()) {
                smeltables.put(s.getBar(), s);
            }
        }

        private final int bar;
        private final RequiredItem[] ores;
        private final int levelReq;
        private final int xpReward;
        private final int frame;
        private final int[][] buttons;
        private final Optional<ImmutableSet<SmithableEquipment>> items;

        Bar(int bar, RequiredItem[] ores, int levelReq, int xpReward, int frame, int[][] buttons, Optional<ImmutableSet<SmithableEquipment>> items) {
            this.bar = bar;
            this.ores = ores;
            this.levelReq = levelReq;
            this.xpReward = xpReward;
            this.frame = frame;
            this.buttons = buttons;
            this.items = items;
        }

        public static Optional<Bar> forBarId(int barId) {
            return Optional.ofNullable(smeltables.get(barId));
        }

        public int getBar() {
            return bar;
        }

        public RequiredItem[] getOres() {
            return ores;
        }

        public int getLevelReq() {
            return levelReq;
        }

        public int getXpReward() {
            return xpReward;
        }

        public int getFrame() {
            return frame;
        }

        public Optional<ImmutableSet<SmithableEquipment>> getItems() {
            return items;
        }

        public int[][] getButtons() {
            return buttons;
        }
    }

    /**
     * Handles making equipment from bars.
     *
     * @author Professor Oak
     */
    public static final class EquipmentMaking {

        /**
         * The interface used for creating equipment using the
         * Smithing skill.
         */
        public static final int EQUIPMENT_CREATION_INTERFACE_ID = 994;

        /**
         * The interface ids used for selecting an item to create in the
         * {@code EQUIPMENT_CREATION_INTERFACE_ID}.
         */
        public static final int EQUIPMENT_CREATION_COLUMN_1 = 1119;
        public static final int EQUIPMENT_CREATION_COLUMN_2 = 1120;
        public static final int EQUIPMENT_CREATION_COLUMN_3 = 1121;
        public static final int EQUIPMENT_CREATION_COLUMN_4 = 1122;
        public static final int EQUIPMENT_CREATION_COLUMN_5 = 1123;

        /**
         * This method is triggered when a player clicks
         * on an anvil in the game.
         * <p>
         * We will search for bars and then open the
         * corresponding interface if one was found.
         *
         * @param player
         */
        public static void openInterface(Player player) {
            //Search for bar..
            Optional<Bar> bar = Optional.empty();
            for (Bar b : Bar.values()) {
                if (!b.getItems().isPresent()) {
                    continue;
                }
                if (player.getInventory().contains(b.getBar())) {
                    if (player.getSkillManager().getCurrentLevel(Skill.SMITHING) >= b.getLevelReq()) {
                        bar = Optional.of(b);
                    }
                }
            }

            //Did we find a bar in the player's inventory?
            if (bar.isPresent()) {
                //First, clear the interface from items..
                for (int i = 1119; i <= 1123; i++) {
                    player.getPacketSender().clearItemOnInterface(i);
                }

                //Clear slots that aren't always used..
                player.getPacketSender()
                        .sendString(1132, "")
                        .sendString(1096, "")
                        .sendString(1135, "")
                        .sendString(1134, "");

                //Go through the bar's items..
                for (SmithableEquipment b : bar.get().getItems().get()) {
                    player.getPacketSender().sendSmithingData(b.getItemId(), b.getItemSlot(), b.getItemFrame(), b.getAmount());
                    String barColor = "@red@";
                    String itemColor = "@bla@";
                    if (player.getInventory().getAmount(b.getBarId()) >= b.getBarsRequired()) {
                        barColor = "@gre@";
                    }
                    if (player.getSkillManager().getCurrentLevel(Skill.SMITHING) >= b.getRequiredLevel()) {
                        itemColor = "@whi@";
                    }
                    player.getPacketSender().sendString(b.getBarFrame(), barColor + Integer.toString(b.getBarsRequired()) + " " + (b.getBarsRequired() > 1 ? "bars" : "bar"));
                    player.getPacketSender().sendString(b.getNameFrame(), itemColor + b.getName());
                }

                //Send interface..
                player.getPacketSender().sendInterface(EQUIPMENT_CREATION_INTERFACE_ID);
            } else {
                player.getPacketSender().sendMessage("You don't have any bars in your inventory which can be used with your Smithing level.");
            }
        }

        /**
         * Attempts to initialize a new {@link SKillabl
         *
         * @param itemId
         * @param interfaceId
         * @param slot
         * @param amount
         */
        public static void initialize(Player player, int itemId, int interfaceId, int slot, int amount) {
            //First verify the item we're trying to make..
            for (SmithableEquipment smithable : SmithableEquipment.values()) {
                if (smithable.getItemId() == itemId && smithable.getItemFrame() == interfaceId
                        && smithable.getItemSlot() == slot) {
                    //Start making items..
                    player.getSkillManager().startSkillable(new ItemCreationSkillable(Arrays.asList(new RequiredItem(new Item(HAMMER)), new RequiredItem(new Item(smithable.getBarId(), smithable.getBarsRequired()), true)),
                            new Item(smithable.getItemId(), smithable.getAmount()), amount, Optional.of(new AnimationLoop(new Animation(898), 3)), smithable.getRequiredLevel(), 10, Skill.SMITHING));
                    break;
                }
            }
        }
    }

    /**
     * Handles smelting ores to combine them into bars.
     *
     * @author Professor Oak
     */
    public static final class Smelting extends ItemCreationSkillable {
        /**
         * The {@link Animation} the character will perform
         * when smelting.
         */
        private static final Animation ANIMATION = new Animation(896);

        /**
         * The bar being smelted.
         */
        private final Bar bar;

        /**
         * Constructs this {@link Smelting} instance.
         *
         * @param bar
         * @param amount
         */
        public Smelting(Bar bar, int amount) {
            super(Arrays.asList(bar.getOres()), new Item(bar.getBar()), amount,
                    Optional.of(new AnimationLoop(ANIMATION, 4)), bar.getLevelReq(), bar.getXpReward(), Skill.SMITHING);
            this.bar = bar;
        }

        //Override finishedCycle because we need to handle special cases
        //such as Iron ore 50% chance of failing to smelt.
        @Override
        public void finishedCycle(Player player) {
            //Handle iron bar. It has a 50% chance of failing.
            if (bar == Bar.IRON_BAR) {
                if (Misc.getRandom(2) == 1) {
                    player.getPacketSender().sendMessage("The Iron ore was too impure and you were unable to make an Iron bar.");
                    //We still need to delete the ore and decrement amount.
                    filterRequiredItems(r -> r.isDelete()).forEach(r -> player.getInventory().delete(r.getItem()));
                    decrementAmount();
                    return;
                }
            }

            super.finishedCycle(player);
        }
    }
}
