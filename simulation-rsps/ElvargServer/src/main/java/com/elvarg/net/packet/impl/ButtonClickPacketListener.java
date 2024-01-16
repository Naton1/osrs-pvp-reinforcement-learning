package com.elvarg.net.packet.impl;

import com.elvarg.game.content.Emotes;
import com.elvarg.game.content.ItemsKeptOnDeath;
import com.elvarg.game.content.PrayerHandler;
import com.elvarg.game.content.clan.ClanChatManager;
import com.elvarg.game.content.combat.WeaponInterfaces;
import com.elvarg.game.content.combat.magic.Autocasting;
import com.elvarg.game.content.combat.magic.EffectSpells;
import com.elvarg.game.content.minigames.MinigameHandler;
import com.elvarg.game.content.presets.Presetables;
import com.elvarg.game.content.quests.QuestHandler;
import com.elvarg.game.content.skill.skillable.impl.Smithing;
import com.elvarg.game.content.sound.Music;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.container.impl.Bank;
import com.elvarg.game.model.dialogues.DialogueOption;
import com.elvarg.game.model.equipment.BonusManager;
import com.elvarg.game.model.rights.PlayerRights;
import com.elvarg.net.packet.Packet;
import com.elvarg.net.packet.PacketExecutor;
import org.apache.commons.lang.StringUtils;

import java.util.Arrays;
import java.util.Optional;

/**
 * This packet listener manages a button that the player has clicked upon.
 *
 * @author Gabriel Hannason
 */

public class ButtonClickPacketListener implements PacketExecutor {

	// Dialogues
	public static final int FIRST_DIALOGUE_OPTION_OF_FIVE = 2494;
	public static final int SECOND_DIALOGUE_OPTION_OF_FIVE = 2495;
	public static final int THIRD_DIALOGUE_OPTION_OF_FIVE = 2496;
	public static final int FOURTH_DIALOGUE_OPTION_OF_FIVE = 2497;
	public static final int FIFTH_DIALOGUE_OPTION_OF_FIVE = 2498;
	public static final int FIRST_DIALOGUE_OPTION_OF_FOUR = 2482;
	public static final int SECOND_DIALOGUE_OPTION_OF_FOUR = 2483;
	public static final int THIRD_DIALOGUE_OPTION_OF_FOUR = 2484;
	public static final int FOURTH_DIALOGUE_OPTION_OF_FOUR = 2485;
	public static final int FIRST_DIALOGUE_OPTION_OF_THREE = 2471;
	public static final int SECOND_DIALOGUE_OPTION_OF_THREE = 2472;
	public static final int THIRD_DIALOGUE_OPTION_OF_THREE = 2473;
	public static final int FIRST_DIALOGUE_OPTION_OF_TWO = 2461;
	public static final int SECOND_DIALOGUE_OPTION_OF_TWO = 2462;
	private static final int LOGOUT = 2458;
	private static final int TOGGLE_RUN_ENERGY_ORB = 1050;
	private static final int TOGGLE_RUN_ENERGY_SETTINGS = 42507;
	private static final int OPEN_EQUIPMENT_SCREEN = 27653;
	private static final int OPEN_PRICE_CHECKER = 27651;
	private static final int OPEN_ITEMS_KEPT_ON_DEATH_SCREEN = 27654;
    private static final int TOGGLE_AUTO_RETALIATE_328 = 24115;
    private static final int TOGGLE_AUTO_RETALIATE_425 = 24041;
    private static final int TOGGLE_AUTO_RETALIATE_3796 = 24033;
    private static final int TOGGLE_AUTO_RETALIATE_776 = 24048;
    private static final int TOGGLE_AUTO_RETALIATE_1698 = 24017;
    private static final int TOGGLE_AUTO_RETALIATE_1764 = 24010;
    private static final int TOGGLE_AUTO_RETALIATE_2276 = 22845;
    private static final int TOGGLE_AUTO_RETALIATE_5570 = 24025;
	private static final int DESTROY_ITEM = 14175;
	private static final int CANCEL_DESTROY_ITEM = 14176;
	private static final int PRICE_CHECKER_WITHDRAW_ALL = 18255;
	private static final int PRICE_CHECKER_DEPOSIT_ALL = 18252;
	private static final int TOGGLE_EXP_LOCK = 476;
	private static final int OPEN_WORLD_MAP = 156;

