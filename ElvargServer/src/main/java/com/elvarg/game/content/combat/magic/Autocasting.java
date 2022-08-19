package com.elvarg.game.content.combat.magic;

import com.elvarg.game.content.combat.FightType;
import com.elvarg.game.content.combat.WeaponInterfaces;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.MagicSpellbook;
import com.elvarg.game.model.Skill;
import com.elvarg.game.model.equipment.BonusManager;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

import static com.elvarg.util.ItemIdentifiers.*;

public class Autocasting {

    // Autocast buttons
    private static final int REGULAR_AUTOCAST_BUTTON = 349;
    private static final int DEFENSIVE_AUTOCAST_BUTTON = 24111;
    private static final int CLOSE_REGULAR_AUTOCAST_BUTTON = 2004;
    private static final int CLOSE_ANCIENT_AUTOCAST_BUTTON = 6161;

    private static final int REGULAR_AUTOCAST_TAB = 1829;
    private static final int ANCIENT_AUTOCAST_TAB = 1689;
    private static final int IBANS_AUTOCAST_TAB = 12050;

    public static final Set<Integer> ANCIENT_SPELL_AUTOCAST_STAFFS = Set.of(KODAI_WAND, MASTER_WAND,
            ANCIENT_STAFF,NIGHTMARE_STAFF,VOLATILE_NIGHTMARE_STAFF,ELDRITCH_NIGHTMARE_STAFF, TOXIC_STAFF_OF_THE_DEAD, ELDER_WAND, STAFF_OF_THE_DEAD, STAFF_OF_LIGHT);

    public static final HashMap<Integer, CombatSpells> AUTOCAST_SPELLS = new HashMap<>();

    static {
        // Modern
        AUTOCAST_SPELLS.put(1830, CombatSpells.WIND_STRIKE);
        AUTOCAST_SPELLS.put(1831, CombatSpells.WATER_STRIKE);
        AUTOCAST_SPELLS.put(1832, CombatSpells.EARTH_STRIKE);
        AUTOCAST_SPELLS.put(1833, CombatSpells.FIRE_STRIKE);
        AUTOCAST_SPELLS.put(1834, CombatSpells.WIND_BOLT);
        AUTOCAST_SPELLS.put(1835, CombatSpells.WATER_BOLT);
        AUTOCAST_SPELLS.put(1836, CombatSpells.EARTH_BOLT);
        AUTOCAST_SPELLS.put(1837, CombatSpells.FIRE_BOLT);
        AUTOCAST_SPELLS.put(1838, CombatSpells.WIND_BLAST);
        AUTOCAST_SPELLS.put(1839, CombatSpells.WATER_BLAST);
        AUTOCAST_SPELLS.put(1840, CombatSpells.EARTH_BLAST);
        AUTOCAST_SPELLS.put(1841, CombatSpells.FIRE_BLAST);
        AUTOCAST_SPELLS.put(1842, CombatSpells.WIND_WAVE);
        AUTOCAST_SPELLS.put(1843, CombatSpells.WATER_WAVE);
        AUTOCAST_SPELLS.put(1844, CombatSpells.EARTH_WAVE);
        AUTOCAST_SPELLS.put(1845, CombatSpells.FIRE_WAVE);

        // Ancients
        AUTOCAST_SPELLS.put(13189, CombatSpells.SMOKE_RUSH);
        AUTOCAST_SPELLS.put(13241, CombatSpells.SHADOW_RUSH);
        AUTOCAST_SPELLS.put(13247, CombatSpells.BLOOD_RUSH);
        AUTOCAST_SPELLS.put(6162, CombatSpells.ICE_RUSH);
        AUTOCAST_SPELLS.put(13215, CombatSpells.SMOKE_BURST);
        AUTOCAST_SPELLS.put(13267, CombatSpells.SHADOW_BURST);
        AUTOCAST_SPELLS.put(13167, CombatSpells.BLOOD_BURST);
        AUTOCAST_SPELLS.put(13125, CombatSpells.ICE_BURST);
        AUTOCAST_SPELLS.put(13202, CombatSpells.SMOKE_BLITZ);
        AUTOCAST_SPELLS.put(13254, CombatSpells.SHADOW_BLITZ);
        AUTOCAST_SPELLS.put(13158, CombatSpells.BLOOD_BLITZ);
        AUTOCAST_SPELLS.put(13114, CombatSpells.ICE_BLITZ);
        AUTOCAST_SPELLS.put(13228, CombatSpells.SMOKE_BARRAGE);
        AUTOCAST_SPELLS.put(13280, CombatSpells.SHADOW_BARRAGE);
        AUTOCAST_SPELLS.put(13178, CombatSpells.BLOOD_BARRAGE);
        AUTOCAST_SPELLS.put(13136, CombatSpells.ICE_BARRAGE);
    }

