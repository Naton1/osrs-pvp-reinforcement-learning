package com.elvarg.game.content.combat.ranged;

import java.util.HashMap;
import java.util.Map;

import com.elvarg.game.content.combat.CombatEquipment;
import com.elvarg.game.content.combat.CombatFactory;
import com.elvarg.game.content.combat.FightType;
import com.elvarg.game.entity.impl.Mobile;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.Graphic;
import com.elvarg.game.model.GraphicHeight;
import com.elvarg.game.model.Skill;
import com.elvarg.game.model.container.impl.Equipment;
import com.elvarg.game.task.impl.CombatPoisonEffect.PoisonType;
import com.elvarg.util.Misc;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

/**
 * A table of constants that hold data for all ranged ammo.
 * <p>
 * Edit: This is now purely only data.
 * Updated it and moved all methods to CombatFactory.
 *
 * @author Swiffy96
 * @author Professor Oak
 */
public class RangedData {

    /**
     * A map of items and their respective interfaces.
     */
    private static Map<Integer, RangedWeapon> rangedWeapons = new HashMap<>();
    private static Map<Integer, Ammunition> rangedAmmunition = new HashMap<>();

    @SuppressWarnings("incomplete-switch")
    public static double getSpecialEffectsMultiplier(Player p, Mobile target, int damage) {

        double multiplier = 1.0;

        //Todo: ENCHANTED_RUBY_BOLT
        switch (p.getCombat().getAmmunition()) {

            case ENCHANTED_DIAMOND_BOLT:

                target.performGraphic(new Graphic(758, GraphicHeight.MIDDLE));
                multiplier = 1.15;

                break;

            case ENCHANTED_DRAGON_BOLT:

                boolean multiply = true;
                if (target.isPlayer()) {
                    Player t = target.getAsPlayer();
                    multiply = !(!t.getCombat().getFireImmunityTimer().finished() || CombatEquipment.hasDragonProtectionGear(t));
                }

                if (multiply) {
                    target.performGraphic(new Graphic(756));
                    multiplier = 1.31;
                }

                break;
            case ENCHANTED_EMERALD_BOLT:

                target.performGraphic(new Graphic(752));
                CombatFactory.poisonEntity(target, PoisonType.MILD);

                break;
            case ENCHANTED_JADE_BOLT:

                target.performGraphic(new Graphic(755));
                multiplier = 1.05;

                break;
            case ENCHANTED_ONYX_BOLT:

                target.performGraphic(new Graphic(753));
                multiplier = 1.26;
                int heal = (int) (damage * 0.25) + 10;
                p.getSkillManager().setCurrentLevel(Skill.HITPOINTS, p.getSkillManager().getCurrentLevel(Skill.HITPOINTS) + heal);
                if (p.getSkillManager().getCurrentLevel(Skill.HITPOINTS) >= 1120) {
                    p.getSkillManager().setCurrentLevel(Skill.HITPOINTS, 1120);
                }
                p.getSkillManager().updateSkill(Skill.HITPOINTS);
                if (damage < 250 && Misc.getRandom(3) <= 1) {
                    damage += 150 + Misc.getRandom(80);
                }

                break;

            case ENCHANTED_PEARL_BOLT:


                target.performGraphic(new Graphic(750));
                multiplier = 1.1;


                break;

            case ENCHANTED_RUBY_BOLT:


                break;
            case ENCHANTED_SAPPHIRE_BOLT:

                target.performGraphic(new Graphic(751));
                if (target.isPlayer()) {
                    Player t = target.getAsPlayer();
                    t.getSkillManager().setCurrentLevel(Skill.PRAYER, t.getSkillManager().getCurrentLevel(Skill.PRAYER) - 20);
                    if (t.getSkillManager().getCurrentLevel(Skill.PRAYER) < 0) {
                        t.getSkillManager().setCurrentLevel(Skill.PRAYER, 0);
                    }
                    t.getPacketSender().sendMessage("Your Prayer level has been leeched.");

                    p.getSkillManager().setCurrentLevel(Skill.PRAYER, t.getSkillManager().getCurrentLevel(Skill.PRAYER) + 20);
                    if (p.getSkillManager().getCurrentLevel(Skill.PRAYER) > p.getSkillManager().getMaxLevel(Skill.PRAYER)) {
                        p.getSkillManager().setCurrentLevel(Skill.PRAYER, p.getSkillManager().getMaxLevel(Skill.PRAYER));
                    } else {
                        p.getPacketSender().sendMessage("Your enchanced bolts leech some Prayer points from your opponent..");
                    }
                }


                break;
            case ENCHANTED_TOPAZ_BOLT:

                target.performGraphic(new Graphic(757));
                if (target.isPlayer()) {
                    Player t = target.getAsPlayer();
                    t.getSkillManager().setCurrentLevel(Skill.MAGIC, t.getSkillManager().getCurrentLevel(Skill.MAGIC) - 3);
                    t.getPacketSender().sendMessage("Your Magic level has been reduced.");
                }

                break;
            case ENCHANTED_OPAL_BOLT:

                target.performGraphic(new Graphic(749));
                multiplier = 1.3;

                break;

        }

        return multiplier;
    }