	// Trade buttons
	private static final int TRADE_ACCEPT_BUTTON_1 = 3420;
	private static final int TRADE_ACCEPT_BUTTON_2 = 3546;
	// Duel buttons
	private static final int DUEL_ACCEPT_BUTTON_1 = 6674;
	private static final int DUEL_ACCEPT_BUTTON_2 = 6520;
	// Close buttons
	private static final int CLOSE_BUTTON_1 = 18247;
	private static final int CLOSE_BUTTON_2 = 38117;
	// Presets
	private static final int OPEN_PRESETS = 31015;
	// Settings tab
	private static final int OPEN_ADVANCED_OPTIONS = 42524;
	private static final int OPEN_KEY_BINDINGS = 42552;

	public static boolean handlers(Player player, int button) {
		if (PrayerHandler.togglePrayer(player, button)) {
			return true;
		}
		if (Autocasting.handleWeaponInterface(player, button)
				|| Autocasting.handleAutocastTab(player, button)
				|| Autocasting.toggleAutocast(player, button)) {
			return true;
		}
		if (WeaponInterfaces.changeCombatSettings(player, button)) {
			BonusManager.update(player);
			return true;
		}
		if (EffectSpells.handleSpell(player, button)) {
			return true;
		}
		if (Bank.handleButton(player, button, 0)) {
			return true;
		}
		if (Emotes.doEmote(player, button)) {
			return true;
		}
		if (ClanChatManager.handleButton(player, button, 0)) {
			return true;
		}
		if (player.getSkillManager().pressedSkill(button)) {
			return true;
		}
		if (player.getQuickPrayers().handleButton(button)) {
			return true;
		}
		if (player.getDueling().checkRule(button)) {
			return true;
		}
		if (Smithing.handleButton(player, button)) {
			return true;
		}
		if (Presetables.handleButton(player, button)) {
			return true;
		}
		if (QuestHandler.handleQuestButtonClick(player, button)) {
			return true;
		}
		if (MinigameHandler.handleButtonClick(player, button)) {
			return true;
		}
		if (Music.handleMusicSelection(player, button) && player.getCurrentInterfaceTabId() == 11 && player.choosingMusic) {
			return true;
		}
		return false;
	}

