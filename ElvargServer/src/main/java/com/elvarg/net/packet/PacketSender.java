package com.elvarg.net.packet;

import java.util.List;

import com.elvarg.game.GameConstants;
import com.elvarg.game.entity.impl.Mobile;
import com.elvarg.game.entity.impl.grounditem.ItemOnGround;
import com.elvarg.game.entity.impl.object.GameObject;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.entity.impl.playerbot.PlayerBot;
import com.elvarg.game.model.*;
import com.elvarg.game.model.container.ItemContainer;
import com.elvarg.game.model.container.impl.Bank;
import com.elvarg.game.model.menu.CreationMenu;

/**
 * This class manages making the packets that will be sent (when called upon)
 * onto the associated player's client.
 *
 * @author relex lawl & Gabbe
 */

public class PacketSender {

	private Player player;

	public PacketSender(Player player) {
		this.player = player;
	}

	/**
	 * Sends information about the player to the client.
	 *
	 * @return The PacketSender instance.
	 */
	public PacketSender sendDetails() {
		PacketBuilder out = new PacketBuilder(249);
		out.put(1, ValueType.A);
		out.putShort(player.getIndex());
		player.getSession().write(out);
		return this;
	}

	/**
	 * Sends the map region a player is located in and also sets the player's first
	 * step position of said region as their {@code lastKnownRegion}.
	 *
	 * @return The PacketSender instance.
	 */
	public PacketSender sendMapRegion() {
		player.setAllowRegionChangePacket(true);
		player.setLastKnownRegion(player.getLocation().clone());
		PacketBuilder out = new PacketBuilder(73);
		out.putShort(player.getLocation().getRegionX() + 6, ValueType.A);
		out.putShort(player.getLocation().getRegionY() + 6);
		player.getSession().write(out);
		return this;
	}

	/**
	 * Sends the logout packet for the player.
	 *
	 * @return The PacketSender instance.
	 */
	public PacketSender sendLogout() {
		PacketBuilder out = new PacketBuilder(109);
		player.getSession().write(out);
		return this;
	}

	/**
	 * Requests a reload of the region
	 */
	public PacketSender sendRegionReload() {
		PacketBuilder out = new PacketBuilder(89);
		player.getSession().write(out);
		return this;
	}

	/**
	 * Sets the world's system update time, once timer is 0, everyone will be
	 * disconnected.
	 *
	 * @param time
	 *            The amount of seconds in which world will be updated in.
	 * @return The PacketSender instance.
	 */
	public PacketSender sendSystemUpdate(int time) {
		PacketBuilder out = new PacketBuilder(114);
		out.putShort(time, ByteOrder.LITTLE);
		player.getSession().write(out);
		return this;
	}
	
	public PacketSender sendTeleportInterface(int menu) {
		player.setTeleportInterfaceOpen(true);
		PacketBuilder out = new PacketBuilder(183);
		out.put(menu);
		player.getSession().write(out);
		return this;
	}
	
	public PacketSender sendCreationMenu(CreationMenu menu) {
	    player.setCreationMenu(menu);
	    sendString(31104, menu.getTitle());
        PacketBuilder out = new PacketBuilder(167);
        out.put(menu.getItems().size());
        for (int itemId : menu.getItems()) {
            out.putInt(itemId);
        }
        player.getSession().write(out);
        return this;
    }
	
	public PacketSender sendSpecialAttackState(boolean active) {
		PacketBuilder out = new PacketBuilder(186);
		out.put(active ? 1 : 0);
		player.getSession().write(out);
		return this;
	}

	public PacketSender sendSoundEffect(int soundId, int loopType, int delay, int volume) {
		PacketBuilder out = new PacketBuilder(174);
		out.putShort(soundId)
				.put(loopType)
				.putShort(delay)
				.putShort(volume);
		player.getSession().write(out);
		return this;
	}

	public PacketSender sendSound(int soundId, int volume, int delay) {
		PacketBuilder out = new PacketBuilder(175);
		out.putShort(soundId, ValueType.A, ByteOrder.LITTLE).put(volume).putShort(delay);
		player.getSession().write(out);
		return this;
	}

	public PacketSender sendSong(int id) {
		PacketBuilder out = new PacketBuilder(74);
		out.putShort(id, ByteOrder.LITTLE);
		player.getSession().write(out);
		return this;
	}

	public PacketSender playMusic(int musicId) {
		PacketBuilder out = new PacketBuilder(121);
		out.putShort(musicId);//songid
		out.putShort(0);//delay
		player.getSession().write(out);
		return this;
	}

	public PacketSender sendAutocastId(int id) {
		PacketBuilder out = new PacketBuilder(38);
		out.putShort(id);
		player.getSession().write(out);
		return this;
	}