    public enum RangedWeapon {

        LONGBOW(new int[]{839}, new Ammunition[]{Ammunition.BRONZE_ARROW}, RangedWeaponType.LONGBOW),
        SHORTBOW(new int[]{841}, new Ammunition[]{Ammunition.BRONZE_ARROW}, RangedWeaponType.SHORTBOW),
        OAK_LONGBOW(new int[]{845}, new Ammunition[]{Ammunition.BRONZE_ARROW, Ammunition.IRON_ARROW, Ammunition.STEEL_ARROW}, RangedWeaponType.LONGBOW),
        OAK_SHORTBOW(new int[]{843}, new Ammunition[]{Ammunition.BRONZE_ARROW, Ammunition.IRON_ARROW, Ammunition.STEEL_ARROW}, RangedWeaponType.SHORTBOW),
        WILLOW_LONGBOW(new int[]{847}, new Ammunition[]{Ammunition.BRONZE_ARROW, Ammunition.IRON_ARROW, Ammunition.STEEL_ARROW, Ammunition.MITHRIL_ARROW}, RangedWeaponType.LONGBOW),
        WILLOW_SHORTBOW(new int[]{849}, new Ammunition[]{Ammunition.BRONZE_ARROW, Ammunition.IRON_ARROW, Ammunition.STEEL_ARROW, Ammunition.MITHRIL_ARROW}, RangedWeaponType.SHORTBOW),
        MAPLE_LONGBOW(new int[]{851}, new Ammunition[]{Ammunition.BRONZE_ARROW, Ammunition.IRON_ARROW, Ammunition.STEEL_ARROW, Ammunition.MITHRIL_ARROW, Ammunition.ADAMANT_ARROW}, RangedWeaponType.LONGBOW),
        MAPLE_SHORTBOW(new int[]{853}, new Ammunition[]{Ammunition.BRONZE_ARROW, Ammunition.IRON_ARROW, Ammunition.STEEL_ARROW, Ammunition.MITHRIL_ARROW, Ammunition.ADAMANT_ARROW}, RangedWeaponType.SHORTBOW),
        YEW_LONGBOW(new int[]{855}, new Ammunition[]{Ammunition.BRONZE_ARROW, Ammunition.IRON_ARROW, Ammunition.STEEL_ARROW, Ammunition.MITHRIL_ARROW, Ammunition.ADAMANT_ARROW, Ammunition.RUNE_ARROW, Ammunition.ICE_ARROW}, RangedWeaponType.LONGBOW),
        YEW_SHORTBOW(new int[]{857}, new Ammunition[]{Ammunition.BRONZE_ARROW, Ammunition.IRON_ARROW, Ammunition.STEEL_ARROW, Ammunition.MITHRIL_ARROW, Ammunition.ADAMANT_ARROW, Ammunition.RUNE_ARROW, Ammunition.ICE_ARROW}, RangedWeaponType.SHORTBOW),
        MAGIC_LONGBOW(new int[]{859}, new Ammunition[]{Ammunition.BRONZE_ARROW, Ammunition.IRON_ARROW, Ammunition.STEEL_ARROW, Ammunition.MITHRIL_ARROW, Ammunition.ADAMANT_ARROW, Ammunition.RUNE_ARROW, Ammunition.ICE_ARROW, Ammunition.BROAD_ARROW}, RangedWeaponType.LONGBOW),
        MAGIC_SHORTBOW(new int[]{861, 6724}, new Ammunition[]{Ammunition.BRONZE_ARROW, Ammunition.IRON_ARROW, Ammunition.STEEL_ARROW, Ammunition.MITHRIL_ARROW, Ammunition.ADAMANT_ARROW, Ammunition.RUNE_ARROW, Ammunition.ICE_ARROW, Ammunition.BROAD_ARROW}, RangedWeaponType.SHORTBOW),
        GODBOW(new int[]{19143, 19149, 19146}, new Ammunition[]{Ammunition.BRONZE_ARROW, Ammunition.IRON_ARROW, Ammunition.STEEL_ARROW, Ammunition.MITHRIL_ARROW, Ammunition.ADAMANT_ARROW, Ammunition.RUNE_ARROW, Ammunition.BROAD_ARROW, Ammunition.DRAGON_ARROW}, RangedWeaponType.SHORTBOW),
        ZARYTE_BOW(new int[]{20171}, new Ammunition[]{Ammunition.BRONZE_ARROW, Ammunition.IRON_ARROW, Ammunition.STEEL_ARROW, Ammunition.MITHRIL_ARROW, Ammunition.ADAMANT_ARROW, Ammunition.RUNE_ARROW, Ammunition.BROAD_ARROW, Ammunition.DRAGON_ARROW}, RangedWeaponType.SHORTBOW),

