package com.elvarg.game.content.skill.skillable.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.elvarg.game.definition.ItemDefinition;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.Animation;
import com.elvarg.game.model.AnimationLoop;
import com.elvarg.game.model.Item;
import com.elvarg.game.model.RequiredItem;
import com.elvarg.game.model.Skill;
import com.elvarg.game.model.menu.CreationMenu;
import com.elvarg.util.ItemIdentifiers;

/**
 * Represents the Fletching skill which can be used
 * to create bows, arrows and other items.
 *
 * @author Professor Oak
 */
//TODO: Clean up, merge enums.
public class Fletching extends ItemIdentifiers {

    /**
     * The animation a player will perform when cutting logs.
     */
    private static final Animation CUTTING_LOGS_ANIM = new Animation(1248);

    /**
     * Attempts to fletch ammo.
     *
     * @param player
     * @param itemUsed
     * @param itemUsedWith
     * @return
     */
    public static boolean fletchAmmo(Player player, int itemUsed, int itemUsedWith) {
        //Making ammo such as bolts and arrows..
        for (FletchableAmmo ammo : FletchableAmmo.values()) {
            if ((ammo.getItem1() == itemUsed || ammo.getItem1() == itemUsedWith)
                    && (ammo.getItem2() == itemUsed || ammo.getItem2() == itemUsedWith)) {
                if (player.getSkillManager().getCurrentLevel(Skill.FLETCHING) >= ammo.getLevelReq()) {
                    if (player.getInventory().getAmount(ammo.getItem1()) >= 10 && player.getInventory().getAmount(ammo.getItem2()) >= 10) {
                        player.getInventory().delete(ammo.getItem1(), 10);
                        player.getInventory().delete(ammo.getItem2(), 10);
                        player.getInventory().add(ammo.getOutcome(), 10);
                        player.getSkillManager().addExperience(Skill.FLETCHING, ammo.getXp());
                        String name = ItemDefinition.forId(ammo.getOutcome()).getName();
                        if (!name.endsWith("s"))
                            name += "s";
                        player.getPacketSender().sendMessage("You make some " + name + ".");
                    } else {
                        player.getPacketSender().sendMessage("You must have at least 10 of each supply when fletching a set.");
                    }
                } else {
                    player.getPacketSender().sendMessage("You need a Fletching level of at least " + ammo.getLevelReq() + " to fletch this.");
                }
                return true;
            }
        }
        return false;
    }

    /**
     * Attempts to fletch crossbows.
     *
     * @param player
     * @param itemUsed
     * @param itemUsedWith
     * @return
     */
    public static boolean fletchCrossbow(Player player, int itemUsed, int itemUsedWith) {
        for (FletchableCrossbow c : FletchableCrossbow.values()) {
            if ((c.getStock() == itemUsed || c.getStock() == itemUsedWith)
                    && (c.getLimbs() == itemUsed || c.getLimbs() == itemUsedWith)) {                
                player.getPacketSender().sendCreationMenu(new CreationMenu("How many would you like to make?", Arrays.asList(c.getUnstrung()), (itemId, amount) -> {
                    player.getSkillManager().startSkillable(new ItemCreationSkillable(Arrays.asList(new RequiredItem(new Item(c.getStock()), true), new RequiredItem(new Item(c.getLimbs()), true)), new Item(c.getUnstrung()), amount, Optional.empty(), c.getLevel(), c.getLimbsExp(), Skill.FLETCHING));
                }));
                return true;
            }
        }
        return false;
    }