	public PacketSender sendEnableNoclip() {
		PacketBuilder out = new PacketBuilder(250);
		player.getSession().write(out);
		return this;
	}

	public PacketSender sendURL(String url) {
		PacketBuilder out = new PacketBuilder(251, PacketType.VARIABLE);
		out.putString(url);
		player.getSession().write(out);
		return this;
	}

	/**
	 * Sends a game message to a player in the server.
	 *
	 * @param message
	 *            The message they will receive in chat box.
	 * @return The PacketSender instance.
	 */
	public PacketSender sendMessage(String message) {
		if (player instanceof PlayerBot) {
			// Bots can't read their own messages, yet ;)
			((PlayerBot) player).getChatInteraction().receivedGameMessage(message);
			return this;
		}

		PacketBuilder out = new PacketBuilder(253, PacketType.VARIABLE);
		out.putString(message);
		player.getSession().write(out);
		return this;
	}

	public PacketSender sendSpecialMessage(String name, int type, String message) {
		PacketBuilder out = new PacketBuilder(252, PacketType.VARIABLE);
		out.put(type);
		out.putString(name);
		out.putString(message);
		player.getSession().write(out);
		return this;
	}
	
	public PacketSender sendPoisonType(int type) {
		player.getSession().write(new PacketBuilder(184).put(type));
		return this;
	}

	/**
	 * Sends skill information onto the client, to calculate things such as
	 * constitution, prayer and summoning orb and other configurations.
	 *
	 * @param skill
	 *            The skill being sent.
	 * @return The PacketSender instance.
	 */
	public PacketSender sendSkill(Skill skill) {
		PacketBuilder out = new PacketBuilder(134);
		out.put(skill.ordinal());
		out.putInt(player.getSkillManager().getCurrentLevel(skill));
		out.putInt(player.getSkillManager().getMaxLevel(skill));
		out.putInt(player.getSkillManager().getExperience(skill));
		player.getSession().write(out);
		return this;
	}

	public PacketSender sendExpDrop(Skill skill, int exp) {
		PacketBuilder out = new PacketBuilder(116);
		out.put(skill.ordinal());
		out.putInt(exp);
		player.getSession().write(out);
		return this;
	}

	/**
	 * Sends a configuration button's state.
	 *
	 * @param configId
	 *            The id of the configuration button.
	 * @param state
	 *            The state to set it to.
	 * @return The PacketSender instance.
	 */
	public PacketSender sendConfig(int id, int state) {
		PacketBuilder out = new PacketBuilder(36);
		out.putShort(id, ByteOrder.LITTLE);
		out.put(state);
		player.getSession().write(out);
		return this;
	}

	/**
	 * Sends a interface child's toggle.
	 *
	 * @param id
	 *            The id of the child.
	 * @param state
	 *            The state to set it to.
	 * @return The PacketSender instance.
	 */
	public PacketSender sendToggle(int id, int state) {
		PacketBuilder out = new PacketBuilder(87);
		out.putShort(id, ByteOrder.LITTLE);
		out.putInt(state, ByteOrder.MIDDLE);
		player.getSession().write(out);
		return this;
	}

	/**
	 * Sends the state in which the player has their chat options, such as public,
	 * private, friends only.
	 *
	 * @param publicChat
	 *            The state of their public chat.
	 * @param privateChat
	 *            The state of their private chat.
	 * @param tradeChat
	 *            The state of their trade chat.
	 * @return The PacketSender instance.
	 */
	public PacketSender sendChatOptions(int publicChat, int privateChat, int tradeChat) {
		PacketBuilder out = new PacketBuilder(206);
		out.put(publicChat).put(privateChat).put(tradeChat);
		player.getSession().write(out);
		return this;
	}

	public PacketSender sendRunEnergy() {
		PacketBuilder out = new PacketBuilder(110);
		out.put(player.getRunEnergy());
		player.getSession().write(out);
		return this;
	}

	public PacketSender sendQuickPrayersState(boolean activated) {
		PacketBuilder out = new PacketBuilder(111);
		out.put(activated ? 1 : 0);
		player.getSession().write(out);
		return this;
	}

	public PacketSender updateSpecialAttackOrb() {
		PacketBuilder out = new PacketBuilder(137);
		out.put(player.getSpecialPercentage());
		player.getSession().write(out);
		return this;
	}

	public PacketSender sendDungeoneeringTabIcon(boolean show) {
		PacketBuilder out = new PacketBuilder(103);
		out.put(show ? 1 : 0);
		player.getSession().write(out);
		return this;
	}

