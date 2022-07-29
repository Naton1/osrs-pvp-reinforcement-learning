package com.elvarg.game.entity.impl.playerbot.fightstyle.impl;

import com.elvarg.game.content.PrayerHandler;
import com.elvarg.game.content.combat.CombatSpecial;
import com.elvarg.game.content.presets.Presetable;
import com.elvarg.game.entity.impl.Mobile;
import com.elvarg.game.entity.impl.playerbot.PlayerBot;
import com.elvarg.game.entity.impl.playerbot.fightstyle.CombatAction;
import com.elvarg.game.entity.impl.playerbot.fightstyle.CombatSwitch;
import com.elvarg.game.entity.impl.playerbot.fightstyle.FighterPreset;
import com.elvarg.game.model.Item;
import com.elvarg.game.model.ItemInSlot;
import com.elvarg.game.model.MagicSpellbook;

import static com.elvarg.util.ItemIdentifiers.*;

public class ObbyMaulerFighterPreset implements FighterPreset {

    private static final Presetable BOT_OBBY_MAULER_57 = new Presetable("Obby Mauler", 0,
            new Item[]{
                    new Item(SUPER_STRENGTH_4_), new Item(RANGING_POTION_4_), new Item(MAGIC_SHORTBOW), new Item(RING_OF_RECOIL),
                    new Item(RUNE_ARROW, 175), new Item(COOKED_KARAMBWAN), new Item(COOKED_KARAMBWAN), new Item(COOKED_KARAMBWAN),
                    new Item(COOKED_KARAMBWAN), new Item(COOKED_KARAMBWAN), new Item(COOKED_KARAMBWAN), new Item(COOKED_KARAMBWAN),
                    new Item(TZHAAR_KET_OM), new Item(MANTA_RAY), new Item(MANTA_RAY), new Item(MANTA_RAY),
                    new Item(MANTA_RAY), new Item(MANTA_RAY), new Item(MANTA_RAY), new Item(MANTA_RAY),
                    new Item(MANTA_RAY), new Item(MANTA_RAY), new Item(MANTA_RAY), new Item(MANTA_RAY),
                    new Item(MANTA_RAY), new Item(MANTA_RAY), new Item(MANTA_RAY), new Item(ANGLERFISH),
            },
            new Item[]{
                    new Item(COIF),
                    new Item(AVAS_ACCUMULATOR),
                    new Item(RUNE_KNIFE, 250),
                    new Item(AMULET_OF_GLORY),
                    new Item(IRON_PLATEBODY),
                    new Item(UNHOLY_BOOK),
                    new Item(BLACK_DHIDE_CHAPS),
                    new Item(MITHRIL_GLOVES),
                    new Item(CLIMBING_BOOTS),
                    new Item(RING_OF_RECOIL)
            },
            /* atk, def, str, hp, range, pray, mage */
            new int[]{1, 1, 99, 80, 60, 31, 1},
            MagicSpellbook.NORMAL,
            true
    );

    public static final CombatAction[] COMBAT_ACTIONS = {
            new CombatSwitch(new int[]{MAGIC_SHORTBOW, RUNE_ARROW}) {

                @Override
                public boolean shouldPerform(PlayerBot playerBot, Mobile enemy) {
                    return playerBot.getInventory().getFreeSlots() > 0 &&
                            playerBot.getSpecialPercentage() >= 50 &&
                            // Switch if the enemy has enabled protect from missles or has lowish health
                            (!enemy.getPrayerActive()[PrayerHandler.PROTECT_FROM_MISSILES] && enemy.getHitpoints() < 40);
                }

                @Override
                public void performAfterSwitch(PlayerBot playerBot, Mobile enemy) {
                    if (!playerBot.isSpecialActivated()) {
                        CombatSpecial.activate(playerBot);
                    }
                    playerBot.getCombat().attack(enemy);
                }
            },
            new CombatSwitch(new int[]{TZHAAR_KET_OM}) {

                @Override
                public boolean shouldPerform(PlayerBot playerBot, Mobile enemy) {
                    return playerBot.getInventory().getFreeSlots() > 0 &&
                            // Don't switch if we're frozen
                            playerBot.getMovementQueue().canMove() &&
                            // Switch if the enemy has enabled protect from missles or has low health
                            (enemy.getPrayerActive()[PrayerHandler.PROTECT_FROM_MISSILES] || enemy.getHitpoints() < 35);
                }

                @Override
                public void performAfterSwitch(PlayerBot playerBot, Mobile enemy) {
                    playerBot.getCombat().attack(enemy);
                }
            },
            new CombatSwitch(new int[]{RING_OF_RECOIL}) {

                @Override
                public boolean shouldPerform(PlayerBot playerBot, Mobile enemy) {
                    var hasRing = ItemInSlot.getFromInventory(RING_OF_RECOIL, playerBot.getInventory()) != null;
                    return hasRing && playerBot.getEquipment().getById(RING_OF_RECOIL) == null;
                }

                @Override
                public void performAfterSwitch(PlayerBot playerBot, Mobile enemy) {
                    playerBot.getCombat().attack(enemy);
                }
            },
            new CombatSwitch(new int[]{RUNE_KNIFE, UNHOLY_BOOK}) {

                @Override
                public boolean shouldPerform(PlayerBot playerBot, Mobile enemy) {
                    return true;
                }

                @Override
                public void performAfterSwitch(PlayerBot playerBot, Mobile enemy) {
                    playerBot.getCombat().attack(enemy);
                }
            },
    };

    @Override
    public Presetable getItemPreset() {
        return BOT_OBBY_MAULER_57;
    }

    @Override
    public CombatAction[] getCombatActions() {
        return COMBAT_ACTIONS;
    }
}
