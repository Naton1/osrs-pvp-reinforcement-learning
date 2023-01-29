package com.elvarg.game.entity.impl.object.impl;

import com.elvarg.game.content.sound.Sound;
import com.elvarg.game.content.sound.SoundManager;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.entity.impl.object.GameObject;
import com.elvarg.game.model.Item;
import com.elvarg.game.model.Animation;
import com.elvarg.game.task.TaskManager;
import com.elvarg.game.task.impl.TimedObjectReplacementTask;
import com.elvarg.util.ItemIdentifiers;
import com.elvarg.util.Misc;
import com.elvarg.util.ObjectIdentifiers;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Allows us to cut sticky webs to walk through - wilderness / mage arena.
 *  @author syuil (Michael)
 */

public class WebHandler {

    private static final int WEB_RESPAWN = 400;
    private static final Sound SLASH_SOUND = Sound.SLASH_WEB;
    private static final Sound FAIL_SLASH_SOUND = Sound.FAIL_SLASH_WEB;
    private static final Animation ITEM_ON_WEB_ANIMATION = new Animation(911);
    private static long lastSlash;
    private static final Pattern SHARP_ITEM_PATTERN;
    static {
        SHARP_ITEM_PATTERN = Pattern.compile("(.*2h.*|.*sword.*|.*dagger.*|.*rapier.*|.*scimitar.*|.*halberd.*|.*spear.*|.*axe.*|.*excalibur.*|.*claws.*|.*whip.*)", Pattern.CASE_INSENSITIVE);
    }

    /**
     * Handles if the inventory item used on web is sharp
     */
    public static boolean isSharpItem(Item item) {
        Matcher sharpItemMatcher = SHARP_ITEM_PATTERN.matcher(item.getDefinition().getName());
        return sharpItemMatcher.matches() || (item.getDefinition().getId() == ItemIdentifiers.KNIFE);
    }

    /**
     * Handles the check if the player is wielding a sharp item
     */

    public static boolean wieldingSharpItem(Player player) {
        for (Item t : player.getEquipment().getItems()) {
            if (t != null && t.getId() > 0 && t.getAmount() > 0) {
                Matcher sharpItemMatcher = SHARP_ITEM_PATTERN.matcher(t.getDefinition().getName());
                if (sharpItemMatcher.matches()) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Handles web slashing and web returning if the player has a sharp weapon to cut webs
     * Checks the player, the object(web), and if the player is using the item on web or if they just click on the object
     */

    public static void handleSlashWeb(Player player, GameObject web, boolean itemOnWeb) {
        if (web == null) return;

        if (!web.getDefinition().getName().equalsIgnoreCase("Web")) return;

        long currentTime = System.currentTimeMillis();
        if (currentTime - lastSlash < 4000) return;

        player.performAnimation(itemOnWeb ? ITEM_ON_WEB_ANIMATION : new Animation(player.getAttackAnim()));

        int successfulSlashChance = Misc.random(2);
        if (successfulSlashChance < 2) {
            player.sendMessage("You slash the web apart.");
            SoundManager.sendSound(player, SLASH_SOUND);
            TaskManager.submit(new TimedObjectReplacementTask(web, new GameObject(ObjectIdentifiers.SLASHED_WEB, web.getLocation(), web.getType(), web.getFace(), player.getPrivateArea()), WEB_RESPAWN));
        } else {
            SoundManager.sendSound(player, FAIL_SLASH_SOUND);
            player.sendMessage("You fail to slash the web.");
        }

        lastSlash = currentTime;
    }
}