	public PacketSender sendHeight() {
		player.getSession().write(new PacketBuilder(86).put(player.getLocation().getZ()));
		return this;
	}

	public PacketSender sendIronmanMode(int ironmanMode) {
		PacketBuilder out = new PacketBuilder(112);
		out.put(ironmanMode);
		player.getSession().write(out);
		return this;
	}

	public PacketSender sendShowClanChatOptions(boolean show) {
		PacketBuilder out = new PacketBuilder(115);
		out.put(show ? 1 : 0); // 0 = no right click options
		player.getSession().write(out);
		return this;
	}

	public PacketSender sendRunStatus() {
		PacketBuilder out = new PacketBuilder(113);
		out.put(player.isRunning() ? 1 : 0);
		player.getSession().write(out);
		return this;
	}

	public PacketSender sendWeight(int weight) {
		PacketBuilder out = new PacketBuilder(240);
		out.putShort(weight);
		player.getSession().write(out);
		return this;
	}

	public PacketSender commandFrame(int i) {
		PacketBuilder out = new PacketBuilder(28);
		out.put(i);
		player.getSession().write(out);
		return this;
	}

	public PacketSender sendInterface(int id) {
		if (player.isPlayerBot()) {
			return this;
		}

		PacketBuilder out = new PacketBuilder(97);
		out.putShort(id);
		player.getSession().write(out);
		player.setInterfaceId(id);
		return this;
	}

	public PacketSender sendWalkableInterface(int interfaceId) {
		player.setWalkableInterfaceId(interfaceId);
		PacketBuilder out = new PacketBuilder(208);
		out.putInt(interfaceId);
		player.getSession().write(out);
		return this;
	}

	public PacketSender sendInterfaceDisplayState(int interfaceId, boolean hide) {
		PacketBuilder out = new PacketBuilder(171);
		out.put(hide ? 1 : 0);
		out.putInt(interfaceId);
		player.getSession().write(out);
		return this;
	}

	public PacketSender sendPlayerHeadOnInterface(int id) {
		PacketBuilder out = new PacketBuilder(185);
		out.putShort(id, ValueType.A, ByteOrder.LITTLE);
		player.getSession().write(out);
		return this;
	}

	public PacketSender sendNpcHeadOnInterface(int id, int interfaceId) {
		PacketBuilder out = new PacketBuilder(75);
		out.putShort(id, ValueType.A, ByteOrder.LITTLE);
		out.putShort(interfaceId, ValueType.A, ByteOrder.LITTLE);
		player.getSession().write(out);
		return this;
	}

	public PacketSender sendEnterAmountPrompt(String title) {
		PacketBuilder out = new PacketBuilder(27, PacketType.VARIABLE);
		out.putString(title);
		player.getSession().write(out);
		return this;
	}

	public PacketSender sendEnterInputPrompt(String title) {
		PacketBuilder out = new PacketBuilder(187, PacketType.VARIABLE);
		out.putString(title);
		player.getSession().write(out);
		return this;
	}

	public PacketSender sendInterfaceReset() {
		PacketBuilder out = new PacketBuilder(68);
		player.getSession().write(out);
		return this;
	}

	/**
	 * Closes a player's client.
	 */
	public PacketSender sendExit() {
		PacketBuilder out = new PacketBuilder(62);
		player.getSession().write(out);
		return this;
	}

	public PacketSender sendInterfaceComponentMoval(int x, int y, int id) {
		PacketBuilder out = new PacketBuilder(70);
		out.putShort(x);
		out.putShort(y);
		out.putShort(id, ByteOrder.LITTLE);
		player.getSession().write(out);
		return this;
	}

	/*
	 * public PacketSender sendBlinkingHint(String title, String information, int x,
	 * int y, int speed, int pause, int type, final int time) {
	 * player.getSession().queueMessage(new
	 * PacketBuilder(179).putString(title).putString(information).putShort(x).
	 * putShort(y).put(speed).put(pause).put(type)); if(type > 0) {
	 * TaskManager.submit(new Task(time, player, false) {
	 *
	 * @Override public void execute() {
	 * player.getPacketSender().sendBlinkingHint("", "", 0, 0, 0, 0, -1, 0); stop();
	 * } }); } return this; }
	 */
	public PacketSender sendInterfaceAnimation(int interfaceId, int animationId) {
		PacketBuilder out = new PacketBuilder(200);
		out.putShort(interfaceId);
		out.putShort(animationId);
		player.getSession().write(out);
		return this;
	}

	public PacketSender sendInterfaceModel(int interfaceId, int itemId, int zoom) {
		PacketBuilder out = new PacketBuilder(246);
		out.putShort(interfaceId, ByteOrder.LITTLE);
		out.putShort(zoom).putShort(itemId);
		player.getSession().write(out);
		return this;
	}

