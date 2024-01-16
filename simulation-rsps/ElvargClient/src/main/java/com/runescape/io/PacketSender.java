package com.runescape.io;

import com.runescape.Client;
import com.runescape.net.IsaacCipher;
import com.runescape.util.ChatMessageCodec;

public class PacketSender {
    
    private final Buffer buffer;
    
    public PacketSender(IsaacCipher cipher) {
        buffer = Buffer.create(5000, cipher);
    }
    
    // Used for pinging
    public void sendEmptyPacket() {
        buffer.writeOpcode(0);
    }

    public void sendPathRequest(int x, int y, int plane) {
        buffer.writeOpcode(164);
        buffer.writeShort(x);
        buffer.writeShort(y);
        buffer.writeByte(plane);
    }

    public void sendFriendAddition(long friend) {
        buffer.writeOpcode(188);
        buffer.writeLong(friend);
    }

    public void sendFriendDeletion(long friend) {
        buffer.writeOpcode(215);
        buffer.writeLong(friend);
    }

    public void sendIgnoreAddition(long ignore) {
        buffer.writeOpcode(133);
        buffer.writeLong(ignore);
    }

    public void sendIgnoreDeletion(long ignore) {
        buffer.writeOpcode(74);
        buffer.writeLong(ignore);
    }

    public void sendPrivateMessage(long player, String message) {
        buffer.writeOpcode(126);
        buffer.writeByte(0);
        int savedPosition = buffer.currentPosition;
        buffer.writeLong(player);
        ChatMessageCodec.encode(message, buffer);
        buffer.writeBytes(buffer.currentPosition - savedPosition);
    }
    
    public void sendClanChatMessage(String message) {
        buffer.writeOpcode(104);
        buffer.writeByte(message.length() + 1);
        buffer.writeString(message);
    }
    
    public void sendChatMessage(int colour, int effect, String message) {        
        buffer.writeOpcode(4);
        buffer.writeByte(0);
        int savedPosition = buffer.currentPosition;
        buffer.writeByteS(colour);
        buffer.writeByteS(effect);
        Client.instance.chatBuffer.currentPosition = 0;
        ChatMessageCodec.encode(message, Client.instance.chatBuffer);
        buffer.writeReverseDataA(Client.instance.chatBuffer.payload, 0, Client.instance.chatBuffer.currentPosition);
        buffer.writeBytes(buffer.currentPosition - savedPosition);
        
        /*buffer.writeOpcode(4);
        buffer.writeByte(colour);
        buffer.writeByte(effect);
        buffer.writeString(message);*/
    }

    public void sendAttackNPC(int npcIndex) {
        buffer.writeOpcode(72);
        buffer.writeLEShortA(npcIndex);
    }

    public void sendAttackPlayer(int playerIndex) {
        buffer.writeOpcode(153);
        buffer.writeLEShort(playerIndex);
    }

    public void sendBankTabCreation(int interfaceId, int fromSlot, int toSlot) {
        buffer.writeOpcode(216);
        buffer.writeInt(interfaceId);
        buffer.writeShort(fromSlot);
        buffer.writeLEShort(toSlot);
    }

    public void sendChatboxDuel(int playerIndex) {
        buffer.writeOpcode(128);
        buffer.writeShort(playerIndex);
    }

    public void sendChatConfigurations(int publicMode, int privateMode, int tradeMode) {
        buffer.writeOpcode(95);
        buffer.writeByte(publicMode);
        buffer.writeByte(privateMode);
        buffer.writeByte(tradeMode);
    }

    public void sendButtonClick(int buttonId) {
        buffer.writeOpcode(185);
        buffer.writeInt(buttonId);
    }

    public void sendButtonAction(int buttonId, int actionIndex) {
        buffer.writeOpcode(186);
        buffer.writeInt(buttonId);
        buffer.writeByte(actionIndex);
    }

    public void sendTeleportSelection(int teleportType, int teleportIndex) {
        buffer.writeOpcode(183);
        buffer.writeByte(teleportType);
        buffer.writeByte(teleportIndex);
    }

    public void sendInterfaceClear() {
        buffer.writeOpcode(130);
    }

    public void sendCommand(String command) {
        buffer.writeOpcode(103);
        buffer.writeByte(command.length() + 1);
        buffer.writeString(command);
    }

    public void sendCreationMenuAction(int itemId, int amount) {
        buffer.writeOpcode(166);
        buffer.writeInt(itemId);
        buffer.writeByte(amount);
    }

    public void sendDropItem(int itemId, int interfaceId, int slot) {
        buffer.writeOpcode(87);
        buffer.writeShortA(itemId);
        buffer.writeShort(interfaceId);
        buffer.writeShortA(slot);
    }

    public void sendEnteredSyntax(String syntax) {
        buffer.writeOpcode(60);
        buffer.writeByte(syntax.length() + 1);
        buffer.writeString(syntax);
    }
    
    public void sendEnteredAmount(int amount) {
        buffer.writeOpcode(208);
        buffer.writeInt(amount);
    }