        DARK_BOW(new int[]{11235, 13405, 15701, 15702, 15703, 15704}, new Ammunition[]{Ammunition.BRONZE_ARROW, Ammunition.IRON_ARROW, Ammunition.STEEL_ARROW, Ammunition.MITHRIL_ARROW, Ammunition.ADAMANT_ARROW, Ammunition.RUNE_ARROW, Ammunition.DRAGON_ARROW}, RangedWeaponType.LONGBOW),

        BRONZE_CROSSBOW(new int[]{9174}, new Ammunition[]{Ammunition.BRONZE_BOLT}, RangedWeaponType.CROSSBOW),
        IRON_CROSSBOW(new int[]{9177}, new Ammunition[]{Ammunition.BRONZE_BOLT, Ammunition.OPAL_BOLT, Ammunition.ENCHANTED_OPAL_BOLT, Ammunition.IRON_BOLT}, RangedWeaponType.CROSSBOW),
        STEEL_CROSSBOW(new int[]{9179}, new Ammunition[]{Ammunition.BRONZE_BOLT, Ammunition.OPAL_BOLT, Ammunition.ENCHANTED_OPAL_BOLT, Ammunition.IRON_BOLT, Ammunition.JADE_BOLT, Ammunition.ENCHANTED_JADE_BOLT, Ammunition.STEEL_BOLT, Ammunition.PEARL_BOLT, Ammunition.ENCHANTED_PEARL_BOLT}, RangedWeaponType.CROSSBOW),
        MITHRIL_CROSSBOW(new int[]{9181}, new Ammunition[]{Ammunition.BRONZE_BOLT, Ammunition.OPAL_BOLT, Ammunition.ENCHANTED_OPAL_BOLT, Ammunition.IRON_BOLT, Ammunition.JADE_BOLT, Ammunition.ENCHANTED_JADE_BOLT, Ammunition.STEEL_BOLT, Ammunition.PEARL_BOLT, Ammunition.ENCHANTED_PEARL_BOLT, Ammunition.MITHRIL_BOLT, Ammunition.TOPAZ_BOLT, Ammunition.ENCHANTED_TOPAZ_BOLT}, RangedWeaponType.CROSSBOW),
        ADAMANT_CROSSBOW(new int[]{9183}, new Ammunition[]{Ammunition.BRONZE_BOLT, Ammunition.OPAL_BOLT, Ammunition.ENCHANTED_OPAL_BOLT, Ammunition.IRON_BOLT, Ammunition.JADE_BOLT, Ammunition.ENCHANTED_JADE_BOLT, Ammunition.STEEL_BOLT, Ammunition.PEARL_BOLT, Ammunition.ENCHANTED_PEARL_BOLT, Ammunition.MITHRIL_BOLT, Ammunition.TOPAZ_BOLT, Ammunition.ENCHANTED_TOPAZ_BOLT, Ammunition.ADAMANT_BOLT, Ammunition.SAPPHIRE_BOLT, Ammunition.ENCHANTED_SAPPHIRE_BOLT, Ammunition.EMERALD_BOLT, Ammunition.ENCHANTED_EMERALD_BOLT, Ammunition.RUBY_BOLT, Ammunition.ENCHANTED_RUBY_BOLT}, RangedWeaponType.CROSSBOW),
        RUNE_CROSSBOW(new int[]{9185}, new Ammunition[]{Ammunition.BRONZE_BOLT, Ammunition.OPAL_BOLT, Ammunition.ENCHANTED_OPAL_BOLT, Ammunition.IRON_BOLT, Ammunition.JADE_BOLT, Ammunition.ENCHANTED_JADE_BOLT, Ammunition.STEEL_BOLT, Ammunition.PEARL_BOLT, Ammunition.ENCHANTED_PEARL_BOLT, Ammunition.MITHRIL_BOLT, Ammunition.TOPAZ_BOLT, Ammunition.ENCHANTED_TOPAZ_BOLT, Ammunition.ADAMANT_BOLT, Ammunition.SAPPHIRE_BOLT, Ammunition.ENCHANTED_SAPPHIRE_BOLT, Ammunition.EMERALD_BOLT, Ammunition.ENCHANTED_EMERALD_BOLT, Ammunition.RUBY_BOLT, Ammunition.ENCHANTED_RUBY_BOLT, Ammunition.RUNITE_BOLT, Ammunition.BROAD_BOLT, Ammunition.DIAMOND_BOLT, Ammunition.ENCHANTED_DIAMOND_BOLT, Ammunition.ONYX_BOLT, Ammunition.ENCHANTED_ONYX_BOLT, Ammunition.DRAGON_BOLT, Ammunition.ENCHANTED_DRAGON_BOLT}, RangedWeaponType.CROSSBOW),
        ARMADYL_CROSSBOW(new int[]{11785}, new Ammunition[]{Ammunition.BRONZE_BOLT, Ammunition.OPAL_BOLT, Ammunition.ENCHANTED_OPAL_BOLT, Ammunition.IRON_BOLT, Ammunition.JADE_BOLT, Ammunition.ENCHANTED_JADE_BOLT, Ammunition.STEEL_BOLT, Ammunition.PEARL_BOLT, Ammunition.ENCHANTED_PEARL_BOLT, Ammunition.MITHRIL_BOLT, Ammunition.TOPAZ_BOLT, Ammunition.ENCHANTED_TOPAZ_BOLT, Ammunition.ADAMANT_BOLT, Ammunition.SAPPHIRE_BOLT, Ammunition.ENCHANTED_SAPPHIRE_BOLT, Ammunition.EMERALD_BOLT, Ammunition.ENCHANTED_EMERALD_BOLT, Ammunition.RUBY_BOLT, Ammunition.ENCHANTED_RUBY_BOLT, Ammunition.RUNITE_BOLT, Ammunition.BROAD_BOLT, Ammunition.DIAMOND_BOLT, Ammunition.ENCHANTED_DIAMOND_BOLT, Ammunition.ONYX_BOLT, Ammunition.ENCHANTED_ONYX_BOLT, Ammunition.DRAGON_BOLT, Ammunition.ENCHANTED_DRAGON_BOLT}, RangedWeaponType.CROSSBOW),