	public PacketSender sendWidgetModel(int widget, int model) {
		PacketBuilder out = new PacketBuilder(8);
		out.putShort(widget);
		out.putShort(model);
		player.getSession().write(out);
		return this;
	}

	public PacketSender sendTabInterface(int tabId, int interfaceId) {
		PacketBuilder out = new PacketBuilder(71);
		out.putShort(interfaceId);
		out.put(tabId, ValueType.A);
		player.getSession().write(out);
		return this;
	}

	public PacketSender sendTabs() {
		for (int tab = 0; tab < GameConstants.TAB_INTERFACES.length; tab++) {
			int interface_ = GameConstants.TAB_INTERFACES[tab];

			if (tab == 6) {
				interface_ = player.getSpellbook().getInterfaceId();
			}

			sendTabInterface(tab, interface_);
		}
		return this;
	}

	public PacketSender sendTab(int id) {
		PacketBuilder out = new PacketBuilder(106);
		out.put(id, ValueType.C);
		player.getSession().write(out);
		return this;
	}

	public PacketSender sendFlashingSidebar(int id) {
		PacketBuilder out = new PacketBuilder(24);
		out.put(id, ValueType.S);
		player.getSession().write(out);
		return this;
	}

	public PacketSender sendChatboxInterface(int id) {
		PacketBuilder out = new PacketBuilder(164);
		out.putShort(id, ByteOrder.LITTLE);
		player.getSession().write(out);
		return this;
	}

	public PacketSender sendMapState(int state) {
		PacketBuilder out = new PacketBuilder(99);
		out.put(state);
		player.getSession().write(out);
		return this;
	}

	public PacketSender sendCameraAngle(int x, int y, int level, int speed, int angle) {
		PacketBuilder out = new PacketBuilder(177);
		out.put(x / 64);
		out.put(y / 64);
		out.putShort(level);
		out.put(speed);
		out.put(angle);
		player.getSession().write(out);
		return this;
	}

	public PacketSender sendCameraShake(int verticalAmount, int verticalSpeed, int horizontalAmount,
			int horizontalSpeed) {
		PacketBuilder out = new PacketBuilder(35);
		out.put(verticalAmount);
		out.put(verticalSpeed);
		out.put(horizontalAmount);
		out.put(horizontalSpeed);
		player.getSession().write(out);
		return this;
	}

	public PacketSender sendCameraSpin(int x, int y, int z, int speed, int angle) {
		PacketBuilder out = new PacketBuilder(166);
		out.put(x / 64);
		out.put(y / 64);
		out.putShort(z);
		out.put(speed);
		out.put(angle);
		player.getSession().write(out);
		return this;
	}

	public PacketSender sendCameraNeutrality() {
		PacketBuilder out = new PacketBuilder(107);
		player.getSession().write(out);
		return this;
	}

	public PacketSender sendInterfaceRemoval() {
		if (player.getStatus() == PlayerStatus.BANKING) {
			if (player.isSearchingBank()) {
				Bank.exitSearch(player, false);
			}
		} else if (player.getStatus() == PlayerStatus.PRICE_CHECKING) {
			player.getPriceChecker().withdrawAll();
		} else if (player.getStatus() == PlayerStatus.TRADING) {
			player.getTrading().closeTrade();
		} else if (player.getStatus() == PlayerStatus.DUELING) {
			if (!player.getDueling().inDuel()) {
				player.getDueling().closeDuel();
			}
		}

		player.setStatus(PlayerStatus.NONE);
		player.setEnteredAmountAction(null);
		player.setEnteredSyntaxAction(null);
		player.getDialogueManager().reset();
		player.setShop(null);
		player.setDestroyItem(-1);
		player.setInterfaceId(-1);
		player.setSearchingBank(false);
		player.setTeleportInterfaceOpen(false);
		player.getAppearance().setCanChangeAppearance(false);
		player.getSession().write(new PacketBuilder(219));
		return this;
	}

	public PacketSender sendInterfaceScrollReset(int interfaceId) {
		PacketBuilder out = new PacketBuilder(9);
		out.putInt(interfaceId);
		player.getSession().write(out);
		return this;
	}

	public PacketSender sendScrollbarHeight(int interfaceId, int scrollMax) {
		PacketBuilder out = new PacketBuilder(10);
		out.putInt(interfaceId);
		out.putShort(scrollMax);
		player.getSession().write(out);
		return this;
	}

