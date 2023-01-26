package com.elvarg.game.content.quests.impl;

import com.elvarg.game.content.quests.Quest;
import com.elvarg.game.content.quests.QuestHandler;
import com.elvarg.game.entity.impl.npc.NPC;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.Skill;
import com.elvarg.game.model.dialogues.DialogueExpression;
import com.elvarg.game.model.dialogues.builders.DialogueChainBuilder;
import com.elvarg.game.model.dialogues.entries.impl.*;
import com.elvarg.util.NpcIdentifiers;

import java.util.LinkedHashMap;
import static com.elvarg.game.content.quests.QuestHandler.NOT_STARTED;
import static com.elvarg.game.content.quests.QuestHandler.Quests.COOKS_ASSISTANT;

public class CooksAssistant implements Quest {

	private static final int EGG = 1944;
	private static final int MILK = 1927;
	private static final int FLOUR = 1933;

	private static final int NPC_COOK = NpcIdentifiers.COOK;

	DialogueChainBuilder dialogueBuilder;

	@Override
	public int questTabStringId() {
		return 7333;
	}

	@Override
	public int questTabButtonId() {
		return 28165;
	}

	@Override
	public int questPointsReward() { return 1; }

	@Override
	public int completeStatus() { return 3; }

	public CooksAssistant() {
		this.buildDialogues();
	}

	public void showQuestLog(Player player, int currentStatus) {
		QuestHandler.clearQuestLogInterface(player);

		player.getPacketSender().sendString("@dre@Cook's Assistant", 8144);
		player.getPacketSender().sendString("", 8145);

		switch (currentStatus) {

			case NOT_STARTED:
				player.getPacketSender().sendString("Cook's Assistant", 8144);
				player.getPacketSender().sendString("I can start this quest by speaking to the Cook in the", 8147);
				player.getPacketSender().sendString("Lumbridge Castle kitchen.", 8148);
				player.getPacketSender().sendString("", 8149);
				player.getPacketSender().sendString("There are no minimum requirements.", 8150);
				break;

			case 1:
				player.getPacketSender().sendString("Cook's Assistant", 8144);
				player.getPacketSender().sendString("@str@I've talked to the cook.", 8147);
				player.getPacketSender().sendString("He wants me to gather the following materials:", 8148);
				if (player.getInventory().contains(EGG)) {
					player.getPacketSender().sendString("@str@1 egg", 8149);
				} else {
					player.getPacketSender().sendString("@red@1 egg", 8149);
				}
				if (player.getInventory().contains(MILK)) {
					player.getPacketSender().sendString("@str@1 bucket of milk", 8150);
				} else {
					player.getPacketSender().sendString("@red@1 bucket of milk", 8150);
				}
				if (player.getInventory().contains(FLOUR)) {
					player.getPacketSender().sendString("@str@1 heap of flour", 8151);
				} else {
					player.getPacketSender().sendString("@red@1 pot of flour", 8151);
				}
				break;

			case 2:
				player.getPacketSender().sendString("Cook's Assistant", 8144);
				player.getPacketSender().sendString("@str@I talked to the cook.", 8147);
				player.getPacketSender().sendString("@str@I gave the cook his items.", 8148);
				player.getPacketSender().sendString("I should go speak to the cook.", 8149);
				break;

			case 3:
				player.getPacketSender().sendString("Cook's Assistant", 8144);
				player.getPacketSender().sendString("@str@I talked to the cook.", 8147);
				player.getPacketSender().sendString("@str@I gave him his items.", 8148);
				player.getPacketSender().sendString("@red@     QUEST COMPLETE", 8150);
				player.getPacketSender().sendString("As a reward, I gained 300 Cooking Experience.", 8151);
				break;
		}

		player.getPacketSender().sendInterface(8134);
	}

	@Override
	public boolean firstClickNpc(Player player, NPC npc) {
		if (npc.getId() != NPC_COOK) {
			return false;
		}

		switch (COOKS_ASSISTANT.getProgress(player)) {
			case NOT_STARTED -> player.getDialogueManager().start(this.dialogueBuilder, 0);
			case 1 -> player.getDialogueManager().start(this.dialogueBuilder, 20);
			case 2 -> player.getDialogueManager().start(this.dialogueBuilder, 24);
			case 3 -> {
				// If player has completed this quest, we shouldn't be handling the dialogue anymore
				return false;
			}
		}

		return true;
	}

