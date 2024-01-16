package com.elvarg.net.packet.impl;

import com.elvarg.game.content.DepositBox;
import com.elvarg.game.content.Dueling;
import com.elvarg.game.content.Trading;
import com.elvarg.game.content.combat.CombatSpecial;
import com.elvarg.game.content.combat.WeaponInterfaces;
import com.elvarg.game.content.combat.magic.Autocasting;
import com.elvarg.game.content.skill.skillable.impl.Smithing.EquipmentMaking;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.Flag;
import com.elvarg.game.model.Item;
import com.elvarg.game.model.PlayerStatus;
import com.elvarg.game.model.container.impl.Bank;
import com.elvarg.game.model.container.impl.Equipment;
import com.elvarg.game.model.container.impl.PriceChecker;
import com.elvarg.game.model.container.shop.Shop;
import com.elvarg.game.model.container.shop.ShopManager;
import com.elvarg.game.model.equipment.BonusManager;
import com.elvarg.net.packet.Packet;
import com.elvarg.net.packet.PacketConstants;
import com.elvarg.net.packet.PacketExecutor;

import java.util.zip.DeflaterOutputStream;

public class ItemContainerActionPacketListener implements PacketExecutor {

    private static void firstAction(Player player, Packet packet) {
        int containerId = packet.readInt();
        int slot = packet.readShortA();
        int id = packet.readShortA();

        // Bank withdrawal..
        if (containerId >= Bank.CONTAINER_START && containerId < Bank.CONTAINER_START + Bank.TOTAL_BANK_TABS) {
            Bank.withdraw(player, id, slot, 1, containerId - Bank.CONTAINER_START);
            return;
        }

        if (containerId == 7423) {
            DepositBox.deposit(player, slot, id, 1);
            return;
        }

        switch (containerId) {
            case EquipmentMaking.EQUIPMENT_CREATION_COLUMN_1:
            case EquipmentMaking.EQUIPMENT_CREATION_COLUMN_2:
            case EquipmentMaking.EQUIPMENT_CREATION_COLUMN_3:
            case EquipmentMaking.EQUIPMENT_CREATION_COLUMN_4:
            case EquipmentMaking.EQUIPMENT_CREATION_COLUMN_5:
                if (player.getInterfaceId() == EquipmentMaking.EQUIPMENT_CREATION_INTERFACE_ID) {
                    EquipmentMaking.initialize(player, id, containerId, slot, 1);
                }
                break;
            // Withdrawing items from duel
            case Dueling.MAIN_INTERFACE_CONTAINER:
                if (player.getStatus() == PlayerStatus.DUELING) {
                    player.getDueling().handleItem(id, 1, slot, player.getDueling().getContainer(), player.getInventory());
                }
                break;

            case Trading.INVENTORY_CONTAINER_INTERFACE: // Duel/Trade inventory
                if (player.getStatus() == PlayerStatus.PRICE_CHECKING) {
                    player.getPriceChecker().deposit(id, 1, slot);
                } else if (player.getStatus() == PlayerStatus.TRADING) {
                    player.getTrading().handleItem(id, 1, slot, player.getInventory(), player.getTrading().getContainer());
                } else if (player.getStatus() == PlayerStatus.DUELING) {
                    player.getDueling().handleItem(id, 1, slot, player.getInventory(), player.getDueling().getContainer());
                }
                break;
            case Trading.CONTAINER_INTERFACE_ID:
                if (player.getStatus() == PlayerStatus.TRADING) {
                    player.getTrading().handleItem(id, 1, slot, player.getTrading().getContainer(), player.getInventory());
                }
                break;
            case PriceChecker.CONTAINER_ID:
                player.getPriceChecker().withdraw(id, 1, slot);
                break;

            case Bank.INVENTORY_INTERFACE_ID:
                Bank.deposit(player, id, slot, 1);
                break;

            case Shop.ITEM_CHILD_ID:
            case Shop.INVENTORY_INTERFACE_ID:
                if (player.getStatus() == PlayerStatus.SHOPPING) {
                    ShopManager.priceCheck(player, id, slot, (containerId == Shop.ITEM_CHILD_ID));
                }
                break;

            case Equipment.INVENTORY_INTERFACE_ID: // Unequip
                Item item = player.getEquipment().getItems()[slot];
                if (item == null || item.getId() != id)
                    return;

                // Handle area unequipping behaviour
                if (player.getArea() != null && !player.getArea().canUnequipItem(player, slot, item)) {
                    return;
                }

                boolean stackItem = item.getDefinition().isStackable() && player.getInventory().getAmount(item.getId()) > 0;
                int inventorySlot = player.getInventory().getEmptySlot();
                if (inventorySlot != -1) {

                    player.getEquipment().setItem(slot, new Item(-1, 0));

                    if (stackItem) {
                        player.getInventory().add(item.getId(), item.getAmount());
                    } else {
                        player.getInventory().setItem(inventorySlot, item);
                    }

                    BonusManager.update(player);
                    if (item.getDefinition().getEquipmentType().getSlot() == Equipment.WEAPON_SLOT) {
                        WeaponInterfaces.assign(player);
                        player.setSpecialActivated(false);
                        CombatSpecial.updateBar(player);
                        if (player.getCombat().getAutocastSpell() != null) {
                            Autocasting.setAutocast(player, null);
                            player.getPacketSender().sendMessage("Autocast spell cleared.");
                        }
                    }
                    player.getEquipment().refreshItems();
                    player.getInventory().refreshItems();
                    player.getUpdateFlag().flag(Flag.APPEARANCE);
                } else {
                    player.getInventory().full();
                }
                break;
        }
    }

