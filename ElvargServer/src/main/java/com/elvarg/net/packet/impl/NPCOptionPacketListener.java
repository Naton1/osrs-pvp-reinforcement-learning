package com.elvarg.net.packet.impl;

import com.elvarg.Server;
import com.elvarg.game.World;
import com.elvarg.game.content.PetHandler;
import com.elvarg.game.content.combat.magic.CombatSpell;
import com.elvarg.game.content.combat.magic.CombatSpells;
import com.elvarg.game.content.quests.QuestHandler;
import com.elvarg.game.content.skill.skillable.impl.Fishing;
import com.elvarg.game.content.skill.skillable.impl.Fishing.FishingTool;
import com.elvarg.game.content.skill.skillable.impl.Thieving.Pickpocketing;
import com.elvarg.game.entity.impl.npc.NPC;
import com.elvarg.game.entity.impl.npc.impl.Barricades;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.container.shop.ShopManager;
import com.elvarg.game.model.dialogues.builders.impl.EmblemTraderDialogue;
import com.elvarg.game.model.dialogues.builders.impl.NieveDialogue;
import com.elvarg.game.model.dialogues.builders.impl.ParduDialogue;
import com.elvarg.game.model.rights.PlayerRights;
import com.elvarg.game.entity.impl.npc.NPCInteractionSystem;
import com.elvarg.game.task.impl.WalkToTask;
import com.elvarg.net.packet.Packet;
import com.elvarg.net.packet.PacketConstants;
import com.elvarg.net.packet.PacketExecutor;
import com.elvarg.util.NpcIdentifiers;
import com.elvarg.util.ShopIdentifiers;

public class NPCOptionPacketListener extends NpcIdentifiers implements PacketExecutor {

    @Override
    public void execute(Player player, Packet packet) {
        if (player.busy()) {
            return;
        }

        int index = packet.readLEShortA();

        if (index < 0 || index > World.getNpcs().capacity()) {
            return;
        }

        final NPC npc = World.getNpcs().get(index);

        if (npc == null) {
            return;
        }

        if (!player.getLocation().isWithinDistance(npc.getLocation(), 24)) {
            return;
        }

        if (player.getRights() == PlayerRights.DEVELOPER) {
            player.getPacketSender().sendMessage("InteractionInfo Id=" + npc.getId()+" "+npc.getLocation().toString());
        }

        player.setPositionToFace(npc.getLocation());

        if (packet.getOpcode() == PacketConstants.ATTACK_NPC_OPCODE || packet.getOpcode() == PacketConstants.MAGE_NPC_OPCODE) {
            if (!npc.getCurrentDefinition().isAttackable()) {
                return;
            }
            if (npc.getHitpoints() <= 0) {
                player.getMovementQueue().reset();
                return;
            }

            if (packet.getOpcode() == PacketConstants.MAGE_NPC_OPCODE) {

                int spellId = packet.readShortA();

                CombatSpell spell = CombatSpells.getCombatSpell(spellId);

                if (spell == null) {
                    player.getMovementQueue().reset();
                    return;
                }

                player.setPositionToFace(npc.getLocation());

                player.getCombat().setCastSpell(spell);
            }

            player.getCombat().attack(npc);
            return;
        }

        WalkToTask.submit(player, npc, () -> handleInteraction(player, npc, packet));
    }