    public static boolean handleAutocastTab(final Player player, int actionButtonId) {
        if (AUTOCAST_SPELLS.containsKey(actionButtonId)) {
            setAutocast(player, AUTOCAST_SPELLS.get(actionButtonId).getSpell());
            WeaponInterfaces.assign(player);
            return true;
        }

        switch(actionButtonId) {
            case CLOSE_REGULAR_AUTOCAST_BUTTON:
            case CLOSE_ANCIENT_AUTOCAST_BUTTON:
                setAutocast(player, null); // When clicking cancel, remove autocast?
                player.getPacketSender().sendTabInterface(0, player.getWeapon().getInterfaceId());
                return true;
        }

        return false;
    }

    public static boolean handleWeaponInterface(final Player player, int actionButtonId) {
        if (actionButtonId != REGULAR_AUTOCAST_BUTTON && actionButtonId != DEFENSIVE_AUTOCAST_BUTTON) {
            return false;
        }

        if (player.getSpellbook() == MagicSpellbook.LUNAR) {
            player.getPacketSender().sendMessage("You can't autocast lunar spells.");
            return true;
        }

        if (!player.getEquipment().hasStaffEquipped()) {
            return true;
        }

        switch (player.getSpellbook()) {
            case ANCIENT -> {
                if (!ANCIENT_SPELL_AUTOCAST_STAFFS.contains(player.getEquipment().getWeapon().getId()) && player.getEquipment().getWeapon().getId() != AHRIMS_STAFF) {
                    // Ensure this is a staff capable of casting ancients. Ahrims staff can cast both regular and ancients.
                    player.getPacketSender().sendMessage("You can only autocast regular offensive spells with this staff.");
                    return true;
                }

                player.getPacketSender().sendTabInterface(0, ANCIENT_AUTOCAST_TAB);
            }

            case NORMAL -> {
                if (player.getEquipment().getWeapon().getId() == ANCIENT_STAFF) {
                    player.getPacketSender().sendMessage("You can only autocast ancient magicks with that.");
                    return true;
                }

                player.getPacketSender().sendTabInterface(0, REGULAR_AUTOCAST_TAB);
            }
        }

        player.getPacketSender().sendMessage("You can set a default autocast spell any time from the magic tab.");
        return true;
    }

    public static boolean toggleAutocast(final Player player, int actionButtonId) {
        CombatSpell cbSpell = CombatSpells.getCombatSpell(actionButtonId);
        if (cbSpell == null) {
            return false;
        }
        if (cbSpell.levelRequired() > player.getSkillManager().getCurrentLevel(Skill.MAGIC)) {
            player.getPacketSender().sendMessage("You need a Magic level of at least " + cbSpell.levelRequired() + " to cast this spell.");
            setAutocast(player, null);
            return true;
        }


        if (player.getCombat().getAutocastSpell() != null && player.getCombat().getAutocastSpell() == cbSpell) {

            //Player is already autocasting this spell. Turn it off.
            setAutocast(player, null);

        } else {

            //Set the new autocast spell
            setAutocast(player, cbSpell);

        }

        return true;
    }

    public static void setAutocast(Player player, CombatSpell spell) {
        // First, set the Player's preferred autocast spell
        player.getCombat().setAutocastSpell(spell);

        if (!player.getEquipment().hasStaffEquipped() && spell != null) {
            player.getPacketSender().sendMessage("Default spell set. Please equip a staff to use autocast.");
            return;
        }

        if (spell == null) {
            player.getPacketSender().sendAutocastId(-1).sendConfig(108, 3);
        } else {
            player.getPacketSender().sendAutocastId(spell.spellId()).sendConfig(108, 1);
        }

        BonusManager.update(player);
        updateConfigsOnAutocast(player, spell != null);
    }

    private static final List<FightType> STAFF_FIGHT_TYPES = List.of(
            FightType.STAFF_BASH,
            FightType.STAFF_FOCUS,
            FightType.STAFF_POUND
    );

    private static void updateConfigsOnAutocast(final Player player, boolean autocast) {
        if (autocast) {
            for (final FightType type : STAFF_FIGHT_TYPES) {
                player.getPacketSender().sendConfig(type.getParentId(), 3);
            }
        }
    }
}