        BRONZE_DART(new int[]{806}, new Ammunition[]{Ammunition.BRONZE_DART}, RangedWeaponType.DART),
        IRON_DART(new int[]{807}, new Ammunition[]{Ammunition.IRON_DART}, RangedWeaponType.DART),
        STEEL_DART(new int[]{808}, new Ammunition[]{Ammunition.STEEL_DART}, RangedWeaponType.DART),
        MITHRIL_DART(new int[]{809}, new Ammunition[]{Ammunition.MITHRIL_DART}, RangedWeaponType.DART),
        ADAMANT_DART(new int[]{810}, new Ammunition[]{Ammunition.ADAMANT_DART}, RangedWeaponType.DART),
        RUNE_DART(new int[]{811}, new Ammunition[]{Ammunition.RUNE_DART}, RangedWeaponType.DART),
        DRAGON_DART(new int[]{11230}, new Ammunition[]{Ammunition.DRAGON_DART}, RangedWeaponType.DART),

        BRONZE_KNIFE(new int[]{864, 870, 5654}, new Ammunition[]{Ammunition.BRONZE_KNIFE}, RangedWeaponType.KNIFE),
        IRON_KNIFE(new int[]{863, 871, 5655}, new Ammunition[]{Ammunition.IRON_KNIFE}, RangedWeaponType.KNIFE),
        STEEL_KNIFE(new int[]{865, 872, 5656}, new Ammunition[]{Ammunition.STEEL_KNIFE}, RangedWeaponType.KNIFE),
        BLACK_KNIFE(new int[]{869, 874, 5658}, new Ammunition[]{Ammunition.BLACK_KNIFE}, RangedWeaponType.KNIFE),
        MITHRIL_KNIFE(new int[]{866, 873, 5657}, new Ammunition[]{Ammunition.MITHRIL_KNIFE}, RangedWeaponType.KNIFE),
        ADAMANT_KNIFE(new int[]{867, 875, 5659}, new Ammunition[]{Ammunition.ADAMANT_KNIFE}, RangedWeaponType.KNIFE),
        RUNE_KNIFE(new int[]{868, 876, 5660, 5667}, new Ammunition[]{Ammunition.RUNE_KNIFE}, RangedWeaponType.KNIFE),