    /**
     * Attempts to string a bow.
     *
     * @param player
     * @param itemUsed
     * @param itemUsedWith
     * @return
     */
    public static boolean stringBow(Player player, int itemUsed, int itemUsedWith) {
        if (itemUsed == BOW_STRING || itemUsedWith == BOW_STRING || itemUsed == CROSSBOW_STRING || itemUsedWith == CROSSBOW_STRING) {
            int string = itemUsed == BOW_STRING || itemUsed == CROSSBOW_STRING ? itemUsed : itemUsedWith;
            int unstrung = itemUsed == BOW_STRING || itemUsed == CROSSBOW_STRING ? itemUsedWith : itemUsed;
            StringableBow bow = StringableBow.unstrungBows.get(unstrung);
            if (bow != null) {
                if (bow.getBowStringId() == string) {
                    player.getPacketSender().sendCreationMenu(new CreationMenu("How many would you like to make?", Arrays.asList(bow.getResult()), (itemId, amount) -> {
                        player.getSkillManager().startSkillable(new ItemCreationSkillable(Arrays.asList(new RequiredItem(new Item(bow.getItemId()), true), new RequiredItem(new Item(bow.getBowStringId()), true)), new Item(bow.getResult()), amount, Optional.of(new AnimationLoop(bow.getAnimation(), 3)), bow.getLevelReq(), bow.getExp(), Skill.FLETCHING));
                    }));
                    return true;
                } else {
                    player.getPacketSender().sendMessage("This bow cannot be strung with that.");
                    return false;
                }
            }
        }
        return false;
    }

    /**
     * Attempts to fletch logs.
     *
     * @param player
     * @param itemUsed
     * @param itemUsedWith
     * @return
     */
    public static boolean fletchLog(Player player, int itemUsed, int itemUsedWith) {
        if (itemUsed == ItemIdentifiers.KNIFE || itemUsedWith == ItemIdentifiers.KNIFE) {
            int logId = itemUsed == ItemIdentifiers.KNIFE ? itemUsedWith : itemUsed;
            FletchableLog list = FletchableLog.logs.get(logId);
            if (list != null) {                
                List<Integer> products = new ArrayList<>();
                for (int i = 0; i < list.getFletchable().length; i++) {
                    products.add(list.getFletchable()[i].getProduct().getId());
                }
                CreationMenu menu = new CreationMenu("What would you like to make?", products, (itemId, amount) -> {
                    for (FletchableItem fl : list.getFletchable()) {
                        if (fl.getProduct().getId() == itemId) {
                            player.getSkillManager().startSkillable(new ItemCreationSkillable(Arrays.asList(new RequiredItem(new Item(KNIFE), false), new RequiredItem(new Item(list.getLogId()), true)), fl.getProduct(), amount, Optional.of(new AnimationLoop(fl.getAnimation(), 3)), fl.getLevelRequired(), fl.getExperience(), Skill.FLETCHING));
                        }
                    }
                });
                player.getPacketSender().sendCreationMenu(menu);
                return true;
            }
        }
        return false;
    }

    /**
     * Represents ammo which can be made
     * using the Fletching skill.
     *
     * @author Professor Oak
     */
    public enum FletchableAmmo {
        HEADLESS_ARROWS(52, 314, 53, 15, 1),

        BRONZE_ARROWS(53, 39, 882, 20, 1),
        IRON_ARROWS(53, 40, 884, 38, 15),
        STEEL_ARROWS(53, 41, 886, 75, 30),
        MITHRIL_ARROWS(53, 42, 888, 113, 45),
        ADAMANT_ARROWS(53, 43, 890, 150, 60),
        RUNE_ARROWS(53, 44, 892, 188, 75),
        DRAGON_ARROWS(53, 11237, 11212, 225, 90),

        BRONZE_DARTS(314, 819, 806, 2, 10),
        IRON_DARTS(314, 820, 807, 4, 22),
        STEEL_DARTS(314, 821, 808, 8, 37),
        MITHRIL_DARTS(314, 822, 809, 12, 52),
        ADAMANT_DARTS(314, 823, 810, 15, 67),
        RUNE_DARTS(314, 824, 811, 20, 81),
        DRAGON_DARTS(314, 11232, 11230, 25, 95),

