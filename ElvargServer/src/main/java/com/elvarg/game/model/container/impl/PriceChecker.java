package com.elvarg.game.model.container.impl;

import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.Item;
import com.elvarg.game.model.PlayerStatus;
import com.elvarg.game.model.container.ItemContainer;
import com.elvarg.game.model.container.StackType;
import com.elvarg.util.Misc;

import java.util.List;

public class PriceChecker extends ItemContainer {

	public static final int INTERFACE_ID = 42000, CONTAINER_ID = 18500;
	private static final int TEXT_START_ROW_1 = 18300;
	private static final int TEXT_START_ROW_2 = 18400;

	public PriceChecker(Player player) {
		super(player);
	}

	public ItemContainer open() {
		getPlayer().setStatus(PlayerStatus.PRICE_CHECKING);
		getPlayer().getMovementQueue().reset();
		refreshItems();
		return this;
	}

	@Override
	public int capacity() {
		return 24;
	}

	@Override
	public StackType stackType() {
		return StackType.DEFAULT;
	}

	@Override
	public ItemContainer refreshItems() {

		List<Item> items_ = getValidItems();

		if (items_.size() > 0) {
			getPlayer().getPacketSender().sendString(18355, "").sendString(18351,
					"" + Misc.insertCommasToNumber(getTotalValue())); // TOTAL VALUE

			// Send item prices
			for (int i = 0; i < capacity(); i++) {
				String itemPrice = "";
				String totalPrice = "";

				if (getItems()[i].isValid()) {
					long value = getItems()[i].getDefinition().getValue();
					long amount = getItems()[i].getAmount();
					long total_price = (value * amount);

					if (total_price >= Integer.MAX_VALUE) {
						totalPrice = "Too High!";
					} else {
						totalPrice = " = " + Misc.insertCommasToNumber(Long.toString(total_price));
					}

					itemPrice = "" + Misc.insertCommasToNumber(Long.toString(value)) + " x" + Long.toString(amount);
				}

				getPlayer().getPacketSender().sendString(TEXT_START_ROW_1 + i, itemPrice);
				getPlayer().getPacketSender().sendString(TEXT_START_ROW_2 + i, totalPrice);
			}

		} else {
			getPlayer().getPacketSender().sendString(18355, "Click an item in your inventory to check it's wealth.")
					.sendString(18351, "0"); // TOTAL VALUE

			// Reset item prices
			for (int i = 0; i < capacity(); i++) {
				getPlayer().getPacketSender().sendString(TEXT_START_ROW_1 + i, "");
				getPlayer().getPacketSender().sendString(TEXT_START_ROW_2 + i, "");
			}
		}

		getPlayer().getPacketSender().sendInterfaceSet(INTERFACE_ID, 3321);
		getPlayer().getPacketSender().sendItemContainer(this, CONTAINER_ID);
		getPlayer().getPacketSender().sendItemContainer(getPlayer().getInventory(), 3322);
		return this;
	}

	@Override
	public ItemContainer full() {
		getPlayer().getPacketSender().sendMessage("The pricechecker cannot hold any more items.");
		return this;
	}

	public void withdrawAll() {
		if (getPlayer().getStatus() == PlayerStatus.PRICE_CHECKING && getPlayer().getInterfaceId() == INTERFACE_ID) {
			for (Item item : getValidItems()) {
				switchItem(getPlayer().getInventory(), item.clone(), false, false);
			}
			refreshItems();
			getPlayer().getInventory().refreshItems();
		}
	}

	public void depositAll() {
		if (getPlayer().getStatus() == PlayerStatus.PRICE_CHECKING && getPlayer().getInterfaceId() == INTERFACE_ID) {
			for (Item item : getPlayer().getInventory().getValidItems()) {
				if (item.getDefinition().getValue() > 0) {
					getPlayer().getInventory().switchItem(this, item.clone(), false, false);
				}
			}
			refreshItems();
			getPlayer().getInventory().refreshItems();
		}
	}

	public boolean deposit(int id, int amount, int slot) {
		if (getPlayer().getStatus() == PlayerStatus.PRICE_CHECKING && getPlayer().getInterfaceId() == INTERFACE_ID) {

			// Verify item
			if (getPlayer().getInventory().getItems()[slot].getId() == id) {

				// Perform switch
				final Item item = new Item(id, amount);
				if (!item.getDefinition().isSellable()) {
					getPlayer().getPacketSender()
							.sendMessage("That item cannot be pricechecked because it isn't sellable.");
					return true;
				}
				if (item.getDefinition().getValue() == 0) {
					getPlayer().getPacketSender()
							.sendMessage("There's no point pricechecking that item. It has no value.");
					return true;
				}

				if (item.getAmount() == 1) {
					getPlayer().getInventory().switchItem(this, item, slot, false, true);
				} else {
					getPlayer().getInventory().switchItem(this, item, false, true);
				}
			}
			return true;
		}
		return false;
	}

	public boolean withdraw(int id, int amount, int slot) {
		if (getPlayer().getStatus() == PlayerStatus.PRICE_CHECKING && getPlayer().getInterfaceId() == INTERFACE_ID) {

			// Verify item
			if (getItems()[slot].getId() == id) {

				// Perform switch
				final Item item = new Item(id, amount);
				if (item.getAmount() == 1) {
					switchItem(getPlayer().getInventory(), item, slot, false, true);
				} else {
					switchItem(getPlayer().getInventory(), item, false, true);
				}
			}
			return true;
		}
		return false;
	}
}