    private static void secondAction(Player player, Packet packet) {
        int interfaceId = packet.readInt();
        int id = packet.readLEShortA();
        int slot = packet.readLEShort();

        // Bank withdrawal..
        if (interfaceId >= Bank.CONTAINER_START && interfaceId < Bank.CONTAINER_START + Bank.TOTAL_BANK_TABS) {
            Bank.withdraw(player, id, slot, 5, interfaceId - Bank.CONTAINER_START);
            return;
        }

        if (interfaceId == 7423) {
            DepositBox.deposit(player, slot, id, 5);
            return;
        }

        switch (interfaceId) {
            case EquipmentMaking.EQUIPMENT_CREATION_COLUMN_1:
            case EquipmentMaking.EQUIPMENT_CREATION_COLUMN_2:
            case EquipmentMaking.EQUIPMENT_CREATION_COLUMN_3:
            case EquipmentMaking.EQUIPMENT_CREATION_COLUMN_4:
            case EquipmentMaking.EQUIPMENT_CREATION_COLUMN_5:
                if (player.getInterfaceId() == EquipmentMaking.EQUIPMENT_CREATION_INTERFACE_ID) {
                    EquipmentMaking.initialize(player, id, interfaceId, slot, 5);
                }
                break;
            case Shop.INVENTORY_INTERFACE_ID:
                if (player.getStatus() == PlayerStatus.SHOPPING) {
                    ShopManager.sellItem(player, slot, id, 1);
                }
                break;
            case Shop.ITEM_CHILD_ID:
                if (player.getStatus() == PlayerStatus.SHOPPING) {
                    ShopManager.buyItem(player, slot, id, 1);
                }
                break;
            case Bank.INVENTORY_INTERFACE_ID:
                Bank.deposit(player, id, slot, 5);
                break;
            case Dueling.MAIN_INTERFACE_CONTAINER:
                if (player.getStatus() == PlayerStatus.DUELING) {
                    player.getDueling().handleItem(id, 5, slot, player.getDueling().getContainer(), player.getInventory());
                }
                break;
            case Trading.INVENTORY_CONTAINER_INTERFACE: // Duel/Trade inventory
                if (player.getStatus() == PlayerStatus.PRICE_CHECKING) {
                    player.getPriceChecker().deposit(id, 5, slot);
                } else if (player.getStatus() == PlayerStatus.TRADING) {
                    player.getTrading().handleItem(id, 5, slot, player.getInventory(), player.getTrading().getContainer());
                } else if (player.getStatus() == PlayerStatus.DUELING) {
                    player.getDueling().handleItem(id, 5, slot, player.getInventory(), player.getDueling().getContainer());
                }
                break;
            case Trading.CONTAINER_INTERFACE_ID:
                if (player.getStatus() == PlayerStatus.TRADING) {
                    player.getTrading().handleItem(id, 5, slot, player.getTrading().getContainer(), player.getInventory());
                }
                break;
            case PriceChecker.CONTAINER_ID:
                player.getPriceChecker().withdraw(id, 5, slot);
                break;
        }
    }