	public PacketSender sendInterfaceSet(int interfaceId, int sidebarInterfaceId) {
		PacketBuilder out = new PacketBuilder(248);
		out.putShort(interfaceId, ValueType.A);
		out.putShort(sidebarInterfaceId);
		player.getSession().write(out);
		player.setInterfaceId(interfaceId);
		return this;
	}

	public PacketSender sendItemContainer(ItemContainer container, int interfaceId) {

		PacketBuilder out = new PacketBuilder(53, PacketType.VARIABLE_SHORT);

		out.putInt(interfaceId);
		out.putShort(container.capacity());
		for (Item item : container.getItems()) {
			if (item == null || item.getId() <= 0 || item.getAmount() <= 0 && !(container instanceof Bank)) {
				out.putInt(-1);
				continue;
			}
			out.putInt(item.getAmount());
			out.putShort(item.getId() + 1);
		}

		player.getSession().write(out);
		return this;
	}

	public PacketSender sendCurrentBankTab(int current_tab) {
		PacketBuilder out = new PacketBuilder(55);
		out.put(current_tab);
		player.getSession().write(out);
		return this;
	}

	public PacketSender sendEffectTimer(int delay, EffectTimer e) {

		PacketBuilder out = new PacketBuilder(54);

		out.putShort(delay);
		out.putShort(e.getClientSprite());

		player.getSession().write(out);
		return this;
	}

	public PacketSender sendInterfaceItems(int interfaceId, List<Item> items) {
		if (player.isPlayerBot()) {
			return this;
		}

		PacketBuilder out = new PacketBuilder(53, PacketType.VARIABLE_SHORT);
		out.putInt(interfaceId);
		out.putShort(items.size());
		for (Item item : items) {
			if (item == null) {
				out.putInt(-1);
				continue;
			}
			out.putInt(item.getAmount());
			out.putShort(item.getId() + 1);
		}
		player.getSession().write(out);
		return this;
	}

	public PacketSender sendItemOnInterface(int interfaceId, int item, int amount) {
		PacketBuilder out = new PacketBuilder(53, PacketType.VARIABLE_SHORT);
		out.putInt(interfaceId);
		out.putShort(1);
		out.putInt(amount);
		out.putShort(item + 1);
		player.getSession().write(out);
		return this;
	}

	public PacketSender sendItemOnInterface(int frame, int item, int slot, int amount) {
		PacketBuilder out = new PacketBuilder(34, PacketType.VARIABLE_SHORT);
		out.putShort(frame);
		out.put(slot);
		out.putInt(amount);
		out.putShort(item + 1);
		player.getSession().write(out);
		return this;
	}

	/*
	 * public PacketSender sendConstructionInterfaceItems(ArrayList<Furniture>
	 * items) { PacketBuilder builder = new PacketBuilder(53, PacketType.VARIABLE_SHORT);
	 * builder.writeShort(38274); builder.writeShort(items.size()); for (int i = 0;
	 * i < items.size(); i++) { builder.writeByte(1);
	 * builder.writeLEShortA(items.get(i).getItemId() + 1); }
	 * player.write(builder.toPacket()); return this; }
	 */

	public PacketSender clearItemOnInterface(int frame) {
		PacketBuilder out = new PacketBuilder(72);
		out.putShort(frame);
		player.getSession().write(out);
		return this;
	}

	public PacketSender sendSmithingData(int id, int slot, int interfaceId, int amount) {
		PacketBuilder out = new PacketBuilder(34, PacketType.VARIABLE_SHORT);
		out.putShort(interfaceId);
		out.put(slot);
		out.putInt(amount);
		out.putShort(id + 1);
		out.put(amount);
		player.getSession().write(out);
		return this;
	}

	public PacketSender sendInteractionOption(String option, int slot, boolean top) {
		PacketBuilder out = new PacketBuilder(104, PacketType.VARIABLE);
		out.put(slot, ValueType.C);
		out.put(top ? 1 : 0, ValueType.A);
		out.putString(option);
		player.getSession().write(out);
		PlayerInteractingOption interactingOption = PlayerInteractingOption.forName(option);
		if (option != null)
			player.setPlayerInteractingOption(interactingOption);
		return this;
	}

	public PacketSender sendString(int id, String string) {
		if (!player.getFrameUpdater().shouldUpdate(string, id)) {
			return this;
		}
		PacketBuilder out = new PacketBuilder(126, PacketType.VARIABLE_SHORT);
		out.putString(string);
		out.putInt(id);
		player.getSession().write(out);
		return this;
	}

	public PacketSender sendString(String string, int id) {
		return this.sendString(id, string);
	}

