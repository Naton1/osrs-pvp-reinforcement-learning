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
import com.elvarg.util.timers.TimerKey;

import static com.elvarg.util.ItemIdentifiers.*;

public class ObbyMaulerFighterPreset implements FighterPreset {

    private static final Presetable BOT_OBBY_MAULER_57 = new Presetable("Obby Mauler",
            new Item[]{
                    new Item(SUPER_STRENGTH_4_), new Item(RANGING_POTION_4_), new Item(MAGIC_SHORTBOW), new Item(RING_OF_RECOIL),
                    new Item(BERSERKER_NECKLACE), new Item(FIRE_CAPE), new Item(COOKED_KARAMBWAN), new Item(COOKED_KARAMBWAN),
                    new Item(COOKED_KARAMBWAN), new Item(COOKED_KARAMBWAN), new Item(COOKED_KARAMBWAN), new Item(COOKED_KARAMBWAN),
                    new Item(TZHAAR_KET_OM), new Item(MANTA_RAY), new Item(MANTA_RAY), new Item(MANTA_RAY),
                    new Item(MANTA_RAY), new Item(MANTA_RAY), new Item(MANTA_RAY), new Item(MANTA_RAY),
                    new Item(MANTA_RAY), new Item(MANTA_RAY), new Item(MANTA_RAY), new Item(MANTA_RAY),
                    new Item(MANTA_RAY), new Item(MANTA_RAY), new Item(MANTA_RAY), new Item(ANGLERFISH),
            },
            new Item[]{
                    new Item(COIF),
                    new Item(AVAS_ACCUMULATOR),
                    new Item(MAGIC_SHORTBOW),
                    new Item(RUNE_ARROW, 200),
                    new Item(AMULET_OF_GLORY),
                    new Item(LEATHER_BODY),
                    new Item(BLACK_DHIDE_CHAPS),
                    new Item(MITHRIL_GLOVES),
                    new Item(CLIMBING_BOOTS),
                    new Item(RING_OF_RECOIL)
            },
            /* atk, def, str, hp, range, pray, mage */
            new int[]{1, 1, 99, 80, 70, 13, 1},
            MagicSpellbook.NORMAL,
            true
    );

    public static final CombatAction[] COMBAT_ACTIONS = {
            new CombatSwitch(new int[]{MAGIC_SHORTBOW, RUNE_ARROW, AVAS_ACCUMULATOR}, new PrayerHandler.PrayerData[]{ PrayerHandler.PrayerData.SHARP_EYE }) {

                @Override
                public boolean shouldPerform(PlayerBot playerBot, Mobile enemy) {
                    return playerBot.getSpecialPercentage() >= 55 &&
                            (!enemy.getPrayerActive()[PrayerHandler.PROTECT_FROM_MISSILES]
                                    && enemy.getHitpointsAfterPendingDamage() < 40);
                }

                @Override
                public void performAfterSwitch(PlayerBot playerBot, Mobile enemy) {
                    if (!playerBot.isSpecialActivated()) {
                        CombatSpecial.activate(playerBot);
                    }
                    playerBot.getCombat().attack(enemy);
                }
            },
            new CombatSwitch(new int[]{TZHAAR_KET_OM, BERSERKER_NECKLACE, FIRE_CAPE}, new PrayerHandler.PrayerData[]{ PrayerHandler.PrayerData.SUPERHUMAN_STRENGTH }) {

                @Override
                public boolean shouldPerform(PlayerBot playerBot, Mobile enemy) {
                    boolean canAttackNextTick = playerBot.getTimers().willEndIn(TimerKey.COMBAT_ATTACK, 1);
                    return canAttackNextTick && playerBot.getMovementQueue().getMobility().canMove() &&
                            enemy.getHitpointsAfterPendingDamage() < 38;
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
            new CombatSwitch(new int[]{MAGIC_SHORTBOW, RUNE_ARROW, AVAS_ACCUMULATOR}, new PrayerHandler.PrayerData[]{ PrayerHandler.PrayerData.SHARP_EYE }) {

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