        BRONZE_BOLTS(314, 9375, 877, 5, 9),
        OPAL_BOLTS(877, 45, 879, 16, 11),
        IRON_BOLTS(314, 9377, 9140, 15, 39),
        PEARL_BOLTS(9140, 46, 880, 32, 41),
        SILVER_BOLTS(314, 9382, 9145, 25, 43),
        STEEL_BOLTS(314, 9378, 9141, 35, 46),
        RED_TOPAZ_BOLTS(9141, 9188, 9336, 39, 48),
        BARBED_BOLTS(877, 47, 881, 95, 51),
        MITHRIL_BOLTS(314, 9379, 9142, 50, 54),
        BROAD_BOLTS(314, 11876, 11875, 30, 55),
        SAPPHIRE_BOLTS(9142, 9189, 9337, 47, 56),
        EMERALD_BOLTS(9142, 9190, 9338, 58, 55),
        ADAMANTITE_BOLTS(314, 9380, 9143, 70, 61),
        RUBY_BOLTS(9143, 9191, 9339, 63, 63),
        DIAMOND_BOLTS(9143, 9192, 9340, 70, 65),
        RUNITE_BOLTS(314, 9381, 9144, 100, 69),
        DRAGONSTONE_BOLTS(9144, 9193, 9341, 82, 71),
        ONYX_BOLTS(9144, 9194, 9342, 94, 73),;

        public final int item1, item2, outcome, xp, levelReq;

        private FletchableAmmo(int item1, int item2, int outcome, int xp, int levelReq) {
            this.item1 = item1;
            this.item2 = item2;
            this.outcome = outcome;
            this.xp = xp;
            this.levelReq = levelReq;
        }

        public int getItem1() {
            return item1;
        }

        public int getItem2() {
            return item2;
        }

        public int getOutcome() {
            return outcome;
        }

        public int getXp() {
            return xp;
        }

        public int getLevelReq() {
            return levelReq;
        }
    }

    /**
     * Represents crossbows which can be made via the Fletching skill.
     *
     * @author Professor Oak
     */
    public enum FletchableCrossbow {
        BRONZE_CROSSBOW(WOODEN_STOCK, BRONZE_LIMBS, BRONZE_CROSSBOW_U_, 9, 12),
        IRON_CROSSBOW(OAK_STOCK, IRON_LIMBS, IRON_CROSSBOW_U_, 39, 44),
        STEEL_CROSSBOW(WILLOW_STOCK, STEEL_LIMBS, STEEL_CROSSBOW_U_, 46, 54),
        MITHRIL_CROSSBOW(MAPLE_STOCK, MITHRIL_LIMBS, MITHRIL_CROSSBOW_U_, 54, 64),
        ADAMANT_CROSSBOW(MAHOGANY_STOCK, ADAMANTITE_LIMBS, ADAMANT_CROSSBOW_U_, 61, 82),
        RUNE_CROSSBOW(YEW_STOCK, RUNITE_LIMBS, RUNITE_CROSSBOW_U_, 69, 100),;

        private final int stock, limbs, unstrung, level, limbsExp;

        FletchableCrossbow(int stock, int limbs, int unstrung, int level, int limbsExp) {
            this.stock = stock;
            this.limbs = limbs;
            this.unstrung = unstrung;
            this.level = level;
            this.limbsExp = limbsExp;
        }

        public int getStock() {
            return stock;
        }

        public int getUnstrung() {
            return unstrung;
        }

        public int getLimbs() {
            return limbs;
        }

        public int getLevel() {
            return level;
        }


        public int getLimbsExp() {
            return limbsExp;
        }
    }

    /**
     * Represents all bows which can be strung.
     *
     * @author Professor Oak
     */
    public enum StringableBow {
        //Regular bows
        SB(SHORTBOW_U_, BOW_STRING, SHORTBOW, 5, 10, new Animation(6678)),
        SL(LONGBOW_U_, BOW_STRING, LONGBOW, 10, 20, new Animation(6684)),