	private void buildDialogues() {
		this.dialogueBuilder  = new DialogueChainBuilder();
		this.dialogueBuilder.add(
				new NpcDialogue(0, NPC_COOK, "What am I to do?", DialogueExpression.SAD),
				new OptionsDialogue(1, new LinkedHashMap<>() {{
					put("What's wrong?", (player) -> player.getDialogueManager().start(2));
					put("Can you cook me a cake?", (player) -> player.getDialogueManager().start(14));
					put("You don't look very happy.", (player) -> player.getDialogueManager().start(2));
					put("Nice hat.", (player) -> player.getDialogueManager().start(17));
				}}),

				new PlayerDialogue(2, "What's wrong?"),
				new NpcDialogue(3, NPC_COOK,
						"Oh dear, oh dear, oh dear, I'm in a terrible terrible" +
								"mess! It's the Duke's birthday today, and I should be" +
								"making him a lovely big birthday cake!", DialogueExpression.SAD),
				new NpcDialogue(4, NPC_COOK,
						"I've forgotten to buy the ingredients. I'll never get" +
								"them in time now. He'll sack me! What will I do? I have" +
								"four children and a goat to look after. Would you help" +
								"me? Please?", DialogueExpression.SAD),
				new OptionsDialogue(5, new LinkedHashMap<>() {{
					put("I'm always happy to help a cook in distress.", (player) -> {
						COOKS_ASSISTANT.setProgress(player, 1);
						player.getDialogueManager().start(6);
					});
					put("I can't right now, Maybe later.", (player) -> player.getDialogueManager().start(11));
				}}),

				new PlayerDialogue(6, "Yes, I'll help you.", DialogueExpression.HAPPY),
				new NpcDialogue(7, NPC_COOK, "Oh thank you, thank you. I need milk, an egg, and " +
						"flour. I'd be very grateful if you can get them for me.", DialogueExpression.HAPPY),
				new PlayerDialogue(8, "So where do I find these ingredients then?", DialogueExpression.DISTRESSED),
				new NpcDialogue(9, NPC_COOK, "You can find flour in any of the shops here." +
						"You can find eggs by killing chickens." +
						"You can find milk by using a bucket on a cow"),
				new EndDialogue(10),

				new PlayerDialogue(11, "I can't right now, Maybe later.", DialogueExpression.SAD),
				new NpcDialogue(12, NPC_COOK, "Oh please! Hurry then!", DialogueExpression.DISTRESSED),
				new EndDialogue(13),

				new PlayerDialogue(14, "Can you bake me a cake?", DialogueExpression.EVIL_LAUGH_SHORT),
				new NpcDialogue(15, NPC_COOK, "Does it look like I have the time?", DialogueExpression.ANGRY_1),
				new EndDialogue(16),

				new PlayerDialogue(17, "Nice hat!", DialogueExpression.EVIL_LAUGH_SHORT),
				new NpcDialogue(18, NPC_COOK, "I don't have time for your jibber-jabber!", DialogueExpression.ANGRY_1),
				new EndDialogue(19),

				new NpcDialogue(20, NPC_COOK, "How are you getting on with finding the ingredients?", DialogueExpression.DISTRESSED, (player) -> {
					if (player.getInventory().contains(EGG) && player.getInventory().contains(MILK)
							&& player.getInventory().contains(FLOUR)) {
						player.getDialogueManager().start(this.dialogueBuilder, 21);
					} else {
						player.getDialogueManager().start(this.dialogueBuilder, 24);
					}
				}),

				new PlayerDialogue(21, "Here's all the ingredients!"),
				new NpcDialogue(22, NPC_COOK, "You brought me everything I need! I'm saved!", DialogueExpression.HAPPY, (player) -> {
					player.getInventory().delete(EGG, 1);
					player.getInventory().delete(MILK, 1);
					player.getInventory().delete(FLOUR, 1);

					COOKS_ASSISTANT.setProgress(player, 2);
				}),
				new PlayerDialogue(24, "So do I get to go to the Duke's Party?"),
				new NpcDialogue(25, NPC_COOK, "I'm afraid not, only the big cheeses get to dine with the"
						+"Duke.", DialogueExpression.SLIGHTLY_SAD),
				new PlayerDialogue(26, "Well, maybe one day I'll be important enough to sit on" +
						"the Duke's table"),
				new NpcDialogue(27, NPC_COOK, "Maybe, but I won't be holding my breath.", DialogueExpression.LAUGHING, (player) -> {
					COOKS_ASSISTANT.showRewardInterface(player, new String[] { "1 Quest Point", "500 Coins", "300 Cooking XP", "", "", "" }, 326);
					player.getInventory().add(995, 500);
					player.getSkillManager().addExperience(Skill.COOKING, 300);
					player.getPacketSender().sendMessage("You completed " + COOKS_ASSISTANT.getName() + "!");
					//client.getActionSender().sendQuickSong(93, 0);
				}),

				new PlayerDialogue(24, "I don't have all the ingredients yet!", DialogueExpression.SAD),
				new EndDialogue(25)
		);
	}
}