	public PacketSender clearInterfaceText(int start, int end) {
		for (int i = start; i <= end; i++) {
			player.getFrameUpdater().interfaceTextMap.remove(i);
		}
		PacketBuilder out = new PacketBuilder(105);
		out.putInt(start);
		out.putInt(end);
		player.getSession().write(out);
		return this;
	}
	
	public PacketSender clearInterfaceItems(int start, int end) {
		PacketBuilder out = new PacketBuilder(112);
		out.putInt(start);
		out.putInt(end);
		player.getSession().write(out);
		return this;
	}

	/**
	 * Sends all of the player's rights to the client.
	 */
	public PacketSender sendRights() {
		PacketBuilder out = new PacketBuilder(127);
		out.put(player.getRights().ordinal());
		out.put(player.getDonatorRights().ordinal());
		player.getSession().write(out);
		return this;
	}

	/**
	 * Sends a hint to specified position.
	 *
	 * @param position
	 *            The position to create the hint.
	 * @param tilePosition
	 *            The position on the square (middle = 2; west = 3; east = 4; south
	 *            = 5; north = 6)
	 * @return The Packet Sender instance.
	 */
	public PacketSender sendPositionalHint(Location position, int tilePosition) {
		PacketBuilder out = new PacketBuilder(254);
		out.put(tilePosition);
		out.putShort(position.getX());
		out.putShort(position.getY());
		out.put(position.getZ());
		player.getSession().write(out);
		return this;
	}

	/**
	 * Sends a hint above an entity's head.
	 *
	 * @param mobile
	 *            The target entity to draw hint for.
	 * @return The PacketSender instance.
	 */
	public PacketSender sendEntityHint(Mobile mobile) {
		int type = mobile instanceof Player ? 10 : 1;
		PacketBuilder out = new PacketBuilder(254);
		out.put(type);
		out.putShort(mobile.getIndex());
		out.putInt(0, ByteOrder.TRIPLE_INT);
		player.getSession().write(out);
		return this;
	}

	/**
	 * Sends a hint removal above an entity's head.
	 *
	 * @param playerHintRemoval
	 *            Remove hint from a player or an NPC?
	 * @return The PacketSender instance.
	 */
	public PacketSender sendEntityHintRemoval(boolean playerHintRemoval) {
		int type = playerHintRemoval ? 10 : 1;
		PacketBuilder out = new PacketBuilder(254);
		out.put(type).putShort(-1);
		out.putInt(0, ByteOrder.TRIPLE_INT);
		player.getSession().write(out);
		return this;
	}

	public PacketSender sendMultiIcon(int value) {
		PacketBuilder out = new PacketBuilder(61);
		out.put(value);
		player.getSession().write(out);
		player.setMultiIcon(value);
		return this;
	}

	public PacketSender sendPrivateMessage(Player target, byte[] message, int size) {
		if (player instanceof PlayerBot) {
			((PlayerBot) player).getChatInteraction().receivedPrivateMessage(message, target);
			return this;
		}

		PacketBuilder out = new PacketBuilder(196, PacketType.VARIABLE);
		out.putLong(target.getLongUsername());
		out.putInt(target.getRelations().getPrivateMessageId());
		out.put(target.getRights().ordinal());
		out.put(target.getDonatorRights().ordinal());
		out.putBytes(message, size);
		player.getSession().write(out);
		return this;
	}

	public PacketSender sendFriendStatus(int status) {
		PacketBuilder out = new PacketBuilder(221);
		out.put(status);
		player.getSession().write(out);
		return this;
	}

	public PacketSender sendFriend(long name, int world) {
		world = world != 0 ? world + 9 : world;
		PacketBuilder out = new PacketBuilder(50);
		out.putLong(name);
		out.put(world);
		player.getSession().write(out);
		return this;
	}

	public PacketSender sendDeleteFriend(long name) {
		PacketBuilder out = new PacketBuilder(51);
		out.putLong(name);
		player.getSession().write(out);
		return this;
	}

	public PacketSender sendAddIgnore(long name) {
		PacketBuilder out = new PacketBuilder(214);
		out.putLong(name);
		player.getSession().write(out);
		return this;
	}

	public PacketSender sendDeleteIgnore(long name) {
		PacketBuilder out = new PacketBuilder(215);
		out.putLong(name);
		player.getSession().write(out);
		return this;
	}

	public PacketSender sendTotalExp(long exp) {
		PacketBuilder out = new PacketBuilder(108);
		out.putLong(exp);
		player.getSession().write(out);
		return this;
	}

	public PacketSender sendAnimationReset() {
		PacketBuilder out = new PacketBuilder(1);
		player.getSession().write(out);
		return this;
	}