    private static void thirdAction(Player player, Packet packet) {
        int interfaceId = packet.readInt();
        int id = packet.readShortA();
        int slot = packet.readShortA();

        // Bank withdrawal..
        if (interfaceId >= Bank.CONTAINER_START && interfaceId < Bank.CONTAINER_START + Bank.TOTAL_BANK_TABS) {
            Bank.withdraw(player, id, slot, 10, interfaceId - Bank.CONTAINER_START);
            return;
        }

        if (interfaceId == 7423) {
            DepositBox.deposit(player, slot, id, 10);
            return;
        }

        switch (interfaceId) {
            case EquipmentMaking.EQUIPMENT_CREATION_COLUMN_1:
            case EquipmentMaking.EQUIPMENT_CREATION_COLUMN_2:
            case EquipmentMaking.EQUIPMENT_CREATION_COLUMN_3:
            case EquipmentMaking.EQUIPMENT_CREATION_COLUMN_4:
            case EquipmentMaking.EQUIPMENT_CREATION_COLUMN_5:
                if (player.getInterfaceId() == EquipmentMaking.EQUIPMENT_CREATION_INTERFACE_ID) {
                    EquipmentMaking.initialize(player, id, interfaceId, slot, 10);
                }
                break;
            case Shop.INVENTORY_INTERFACE_ID:
                if (player.getStatus() == PlayerStatus.SHOPPING) {
                    ShopManager.sellItem(player, slot, id, 5);
                }
                break;
            case Shop.ITEM_CHILD_ID:
                if (player.getStatus() == PlayerStatus.SHOPPING) {
                    ShopManager.buyItem(player, slot, id, 5);
                }
                break;
            case Bank.INVENTORY_INTERFACE_ID:
                Bank.deposit(player, id, slot, 10);
                break;
            // Withdrawing items from duel
            case Dueling.MAIN_INTERFACE_CONTAINER:
                if (player.getStatus() == PlayerStatus.DUELING) {
                    player.getDueling().handleItem(id, 10, slot, player.getDueling().getContainer(), player.getInventory());
                }
                break;
            case Trading.INVENTORY_CONTAINER_INTERFACE: // Duel/Trade inventory
                if (player.getStatus() == PlayerStatus.PRICE_CHECKING) {
                    player.getPriceChecker().deposit(id, 10, slot);
                } else if (player.getStatus() == PlayerStatus.TRADING) {
                    player.getTrading().handleItem(id, 10, slot, player.getInventory(), player.getTrading().getContainer());
                } else if (player.getStatus() == PlayerStatus.DUELING) {
                    player.getDueling().handleItem(id, 10, slot, player.getInventory(), player.getDueling().getContainer());
                }
                break;
            case Trading.CONTAINER_INTERFACE_ID:
                if (player.getStatus() == PlayerStatus.TRADING) {
                    player.getTrading().handleItem(id, 10, slot, player.getTrading().getContainer(), player.getInventory());
                }
                break;
            case PriceChecker.CONTAINER_ID:
                player.getPriceChecker().withdraw(id, 10, slot);
                break;
        }
    }