        OSB(OAK_SHORTBOW_U_, BOW_STRING, OAK_SHORTBOW, 20, 33, new Animation(6679)),
        OSL(OAK_LONGBOW_U_, BOW_STRING, OAK_LONGBOW, 25, 50, new Animation(6685)),

        WSB(WILLOW_SHORTBOW_U_, BOW_STRING, WILLOW_SHORTBOW, 35, 66, new Animation(6680)),
        WLB(WILLOW_LONGBOW_U_, BOW_STRING, WILLOW_LONGBOW, 40, 83, new Animation(6686)),

        MASB(MAPLE_SHORTBOW_U_, BOW_STRING, MAPLE_SHORTBOW, 50, 100, new Animation(6681)),
        MASL(MAPLE_LONGBOW_U_, BOW_STRING, MAPLE_LONGBOW, 55, 116, new Animation(6687)),

        YSB(YEW_SHORTBOW_U_, BOW_STRING, YEW_SHORTBOW, 65, 135, new Animation(6682)),
        YLB(YEW_LONGBOW_U_, BOW_STRING, YEW_LONGBOW, 70, 150, new Animation(6688)),

        MSB(MAGIC_SHORTBOW_U_, BOW_STRING, MAGIC_SHORTBOW, 80, 166, new Animation(6683)),
        MSL(MAGIC_LONGBOW_U_, BOW_STRING, MAGIC_LONGBOW, 85, 183, new Animation(6689)),

        //Crossbows
        BCBOW(BRONZE_CROSSBOW_U_, CROSSBOW_STRING, BRONZE_CROSSBOW, 9, 12, new Animation(6671)),
        ICBOW(IRON_CROSSBOW_U_, CROSSBOW_STRING, IRON_CROSSBOW, 39, 44, new Animation(6673)),
        SCBOW(STEEL_CROSSBOW_U_, CROSSBOW_STRING, STEEL_CROSSBOW, 46, 54, new Animation(6674)),
        MCBOW(MITHRIL_CROSSBOW_U_, CROSSBOW_STRING, MITH_CROSSBOW, 54, 64, new Animation(6675)),
        ACBOW(ADAMANT_CROSSBOW_U_, CROSSBOW_STRING, ADAMANT_CROSSBOW, 61, 82, new Animation(6676)),
        RCBOW(RUNITE_CROSSBOW_U_, CROSSBOW_STRING, RUNE_CROSSBOW, 69, 100, new Animation(6677)),;

        static Map<Integer, StringableBow> unstrungBows = new HashMap<Integer, StringableBow>();

        static {
            for (StringableBow l : StringableBow.values()) {
                unstrungBows.put(l.getItemId(), l);
            }
        }

        private final int itemId, bowStringId, result, levelReq, exp;
        private final Animation animation;

        StringableBow(int itemId, int bowStringId, int result, int levelReq, int exp, Animation animation) {
            this.itemId = itemId;
            this.bowStringId = bowStringId;
            this.result = result;
            this.levelReq = levelReq;
            this.exp = exp;
            this.animation = animation;
        }

        public int getItemId() {
            return itemId;
        }

        public int getBowStringId() {
            return bowStringId;
        }

        public int getResult() {
            return result;
        }

        public int getLevelReq() {
            return levelReq;
        }

        public int getExp() {
            return exp;
        }

        public Animation getAnimation() {
            return animation;
        }
    }

