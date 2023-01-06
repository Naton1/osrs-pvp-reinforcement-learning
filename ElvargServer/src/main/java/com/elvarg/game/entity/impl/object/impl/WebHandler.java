package com.elvarg.game.entity.impl.object.impl;

import com.elvarg.game.Sound;
import com.elvarg.game.Sounds;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.entity.impl.object.GameObject;
import com.elvarg.game.entity.impl.object.ObjectManager;
import com.elvarg.game.model.Item;
import com.elvarg.game.model.Animation;
import com.elvarg.game.task.Task;
import com.elvarg.game.task.TaskManager;
import com.elvarg.game.model.Location;
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

    private static long lastSlash;
    static Pattern sharpItemPattern = Pattern.compile("(.*2h.*|.*sword.*|.*dagger.*|.*rapier.*|.*scimitar.*|.*halberd.*|.*spear.*|.*axe.*|.*excalibur.*|.*claws.*|.*whip.*)", Pattern.CASE_INSENSITIVE);


    /**
     * Handles the check when using an item on the web, ensuring it is a sharp item
     */
    public static boolean isSharpItem(Item item) {
        Matcher sharpItemMatcher = sharpItemPattern.matcher(item.getDefinition().getName());
        return sharpItemMatcher.matches() || (item.getDefinition().getId() == ItemIdentifiers.KNIFE);
    }

    /**
     * Handles if the player has the sharp item in the inventory
     */
    public static boolean hasSharpItem(Player player) {
        for (Item item : player.getInventory().getItems()) {
            if (item != null && item.getId() > 0 && item.getAmount() > 0) {
                Matcher sharpItemMatcher = sharpItemPattern.matcher(item.getDefinition().getName());
                if (sharpItemMatcher.matches()) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Handles the check if the player is wielding a sharp item
     */

    public static boolean wieldingSharpItem(Player player) {
        for (Item t : player.getEquipment().getItems()) {
            if (t != null && t.getId() > 0 && t.getAmount() > 0) {
                Matcher sharpItemMatcher = sharpItemPattern.matcher(t.getDefinition().getName());
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

        if (web == null) {
            return;
        }
        if (web.getDefinition().getName().equalsIgnoreCase("Web")) {
            if (System.currentTimeMillis() - lastSlash < 4000) {
                return;
            }
            if (wieldingSharpItem(player) || hasSharpItem(player)) {
                if (itemOnWeb) {
                    player.performAnimation(new Animation(911));
                    Sounds.sendSound(player, Sound.SLASH_WEB);
                } else {
                    player.performAnimation(new Animation(player.getAttackAnim()));
                    Sounds.sendSound(player, Sound.SLASH_WEB);
                }
                int i = Misc.random(2);
                if (i < 2) {
                    player.sendMessage("You slash the web apart.");
                    ObjectManager.deregister(web, true);
                    ObjectManager.register(new GameObject(ObjectIdentifiers.SLASHED_WEB, new Location(web.getLocation().getX(), web.getLocation().getY()), web.getType(), web.getFace(), null), true);
                    TaskManager.submit(new Task(4000, false) {

                        @Override
                        public void execute() {
                            ObjectManager.deregister(new GameObject(ObjectIdentifiers.SLASHED_WEB, new Location(web.getLocation().getX(), web.getLocation().getY()), web.getType(), web.getFace(), null), true);
                            ObjectManager.register(new GameObject(ObjectIdentifiers.WEB, new Location(web.getLocation().getX(), web.getLocation().getY()), web.getType(), web.getFace(), null), true);
                            stop();
                        }
                    });
                } else {
                    player.sendMessage("You fail to slash the web.");
                }
                lastSlash = System.currentTimeMillis();
            } else {
                player.sendMessage("Only a sharp blade can cut through this sticky web.");
            }
        }
    }
}