    private static void fourthAction(Player player, Packet packet) {
        int slot = packet.readShortA();
        int interfaceId = packet.readInt();
        int id = packet.readShortA();

        // Bank withdrawal..
        if (interfaceId >= Bank.CONTAINER_START && interfaceId < Bank.CONTAINER_START + Bank.TOTAL_BANK_TABS) {
            Bank.withdraw(player, id, slot, -1, interfaceId - Bank.CONTAINER_START);
            return;
        }

        if (interfaceId == 7423) {
            DepositBox.deposit(player, slot, id, Integer.MAX_VALUE);
            return;
        }

        switch (interfaceId) {
            case Shop.INVENTORY_INTERFACE_ID:
                if (player.getStatus() == PlayerStatus.SHOPPING) {
                    ShopManager.sellItem(player, slot, id, 10);
                }
                break;
            case Shop.ITEM_CHILD_ID:
                if (player.getStatus() == PlayerStatus.SHOPPING) {
                    ShopManager.buyItem(player, slot, id, 10);
                }
                break;
            case Bank.INVENTORY_INTERFACE_ID:
                Bank.deposit(player, id, slot, -1);
                break;
            // Withdrawing items from duel
            case Dueling.MAIN_INTERFACE_CONTAINER:
                if (player.getStatus() == PlayerStatus.DUELING) {
                    player.getDueling().handleItem(id, player.getDueling().getContainer().getAmount(id), slot,
                            player.getDueling().getContainer(), player.getInventory());
                }
                break;
            case Trading.INVENTORY_CONTAINER_INTERFACE: // Duel/Trade inventory
                if (player.getStatus() == PlayerStatus.PRICE_CHECKING) {
                    player.getPriceChecker().deposit(id, player.getInventory().getAmount(id), slot);
                } else if (player.getStatus() == PlayerStatus.TRADING) {
                    player.getTrading().handleItem(id, player.getInventory().getAmount(id), slot, player.getInventory(),
                            player.getTrading().getContainer());
                } else if (player.getStatus() == PlayerStatus.DUELING) {
                    player.getDueling().handleItem(id, player.getInventory().getAmount(id), slot, player.getInventory(),
                            player.getDueling().getContainer());
                }
                break;
            case Trading.CONTAINER_INTERFACE_ID:
                if (player.getStatus() == PlayerStatus.TRADING) {
                    player.getTrading().handleItem(id, player.getTrading().getContainer().getAmount(id), slot,
                            player.getTrading().getContainer(), player.getInventory());
                }
                break;
            case PriceChecker.CONTAINER_ID:
                player.getPriceChecker().withdraw(id, player.getPriceChecker().getAmount(id), slot);
                break;
        }
    }

