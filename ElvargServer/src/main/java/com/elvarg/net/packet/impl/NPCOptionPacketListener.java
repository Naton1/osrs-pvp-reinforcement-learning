package com.elvarg.net.packet.impl;

import com.elvarg.game.World;
import com.elvarg.game.content.PetHandler;
import com.elvarg.game.content.combat.magic.CombatSpell;
import com.elvarg.game.content.combat.magic.CombatSpells;
import com.elvarg.game.content.skill.skillable.impl.Fishing;
import com.elvarg.game.content.skill.skillable.impl.Fishing.FishingTool;
import com.elvarg.game.content.skill.skillable.impl.Thieving.Pickpocketing;
import com.elvarg.game.entity.impl.npc.NPC;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.container.shop.ShopManager;
import com.elvarg.game.model.dialogues.builders.impl.NieveDialogue;
import com.elvarg.game.model.movement.WalkToAction;
import com.elvarg.game.model.rights.PlayerRights;
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

		switch (packet.getOpcode()) {
		case PacketConstants.ATTACK_NPC_OPCODE:
			attackNPC(player, packet);
			break;
		case PacketConstants.FIRST_CLICK_NPC_OPCODE:
			firstClick(player, packet);
			break;
		case PacketConstants.SECOND_CLICK_NPC_OPCODE:
			handleSecondClick(player, packet);
			break;
		case PacketConstants.THIRD_CLICK_NPC_OPCODE:
			handleThirdClick(player, packet);
			break;
		case PacketConstants.FOURTH_CLICK_NPC_OPCODE:
			handleFourthClick(player, packet);
			break;
		case PacketConstants.MAGE_NPC_OPCODE:
			mageNpc(player, packet);
			break;
		}
	}

	private static void firstClick(Player player, Packet packet) {
		int index = packet.readLEShort();
		if (index < 0 || index > World.getNpcs().capacity()) {
			return;
		}
		final NPC npc = World.getNpcs().get(index);
		if (npc == null) {
			return;
		}

		if (player.getRights() == PlayerRights.DEVELOPER) {
			player.getPacketSender().sendMessage(
					"First click NPC: " + Integer.toString(npc.getId()) + ". " + npc.getLocation().toString());
		}

        player.setMobileInteraction(npc);
        player.setFollowing(npc);
        player.setWalkToTask(new WalkToAction(player) {
			@Override
			public void execute() {
				npc.setPositionToFace(player.getLocation());
				player.setPositionToFace(npc.getLocation());

				// Check if we're interacting with our pet..
				if (PetHandler.interact(player, npc)) {
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
					/*// And then start dialogue
					//DialogueManager.start(player, 0);
					// Set dialogue options
					player.setDialogueOptions(new DialogueOptions() {
						@Override
						public void handleOption(Player player, int option) {
							switch (option) {
							case 1:
								ShopManager.open(player, ShopIdentifiers.PVP_SHOP);
								break;
							case 2:
								// Sell emblems option
								player.setDialogueOptions(new DialogueOptions() {
									@Override
									public void handleOption(Player player, int option) {
										if (option == 1) {
											int cost = BountyHunter.getValueForEmblems(player, true);
											player.getPacketSender().sendMessage("@red@You have received " + cost
													+ " blood money for your emblem(s).");
											//DialogueManager.start(player, 4);
										} else {
											player.getPacketSender().sendInterfaceRemoval();
										}
									}
								});
								int value = BountyHunter.getValueForEmblems(player, false);
								if (value > 0) {
									player.setDialogue(//DialogueManager.getDialogues().get(10)); // Yes / no option
									//DialogueManager.sendStatement(player,
											"I will give you " + value + " blood money for those emblems. Agree?");
								} else {
									//DialogueManager.start(player, 5);
								}
								break;
							case 3:
								// Skull me option
								if (player.isSkulled()) {
									//DialogueManager.start(player, 3);
								} else {
									//DialogueManager.start(player, 22);
									player.setDialogueOptions(new DialogueOptions() {
										@Override
										public void handleOption(Player player, int option) {
											if (option == 1) {
												CombatFactory.skull(player, SkullType.WHITE_SKULL, 300);
											} else if (option == 2) {
												CombatFactory.skull(player, SkullType.RED_SKULL, 300);
											}
											player.getPacketSender().sendInterfaceRemoval();
										}
									});
								}
								break;
							case 4:
								// Cancel option
								player.getPacketSender().sendInterfaceRemoval();
								break;
							}
						}
					});*/
					break;

				case PERDU:
					// Set dialogue options
					/*player.setDialogueOptions(new DialogueOptions() {
						@Override
						public void handleOption(Player player, int option) {
							if (option == 1) {

								int cost = BrokenItem.getRepairCost(player);

								player.setDialogueOptions(new DialogueOptions() {
									@Override
									public void handleOption(Player player, int option) {
										if (option == 1) {
											BrokenItem.repair(player);
										} else {
											player.getPacketSender().sendInterfaceRemoval();
										}
									}
								});

								if (cost > 0) {
									player.setDialogue(//DialogueManager.getDialogues().get(10)); // Yes / no option
									//DialogueManager.sendStatement(player, "It will cost you " + cost
											+ " blood money to fix your broken items. Agree?");
								} else {
									//DialogueManager.start(player, 20);
								}

							} else {
								player.getPacketSender().sendInterfaceRemoval();
							}
						}
					});

					// Start main dialogue
					//DialogueManager.start(player, 19);*/
					break;

				case FINANCIAL_ADVISOR:
					//DialogueManager.start(player, 15);
					// Removed
					break;
				case NIEVE:
				    player.getDialogueManager().start(new NieveDialogue());
				    break;
				}
            }

            @Override
            public boolean inDistance() {
                return player.calculateDistance(npc) <= 1;
            }
		});
	}

	public void handleSecondClick(Player player, Packet packet) {
		int index = packet.readLEShortA();
		if (index < 0 || index > World.getNpcs().capacity()) {
			return;
		}
		final NPC npc = World.getNpcs().get(index);
		if (npc == null) {
			return;
		}

		if (player.getRights() == PlayerRights.DEVELOPER) {
			player.getPacketSender().sendMessage(
					"Second click NPC: " + Integer.toString(npc.getId()) + ". " + npc.getLocation().toString());
		}

        player.setMobileInteraction(npc);
        player.setFollowing(npc);
        player.setWalkToTask(new WalkToAction(player) {
			@Override
			public void execute() {
				npc.setPositionToFace(player.getLocation());
				player.setPositionToFace(npc.getLocation());

				// Check if we're picking up our pet..
				if (PetHandler.pickup(player, npc)) {
					return;
				}

				// Check if we're thieving..
				if (Pickpocketing.init(player, npc)) {
					return;
				}

				switch (npc.getId()) {
				case NIEVE:
                    player.getDialogueManager().start(new NieveDialogue(), 2);
                    break;
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

				}
			}
			
			@Override
            public boolean inDistance() {
                return player.calculateDistance(npc) <= 1;
            }
		});
	}

	public void handleThirdClick(Player player, Packet packet) {
		int index = packet.readShort();
		if (index < 0 || index > World.getNpcs().capacity()) {
			return;
		}
		final NPC npc = World.getNpcs().get(index);
		if (npc == null) {
			return;
		}

		if (player.getRights() == PlayerRights.DEVELOPER) {
			player.getPacketSender().sendMessage(
					"Third click NPC: " + Integer.toString(npc.getId()) + ". " + npc.getLocation().toString());
		}

        player.setMobileInteraction(npc);
        player.setFollowing(npc);
        player.setWalkToTask(new WalkToAction(player) {
            @Override
            public void execute() {
                npc.setPositionToFace(player.getLocation());
                player.setPositionToFace(npc.getLocation());

                if (PetHandler.morph(player, npc)) {
                    return;
                }
                switch (npc.getId()) {

                case EMBLEM_TRADER:
                    // Sell emblems option
                  /*  player.setDialogueOptions(new DialogueOptions() {
                        @Override
                        public void handleOption(Player player, int option) {
                            if (option == 1) {
                                int cost = BountyHunter.getValueForEmblems(player, true);
                                player.getPacketSender().sendMessage(
                                        "@red@You have received " + cost + " blood money for your emblem(s).");
                                //DialogueManager.start(player, 4);
                            } else {
                                player.getPacketSender().sendInterfaceRemoval();
                            }
                        }
                    });
                    int value = BountyHunter.getValueForEmblems(player, false);
                    if (value > 0) {
                        player.setDialogue(//DialogueManager.getDialogues().get(10)); // Yes / no option
                        //DialogueManager.sendStatement(player,
                                "I will give you " + value + " blood money for those emblems. Agree?");
                    } else {
                        //DialogueManager.start(player, 5);
                    }*/
                    break;
                case MAGIC_INSTRUCTOR:
                    ShopManager.open(player, ShopIdentifiers.MAGE_RUNES_SHOP);
                    break;
                }
            }

            @Override
            public boolean inDistance() {
                return player.calculateDistance(npc) <= 1;
            }
        });
	}

	public void handleFourthClick(Player player, Packet packet) {
		int index = packet.readLEShort();
		if (index < 0 || index > World.getNpcs().capacity()) {
			return;
		}
		final NPC npc = World.getNpcs().get(index);
		if (npc == null) {
			return;
		}

		if (player.getRights() == PlayerRights.DEVELOPER) {
			player.getPacketSender().sendMessage(
					"Fourth click NPC: " + Integer.toString(npc.getId()) + ". " + npc.getLocation().toString());
		}

        player.setMobileInteraction(npc);
        player.setFollowing(npc);
        player.setWalkToTask(new WalkToAction(player) {
            @Override
            public void execute() {
                npc.setPositionToFace(player.getLocation());
                player.setPositionToFace(npc.getLocation());

                switch (npc.getId()) {
                case EMBLEM_TRADER:
                   /* if (player.isSkulled()) {
                        //DialogueManager.start(player, 3);
                    } else {
                        //DialogueManager.start(player, 22);
                        player.setDialogueOptions(new DialogueOptions() {
                            @Override
                            public void handleOption(Player player, int option) {
                                if (option == 1) {
                                    CombatFactory.skull(player, SkullType.WHITE_SKULL, 300);
                                } else if (option == 2) {
                                    CombatFactory.skull(player, SkullType.RED_SKULL, 300);
                                }
                                player.getPacketSender().sendInterfaceRemoval();
                            }
                        });
                    }*/
                    break;
                }
            }

            @Override
            public boolean inDistance() {
                return player.calculateDistance(npc) <= 1;
            }
		});
	}

	private static void attackNPC(Player player, Packet packet) {
		int index = packet.readShortA();
		if (index < 0 || index > World.getNpcs().capacity()) {
			return;
		}
		final NPC interact = World.getNpcs().get(index);

		if (interact == null || interact.getDefinition() == null) {
			return;
		}

        if (player.getRights() == PlayerRights.DEVELOPER) {
            player.getPacketSender().sendMessage("Attack NPC: " + Integer.toString(interact.getId()) + ". " + interact.getLocation().toString());
        }

		if (!interact.getDefinition().isAttackable()) {
			return;
		}

		if (interact == null || interact.getHitpoints() <= 0) {
			player.getMovementQueue().reset();
			return;
		}

		player.getCombat().attack(interact);
	}

	private static void mageNpc(Player player, Packet packet) {
		int npcIndex = packet.readLEShortA();
		int spellId = packet.readShortA();

		if (npcIndex < 0 || spellId < 0 || npcIndex > World.getNpcs().capacity()) {
			return;
		}

		final NPC interact = World.getNpcs().get(npcIndex);

		if (interact == null || interact.getDefinition() == null) {
			return;
		}

		if (player.getRights() == PlayerRights.DEVELOPER) {
			player.getPacketSender().sendMessage(
					"Magic on NPC: " + Integer.toString(interact.getId()) + ". " + interact.getLocation().toString());
		}

		if (!interact.getDefinition().isAttackable()) {
			return;
		}

		if (interact == null || interact.getHitpoints() <= 0) {
			player.getMovementQueue().reset();
			return;
		}

		CombatSpell spell = CombatSpells.getCombatSpell(spellId);

		if (spell == null) {
			player.getMovementQueue().reset();
			return;
		}

		player.setPositionToFace(interact.getLocation());
		player.getCombat().setCastSpell(spell);

		player.getCombat().attack(interact);
	}
}