    public void sendEquipItem(int itemId, int slot, int interfaceId) {
        buffer.writeOpcode(41);
        buffer.writeShort(itemId);
        buffer.writeShortA(slot);
        buffer.writeShortA(interfaceId);
    }

    public void sendExamineItem(int itemId, int interfaceId) {
        buffer.writeOpcode(2);
        buffer.writeShort(itemId);
        buffer.writeInt(interfaceId);
    }

    public void sendExamineNPC(int npcId) {
        buffer.writeOpcode(6);
        buffer.writeShort(npcId);
    }

    public void sendRegionChange() {
        buffer.writeOpcode(210);
        buffer.writeInt(0x3f008edd);
    }

    public void sendFinalizedRegionChange() {
        buffer.writeOpcode(121);
    }

    public void sendFollowPlayer(int playerIndex) {
        buffer.writeOpcode(73);
        buffer.writeLEShort(playerIndex);
    }
    
    public void sendTradePlayer(int playerIndex) {
        buffer.writeOpcode(139);
        buffer.writeLEShort(playerIndex);
    }

    public void sendItemContainerOption1(int interfaceId, int slot, int itemId) {
        buffer.writeOpcode(145);
        buffer.writeInt(interfaceId);
        buffer.writeShortA(slot);
        buffer.writeShortA(itemId);
    }

    public void sendItemContainerOption2(int interfaceId, int itemId, int slot) {
        buffer.writeOpcode(117);
        buffer.writeInt(interfaceId);
        buffer.writeLEShortA(itemId);
        buffer.writeLEShort(slot);
    }

    public void sendItemContainerOption3(int interfaceId, int itemId, int slot) {
        buffer.writeOpcode(43);
        buffer.writeInt(interfaceId);
        buffer.writeShortA(itemId);
        buffer.writeShortA(slot);
    }

    public void sendItemContainerOption4(int slot, int interfaceId, int itemId) {
        buffer.writeOpcode(129);
        buffer.writeShortA(slot);
        buffer.writeInt(interfaceId);
        buffer.writeShortA(itemId);
    }

    public void sendItemContainerOption5(int interfaceId, int slot, int itemId) {
        buffer.writeOpcode(135);
        buffer.writeInt(interfaceId);
        buffer.writeLEShort(slot);
        buffer.writeLEShort(itemId);
    }
    
    public void sendItemContainerSlotSwap(int interfaceId, int dummy, int fromSlot, int toSlot) {
        buffer.writeOpcode(214);
        buffer.writeInt(interfaceId);
        buffer.writeNegatedByte(dummy);
        buffer.writeLEShortA(fromSlot);
        buffer.writeLEShort(toSlot);
    }

    public void sendUseItemOnGroundItem(int interfaceId, int itemId, int groundItemId, int y, int dummy, int x) {
        buffer.writeOpcode(25);
        buffer.writeLEShort(interfaceId);
        buffer.writeShortA(itemId);
        buffer.writeShort(groundItemId);
        buffer.writeShortA(y);
        buffer.writeLEShortA(dummy);
        buffer.writeShort(x);
    }

    public void sendUseItemOnItem(int slot, int anInt1283, int itemId, int anInt1284, int anInt1285, int interfaceId) {
        buffer.writeOpcode(53);
        buffer.writeShort(slot);
        buffer.writeShortA(anInt1283);
        buffer.writeLEShortA(itemId);
        buffer.writeShort(anInt1284);
        buffer.writeLEShort(anInt1285);
        buffer.writeShort(interfaceId);
    }

    public void sendUseItemOnNPC(int npcId, int npcIndex, int itemSlot, int anInt1284) {
        buffer.writeOpcode(57);
        buffer.writeShortA(npcId);
        buffer.writeShortA(npcIndex);
        buffer.writeLEShort(itemSlot);
        buffer.writeShortA(anInt1284);
    }

    public void sendUseItemOnObject(int interfaceId, int objectId, int objectY, int itemSlot, int objectX, int itemId) {
        buffer.writeOpcode(192);
        buffer.writeShort(interfaceId);
        buffer.writeShort(objectId);
        buffer.writeLEShortA(objectY);
        buffer.writeLEShort(itemSlot);
        buffer.writeLEShortA(objectX);
        buffer.writeShort(itemId);
    }

    public void sendUseItemOnPlayer(int interfaceId, int playerIndex, int itemId, int itemSlot) {
        buffer.writeOpcode(14);
        buffer.writeShortA(interfaceId);
        buffer.writeShort(playerIndex);
        buffer.writeShort(itemId);
        buffer.writeLEShort(itemSlot);
    }

    public void sendUseMagicOnGroundItem(int groundItemX, int groundItemId, int groundItemY, int spellId) {
        buffer.writeOpcode(181);
        buffer.writeLEShort(groundItemX);
        buffer.writeShort(groundItemId);
        buffer.writeLEShort(groundItemY);
        buffer.writeShortA(spellId);
    }