    private static void fifthAction(Player player, Packet packet) {
        int interfaceId = packet.readInt();
        int slot = packet.readLEShort();
        int id = packet.readLEShort();

        // Bank withdrawal..
        if (interfaceId >= Bank.CONTAINER_START && interfaceId < Bank.CONTAINER_START + Bank.TOTAL_BANK_TABS) {
            player.setEnteredAmountAction((amount) -> {
                Bank.withdraw(player, id, slot, amount, interfaceId - Bank.CONTAINER_START);
            });
            player.getPacketSender().sendEnterAmountPrompt("How many would you like to withdraw?");
            return;
        }

        if (interfaceId == 7423) {
            player.setEnteredAmountAction((amount) -> DepositBox.deposit(player, slot, id, amount));
            player.getPacketSender().sendEnterAmountPrompt("How many would you like to deposit?");
            return;
        }

        switch (interfaceId) {
            case Shop.INVENTORY_INTERFACE_ID:
                player.setEnteredAmountAction((amount) -> {
                    ShopManager.sellItem(player, slot, id, amount);
                });
                player.getPacketSender().sendEnterAmountPrompt("How many would you like to sell?");
                break;
            case Shop.ITEM_CHILD_ID:
                player.setEnteredAmountAction((amount) -> {
                    ShopManager.buyItem(player, slot, id, amount);
                });
                player.getPacketSender().sendEnterAmountPrompt("How many would you like to buy?");
                break;

            case Bank.INVENTORY_INTERFACE_ID:
                player.setEnteredAmountAction((amount) -> {
                    Bank.deposit(player, id, slot, amount);
                });
                player.getPacketSender().sendEnterAmountPrompt("How many would you like to bank?");
                break;
            case Trading.INVENTORY_CONTAINER_INTERFACE: // Duel/Trade inventory
                if (player.getStatus() == PlayerStatus.PRICE_CHECKING) {
                    player.setEnteredAmountAction((amount) -> {
                        player.getPriceChecker().deposit(id, amount, slot);
                    });
                    player.getPacketSender().sendEnterAmountPrompt("How many would you like to deposit?");
                } else if (player.getStatus() == PlayerStatus.TRADING) {
                    player.setEnteredAmountAction((amount) -> {
                        player.getTrading().handleItem(id, amount, slot, player.getInventory(), player.getTrading().getContainer());
                    });
                    player.getPacketSender().sendEnterAmountPrompt("How many would you like to offer?");
                } else if (player.getStatus() == PlayerStatus.DUELING) {
                    player.setEnteredAmountAction((amount) -> {
                        player.getDueling().handleItem(id, amount, slot, player.getInventory(), player.getDueling().getContainer());
                    });
                    player.getPacketSender().sendEnterAmountPrompt("How many would you like to offer?");
                }
                break;
            case Trading.CONTAINER_INTERFACE_ID:
                if (player.getStatus() == PlayerStatus.TRADING) {
                    player.setEnteredAmountAction((amount) -> {
                        player.getTrading().handleItem(id, amount, slot, player.getTrading().getContainer(), player.getInventory());
                    });
                    player.getPacketSender().sendEnterAmountPrompt("How many would you like to remove?");
                }
                break;
            case Dueling.MAIN_INTERFACE_CONTAINER:
                if (player.getStatus() == PlayerStatus.DUELING) {
                    player.setEnteredAmountAction((amount) -> {
                        player.getDueling().handleItem(id, amount, slot, player.getDueling().getContainer(), player.getInventory());
                    });
                    player.getPacketSender().sendEnterAmountPrompt("How many would you like to remove?");
                }
                break;
            case PriceChecker.CONTAINER_ID:                
                player.setEnteredAmountAction((amount) -> {                    
                    player.getPriceChecker().withdraw(id, amount, slot);
                });
                player.getPacketSender().sendEnterAmountPrompt("How many would you like to withdraw?");
                break;
        }
    }

    private static void sixthAction(Player player, Packet packet) {
    }

    @Override
    public void execute(Player player, Packet packet) {

        if (player == null || player.getHitpoints() <= 0) {
            return;
        }

        switch (packet.getOpcode()) {
            case PacketConstants.FIRST_ITEM_CONTAINER_ACTION_OPCODE:
                firstAction(player, packet);
                break;
            case PacketConstants.SECOND_ITEM_CONTAINER_ACTION_OPCODE:
                secondAction(player, packet);
                break;
            case PacketConstants.THIRD_ITEM_CONTAINER_ACTION_OPCODE:
                thirdAction(player, packet);
                break;
            case PacketConstants.FOURTH_ITEM_CONTAINER_ACTION_OPCODE:
                fourthAction(player, packet);
                break;
            case PacketConstants.FIFTH_ITEM_CONTAINER_ACTION_OPCODE:
                fifthAction(player, packet);
                break;
            case PacketConstants.SIXTH_ITEM_CONTAINER_ACTION_OPCODE:
                sixthAction(player, packet);
                break;
        }
    }
}