        TOKTZ_XIL_UL(new int[]{6522}, new Ammunition[]{Ammunition.TOKTZ_XIL_UL}, RangedWeaponType.TOKTZ_XIL_UL),

        KARILS_CROSSBOW(new int[]{4734}, new Ammunition[]{Ammunition.BOLT_RACK}, RangedWeaponType.CROSSBOW),

        BALLISTA(new int[]{19478, 19481}, new Ammunition[]{Ammunition.BRONZE_JAVELIN, Ammunition.IRON_JAVELIN, Ammunition.STEEL_JAVELIN, Ammunition.MITHRIL_JAVELIN, Ammunition.ADAMANT_JAVELIN, Ammunition.RUNE_JAVELIN, Ammunition.DRAGON_JAVELIN}, RangedWeaponType.BALLISTA),

        TOXIC_BLOWPIPE(new int[]{12926}, new Ammunition[]{Ammunition.DRAGON_DART}, RangedWeaponType.BLOWPIPE);

        static {
            for (RangedWeapon data : RangedWeapon.values()) {
                for (int i : data.getWeaponIds()) {
                    rangedWeapons.put(i, data);
                }
            }
        }

        private int[] weaponIds;
        private Ammunition[] ammunitionData;
        private RangedWeaponType type;

        RangedWeapon(int[] weaponIds, Ammunition[] ammunitionData, RangedWeaponType type) {
            this.weaponIds = weaponIds;
            this.ammunitionData = ammunitionData;
            this.type = type;
        }

        public static RangedWeapon getFor(Player p) {
            int weapon = p.getEquipment().getItems()[Equipment.WEAPON_SLOT].getId();
            return rangedWeapons.get(weapon);
        }

        public int[] getWeaponIds() {
            return weaponIds;
        }

        public Ammunition[] getAmmunitionData() {
            return ammunitionData;
        }

        public RangedWeaponType getType() {
            return type;
        }
    }

    public enum Ammunition {