    private void handleInteraction(Player player, NPC npc, Packet packet) {

        final int opcode = packet.getOpcode();

        npc.setMobileInteraction(player);

        npc.setPositionToFace(player.getLocation());

        if (opcode == PacketConstants.FIRST_CLICK_NPC_OPCODE) {
            if (PetHandler.interact(player, npc)) {
				// Player was interacting with their pet
                return;
            }

			if (QuestHandler.firstClickNpc(player, npc)) {
				// NPC Click was handled by a quest
				return;
			}

			if (NPCInteractionSystem.handleFirstOption(player, npc)) {
				// Player is interacting with a defined NPC
				return;
			}

            switch (npc.getId()) {
                case SHOP_KEEPER_4:
                    ShopManager.open(player, ShopIdentifiers.GENERAL_STORE);
                    break;
                case CHARLIE_THE_COOK:
                    ShopManager.open(player, ShopIdentifiers.FOOD_SHOP);
                    break;
                case RICK:
                    ShopManager.open(player, ShopIdentifiers.PURE_SHOP);
                    break;
                case AJJAT:
                    ShopManager.open(player, ShopIdentifiers.ARMOR_SHOP);
                    break;
                case MAGIC_INSTRUCTOR:
                    ShopManager.open(player, ShopIdentifiers.MAGE_ARMOR_SHOP);
                    break;
                case ARMOUR_SALESMAN:
                    ShopManager.open(player, ShopIdentifiers.RANGE_SHOP);
                    break;
                case BANKER_2:
                case TZHAAR_KET_ZUH:
                    player.getBank(player.getCurrentBankTab()).open();
                    break;
                case MAKE_OVER_MAGE:
                    player.getPacketSender().sendInterfaceRemoval().sendInterface(3559);
                    player.getAppearance().setCanChangeAppearance(true);
                    break;
                case SECURITY_GUARD:
                    //DialogueManager.start(player, 2500);
                    break;
                case EMBLEM_TRADER:
                case EMBLEM_TRADER_2:
                case EMBLEM_TRADER_3:
                    player.getDialogueManager().start(new EmblemTraderDialogue());
                    break;

                case PERDU:
                    player.getDialogueManager().start(new ParduDialogue());
                    break;

                case FINANCIAL_ADVISOR:
                    //DialogueManager.start(player, 15);
                    // Removed
                    break;
                case NIEVE:
                    player.getDialogueManager().start(new NieveDialogue());
                    break;
            }
            return;
        }


        if (opcode == PacketConstants.SECOND_CLICK_NPC_OPCODE) {
			if (PetHandler.pickup(player, npc)) {
				// Player is picking up their pet
				return;
			}

			if (Pickpocketing.init(player, npc)) {
				// Player is trying to thieve from an NPC
				return;
			}

			if (NPCInteractionSystem.handleSecondOption(player, npc)) {
				// Player is interacting with a defined NPC
				return;
			}

            switch (npc.getId()) {
                case NIEVE:
                    player.getDialogueManager().start(new NieveDialogue(), 2);
                    break;
                case BANKER:
                case BANKER_2:
                case BANKER_3:
                case BANKER_4:
                case BANKER_5:
                case BANKER_6:
                case BANKER_7:
                case TZHAAR_KET_ZUH:
                    player.getBank(player.getCurrentBankTab()).open();
                    break;
                case 1497: // Net and bait
                case 1498: // Net and bait
                    player.getSkillManager().startSkillable(new Fishing(npc, FishingTool.FISHING_ROD));
                    break;
                case RICHARD_2:
                    ShopManager.open(player, ShopIdentifiers.TEAMCAPE_SHOP);
                    break;
                case EMBLEM_TRADER:
                case EMBLEM_TRADER_2:
                case EMBLEM_TRADER_3:
                    ShopManager.open(player, ShopIdentifiers.PVP_SHOP);
                    break;
                case MAGIC_INSTRUCTOR:
                    ShopManager.open(player, ShopIdentifiers.MAGE_ARMOR_SHOP);
                    break;
                case SQUIRE_8:
                    ShopManager.open(player, ShopIdentifiers.VOID_MAGIC_SHOP);
                    break;
                case SQUIRE_6:
                    ShopManager.open(player, ShopIdentifiers.VOID_RANGED_SHOP);
                    break;

            }
            return;
        }

        if (opcode == PacketConstants.THIRD_CLICK_NPC_OPCODE) {
            if (PetHandler.morph(player, npc)) {
				// Player is morphing their pet
                return;
            }

			if (NPCInteractionSystem.handleThirdOption(player, npc)) {
				// Player is interacting with a defined NPC
				return;
			}

            switch (npc.getId()) {

                case EMBLEM_TRADER:
                    player.getDialogueManager().start(new EmblemTraderDialogue(), 2);
                    break;
                case MAGIC_INSTRUCTOR:
                    ShopManager.open(player, ShopIdentifiers.MAGE_RUNES_SHOP);
                    break;
            }
            return;
        }

        if (opcode == PacketConstants.FOURTH_CLICK_NPC_OPCODE) {
			if (NPCInteractionSystem.handleForthOption(player, npc)) {
				// Player is interacting with a defined NPC
				return;
			}

            switch (npc.getId()) {
                case EMBLEM_TRADER:
                    player.getDialogueManager().start(new EmblemTraderDialogue(), 5);
                    break;
            }
            return;
        }
    }
}