	public PacketSender sendGraphic(Graphic graphic, Location position) {
		sendPosition(position);
		PacketBuilder out = new PacketBuilder(4);
		out.put(0);
		out.putShort(graphic.getId());
		out.put(position.getZ());
		out.putShort(graphic.getDelay());
		player.getSession().write(out);
		return this;
	}

	public PacketSender sendGlobalGraphic(Graphic graphic, Location position) {
		sendGraphic(graphic, position);
		for (Player p : player.getLocalPlayers()) {
			p.getPacketSender().sendGraphic(graphic, position);
		}
		return this;
	}

	public PacketSender sendObject(GameObject object) {
		sendPosition(object.getLocation());
		PacketBuilder out = new PacketBuilder(151);
		out.put(object.getLocation().getZ(), ValueType.A);
		out.putShort(object.getId(), ByteOrder.LITTLE);
		out.put((byte) ((object.getType() << 2) + (object.getFace() & 3)), ValueType.S);
		player.getSession().write(out);
		return this;
	}

	public PacketSender sendObjectRemoval(GameObject object) {
		if (object == null) {
			return this;
		}

		sendPosition(object.getLocation());
		PacketBuilder out = new PacketBuilder(101);
		out.put((object.getType() << 2) + (object.getFace() & 3), ValueType.C);
		out.put(object.getLocation().getZ());
		player.getSession().write(out);
		return this;
	}

	public PacketSender sendObjectAnimation(GameObject object, Animation anim) {
		sendPosition(object.getLocation());
		PacketBuilder out = new PacketBuilder(160);
		out.put(0, ValueType.S);
		out.put((object.getType() << 2) + (object.getFace() & 3), ValueType.S);
		out.putShort(anim.getId(), ValueType.A);
		player.getSession().write(out);
		return this;
	}

	public PacketSender alterGroundItem(ItemOnGround item) {
		sendPosition(item.getLocation());
		PacketBuilder out = new PacketBuilder(84);
		out.put(0);
		out.putShort(item.getItem().getId()).putInt(item.getOldAmount()).putInt(item.getItem().getAmount());
		player.getSession().write(out);
		return this;
	}

	public PacketSender createGroundItem(ItemOnGround item) {
		sendPosition(item.getLocation());
		PacketBuilder out = new PacketBuilder(44);
		out.putShort(item.getItem().getId(), ValueType.A, ByteOrder.LITTLE);
		out.putInt(item.getItem().getAmount()).put(0);
		player.getSession().write(out);
		return this;
	}

	public PacketSender deleteGroundItem(ItemOnGround item) {
		sendPosition(item.getLocation());
		PacketBuilder out = new PacketBuilder(156);
		out.put(0, ValueType.A);
		out.putShort(item.getItem().getId());
		player.getSession().write(out);
		return this;
	}

	/**
	 * Deletes spawns related to regions, such as ground items and objects.
	 *
	 * @return
	 */
	public PacketSender deleteRegionalSpawns() {
		player.getSession().write(new PacketBuilder(178));
		return this;
	}

	public PacketSender sendPosition(final Location position) {
		final Location other = player.getLastKnownRegion();
		PacketBuilder out = new PacketBuilder(85);
		out.put(position.getY() - 8 * other.getRegionY(), ValueType.C);
		out.put(position.getX() - 8 * other.getRegionX(), ValueType.C);
		player.getSession().write(out);
		return this;
	}

	public PacketSender sendConsoleMessage(String message) {
		PacketBuilder out = new PacketBuilder(123);
		out.putString(message);
		player.getSession().write(out);
		return this;
	}

	public PacketSender sendInterfaceSpriteChange(int childId, int firstSprite, int secondSprite) {
		// player.write(new
		// PacketBuilder(140).writeShort(childId).writeByte((firstSprite << 0) +
		// (secondSprite & 0x0)).toPacket());
		return this;
	}

	public int getRegionOffset(Location position) {
		int x = position.getX() - (position.getRegionX() << 4);
		int y = position.getY() - (position.getRegionY() & 0x7);
		int offset = ((x & 0x7)) << 4 + (y & 0x7);
		return offset;
	}

	public PacketSender sendProjectile(Location start, Location end, int offset, int speed, int projectileId,
			int startHeight, int endHeight, Mobile lockon, int delay, int angle, int distanceOffset) {
		sendPosition(start);
		PacketBuilder out = new PacketBuilder(117);
		out.put(offset);
		out.put((end.getX() - start.getX()));
		out.put((end.getY() - start.getY()));		
        if (lockon != null) {
            out.putShort(lockon.isPlayer() ? -(lockon.getIndex() + 1) : lockon.getIndex() + 1);
        } else {
            out.putShort(0);
        }
        out.putShort(projectileId);
		out.put(startHeight);
		out.put(endHeight);
		out.putShort(delay);
		out.putShort(speed);
		out.put(angle);
		out.put(distanceOffset);
		player.getSession().write(out);
		return this;
	}