        BRONZE_ARROW(882, new Graphic(19, GraphicHeight.HIGH), 10, 7),
        IRON_ARROW(884, new Graphic(18, GraphicHeight.HIGH), 9, 10),
        STEEL_ARROW(886, new Graphic(20, GraphicHeight.HIGH), 11, 16),
        MITHRIL_ARROW(888, new Graphic(21, GraphicHeight.HIGH), 12, 22),
        ADAMANT_ARROW(890, new Graphic(22, GraphicHeight.HIGH), 13, 31),
        RUNE_ARROW(892, new Graphic(24, GraphicHeight.HIGH), 15, 50),
        ICE_ARROW(78, new Graphic(25, GraphicHeight.HIGH), 16, 58),
        BROAD_ARROW(4160, new Graphic(20, GraphicHeight.HIGH), 11, 58),
        DRAGON_ARROW(11212, new Graphic(1111, GraphicHeight.HIGH), 1120, 65),

        BRONZE_BOLT(877, new Graphic(955, GraphicHeight.HIGH), 27, 13),
        OPAL_BOLT(879, new Graphic(955, GraphicHeight.HIGH), 27, 20),
        ENCHANTED_OPAL_BOLT(9236, new Graphic(955, GraphicHeight.HIGH), 27, 20),
        IRON_BOLT(9140, new Graphic(955, GraphicHeight.HIGH), 27, 28),
        JADE_BOLT(9335, new Graphic(955, GraphicHeight.HIGH), 27, 31),
        ENCHANTED_JADE_BOLT(9237, new Graphic(955, GraphicHeight.HIGH), 27, 31),
        STEEL_BOLT(9141, new Graphic(955, GraphicHeight.HIGH), 27, 35),
        PEARL_BOLT(880, new Graphic(955, GraphicHeight.HIGH), 27, 38),
        ENCHANTED_PEARL_BOLT(9238, new Graphic(955, GraphicHeight.HIGH), 27, 38),
        MITHRIL_BOLT(9142, new Graphic(955, GraphicHeight.HIGH), 27, 40),
        TOPAZ_BOLT(9336, new Graphic(955, GraphicHeight.HIGH), 27, 50),
        ENCHANTED_TOPAZ_BOLT(9239, new Graphic(955, GraphicHeight.HIGH), 27, 50),
        ADAMANT_BOLT(9143, new Graphic(955, GraphicHeight.HIGH), 27, 60),
        SAPPHIRE_BOLT(9337, new Graphic(955, GraphicHeight.HIGH), 27, 65),
        ENCHANTED_SAPPHIRE_BOLT(9240, new Graphic(955, GraphicHeight.HIGH), 27, 65),
        EMERALD_BOLT(9338, new Graphic(955, GraphicHeight.HIGH), 27, 70),
        ENCHANTED_EMERALD_BOLT(9241, new Graphic(955, GraphicHeight.HIGH), 27, 70),
        RUBY_BOLT(9339, new Graphic(955, GraphicHeight.HIGH), 27, 75),
        ENCHANTED_RUBY_BOLT(9242, new Graphic(955, GraphicHeight.HIGH), 27, 75),
        BROAD_BOLT(13280, new Graphic(955, GraphicHeight.HIGH), 27, 100),
        RUNITE_BOLT(9144, new Graphic(955, GraphicHeight.HIGH), 27, 115),
        DIAMOND_BOLT(9340, new Graphic(955, GraphicHeight.HIGH), 27, 105),
        ENCHANTED_DIAMOND_BOLT(9243, new Graphic(955, GraphicHeight.HIGH), 27, 105),
        DRAGON_BOLT(9341, new Graphic(955, GraphicHeight.HIGH), 27, 117),
        ENCHANTED_DRAGON_BOLT(9244, new Graphic(955, GraphicHeight.HIGH), 27, 117),
        ONYX_BOLT(9342, new Graphic(955, GraphicHeight.HIGH), 27, 120),
        ENCHANTED_ONYX_BOLT(9245, new Graphic(955, GraphicHeight.HIGH), 27, 120),