    /**
     * An enumerated type listing all of the items that can be made
     * from a specific log.
     *
     * @author Professor Oak
     */
    public enum FletchableLog {
        REGULAR(LOGS,
                new FletchableItem(new Item(ARROW_SHAFT, 15), 1, 5, CUTTING_LOGS_ANIM),
                new FletchableItem(new Item(WOODEN_STOCK), 9, 6, CUTTING_LOGS_ANIM),
                new FletchableItem(new Item(SHORTBOW_U_), 5, 10, CUTTING_LOGS_ANIM),
                new FletchableItem(new Item(LONGBOW_U_), 10, 20, CUTTING_LOGS_ANIM)),
        OAK(OAK_LOGS,
                new FletchableItem(new Item(ARROW_SHAFT, 30), 15, 10, CUTTING_LOGS_ANIM),
                new FletchableItem(new Item(OAK_STOCK), 24, 16, CUTTING_LOGS_ANIM),
                new FletchableItem(new Item(OAK_SHORTBOW_U_), 20, 33, CUTTING_LOGS_ANIM),
                new FletchableItem(new Item(OAK_LONGBOW_U_), 25, 50, CUTTING_LOGS_ANIM)),
        WILLOW(WILLOW_LOGS,
                new FletchableItem(new Item(ARROW_SHAFT, 45), 30, 15, CUTTING_LOGS_ANIM),
                new FletchableItem(new Item(WILLOW_STOCK), 39, 22, CUTTING_LOGS_ANIM),
                new FletchableItem(new Item(WILLOW_SHORTBOW_U_), 35, 66, CUTTING_LOGS_ANIM),
                new FletchableItem(new Item(WILLOW_LONGBOW_U_), 40, 83, CUTTING_LOGS_ANIM)),
        MAPLE(MAPLE_LOGS,
                new FletchableItem(new Item(ARROW_SHAFT, 60), 45, 20, CUTTING_LOGS_ANIM),
                new FletchableItem(new Item(MAPLE_STOCK), 54, 32, CUTTING_LOGS_ANIM),
                new FletchableItem(new Item(MAPLE_SHORTBOW_U_), 50, 100, CUTTING_LOGS_ANIM),
                new FletchableItem(new Item(MAPLE_LONGBOW_U_), 55, 116, CUTTING_LOGS_ANIM)),
        YEW(YEW_LOGS,
                new FletchableItem(new Item(ARROW_SHAFT, 75), 60, 25, CUTTING_LOGS_ANIM),
                new FletchableItem(new Item(YEW_STOCK), 69, 50, CUTTING_LOGS_ANIM),
                new FletchableItem(new Item(YEW_SHORTBOW_U_), 65, 135, CUTTING_LOGS_ANIM),
                new FletchableItem(new Item(YEW_LONGBOW_U_), 70, 150, CUTTING_LOGS_ANIM)),
        MAGIC(MAGIC_LOGS,
                new FletchableItem(new Item(ARROW_SHAFT, 90), 75, 30, CUTTING_LOGS_ANIM),
                new FletchableItem(new Item(MAGIC_SHORTBOW_U_), 80, 166, CUTTING_LOGS_ANIM),
                new FletchableItem(new Item(MAGIC_LONGBOW_U_), 85, 183, CUTTING_LOGS_ANIM)),;

        static Map<Integer, FletchableLog> logs = new HashMap<Integer, FletchableLog>();

        static {
            for (FletchableLog l : FletchableLog.values()) {
                logs.put(l.getLogId(), l);
            }
        }

        private final int logId;
        private final FletchableItem[] fletchable;

        FletchableLog(int logId, FletchableItem... fletchable) {
            this.logId = logId;
            this.fletchable = fletchable;
        }

        public int getLogId() {
            return logId;
        }

        public FletchableItem[] getFletchable() {
            return fletchable;
        }
    }

    /**
     * Represents a fletchable item.
     *
     * @author Professor Oak
     */
    public static final class FletchableItem {
        private final Item product;
        private final int levelRequired;
        private final int experience;
        private final Animation animation;

        public FletchableItem(Item product, int levelRequired, int experience, Animation animation) {
            this.product = product;
            this.levelRequired = levelRequired;
            this.experience = experience;
            this.animation = animation;
        }

        public Item getProduct() {
            return product;
        }

        public int getLevelRequired() {
            return levelRequired;
        }

        public int getExperience() {
            return experience;
        }

        public Animation getAnimation() {
            return animation;
        }
    }
}
