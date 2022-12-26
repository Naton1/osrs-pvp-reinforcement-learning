package com.elvarg.game.model;

import com.elvarg.game.content.combat.magic.Autocasting;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.teleportation.TeleportType;

/**
 * Represents a player's magic spellbook.
 *
 * @author relex lawl
 */

public enum MagicSpellbook {

    NORMAL(1151, TeleportType.NORMAL),
    ANCIENT(12855, TeleportType.ANCIENT),
    LUNAR(29999, TeleportType.LUNAR);

    /**
     * The spellbook's interface id
     */
    private final int interfaceId;
    /**
     * The spellbook's teleport type
     */
    private TeleportType teleportType;

    /**
     * The MagicSpellBook constructor.
     *
     * @param interfaceId The spellbook's interface id.
     * @param message     The message received upon switching to said spellbook.
     */
    private MagicSpellbook(int interfaceId, TeleportType teleportType) {
        this.interfaceId = interfaceId;
        this.teleportType = teleportType;
    }

    /**
     * Gets the MagicSpellBook for said id.
     *
     * @param id The ordinal of the SpellBook to fetch.
     * @return The MagicSpellBook who's ordinal is equal to id.
     */
    public static MagicSpellbook forId(int id) {
        for (MagicSpellbook book : MagicSpellbook.values()) {
            if (book.ordinal() == id) {
                return book;
            }
        }
        return NORMAL;
    }

    /**
     * Changes the magic spellbook for a player.
     *
     * @param player The player changing spellbook.
     * @param book   The new spellbook.
     */
    public static void changeSpellbook(Player player, MagicSpellbook book) {
        if (book == player.getSpellbook()) {
            // Already using this spellbook
            return;
        }

        if (book == LUNAR) {
            if (player.getSkillManager().getMaxLevel(Skill.DEFENCE) < 40) {
                player.getPacketSender().sendMessage("You need at least level 40 Defence to use the Lunar spellbook.");
                return;
            }
        }

        //Update spellbook
        player.setSpellbook(book);

        //Reset autocast
        Autocasting.setAutocast(player, null);

        //Send notification message
        player.getPacketSender().sendMessage("You have changed your magic spellbook.").

                //Send the new spellbook interface to the client side tabs
                        sendTabInterface(6, player.getSpellbook().getInterfaceId());
    }

    /**
     * Gets the interface to switch tab interface to.
     *
     * @return The interface id of said spellbook.
     */
    public int getInterfaceId() {
        return interfaceId;
    }

    /**
     * Gets the spellbook's teleport type
     *
     * @return The teleport type of said spellbook.
     */
    public TeleportType getTeleportType() {
        return teleportType;
    }
}