	@Override
	public void execute(Player player, Packet packet) {
		int button = packet.readInt();

		if (player.getHitpoints() <= 0 || player.isTeleporting()) {
			return;
		}

		if (player.getRights() == PlayerRights.DEVELOPER) {
			player.getPacketSender().sendMessage("Button clicked: " + Integer.toString(button) + ".");
		}


		if (handlers(player, button)) {
			return;
		}

		switch (button) {


			/** Music toggle button **/
			case 42538: {
				Music.switchTabs(player, true);
				break;
			}

		case OPEN_PRESETS:
			if (player.busy()) {
				player.getPacketSender().sendInterfaceRemoval();
			}
			Presetables.open(player);
			break;

		case OPEN_WORLD_MAP:
			if (player.busy()) {
				player.getPacketSender().sendInterfaceRemoval();
			}
			player.getPacketSender().sendInterface(54000);
			break;

		case LOGOUT:
			if (player.canLogout()) {
				player.requestLogout();
			} else {
				player.getPacketSender().sendMessage("You cannot log out at the moment.");
			}
			break;

		case TOGGLE_RUN_ENERGY_ORB:
		case TOGGLE_RUN_ENERGY_SETTINGS:
			if (player.busy()) {
				player.getPacketSender().sendInterfaceRemoval();
			}
			if (player.getRunEnergy() > 0) {
				player.setRunning(!player.isRunning());
			} else {
				player.setRunning(false);
			}
			player.getPacketSender().sendRunStatus();
			break;

		case OPEN_ADVANCED_OPTIONS:
			if (player.busy()) {
				player.getPacketSender().sendInterfaceRemoval();
			}
			player.getPacketSender().sendInterface(23000);
			break;

		case OPEN_KEY_BINDINGS:
			if (player.busy()) {
				player.getPacketSender().sendInterfaceRemoval();
			}
			player.getPacketSender().sendInterface(53000);
			break;

		case OPEN_EQUIPMENT_SCREEN:
			if (player.busy()) {
				player.getPacketSender().sendInterfaceRemoval();
			}
			BonusManager.open(player);
			break;

		case OPEN_PRICE_CHECKER:
			if (player.busy()) {
				player.getPacketSender().sendInterfaceRemoval();
			}
			player.getPriceChecker().open();
			break;

		case OPEN_ITEMS_KEPT_ON_DEATH_SCREEN:
			if (player.busy()) {
				player.getPacketSender().sendInterfaceRemoval();
			}
			ItemsKeptOnDeath.open(player);
			break;

		case PRICE_CHECKER_WITHDRAW_ALL:
			player.getPriceChecker().withdrawAll();
			break;

		case PRICE_CHECKER_DEPOSIT_ALL:
			player.getPriceChecker().depositAll();
			break;

		case TRADE_ACCEPT_BUTTON_1:
		case TRADE_ACCEPT_BUTTON_2:
			player.getTrading().acceptTrade();
			break;

		case DUEL_ACCEPT_BUTTON_1:
		case DUEL_ACCEPT_BUTTON_2:
			player.getDueling().acceptDuel();
			break;

		case TOGGLE_AUTO_RETALIATE_328:
        case TOGGLE_AUTO_RETALIATE_425:
        case TOGGLE_AUTO_RETALIATE_3796:
        case TOGGLE_AUTO_RETALIATE_776:
        case TOGGLE_AUTO_RETALIATE_1764:
        case TOGGLE_AUTO_RETALIATE_2276:
        case TOGGLE_AUTO_RETALIATE_5570:
        case TOGGLE_AUTO_RETALIATE_1698:
            player.setAutoRetaliate(!player.autoRetaliate());
            break;

		case DESTROY_ITEM:
			final int item = player.getDestroyItem();
			player.getPacketSender().sendInterfaceRemoval();
			if (item != -1) {
				player.getInventory().delete(item, player.getInventory().getAmount(item));
			}
			break;

		case CANCEL_DESTROY_ITEM:
			player.getPacketSender().sendInterfaceRemoval();
			break;

		case TOGGLE_EXP_LOCK:
			player.setExperienceLocked(!player.experienceLocked());
			if (player.experienceLocked()) {
				player.getPacketSender().sendMessage("Your experience is now @red@locked.");
			} else {
				player.getPacketSender().sendMessage("Your experience is now @red@unlocked.");
			}
			break;

		case CLOSE_BUTTON_1:
		case CLOSE_BUTTON_2:
		case 16999:
			player.getPacketSender().sendInterfaceRemoval();
			break;

		case FIRST_DIALOGUE_OPTION_OF_FIVE:
		case FIRST_DIALOGUE_OPTION_OF_FOUR:
		case FIRST_DIALOGUE_OPTION_OF_THREE:
		case FIRST_DIALOGUE_OPTION_OF_TWO:
		    player.getDialogueManager().handleOption(player, DialogueOption.FIRST_OPTION);
			break;
		case SECOND_DIALOGUE_OPTION_OF_FIVE:
		case SECOND_DIALOGUE_OPTION_OF_FOUR:
		case SECOND_DIALOGUE_OPTION_OF_THREE:
		case SECOND_DIALOGUE_OPTION_OF_TWO:
		    player.getDialogueManager().handleOption(player, DialogueOption.SECOND_OPTION);
			break;
		case THIRD_DIALOGUE_OPTION_OF_FIVE:
		case THIRD_DIALOGUE_OPTION_OF_FOUR:
		case THIRD_DIALOGUE_OPTION_OF_THREE:
		    player.getDialogueManager().handleOption(player, DialogueOption.THIRD_OPTION);
			break;
		case FOURTH_DIALOGUE_OPTION_OF_FIVE:
		case FOURTH_DIALOGUE_OPTION_OF_FOUR:
		    player.getDialogueManager().handleOption(player, DialogueOption.FOURTH_OPTION);
			break;
		case FIFTH_DIALOGUE_OPTION_OF_FIVE:
		    player.getDialogueManager().handleOption(player, DialogueOption.FIFTH_OPTION);
			break;
		default:
			// player.getPacketSender().sendMessage("Player "+player.getUsername()+", click
			// button: "+button);
			break;
		}
	}
}