        BRONZE_DART(806, new Graphic(232, GraphicHeight.HIGH), 226, 1),
        IRON_DART(807, new Graphic(233, GraphicHeight.HIGH), 227, 4),
        STEEL_DART(808, new Graphic(234, GraphicHeight.HIGH), 228, 6),
        MITHRIL_DART(809, new Graphic(235, GraphicHeight.HIGH), 229, 8),
        ADAMANT_DART(810, new Graphic(236, GraphicHeight.HIGH), 230, 13),
        RUNE_DART(811, new Graphic(237, GraphicHeight.HIGH), 231, 17),
        DRAGON_DART(11230, new Graphic(1123, GraphicHeight.HIGH), 226, 24),

        BRONZE_KNIFE(864, new Graphic(219, GraphicHeight.HIGH), 212, 3),
        BRONZE_KNIFE_P1(870, new Graphic(219, GraphicHeight.HIGH), 212, 3),
        BRONZE_KNIFE_P2(5654, new Graphic(219, GraphicHeight.HIGH), 212, 3),
        BRONZE_KNIFE_P3(5661, new Graphic(219, GraphicHeight.HIGH), 212, 3),

        IRON_KNIFE(863, new Graphic(220, GraphicHeight.HIGH), 213, 4),
        IRON_KNIFE_P1(871, new Graphic(220, GraphicHeight.HIGH), 213, 4),
        IRON_KNIFE_P2(5655, new Graphic(220, GraphicHeight.HIGH), 213, 4),
        IRON_KNIFE_P3(5662, new Graphic(220, GraphicHeight.HIGH), 213, 4),

        STEEL_KNIFE(865, new Graphic(221, GraphicHeight.HIGH), 214, 7),
        STEEL_KNIFE_P1(872, new Graphic(221, GraphicHeight.HIGH), 214, 7),
        STEEL_KNIFE_P2(5656, new Graphic(221, GraphicHeight.HIGH), 214, 7),
        STEEL_KNIFE_P3(5663, new Graphic(221, GraphicHeight.HIGH), 214, 7),

        BLACK_KNIFE(869, new Graphic(222, GraphicHeight.HIGH), 215, 8),
        BLACK_KNIFE_P1(874, new Graphic(222, GraphicHeight.HIGH), 215, 8),
        BLACK_KNIFE_P2(5658, new Graphic(222, GraphicHeight.HIGH), 215, 8),
        BLACK_KNIFE_P3(5665, new Graphic(222, GraphicHeight.HIGH), 215, 8),

        MITHRIL_KNIFE(866, new Graphic(223, GraphicHeight.HIGH), 215, 10),
        MITHRIL_KNIFE_P1(873, new Graphic(223, GraphicHeight.HIGH), 215, 10),
        MITHRIL_KNIFE_P2(5657, new Graphic(223, GraphicHeight.HIGH), 215, 10),
        MITHRIL_KNIFE_P3(5664, new Graphic(223, GraphicHeight.HIGH), 215, 10),

        ADAMANT_KNIFE(867, new Graphic(224, GraphicHeight.HIGH), 217, 14),
        ADAMANT_KNIFE_P1(875, new Graphic(224, GraphicHeight.HIGH), 217, 14),
        ADAMANT_KNIFE_P2(5659, new Graphic(224, GraphicHeight.HIGH), 217, 14),
        ADAMANT_KNIFE_P3(5666, new Graphic(224, GraphicHeight.HIGH), 217, 14),

        RUNE_KNIFE(868, new Graphic(225, GraphicHeight.HIGH), 218, 24),
        RUNE_KNIFE_P1(876, new Graphic(225, GraphicHeight.HIGH), 218, 24),
        RUNE_KNIFE_P2(5660, new Graphic(225, GraphicHeight.HIGH), 218, 24),
        RUNE_KNIFE_P3(5667, new Graphic(225, GraphicHeight.HIGH), 218, 24),

		/*	BRONZE_THROWNAXE(800, 43, 36, 3, 44, 7),
        IRON_THROWNAXE(801, 42, 35, 3, 44, 9),
		STEEL_THROWNAXE(802, 44, 37, 3, 44, 11),
		MITHRIL_THROWNAXE(803, 45, 38, 3, 44, 13),
		ADAMANT_THROWNAXE(804, 46, 39, 3, 44, 15),
		RUNE_THROWNAXE(805, 48, 41, 3, 44, 17),*/