    public void sendUseMagicOnItem(int itemSlot, int itemId, int interfaceId, int spellId) {
        buffer.writeOpcode(237);
        buffer.writeShort(itemSlot);
        buffer.writeShortA(itemId);
        buffer.writeShort(interfaceId);
        buffer.writeShortA(spellId);
    }

    public void sendUseMagicOnNPC(int npcIndex, int spellId) {
        buffer.writeOpcode(131);
        buffer.writeLEShortA(npcIndex);
        buffer.writeShortA(spellId);
    }

    public void sendUseMagicOnPlayer(int playerIndex, int selectedSpellId) {
        buffer.writeOpcode(249);
        buffer.writeShortA(playerIndex);
        buffer.writeLEShort(selectedSpellId);
    }

    public void sendItemOption1(int interfaceId, int slot, int itemId) {
        buffer.writeOpcode(122);
        buffer.writeShort(interfaceId);
        buffer.writeShort(slot);
        buffer.writeShort(itemId);
    }

    public void sendItemOption2(int interfaceId, int slot, int itemId) {
        buffer.writeOpcode(75);
        buffer.writeLEShortA(interfaceId);
        buffer.writeLEShort(slot);
        buffer.writeShortA(itemId);
    }

    public void sendItemOption3(int itemId, int slot, int interfaceId) {
        buffer.writeOpcode(16);
        buffer.writeShortA(itemId);
        buffer.writeLEShortA(slot);
        buffer.writeLEShortA(interfaceId);
    }

    public void sendNextDialogue(int interfaceId) {
        buffer.writeOpcode(40);
        buffer.writeShort(interfaceId);
    }

    public void sendNPCOption1(int npcIndex) {
        buffer.writeOpcode(155);
        buffer.writeLEShortA(npcIndex);
    }

    public void sendNPCOption2(int npcIndex) {
        buffer.writeOpcode(17);
        buffer.writeLEShortA(npcIndex);
    }

    public void sendNPCOption3(int npcIndex) {
        buffer.writeOpcode(21);
        buffer.writeLEShortA(npcIndex);
    }

    public void sendNPCOption4(int npcIndex) {
        buffer.writeOpcode(18);
        buffer.writeLEShortA(npcIndex);
    }

    public void sendObjectOption1(int x, int id, int y) {
        buffer.writeOpcode(132);
        buffer.writeLEShortA(x);
        buffer.writeShort(id);
        buffer.writeShortA(y);
    }

    public void sendObjectOption2(int id, int y, int x) {
        buffer.writeOpcode(252);
        buffer.writeLEShortA(id);
        buffer.writeLEShort(y);
        buffer.writeShortA(x);
    }

    public void sendObjectOption3(int x, int y, int id) {
        buffer.writeOpcode(70);
        buffer.writeLEShort(x);
        buffer.writeShort(y);
        buffer.writeLEShortA(id);
    }

    public void sendObjectOption4(int x, int id, int y) {
        buffer.writeOpcode(234);
        buffer.writeLEShortA(x);
        buffer.writeShortA(id);
        buffer.writeLEShortA(y);
    }

    public void sendObjectOption5(int id, int x, int y) {
        buffer.writeOpcode(228);
        buffer.writeShortA(x);
        buffer.writeShortA(id);
        buffer.writeShort(y);
    }

    public void sendPlayerOption1(int playerIndex) {
        buffer.writeOpcode(128);
        buffer.writeShort(playerIndex);
    }

    public void sendPlayerInactive() {
        buffer.writeOpcode(202);
    }

    public void sendPickupItem(int y, int itemId, int x) {
        buffer.writeOpcode(236);
        buffer.writeLEShort(y);
        buffer.writeShort(itemId);
        buffer.writeLEShort(x);
    }

    public void sendInterfaceTab(int tabId) {
        buffer.writeOpcode(239);
        buffer.writeByte(tabId);
        System.err.println("setTabId="+tabId);
    }

    public void sendGroundItemOption1(int y, int itemId, int x) {
        buffer.writeOpcode(235);
        buffer.writeLEShort(y);
        buffer.writeShort(itemId);
        buffer.writeLEShort(x);
    }
    
    public void sendSpawnTabSelection(int item, boolean spawnX, boolean toBank) {
        buffer.writeOpcode(187);
        buffer.writeInt(item);
        buffer.writeByte(spawnX ? 1 : 0);
        buffer.writeByte(toBank ? 1 : 0);
    }
    
    public void sendSpecialAttackToggle(int interfaceId) {
        buffer.writeOpcode(184);
        buffer.writeInt(interfaceId);
    }
    
    public void sendAppearanceChange(boolean isMale, int[] appearance, int[] colours) {
        buffer.writeOpcode(11);
        buffer.writeByte(isMale ? 0 : 1);
        for (int i1 = 0; i1 < 7; i1++) {
            buffer.writeByte(appearance[i1]);
        }
        for (int l1 = 0; l1 < 5; l1++) {
            buffer.writeByte(colours[l1]);
        }
    }
    
    public Buffer getBuffer() {
        return buffer;
    }
}