	/*
	 * public PacketSender sendCombatBoxData(Character character) { PacketBuilder
	 * out = new PacketBuilder(125); out.putShort(character.getIndex());
	 * out.put(character.isPlayer() ? 0 : 1); if(character.isPlayer()) {
	 * player.getSession().queueMessage(out); } else { NPC npc = (NPC) character;
	 * boolean sendList = npc.getDefaultConstitution() >= 2500 &&
	 * Location.inMulti(npc); out.put(sendList ? 1 : 0); if(sendList) {
	 * List<DamageDealer> list = npc.fetchNewDamageMap() ?
	 * npc.getCombatBuilder().getTopKillers(npc) : npc.getDamageDealerMap();
	 * if(npc.fetchNewDamageMap()) { npc.setDamageDealerMap(list);
	 * npc.setFetchNewDamageMap(false); } out.put(list.size()); for(int i = 0; i <
	 * list.size(); i++) { DamageDealer dd = list.get(i);
	 * out.putString(dd.getPlayer().getUsername()); out.putShort(dd.getDamage()); }
	 * } player.getSession().queueMessage(out); } return this; }
	 */

	public PacketSender sendHideCombatBox() {
		player.getSession().write(new PacketBuilder(128));
		return this;
	}

	/*
	 * public void sendConstructMapRegion(Palette palette) { try {
	 * player.setLastKnownRegion(player.getPosition()); PacketBuilder bldr = new
	 * PacketBuilder(241); // Construction.buildRoofs(palette, player);
	 * bldr.putShort(player.getPosition().getRegionY() + 6, ValueType.A);
	 * bldr.initializeAccess(AccessType.BIT); for (int z = 0; z < 4; z++) { for (int
	 * x = 0; x < 13; x++) { for (int y = 0; y < 13; y++) { PaletteTile tile =
	 * palette.getTile(x, y, z); boolean b = false; if (x < 2 || x > 10 || y < 2 ||
	 * y > 10) b = true; int visible = tile != null && !b ? 1 : 0; bldr.putBits(1,
	 * visible); if (visible == 1) { bldr.putBits(2, tile.getZ()); bldr.putBits(10,
	 * tile.getX()); bldr.putBits(11, tile.getY()); bldr.putBits(2,
	 * tile.getRotation()); bldr.putBits(1, 0); } } } }
	 * bldr.initializeAccess(AccessType.BYTE);
	 * bldr.putShort(player.getPosition().getRegionX() + 6);
	 * player.getSession().write(bldr); } catch (Exception e) { e.printStackTrace();
	 * } }
	 */

	/*
	 * public PacketSender constructMapRegion(Palette palette) { PacketBuilder bldr
	 * = new PacketBuilder(241); if(palette != null) { bldr.putString("palette");
	 * //Inits map construction sequence
	 * bldr.putString(""+(player.getPosition().getRegionY() + 6)+"");
	 * bldr.putString(""+(player.getPosition().getRegionX() + 6)+""); for (int z =
	 * 0; z < 4; z++) { for (int x = 0; x < 13; x++) { for (int y = 0; y < 13; y++)
	 * { PaletteTile tile = palette.getTile(x, y, z); boolean b = false; if (x < 2
	 * || x > 10 || y < 2 || y > 10) b = true; int toWrite = !b && tile != null ? 5
	 * : 0; bldr.putString(""+toWrite+""); if(toWrite == 5) { int val = tile.getX()
	 * << 14 | tile.getY() << 3 | tile.getZ() << 24 | tile.getRotation() << 1;
	 * bldr.putString(""+val+""); } } } } } else { bldr.putString("null"); //Resets
	 * map construction sequence } player.getSession().queueMessage(bldr); return
	 * this; }
	 * 
	 * public PacketSender sendConstructionInterfaceItems(ArrayList<Furniture>
	 * items) { PacketBuilder builder = new PacketBuilder(53, PacketType.VARIABLE_SHORT);
	 * builder.putShort(38274); builder.putShort(items.size()); for (int i = 0; i <
	 * items.size(); i++) { builder.put(1);
	 * builder.putShort(items.get(i).getItemId() + 1, ValueType.A,
	 * ByteOrder.LITTLE); } player.getSession().queueMessage(builder); return this;
	 * }
	 */

	public PacketSender sendObjectsRemoval(int chunkX, int chunkY, int height) {
		player.getSession().write(new PacketBuilder(153).put(chunkX).put(chunkY).put(height));
		return this;
	}
}