        BRONZE_JAVELIN(825, null, 200, 25),
        IRON_JAVELIN(826, null, 201, 42),
        STEEL_JAVELIN(827, null, 202, 64),
        MITHRIL_JAVELIN(828, null, 203, 85),
        ADAMANT_JAVELIN(829, null, 204, 107),
        RUNE_JAVELIN(830, null, 205, 124),
        DRAGON_JAVELIN(19484, null, 1301, 150),

        TOKTZ_XIL_UL(6522, null, 442, 58),

        BOLT_RACK(4740, null, 27, 55),;

        //Ammo that shouldn't be dropped on the floor
        private static final ImmutableSet<Ammunition> NO_GROUND_DROP = Sets.immutableEnumSet(BRONZE_JAVELIN, IRON_JAVELIN, STEEL_JAVELIN, ADAMANT_JAVELIN, RUNE_JAVELIN, DRAGON_JAVELIN);

        static {
            for (Ammunition data : Ammunition.values()) {
                rangedAmmunition.put(data.getItemId(), data);
            }
        }

        private final Graphic startGfx;
        private int itemId;
        private int projectileId;
        private int strength;

        Ammunition(int itemId, Graphic startGfx, int projectileId, int strength) {
            this.itemId = itemId;
            this.startGfx = startGfx;
            this.projectileId = projectileId;
            this.strength = strength;
        }

        public static Ammunition getFor(Player p) {
            //First try to get a throw weapon as ammo
            int weapon = p.getEquipment().getItems()[Equipment.WEAPON_SLOT].getId();
            Ammunition throwWeapon = rangedAmmunition.get(weapon);

            //Toxic blowpipe should always fire dragon darts.
            if (weapon == 12926) {
                return Ammunition.DRAGON_DART;
            }

            //Didnt find one. Try arrows
            if (throwWeapon == null) {
                return rangedAmmunition.get(p.getEquipment().getItems()[Equipment.AMMUNITION_SLOT].getId());
            }

            return throwWeapon;
        }

        public static Ammunition getFor(int item) {
            //First try to get a throw weapon as ammo
            Ammunition throwWeapon = rangedAmmunition.get(item);

            //Didnt find one. Try arrows
            if (throwWeapon == null) {
                return rangedAmmunition.get(item);
            }

            return throwWeapon;
        }

        public int getItemId() {
            return itemId;
        }

        public Graphic getStartGraphic() {
            return startGfx;
        }

        public int getProjectileId() {
            return projectileId;
        }

        public int getStrength() {
            return strength;
        }

        public boolean dropOnFloor() {
            return !NO_GROUND_DROP.contains(this);
        }
    }

    public enum RangedWeaponType {
        KNIFE(4, 6, FightType.KNIFE_LONGRANGE),
        DART(3, 5, FightType.DART_LONGRANGE),
        TOKTZ_XIL_UL(5, 6, FightType.OBBY_RING_LONGRANGE),
        LONGBOW(9, 10, FightType.LONGBOW_LONGRANGE),
        BLOWPIPE(5, 7, FightType.BLOWPIPE_LONGRANGE),
        SHORTBOW(7, 9, FightType.SHORTBOW_LONGRANGE),
        CROSSBOW(7, 9, FightType.CROSSBOW_LONGRANGE),
        BALLISTA(7, 9, FightType.BALLISTA_LONGRANGE),
        ;
    	
    	private final FightType longRangeFightType;
        private final int defaultDistance;
        private final int longRangeDistance;

        RangedWeaponType(int defaultDistance, int longRangeDistance, FightType longRangeFightType) {
            this.defaultDistance = defaultDistance;
            this.longRangeDistance = longRangeDistance;
            this.longRangeFightType = longRangeFightType;
        }

        public int getDefaultDistance() {
            return defaultDistance;
        }
        
        public int getLongRangeDistance() {
        	return longRangeDistance;
        }

		public FightType getLongRangeFightType() {
			return longRangeFightType;
		}
    }

}
