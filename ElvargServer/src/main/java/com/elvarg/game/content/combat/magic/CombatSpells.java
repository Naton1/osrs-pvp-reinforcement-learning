package com.elvarg.game.content.combat.magic;

import java.util.Arrays;
import java.util.Optional;

import com.elvarg.game.content.PrayerHandler;
import com.elvarg.game.content.combat.CombatFactory;
import com.elvarg.game.entity.impl.Mobile;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.Animation;
import com.elvarg.game.model.EffectTimer;
import com.elvarg.game.model.Graphic;
import com.elvarg.game.model.GraphicHeight;
import com.elvarg.game.model.Item;
import com.elvarg.game.model.MagicSpellbook;
import com.elvarg.game.model.Projectile;
import com.elvarg.game.model.Skill;
import com.elvarg.game.task.impl.CombatPoisonEffect.PoisonType;

public enum CombatSpells {
    WIND_STRIKE(new CombatNormalSpell() {
        @Override
        public Optional<Animation> castAnimation() {
            return Optional.of(new Animation(711));
        }

        @Override
        public Optional<Projectile> castProjectile(Mobile cast, Mobile castOn) {
            return Optional.of(new Projectile(cast, castOn, 91, 0, 20, 43, 31));
        }

        @Override
        public Optional<Graphic> endGraphic() {
            return Optional.of(new Graphic(92, GraphicHeight.HIGH));
        }

        @Override
        public int maximumHit() {
            return 2;
        }

        @Override
        public Optional<Graphic> startGraphic() {
            return Optional.of(new Graphic(90, GraphicHeight.HIGH));
        }

        @Override
        public int baseExperience() {
            return 5;
        }

        @Override
        public Optional<Item[]> equipmentRequired(Player player) {
            return Optional.empty();
        }

        @Override
        public Optional<Item[]> itemsRequired(Player player) {
            return Optional.of(new Item[] { new Item(556), new Item(558) });
        }

        @Override
        public int levelRequired() {
            return 1;
        }

        @Override
        public int spellId() {
            return 1152;
        }
    }),
    CONFUSE(new CombatEffectSpell() {
        @Override
        public Optional<Animation> castAnimation() {
            return Optional.of(new Animation(716));
        }

        @Override
        public Optional<Projectile> castProjectile(Mobile cast, Mobile castOn) {
            return Optional.of(new Projectile(cast, castOn, 103, 0, 20, 43, 31));
        }

        @Override
        public void spellEffect(Mobile cast, Mobile castOn) {
        	if (castOn.isPlayer()) {
				Player player = (Player) castOn;

				if (player.getSkillManager().getCurrentLevel(Skill.ATTACK) < player.getSkillManager().getMaxLevel(Skill.ATTACK)) {
					if (cast.isPlayer()) {
						((Player) cast).getPacketSender().sendMessage(
								"The spell has no effect because the player has already been weakened.");
					}
					return;
				}
				int decrease = (int) (0.05 * (player.getSkillManager().getCurrentLevel(Skill.ATTACK)));
				player.getSkillManager().setCurrentLevel(Skill.ATTACK, player.getSkillManager().getCurrentLevel(Skill.ATTACK) - decrease);
				player.getSkillManager().updateSkill(Skill.ATTACK);

				player.getPacketSender().sendMessage(
						"You feel slightly weakened.");
			} /*else if (castOn.isNpc()) {
				NPC npc = (NPC) castOn;

				if (npc.getDefenceWeakened()[0] || npc.getStrengthWeakened()[0]) {
					if (cast.isPlayer()) {
						((Player) cast).getPacketSender().sendMessage(
								"The spell has no effect because the NPC has already been weakened.");
					}
					return;
				}

				npc.getDefenceWeakened()[0] = true;
			}*/
        }

        @Override
        public Optional<Graphic> endGraphic() {
            return Optional.of(new Graphic(104, GraphicHeight.HIGH));
        }

        @Override
        public Optional<Graphic> startGraphic() {
            return Optional.of(new Graphic(102, GraphicHeight.HIGH));
        }

        @Override
        public int baseExperience() {
            return 13;
        }

        @Override
        public Optional<Item[]> itemsRequired(Player player) {
            return Optional.of(new Item[] { new Item(555, 3), new Item(557, 2), new Item(559) });
        }

        @Override
        public int levelRequired() {
            return 3;
        }

        @Override
        public int spellId() {
            return 1153;
        }
    }),
    WATER_STRIKE(new CombatNormalSpell() {
        @Override
        public Optional<Animation> castAnimation() {
            return Optional.of(new Animation(711));
        }

        @Override
        public Optional<Projectile> castProjectile(Mobile cast, Mobile castOn) {
            return Optional.of(new Projectile(cast, castOn, 94, 0, 20, 43, 31));
        }

        @Override
        public Optional<Graphic> endGraphic() {
            return Optional.of(new Graphic(95, GraphicHeight.HIGH));
        }

        @Override
        public int maximumHit() {
            return 4;
        }

        @Override
        public Optional<Graphic> startGraphic() {
            return Optional.of(new Graphic(93, GraphicHeight.HIGH));
        }

        @Override
        public int baseExperience() {
            return 7;
        }

        @Override
        public Optional<Item[]> equipmentRequired(Player player) {
            return Optional.empty();
        }

        @Override
        public Optional<Item[]> itemsRequired(Player player) {
            return Optional.of(new Item[] { new Item(555), new Item(556), new Item(558) });
        }

        @Override
        public int levelRequired() {
            return 5;
        }

        @Override
        public int spellId() {
            return 1154;
        }
    }),
    EARTH_STRIKE(new CombatNormalSpell() {
        @Override
        public Optional<Animation> castAnimation() {
            return Optional.of(new Animation(711));
        }

        @Override
        public Optional<Projectile> castProjectile(Mobile cast, Mobile castOn) {
            return Optional.of(new Projectile(cast, castOn, 97, 0, 20, 43, 31));
        }

        @Override
        public Optional<Graphic> endGraphic() {
            return Optional.of(new Graphic(98, GraphicHeight.HIGH));
        }

        @Override
        public int maximumHit() {
            return 6;
        }

        @Override
        public Optional<Graphic> startGraphic() {
            return Optional.of(new Graphic(96, GraphicHeight.HIGH));
        }

        @Override
        public int baseExperience() {
            return 9;
        }

        @Override
        public Optional<Item[]> equipmentRequired(Player player) {
            return Optional.empty();
        }

        @Override
        public Optional<Item[]> itemsRequired(Player player) {
            return Optional.of(new Item[] { new Item(556, 1), new Item(558, 1), new Item(557, 2) });
        }

        @Override
        public int levelRequired() {
            return 9;
        }

        @Override
        public int spellId() {
            return 1156;
        }
    }),
    WEAKEN(new CombatEffectSpell() {
        @Override
        public Optional<Animation> castAnimation() {
            return Optional.of(new Animation(716));
        }

        @Override
        public Optional<Projectile> castProjectile(Mobile cast, Mobile castOn) {
            return Optional.of(new Projectile(cast, castOn, 106, 0, 20, 43, 31));
        }

        @Override
        public void spellEffect(Mobile cast, Mobile castOn) {
        	if (castOn.isPlayer()) {
				Player player = (Player) castOn;

				if (player.getSkillManager().getCurrentLevel(Skill.STRENGTH) < player.getSkillManager().getMaxLevel(Skill.STRENGTH)) {
					if (cast.isPlayer()) {
						((Player) cast).getPacketSender().sendMessage(
								"The spell has no effect because the player has already been weakened.");
					}
					return;
				}

				int decrease = (int) (0.05 * (player.getSkillManager().getCurrentLevel(Skill.STRENGTH)));
				player.getSkillManager().setCurrentLevel(Skill.STRENGTH, player.getSkillManager().getCurrentLevel(Skill.STRENGTH) - decrease);
				player.getSkillManager().updateSkill(Skill.STRENGTH);
				player.getPacketSender().sendMessage(
						"You feel slightly weakened.");
			} /*else if (castOn.isNpc()) {
				NPC npc = (NPC) castOn;

				if (npc.getDefenceWeakened()[1] || npc.getStrengthWeakened()[1]) {
					if (cast.isPlayer()) {
						((Player) cast).getPacketSender().sendMessage(
								"The spell has no effect because the NPC has already been weakened.");
					}
					return;
				}

				npc.getDefenceWeakened()[1] = true;
			}*/
        }

        @Override
        public Optional<Graphic> endGraphic() {
            return Optional.of(new Graphic(107, GraphicHeight.HIGH));
        }

        @Override
        public Optional<Graphic> startGraphic() {
            return Optional.of(new Graphic(105, GraphicHeight.HIGH));
        }

        @Override
        public int baseExperience() {
            return 21;
        }

        @Override
        public Optional<Item[]> itemsRequired(Player player) {
            return Optional.of(new Item[] { new Item(555, 3), new Item(557, 2), new Item(559, 1) });
        }

        @Override
        public int levelRequired() {
            return 11;
        }

        @Override
        public int spellId() {
            return 1157;
        }

		@Override
		public MagicSpellbook getSpellbook() {
			return null;
		}
    }),
    FIRE_STRIKE(new CombatNormalSpell() {
        @Override
        public Optional<Animation> castAnimation() {
            return Optional.of(new Animation(711));
        }

        @Override
        public Optional<Projectile> castProjectile(Mobile cast, Mobile castOn) {
            return Optional.of(new Projectile(cast, castOn, 100, 0, 20, 43, 31));
        }

        @Override
        public Optional<Graphic> endGraphic() {
            return Optional.of(new Graphic(101, GraphicHeight.HIGH));
        }

        @Override
        public int maximumHit() {
            return 8;
        }

        @Override
        public Optional<Graphic> startGraphic() {
            return Optional.of(new Graphic(99, GraphicHeight.HIGH));
        }

        @Override
        public int baseExperience() {
            return 11;
        }

        @Override
        public Optional<Item[]> equipmentRequired(Player player) {
            return Optional.empty();
        }

        @Override
        public Optional<Item[]> itemsRequired(Player player) {
            return Optional.of(new Item[] { new Item(556, 1), new Item(558, 1), new Item(554, 3) });
        }

        @Override
        public int levelRequired() {
            return 13;
        }

        @Override
        public int spellId() {
            return 1158;
        }
    }),
    WIND_BOLT(new CombatNormalSpell() {
        @Override
        public Optional<Animation> castAnimation() {
            return Optional.of(new Animation(711));
        }

        @Override
        public Optional<Projectile> castProjectile(Mobile cast, Mobile castOn) {
            return Optional.of(new Projectile(cast, castOn, 118, 0, 20, 43, 31));
        }

        @Override
        public Optional<Graphic> endGraphic() {
            return Optional.of(new Graphic(119, GraphicHeight.HIGH));
        }

        @Override
        public int maximumHit() {
            return 9;
        }

        @Override
        public Optional<Graphic> startGraphic() {
            return Optional.of(new Graphic(117, GraphicHeight.HIGH));
        }

        @Override
        public int baseExperience() {
            return 13;
        }

        @Override
        public Optional<Item[]> equipmentRequired(Player player) {
            return Optional.empty();
        }

        @Override
        public Optional<Item[]> itemsRequired(Player player) {
            return Optional.of(new Item[] { new Item(556, 2), new Item(562, 1) });
        }

        @Override
        public int levelRequired() {
            return 17;
        }

        @Override
        public int spellId() {
            return 1160;
        }
    }),
    CURSE(new CombatEffectSpell() {
        @Override
        public Optional<Animation> castAnimation() {
            return Optional.of(new Animation(710));
        }

        @Override
        public Optional<Projectile> castProjectile(Mobile cast, Mobile castOn) {
            return Optional.of(new Projectile(cast, castOn, 109, 0, 20, 43, 31));
        }

        @Override
        public void spellEffect(Mobile cast, Mobile castOn) {
        	if (castOn.isPlayer()) {
				Player player = (Player) castOn;

				if (player.getSkillManager().getCurrentLevel(Skill.DEFENCE) < player.getSkillManager().getMaxLevel(Skill.DEFENCE)) {
					if (cast.isPlayer()) {
						((Player) cast).getPacketSender().sendMessage(
								"The spell has no effect because the player has already been weakened.");
					}
					return;
				}

				int decrease = (int) (0.05 * (player.getSkillManager().getCurrentLevel(Skill.DEFENCE)));
				player.getSkillManager().setCurrentLevel(Skill.DEFENCE, player.getSkillManager().getCurrentLevel(Skill.DEFENCE) - decrease);
				player.getSkillManager().updateSkill(Skill.DEFENCE);

				player.getPacketSender().sendMessage(
						"You feel slightly weakened.");
			}/* else if (castOn.isNpc()) {
				NPC npc = (NPC) castOn;

				if (npc.getDefenceWeakened()[2] || npc.getStrengthWeakened()[2]) {
					if (cast.isPlayer()) {
						((Player) cast).getPacketSender().sendMessage(
								"The spell has no effect because the NPC has already been weakened.");
					}
					return;
				}

				npc.getDefenceWeakened()[2] = true;
			}*/
        }

        @Override
        public Optional<Graphic> endGraphic() {
            return Optional.of(new Graphic(110, GraphicHeight.HIGH));
        }

        @Override
        public Optional<Graphic> startGraphic() {
            return Optional.of(new Graphic(108, GraphicHeight.HIGH));
        }

        @Override
        public int baseExperience() {
            return 29;
        }

        @Override
        public Optional<Item[]> itemsRequired(Player player) {
            return Optional.of(new Item[] { new Item(555, 2), new Item(557, 3), new Item(559, 1) });
        }

        @Override
        public int levelRequired() {
            return 19;
        }

        @Override
        public int spellId() {
            return 1161;
        }
    }),
    BIND(new CombatEffectSpell() {
        @Override
        public Optional<Animation> castAnimation() {
            return Optional.of(new Animation(710));
        }

        @Override
        public Optional<Projectile> castProjectile(Mobile cast, Mobile castOn) {
            return Optional.of(new Projectile(cast, castOn, 178, 0, 20, 43, 31));
        }

        @Override
        public void spellEffect(Mobile cast, Mobile castOn) {
			CombatFactory.freeze(castOn, 5);
        }

        @Override
        public Optional<Graphic> endGraphic() {
            return Optional.of(new Graphic(181, GraphicHeight.HIGH));
        }

        @Override
        public Optional<Graphic> startGraphic() {
            return Optional.of(new Graphic(177, GraphicHeight.HIGH));
        }

        @Override
        public int baseExperience() {
            return 30;
        }

        @Override
        public Optional<Item[]> itemsRequired(Player player) {
            return Optional.of(new Item[] { new Item(555, 3), new Item(557, 3), new Item(561, 2) });
        }

        @Override
        public int levelRequired() {
            return 20;
        }

        @Override
        public int spellId() {
            return 1572;
        }
    }),
    WATER_BOLT(new CombatNormalSpell() {
        @Override
        public Optional<Animation> castAnimation() {
            return Optional.of(new Animation(711));
        }

        @Override
        public Optional<Projectile> castProjectile(Mobile cast, Mobile castOn) {
            return Optional.of(new Projectile(cast, castOn, 121, 0, 20, 43, 31));
        }

        @Override
        public Optional<Graphic> endGraphic() {
            return Optional.of(new Graphic(122, GraphicHeight.HIGH));
        }

        @Override
        public int maximumHit() {
            return 10;
        }

        @Override
        public Optional<Graphic> startGraphic() {
            return Optional.of(new Graphic(120, GraphicHeight.HIGH));
        }

        @Override
        public int baseExperience() {
            return 16;
        }

        @Override
        public Optional<Item[]> equipmentRequired(Player player) {
            return Optional.empty();
        }

        @Override
        public Optional<Item[]> itemsRequired(Player player) {
            return Optional.of(new Item[] { new Item(556, 2), new Item(562, 1), new Item(555, 2) });
        }

        @Override
        public int levelRequired() {
            return 23;
        }

        @Override
        public int spellId() {
            return 1163;
        }
    }),
    EARTH_BOLT(new CombatNormalSpell() {
        @Override
        public Optional<Animation> castAnimation() {
            return Optional.of(new Animation(711));
        }

        @Override
        public Optional<Projectile> castProjectile(Mobile cast, Mobile castOn) {
            return Optional.of(new Projectile(cast, castOn, 124, 0, 20, 43, 31));
        }

        @Override
        public Optional<Graphic> endGraphic() {
            return Optional.of(new Graphic(125, GraphicHeight.HIGH));
        }

        @Override
        public int maximumHit() {
            return 11;
        }

        @Override
        public Optional<Graphic> startGraphic() {
            return Optional.of(new Graphic(123, GraphicHeight.HIGH));
        }

        @Override
        public int baseExperience() {
            return 19;
        }

        @Override
        public Optional<Item[]> equipmentRequired(Player player) {
            return Optional.empty();
        }

        @Override
        public Optional<Item[]> itemsRequired(Player player) {
            return Optional.of(new Item[] { new Item(556, 2), new Item(562, 1), new Item(557, 3) });
        }

        @Override
        public int levelRequired() {
            return 29;
        }

        @Override
        public int spellId() {
            return 1166;
        }
    }),
    FIRE_BOLT(new CombatNormalSpell() {
        @Override
        public Optional<Animation> castAnimation() {
            return Optional.of(new Animation(711));
        }

        @Override
        public Optional<Projectile> castProjectile(Mobile cast, Mobile castOn) {
            return Optional.of(new Projectile(cast, castOn, 127, 0, 20, 43, 31));
        }

        @Override
        public Optional<Graphic> endGraphic() {
            return Optional.of(new Graphic(128, GraphicHeight.HIGH));
        }

        @Override
        public int maximumHit() {
            return 12;
        }

        @Override
        public Optional<Graphic> startGraphic() {
            return Optional.of(new Graphic(126, GraphicHeight.HIGH));
        }

        @Override
        public int baseExperience() {
            return 22;
        }

        @Override
        public Optional<Item[]> equipmentRequired(Player player) {
            return Optional.empty();
        }

        @Override
        public Optional<Item[]> itemsRequired(Player player) {
            return Optional.of(new Item[] { new Item(556, 3), new Item(562, 1), new Item(554, 4) });
        }

        @Override
        public int levelRequired() {
            return 35;
        }

        @Override
        public int spellId() {
            return 1169;
        }
    }),
    CRUMBLE_UNDEAD(new CombatNormalSpell() {
        @Override
        public Optional<Animation> castAnimation() {
            return Optional.of(new Animation(724));
        }

        @Override
        public Optional<Projectile> castProjectile(Mobile cast, Mobile castOn) {
            return Optional.of(new Projectile(cast, castOn, 146, 0, 20, 43, 31));
        }

        @Override
        public Optional<Graphic> endGraphic() {
            return Optional.of(new Graphic(147));
        }

        @Override
        public int maximumHit() {
            return 15;
        }

        @Override
        public Optional<Graphic> startGraphic() {
            return Optional.of(new Graphic(145, 6553600));
        }

        @Override
        public int baseExperience() {
            return 24;
        }

        @Override
        public Optional<Item[]> equipmentRequired(Player player) {
            return Optional.empty();
        }

        @Override
        public Optional<Item[]> itemsRequired(Player player) {
            return Optional.of(new Item[] { new Item(556, 2), new Item(562, 1), new Item(557, 2) });
        }

        @Override
        public int levelRequired() {
            return 39;
        }

        @Override
        public int spellId() {
            return 1171;
        }
    }),
    WIND_BLAST(new CombatNormalSpell() {
        @Override
        public Optional<Animation> castAnimation() {
            return Optional.of(new Animation(711));
        }

        @Override
        public Optional<Projectile> castProjectile(Mobile cast, Mobile castOn) {
            return Optional.of(new Projectile(cast, castOn, 133, 0, 20, 43, 31));
        }

        @Override
        public Optional<Graphic> endGraphic() {
            return Optional.of(new Graphic(134, GraphicHeight.HIGH));
        }

        @Override
        public int maximumHit() {
            return 13;
        }

        @Override
        public Optional<Graphic> startGraphic() {
            return Optional.of(new Graphic(132, GraphicHeight.HIGH));
        }

        @Override
        public int baseExperience() {
            return 25;
        }

        @Override
        public Optional<Item[]> equipmentRequired(Player player) {
            return Optional.empty();
        }

        @Override
        public Optional<Item[]> itemsRequired(Player player) {
            return Optional.of(new Item[] { new Item(556, 3), new Item(560, 1) });
        }

        @Override
        public int levelRequired() {
            return 41;
        }

        @Override
        public int spellId() {
            return 1172;
        }
    }),
    WATER_BLAST(new CombatNormalSpell() {
        @Override
        public Optional<Animation> castAnimation() {
            return Optional.of(new Animation(711));
        }

        @Override
        public Optional<Projectile> castProjectile(Mobile cast, Mobile castOn) {
            return Optional.of(new Projectile(cast, castOn, 136, 0, 20, 43, 31));
        }

        @Override
        public Optional<Graphic> endGraphic() {
            return Optional.of(new Graphic(137, GraphicHeight.HIGH));
        }

        @Override
        public int maximumHit() {
            return 14;
        }

        @Override
        public Optional<Graphic> startGraphic() {
            return Optional.of(new Graphic(135, GraphicHeight.HIGH));
        }

        @Override
        public int baseExperience() {
            return 28;
        }

        @Override
        public Optional<Item[]> equipmentRequired(Player player) {
            return Optional.empty();
        }

        @Override
        public Optional<Item[]> itemsRequired(Player player) {
            return Optional.of(new Item[] { new Item(555, 3), new Item(556, 3), new Item(560, 1) });
        }

        @Override
        public int levelRequired() {
            return 47;
        }

        @Override
        public int spellId() {
            return 1175;
        }
    }),
    IBAN_BLAST(new CombatNormalSpell() {
        @Override
        public Optional<Animation> castAnimation() {
            return Optional.of(new Animation(708));
        }

        @Override
        public Optional<Projectile> castProjectile(Mobile cast, Mobile castOn) {
            return Optional.of(new Projectile(cast, castOn, 88, 0, 20, 43, 31));
        }

        @Override
        public Optional<Graphic> endGraphic() {
            return Optional.of(new Graphic(89));
        }

        @Override
        public int maximumHit() {
            return 25;
        }

        @Override
        public Optional<Graphic> startGraphic() {
            return Optional.of(new Graphic(87, 6553600));
        }

        @Override
        public int baseExperience() {
            return 30;
        }

        @Override
        public Optional<Item[]> equipmentRequired(Player player) {
            return Optional.of(new Item[] { new Item(1409) });
        }

        @Override
        public Optional<Item[]> itemsRequired(Player player) {
            return Optional.of(new Item[] { new Item(560, 1), new Item(554, 5) });
        }

        @Override
        public int levelRequired() {
            return 50;
        }

        @Override
        public int spellId() {
            return 1539;
        }
    }),
    SNARE(new CombatEffectSpell() {
        @Override
        public Optional<Animation> castAnimation() {
            return Optional.of(new Animation(710));
        }

        @Override
        public Optional<Projectile> castProjectile(Mobile cast, Mobile castOn) {
            return Optional.of(new Projectile(cast, castOn, 178, 0, 20, 43, 31));
        }

        @Override
        public void spellEffect(Mobile cast, Mobile castOn) {
			CombatFactory.freeze(castOn, 10);
        }

        @Override
        public Optional<Graphic> endGraphic() {
            return Optional.of(new Graphic(180, GraphicHeight.HIGH));
        }

        @Override
        public Optional<Graphic> startGraphic() {
            return Optional.of(new Graphic(177, GraphicHeight.HIGH));
        }

        @Override
        public int baseExperience() {
            return 60;
        }

        @Override
        public Optional<Item[]> itemsRequired(Player player) {
            return Optional.of(new Item[] { new Item(555, 3), new Item(557, 4), new Item(561, 3) });
        }

        @Override
        public int levelRequired() {
            return 50;
        }

        @Override
        public int spellId() {
            return 1582;
        }
    }),
    MAGIC_DART(new CombatNormalSpell() {
        @Override
        public Optional<Animation> castAnimation() {
            return Optional.of(new Animation(1576));
        }

        @Override
        public Optional<Projectile> castProjectile(Mobile cast, Mobile castOn) {
            return Optional.of(new Projectile(cast, castOn, 328, 0, 20, 43, 31));
        }

        @Override
        public Optional<Graphic> endGraphic() {
            return Optional.of(new Graphic(329));
        }

        @Override
        public int maximumHit() {
            return 19;
        }

        @Override
        public Optional<Graphic> startGraphic() {
            return Optional.of(new Graphic(327, 6553600));
        }

        @Override
        public int baseExperience() {
            return 30;
        }

        @Override
        public Optional<Item[]> equipmentRequired(Player player) {
            return Optional.of(new Item[] { new Item(4170) });
        }

        @Override
        public Optional<Item[]> itemsRequired(Player player) {
            return Optional.of(new Item[] { new Item(558, 4), new Item(560, 1) });
        }

        @Override
        public int levelRequired() {
            return 50;
        }

        @Override
        public int spellId() {
            return 12037;
        }
    }),
    EARTH_BLAST(new CombatNormalSpell() {
        @Override
        public Optional<Animation> castAnimation() {
            return Optional.of(new Animation(711));
        }

        @Override
        public Optional<Projectile> castProjectile(Mobile cast, Mobile castOn) {
            return Optional.of(new Projectile(cast, castOn, 139, 0, 20, 43, 31));
        }

        @Override
        public Optional<Graphic> endGraphic() {
            return Optional.of(new Graphic(140, GraphicHeight.HIGH));
        }

        @Override
        public int maximumHit() {
            return 15;
        }

        @Override
        public Optional<Graphic> startGraphic() {
            return Optional.of(new Graphic(138, GraphicHeight.HIGH));
        }

        @Override
        public int baseExperience() {
            return 31;
        }

        @Override
        public Optional<Item[]> equipmentRequired(Player player) {
            return Optional.empty();
        }

        @Override
        public Optional<Item[]> itemsRequired(Player player) {
            return Optional.of(new Item[] { new Item(556, 3), new Item(560, 1), new Item(557, 4) });
        }

        @Override
        public int levelRequired() {
            return 53;
        }

        @Override
        public int spellId() {
            return 1177;
        }
    }),
    FIRE_BLAST(new CombatNormalSpell() {
        @Override
        public Optional<Animation> castAnimation() {
            return Optional.of(new Animation(711));
        }

        @Override
        public Optional<Projectile> castProjectile(Mobile cast, Mobile castOn) {
            return Optional.of(new Projectile(cast, castOn, 130, 0, 20, 43, 31));
        }

        @Override
        public Optional<Graphic> endGraphic() {
            return Optional.of(new Graphic(131, GraphicHeight.HIGH));
        }

        @Override
        public int maximumHit() {
            return 16;
        }

        @Override
        public Optional<Graphic> startGraphic() {
            return Optional.of(new Graphic(129, GraphicHeight.HIGH));
        }

        @Override
        public int baseExperience() {
            return 34;
        }

        @Override
        public Optional<Item[]> equipmentRequired(Player player) {
            return Optional.empty();
        }

        @Override
        public Optional<Item[]> itemsRequired(Player player) {
            return Optional.of(new Item[] { new Item(556, 4), new Item(560, 1), new Item(554, 5) });
        }

        @Override
        public int levelRequired() {
            return 59;
        }

        @Override
        public int spellId() {
            return 1181;
        }
    }),
    SARADOMIN_STRIKE(new CombatNormalSpell() {
        @Override
        public Optional<Animation> castAnimation() {
            return Optional.of(new Animation(811));
        }

        @Override
        public Optional<Projectile> castProjectile(Mobile cast, Mobile castOn) {
            return Optional.empty();
        }

        @Override
        public Optional<Graphic> endGraphic() {
            return Optional.of(new Graphic(76));
        }

        @Override
        public int maximumHit() {
            return 20;
        }

        @Override
        public Optional<Graphic> startGraphic() {
            return Optional.empty();
        }

        @Override
        public int baseExperience() {
            return 35;
        }

        @Override
        public Optional<Item[]> equipmentRequired(Player player) {
            return Optional.of(new Item[] { new Item(2415) });
        }

        @Override
        public Optional<Item[]> itemsRequired(Player player) {
            return Optional.of(new Item[] { new Item(556, 4), new Item(565, 2), new Item(554, 2) });
        }

        @Override
        public int levelRequired() {
            return 60;
        }

        @Override
        public int spellId() {
            return 1190;
        }
    }),
    CLAWS_OF_GUTHIX(new CombatNormalSpell() {
        @Override
        public Optional<Animation> castAnimation() {
            return Optional.of(new Animation(811));
        }

        @Override
        public Optional<Projectile> castProjectile(Mobile cast, Mobile castOn) {
            return Optional.empty();
        }

        @Override
        public Optional<Graphic> endGraphic() {
            return Optional.of(new Graphic(77));
        }

        @Override
        public int maximumHit() {
            return 20;
        }

        @Override
        public Optional<Graphic> startGraphic() {
            return Optional.empty();
        }

        @Override
        public int baseExperience() {
            return 35;
        }

        @Override
        public Optional<Item[]> equipmentRequired(Player player) {
            return Optional.of(new Item[] { new Item(2416) });
        }

        @Override
        public Optional<Item[]> itemsRequired(Player player) {
            return Optional.of(new Item[] { new Item(556, 4), new Item(565, 2), new Item(554, 2) });
        }

        @Override
        public int levelRequired() {
            return 60;
        }

        @Override
        public int spellId() {
            return 1191;
        }
    }),
    FLAMES_OF_ZAMORAK(new CombatNormalSpell() {
        @Override
        public Optional<Animation> castAnimation() {
            return Optional.of(new Animation(811));
        }

        @Override
        public Optional<Projectile> castProjectile(Mobile cast, Mobile castOn) {
            return Optional.empty();
        }

        @Override
        public Optional<Graphic> endGraphic() {
            return Optional.of(new Graphic(78));
        }

        @Override
        public int maximumHit() {
            return 20;
        }

        @Override
        public Optional<Graphic> startGraphic() {
            return Optional.empty();
        }

        @Override
        public int baseExperience() {
            return 35;
        }

        @Override
        public Optional<Item[]> equipmentRequired(Player player) {
            return Optional.of(new Item[] { new Item(2417) });
        }

        @Override
        public Optional<Item[]> itemsRequired(Player player) {
            return Optional.of(new Item[] { new Item(556, 4), new Item(565, 2), new Item(554, 2) });
        }

        @Override
        public int levelRequired() {
            return 60;
        }

        @Override
        public int spellId() {
            return 1192;
        }
    }),
    WIND_WAVE(new CombatNormalSpell() {
        @Override
        public Optional<Animation> castAnimation() {
            return Optional.of(new Animation(727));
        }

        @Override
        public Optional<Projectile> castProjectile(Mobile cast, Mobile castOn) {
            return Optional.of(new Projectile(cast, castOn, 159, 0, 20, 43, 31));
        }

        @Override
        public Optional<Graphic> endGraphic() {
            return Optional.of(new Graphic(160, GraphicHeight.HIGH));
        }

        @Override
        public int maximumHit() {
            return 17;
        }

        @Override
        public Optional<Graphic> startGraphic() {
            return Optional.of(new Graphic(158, GraphicHeight.MIDDLE));
        }

        @Override
        public int baseExperience() {
            return 36;
        }

        @Override
        public Optional<Item[]> equipmentRequired(Player player) {
            return Optional.empty();
        }

        @Override
        public Optional<Item[]> itemsRequired(Player player) {
            return Optional.of(new Item[] { new Item(556, 5), new Item(565, 1) });
        }

        @Override
        public int levelRequired() {
            return 62;
        }

        @Override
        public int spellId() {
            return 1183;
        }
    }),
    WATER_WAVE(new CombatNormalSpell() {
        @Override
        public Optional<Animation> castAnimation() {
            return Optional.of(new Animation(727));
        }

        @Override
        public Optional<Projectile> castProjectile(Mobile cast, Mobile castOn) {
            return Optional.of(new Projectile(cast, castOn, 162, 0, 20, 43, 31));
        }

        @Override
        public Optional<Graphic> endGraphic() {
            return Optional.of(new Graphic(163, GraphicHeight.HIGH));
        }

        @Override
        public int maximumHit() {
            return 18;
        }

        @Override
        public Optional<Graphic> startGraphic() {
            return Optional.of(new Graphic(161, GraphicHeight.MIDDLE));
        }

        @Override
        public int baseExperience() {
            return 37;
        }

        @Override
        public Optional<Item[]> equipmentRequired(Player player) {
            return Optional.empty();
        }

        @Override
        public Optional<Item[]> itemsRequired(Player player) {
            return Optional.of(new Item[] { new Item(556, 5), new Item(565, 1), new Item(555, 7) });
        }

        @Override
        public int levelRequired() {
            return 65;
        }

        @Override
        public int spellId() {
            return 1185;
        }
    }),
    VULNERABILITY(new CombatEffectSpell() {
        @Override
        public Optional<Animation> castAnimation() {
            return Optional.of(new Animation(729));
        }

        @Override
        public Optional<Projectile> castProjectile(Mobile cast, Mobile castOn) {
            return Optional.of(new Projectile(cast, castOn, 168, 0, 20, 43, 31));
        }

        @Override
        public void spellEffect(Mobile cast, Mobile castOn) {
            if (castOn.isPlayer()) {
				Player player = (Player) castOn;

				if (player.getSkillManager().getCurrentLevel(Skill.DEFENCE) < player.getSkillManager().getMaxLevel(Skill.DEFENCE)) {
					if (cast.isPlayer()) {
						((Player) cast).getPacketSender().sendMessage(
								"The spell has no effect because the player is already weakened.");
					}
					return;
				}

				int decrease = (int) (0.10 * (player.getSkillManager().getCurrentLevel(Skill.DEFENCE)));
				player.getSkillManager().setCurrentLevel(Skill.DEFENCE, player.getSkillManager().getCurrentLevel(Skill.DEFENCE) - decrease);
				player.getSkillManager().updateSkill(Skill.DEFENCE);
				player.getPacketSender().sendMessage(
						"You feel slightly weakened.");
			}/* else if (castOn.isNpc()) {
				NPC npc = (NPC) castOn;

				if (npc.getDefenceWeakened()[2] || npc.getStrengthWeakened()[2]) {
					if (cast.isPlayer()) {
						((Player) cast).getPacketSender().sendMessage(
								"The spell has no effect because the NPC is already weakened.");
					}
					return;
				}

				npc.getStrengthWeakened()[2] = true;
			}*/
        }

        @Override
        public Optional<Graphic> endGraphic() {
            return Optional.of(new Graphic(169));
        }

        @Override
        public Optional<Graphic> startGraphic() {
            return Optional.of(new Graphic(167, 6553600));
        }

        @Override
        public int baseExperience() {
            return 76;
        }

        @Override
        public Optional<Item[]> itemsRequired(Player player) {
            return Optional.of(new Item[] { new Item(557, 5), new Item(555, 5), new Item(566, 1) });
        }

        @Override
        public int levelRequired() {
            return 66;
        }

        @Override
        public int spellId() {
            return 1542;
        }
    }),
    EARTH_WAVE(new CombatNormalSpell() {
        @Override
        public Optional<Animation> castAnimation() {
            return Optional.of(new Animation(727));
        }

        @Override
        public Optional<Projectile> castProjectile(Mobile cast, Mobile castOn) {
            return Optional.of(new Projectile(cast, castOn, 165, 0, 20, 43, 31));
        }

        @Override
        public Optional<Graphic> endGraphic() {
            return Optional.of(new Graphic(166, GraphicHeight.HIGH));
        }

        @Override
        public int maximumHit() {
            return 19;
        }

        @Override
        public Optional<Graphic> startGraphic() {
            return Optional.of(new Graphic(164, GraphicHeight.MIDDLE));
        }

        @Override
        public int baseExperience() {
            return 40;
        }

        @Override
        public Optional<Item[]> equipmentRequired(Player player) {
            return Optional.empty();
        }

        @Override
        public Optional<Item[]> itemsRequired(Player player) {
            return Optional.of(new Item[] { new Item(556, 5), new Item(565, 1), new Item(557, 7) });
        }

        @Override
        public int levelRequired() {
            return 70;
        }

        @Override
        public int spellId() {
            return 1188;
        }
    }),
    ENFEEBLE(new CombatEffectSpell() {
        @Override
        public Optional<Animation> castAnimation() {
            return Optional.of(new Animation(729));
        }

        @Override
        public Optional<Projectile> castProjectile(Mobile cast, Mobile castOn) {
            return Optional.of(new Projectile(cast, castOn, 171, 0, 20, 43, 31));
        }

        @Override
        public void spellEffect(Mobile cast, Mobile castOn) {
        	if (castOn.isPlayer()) {
				Player player = (Player) castOn;

				if (player.getSkillManager().getCurrentLevel(Skill.STRENGTH) < player.getSkillManager().getMaxLevel(Skill.STRENGTH)) {
					if (cast.isPlayer()) {
						((Player) cast).getPacketSender().sendMessage(
								"The spell has no effect because the player is already weakened.");
					}
					return;
				}

				int decrease = (int) (0.10 * (player.getSkillManager().getCurrentLevel(Skill.STRENGTH)));
				player.getSkillManager().setCurrentLevel(Skill.STRENGTH, player.getSkillManager().getCurrentLevel(Skill.STRENGTH) - decrease);
				player.getSkillManager().updateSkill(Skill.STRENGTH);

				player.getPacketSender().sendMessage(
						"You feel slightly weakened.");
			} /*else if (castOn.isNpc()) {
				NPC npc = (NPC) castOn;

				if (npc.getDefenceWeakened()[1] || npc.getStrengthWeakened()[1]) {
					if (cast.isPlayer()) {
						((Player) cast).getPacketSender().sendMessage(
								"The spell has no effect because the NPC is already weakened.");
					}
					return;
				}

				npc.getStrengthWeakened()[1] = true;
			}*/
        }

        @Override
        public Optional<Graphic> endGraphic() {
            return Optional.of(new Graphic(172));
        }

        @Override
        public Optional<Graphic> startGraphic() {
            return Optional.of(new Graphic(170, 6553600));
        }

        @Override
        public int baseExperience() {
            return 83;
        }

        @Override
        public Optional<Item[]> itemsRequired(Player player) {
            return Optional.of(new Item[] { new Item(557, 8), new Item(555, 8), new Item(566, 1) });
        }

        @Override
        public int levelRequired() {
            return 73;
        }

        @Override
        public int spellId() {
            return 1543;
        }
    }),
    FIRE_WAVE(new CombatNormalSpell() {
        @Override
        public Optional<Animation> castAnimation() {
            return Optional.of(new Animation(727));
        }

        @Override
        public Optional<Projectile> castProjectile(Mobile cast, Mobile castOn) {
            return Optional.of(new Projectile(cast, castOn, 156, 0, 20, 43, 31));
        }

        @Override
        public Optional<Graphic> endGraphic() {
            return Optional.of(new Graphic(157, GraphicHeight.HIGH));
        }

        @Override
        public int maximumHit() {
            return 20;
        }

        @Override
        public Optional<Graphic> startGraphic() {
            return Optional.of(new Graphic(155, GraphicHeight.MIDDLE));
        }

        @Override
        public int baseExperience() {
            return 42;
        }

        @Override
        public Optional<Item[]> equipmentRequired(Player player) {
            return Optional.empty();
        }

        @Override
        public Optional<Item[]> itemsRequired(Player player) {
            return Optional.of(new Item[] { new Item(556, 5), new Item(565, 1), new Item(554, 7) });
        }

        @Override
        public int levelRequired() {
            return 75;
        }

        @Override
        public int spellId() {
            return 1189;
        }
    }),
    ENTANGLE(new CombatEffectSpell() {
        @Override
        public Optional<Animation> castAnimation() {
            return Optional.of(new Animation(710));
        }

        @Override
        public Optional<Projectile> castProjectile(Mobile cast, Mobile castOn) {
            return Optional.of(new Projectile(cast, castOn, 178, 0, 20, 43, 31));
        }

        @Override
        public void spellEffect(Mobile cast, Mobile castOn) {
        	CombatFactory.freeze(castOn, 15);
        }

        @Override
        public Optional<Graphic> endGraphic() {
            return Optional.of(new Graphic(179, GraphicHeight.HIGH));
        }

        @Override
        public Optional<Graphic> startGraphic() {
            return Optional.of(new Graphic(177, GraphicHeight.HIGH));
        }

        @Override
        public int baseExperience() {
            return 91;
        }

        @Override
        public Optional<Item[]> itemsRequired(Player player) {
            return Optional.of(new Item[] { new Item(555, 5), new Item(557, 5), new Item(561, 4) });
        }

        @Override
        public int levelRequired() {
            return 79;
        }

        @Override
        public int spellId() {
            return 1592;
        }
    }),
    STUN(new CombatEffectSpell() {
        @Override
        public Optional<Animation> castAnimation() {
            return Optional.of(new Animation(729));
        }

        @Override
        public Optional<Projectile> castProjectile(Mobile cast, Mobile castOn) {
            return Optional.of(new Projectile(cast, castOn, 174, 0, 20, 43, 31));
        }

        @Override
        public void spellEffect(Mobile cast, Mobile castOn) {
			if (castOn.isPlayer()) {
				Player player = (Player) castOn;

				if (player.getSkillManager().getCurrentLevel(Skill.ATTACK) < player.getSkillManager().getMaxLevel(Skill.ATTACK)) {
					if (cast.isPlayer()) {
						((Player) cast).getPacketSender().sendMessage(
								"The spell has no effect because the player is already weakened.");
					}
					return;
				}

				int decrease = (int) (0.10 * (player.getSkillManager().getCurrentLevel(Skill.ATTACK)));
				player.getSkillManager().setCurrentLevel(Skill.ATTACK, player.getSkillManager().getCurrentLevel(Skill.ATTACK) - decrease);
				player.getSkillManager().updateSkill(Skill.ATTACK);
				player.getPacketSender().sendMessage(
						"You feel slightly weakened.");
			}/* else if (castOn.isNpc()) {
				NPC npc = (NPC) castOn;

				if (npc.getDefenceWeakened()[0] || npc.getStrengthWeakened()[0]) {
					if (cast.isPlayer()) {
						((Player) cast).getPacketSender().sendMessage(
								"The spell has no effect because the NPC is already weakened.");
					}
					return;
				}

				npc.getStrengthWeakened()[0] = true;
			}*/
        }

        @Override
        public Optional<Graphic> endGraphic() {
            return Optional.of(new Graphic(107));
        }

        @Override
        public Optional<Graphic> startGraphic() {
            return Optional.of(new Graphic(173, 6553600));
        }

        @Override
        public int baseExperience() {
            return 90;
        }

        @Override
        public Optional<Item[]> itemsRequired(Player player) {
            return Optional.of(new Item[] { new Item(557, 12), new Item(555, 12), new Item(556, 1) });
        }

        @Override
        public int levelRequired() {
            return 80;
        }

        @Override
        public int spellId() {
            return 1562;
        }
    }),
    TELEBLOCK(new CombatEffectSpell() {
        @Override
        public Optional<Animation> castAnimation() {
            return Optional.of(new Animation(1819));
        }

        @Override
        public Optional<Projectile> castProjectile(Mobile cast, Mobile castOn) {
            return Optional.of(new Projectile(cast, castOn, 344, 0, 20, 43, 31));
        }

        @Override
        public void spellEffect(Mobile cast, Mobile castOn) {
        	if (castOn.isPlayer()) {
				Player player = (Player) castOn;

				if (!player.getCombat().getTeleBlockTimer().finished()) {
					if (cast.isPlayer()) {
						((Player) cast).getPacketSender().sendMessage(
								"The spell has no effect because the player is already teleblocked.");
					}
					return;
				}

				final int seconds = player.getPrayerActive()[PrayerHandler.PROTECT_FROM_MAGIC] ? 300 : 600;
				
				player.getCombat().getTeleBlockTimer().start(seconds);
				player.getPacketSender().sendEffectTimer(seconds, EffectTimer.TELE_BLOCK)
				.sendMessage("You have just been teleblocked!");
				
			} else if (castOn.isNpc()) {
				if (cast.isPlayer()) {
					((Player) cast).getPacketSender().sendMessage(
							"Your spell has no effect on this target.");
				}
			}
        }

        @Override
        public Optional<Graphic> endGraphic() {
            return Optional.of(new Graphic(345));
        }

        @Override
        public Optional<Graphic> startGraphic() {
            return Optional.empty();
        }

        @Override
        public int baseExperience() {
            return 65;
        }

        @Override
        public Optional<Item[]> itemsRequired(Player player) {
            return Optional.of(new Item[] { new Item(563, 1), new Item(562, 1), new Item(560, 1) });
        }

        @Override
        public int levelRequired() {
            return 85;
        }

        @Override
        public int spellId() {
            return 12445;
        }
    }),
    SMOKE_RUSH(new CombatAncientSpell() {
    	
        @Override
        public void spellEffect(Mobile cast, Mobile castOn, int damage) {
            CombatFactory.poisonEntity(castOn, PoisonType.MILD);
        }

        @Override
        public int spellRadius() {
            return 0;
        }

        @Override
        public Optional<Animation> castAnimation() {
            return Optional.of(new Animation(1978));
        }

        @Override
        public Optional<Projectile> castProjectile(Mobile cast, Mobile castOn) {
            return Optional.of(new Projectile(cast, castOn, 384, 0, 20, 43, 31));
        }

        @Override
        public Optional<Graphic> endGraphic() {
            return Optional.of(new Graphic(385));
        }

        @Override
        public int maximumHit() {
            return 13;
        }

        @Override
        public Optional<Graphic> startGraphic() {
            return Optional.empty();
        }

        @Override
        public int baseExperience() {
            return 30;
        }

        @Override
        public Optional<Item[]> itemsRequired(Player player) {
            return Optional.of(new Item[] { new Item(556, 1), new Item(554, 1), new Item(562, 2), new Item(560, 2) });
        }

        @Override
        public int levelRequired() {
            return 50;
        }

        @Override
        public int spellId() {
            return 12939;
        }
    }),
    SHADOW_RUSH(new CombatAncientSpell() {
        @Override
        public void spellEffect(Mobile cast, Mobile castOn, int damage) {
            if (castOn.isPlayer()) {
				Player player = (Player) castOn;

				if (player.getSkillManager().getCurrentLevel(Skill.ATTACK) < player.getSkillManager().getMaxLevel(Skill.ATTACK)) {
					return;
				}

				int decrease = (int) (0.1 * (player.getSkillManager().getCurrentLevel(Skill.ATTACK)));
				player.getSkillManager().setCurrentLevel(Skill.ATTACK, player.getSkillManager().getCurrentLevel(Skill.ATTACK) - decrease);
				player.getSkillManager().updateSkill(Skill.ATTACK);
			}
        }

        @Override
        public int spellRadius() {
            return 0;
        }

        @Override
        public Optional<Animation> castAnimation() {
            return Optional.of(new Animation(1978));
        }

        @Override
        public Optional<Projectile> castProjectile(Mobile cast, Mobile castOn) {
            return Optional.of(new Projectile(cast, castOn, 378, 0, 20, 43, 31));
        }

        @Override
        public Optional<Graphic> endGraphic() {
            return Optional.of(new Graphic(379));
        }

        @Override
        public int maximumHit() {
            return 14;
        }

        @Override
        public Optional<Graphic> startGraphic() {
            return Optional.empty();
        }

        @Override
        public int baseExperience() {
            return 31;
        }

        @Override
        public Optional<Item[]> itemsRequired(Player player) {
            return Optional.of(new Item[] { new Item(556, 1), new Item(566, 1), new Item(562, 2), new Item(560, 2) });
        }

        @Override
        public int levelRequired() {
            return 52;
        }

        @Override
        public int spellId() {
            return 12987;
        }
    }),
    BLOOD_RUSH(new CombatAncientSpell() {
        @Override
        public void spellEffect(Mobile cast, Mobile castOn, int damage) {
            cast.heal((int) (damage * 0.10));
        }

        @Override
        public int spellRadius() {
            return 0;
        }

        @Override
        public Optional<Animation> castAnimation() {
            return Optional.of(new Animation(1978));
        }

        @Override
        public Optional<Projectile> castProjectile(Mobile cast, Mobile castOn) {
            return Optional.of(new Projectile(cast, castOn, 372, 0, 20, 43, 31));
        }

        @Override
        public Optional<Graphic> endGraphic() {
            return Optional.of(new Graphic(373));
        }

        @Override
        public int maximumHit() {
            return 15;
        }

        @Override
        public Optional<Graphic> startGraphic() {
            return Optional.empty();
        }

        @Override
        public int baseExperience() {
            return 33;
        }

        @Override
        public Optional<Item[]> itemsRequired(Player player) {
            return Optional.of(new Item[] { new Item(565, 1), new Item(562, 2), new Item(560, 2) });
        }

        @Override
        public int levelRequired() {
            return 56;
        }

        @Override
        public int spellId() {
            return 12901;
        }
    }),
    ICE_RUSH(new CombatAncientSpell() {
        @Override
        public void spellEffect(Mobile cast, Mobile castOn, int damage) {
        	CombatFactory.freeze(castOn, 5);
        }

        @Override
        public int spellRadius() {
            return 0;
        }

        @Override
        public Optional<Animation> castAnimation() {
            return Optional.of(new Animation(1978));
        }

        @Override
        public Optional<Projectile> castProjectile(Mobile cast, Mobile castOn) {
            return Optional.of(new Projectile(cast, castOn, 360, 0, 20, 43, 31));
        }

        @Override
        public Optional<Graphic> endGraphic() {
            return Optional.of(new Graphic(361));
        }

        @Override
        public int maximumHit() {
            return 18;
        }

        @Override
        public Optional<Graphic> startGraphic() {
            return Optional.empty();
        }

        @Override
        public int baseExperience() {
            return 34;
        }

        @Override
        public Optional<Item[]> itemsRequired(Player player) {
            return Optional.of(new Item[] { new Item(555, 2), new Item(562, 2), new Item(560, 2) });
        }

        @Override
        public int levelRequired() {
            return 58;
        }

        @Override
        public int spellId() {
            return 12861;
        }
    }),
    SMOKE_BURST(new CombatAncientSpell() {
        @Override
        public void spellEffect(Mobile cast, Mobile castOn, int damage) {
            CombatFactory.poisonEntity(castOn, PoisonType.MILD);
        }

        @Override
        public int spellRadius() {
            return 1;
        }

        @Override
        public Optional<Animation> castAnimation() {
            return Optional.of(new Animation(1979));
        }

        @Override
        public Optional<Projectile> castProjectile(Mobile cast, Mobile castOn) {
            return Optional.empty();
        }

        @Override
        public Optional<Graphic> endGraphic() {
            return Optional.of(new Graphic(389));
        }

        @Override
        public int maximumHit() {
            return 13;
        }

        @Override
        public Optional<Graphic> startGraphic() {
            return Optional.empty();
        }

        @Override
        public int baseExperience() {
            return 36;
        }

        @Override
        public Optional<Item[]> itemsRequired(Player player) {
            return Optional.of(new Item[] { new Item(556, 2), new Item(554, 2), new Item(562, 4), new Item(560, 2) });
        }

        @Override
        public int levelRequired() {
            return 62;
        }

        @Override
        public int spellId() {
            return 12963;
        }
    }),
    SHADOW_BURST(new CombatAncientSpell() {
        @Override
        public void spellEffect(Mobile cast, Mobile castOn, int damage) {
            if (castOn.isPlayer()) {
				Player player = (Player) castOn;

				if (player.getSkillManager().getCurrentLevel(Skill.ATTACK) < player.getSkillManager().getMaxLevel(Skill.ATTACK)) {
					return;
				}

				int decrease = (int) (0.1 * (player.getSkillManager().getCurrentLevel(Skill.ATTACK)));
				player.getSkillManager().setCurrentLevel(Skill.ATTACK, player.getSkillManager().getCurrentLevel(Skill.ATTACK) - decrease);
				player.getSkillManager().updateSkill(Skill.ATTACK);
			}
        }

        @Override
        public int spellRadius() {
            return 1;
        }

        @Override
        public Optional<Animation> castAnimation() {
            return Optional.of(new Animation(1979));
        }

        @Override
        public Optional<Projectile> castProjectile(Mobile cast, Mobile castOn) {
            return Optional.empty();
        }

        @Override
        public Optional<Graphic> endGraphic() {
            return Optional.of(new Graphic(382));
        }

        @Override
        public int maximumHit() {
            return 18;
        }

        @Override
        public Optional<Graphic> startGraphic() {
            return Optional.empty();
        }

        @Override
        public int baseExperience() {
            return 37;
        }

        @Override
        public Optional<Item[]> itemsRequired(Player player) {
            return Optional.of(new Item[] { new Item(556, 1), new Item(566, 2), new Item(562, 4), new Item(560, 2) });
        }

        @Override
        public int levelRequired() {
            return 64;
        }

        @Override
        public int spellId() {
            return 13011;
        }
    }),
    BLOOD_BURST(new CombatAncientSpell() {
        @Override
        public void spellEffect(Mobile cast, Mobile castOn, int damage) {
            cast.heal((int) (damage * 0.15));
        }

        @Override
        public int spellRadius() {
            return 1;
        }

        @Override
        public Optional<Animation> castAnimation() {
            return Optional.of(new Animation(1979));
        }

        @Override
        public Optional<Projectile> castProjectile(Mobile cast, Mobile castOn) {
            return Optional.empty();
        }

        @Override
        public Optional<Graphic> endGraphic() {
            return Optional.of(new Graphic(376));
        }

        @Override
        public int maximumHit() {
            return 21;
        }

        @Override
        public Optional<Graphic> startGraphic() {
            return Optional.empty();
        }

        @Override
        public int baseExperience() {
            return 39;
        }

        @Override
        public Optional<Item[]> itemsRequired(Player player) {
            return Optional.of(new Item[] { new Item(565, 2), new Item(562, 4), new Item(560, 2) });
        }

        @Override
        public int levelRequired() {
            return 68;
        }

        @Override
        public int spellId() {
            return 12919;
        }
    }),
    ICE_BURST(new CombatAncientSpell() {
        @Override
        public void spellEffect(Mobile cast, Mobile castOn, int damage) {
        	CombatFactory.freeze(castOn, 10);
        }

        @Override
        public int spellRadius() {
            return 1;
        }

        @Override
        public Optional<Animation> castAnimation() {
            return Optional.of(new Animation(1979));
        }

        @Override
        public Optional<Projectile> castProjectile(Mobile cast, Mobile castOn) {
            return Optional.empty();
        }

        @Override
        public Optional<Graphic> endGraphic() {
            return Optional.of(new Graphic(363));
        }

        @Override
        public int maximumHit() {
            return 22;
        }

        @Override
        public Optional<Graphic> startGraphic() {
            return Optional.empty();
        }

        @Override
        public int baseExperience() {
            return 40;
        }

        @Override
        public Optional<Item[]> itemsRequired(Player player) {
            return Optional.of(new Item[] { new Item(555, 4), new Item(562, 4), new Item(560, 2) });
        }

        @Override
        public int levelRequired() {
            return 70;
        }

        @Override
        public int spellId() {
            return 12881;
        }
    }),
    SMOKE_BLITZ(new CombatAncientSpell() {
        @Override
        public void spellEffect(Mobile cast, Mobile castOn, int damage) {
            CombatFactory.poisonEntity(castOn, PoisonType.EXTRA);
        }

        @Override
        public int spellRadius() {
            return 0;
        }

        @Override
        public Optional<Animation> castAnimation() {
            return Optional.of(new Animation(1978));
        }

        @Override
        public Optional<Projectile> castProjectile(Mobile cast, Mobile castOn) {
            return Optional.of(new Projectile(cast, castOn, 386, 0, 20, 43, 31));
        }

        @Override
        public Optional<Graphic> endGraphic() {
            return Optional.of(new Graphic(387));
        }

        @Override
        public int maximumHit() {
            return 23;
        }

        @Override
        public Optional<Graphic> startGraphic() {
            return Optional.empty();
        }

        @Override
        public int baseExperience() {
            return 42;
        }

        @Override
        public Optional<Item[]> itemsRequired(Player player) {
            return Optional.of(new Item[] { new Item(556, 2), new Item(554, 2), new Item(565, 2), new Item(560, 2) });
        }

        @Override
        public int levelRequired() {
            return 74;
        }

        @Override
        public int spellId() {
            return 12951;
        }
    }),
    SHADOW_BLITZ(new CombatAncientSpell() {
        @Override
        public void spellEffect(Mobile cast, Mobile castOn, int damage) {
			if (castOn.isPlayer()) {
				Player player = (Player) castOn;

				if (player.getSkillManager().getCurrentLevel(Skill.ATTACK) < player.getSkillManager().getMaxLevel(Skill.ATTACK)) {
					return;
				}

				int decrease = (int) (0.15 * (player.getSkillManager().getCurrentLevel(Skill.ATTACK)));
				player.getSkillManager().setCurrentLevel(Skill.ATTACK, player.getSkillManager().getCurrentLevel(Skill.ATTACK) - decrease);
				player.getSkillManager().updateSkill(Skill.ATTACK);
			}
        }

        @Override
        public int spellRadius() {
            return 0;
        }

        @Override
        public Optional<Animation> castAnimation() {
            return Optional.of(new Animation(1978));
        }

        @Override
        public Optional<Projectile> castProjectile(Mobile cast, Mobile castOn) {
            return Optional.of(new Projectile(cast, castOn, 380, 0, 20, 43, 31));
        }

        @Override
        public Optional<Graphic> endGraphic() {
            return Optional.of(new Graphic(381));
        }

        @Override
        public int maximumHit() {
            return 24;
        }

        @Override
        public Optional<Graphic> startGraphic() {
            return Optional.empty();
        }

        @Override
        public int baseExperience() {
            return 43;
        }

        @Override
        public Optional<Item[]> itemsRequired(Player player) {
            return Optional.of(new Item[] { new Item(556, 2), new Item(566, 2), new Item(565, 2), new Item(560, 2) });
        }

        @Override
        public int levelRequired() {
            return 76;
        }

        @Override
        public int spellId() {
            return 12999;
        }
    }),
    BLOOD_BLITZ(new CombatAncientSpell() {
        @Override
        public void spellEffect(Mobile cast, Mobile castOn, int damage) {
			cast.heal((int) (damage * 0.20));
        }

        @Override
        public int spellRadius() {
            return 0;
        }

        @Override
        public Optional<Animation> castAnimation() {
            return Optional.of(new Animation(1978));
        }

        @Override
        public Optional<Projectile> castProjectile(Mobile cast, Mobile castOn) {
            return Optional.of(new Projectile(cast, castOn, 374, 0, 20, 43, 31));
        }

        @Override
        public Optional<Graphic> endGraphic() {
            return Optional.of(new Graphic(375));
        }

        @Override
        public int maximumHit() {
            return 25;
        }

        @Override
        public Optional<Graphic> startGraphic() {
            return Optional.empty();
        }

        @Override
        public int baseExperience() {
            return 45;
        }

        @Override
        public Optional<Item[]> itemsRequired(Player player) {
            return Optional.of(new Item[] { new Item(565, 4), new Item(560, 2) });
        }

        @Override
        public int levelRequired() {
            return 80;
        }

        @Override
        public int spellId() {
            return 12911;
        }
    }),
    ICE_BLITZ(new CombatAncientSpell() {
        @Override
        public void spellEffect(Mobile cast, Mobile castOn, int damage) {
        	CombatFactory.freeze(castOn, 15);
        }

        @Override
        public int spellRadius() {
            return 0;
        }

        @Override
        public Optional<Animation> castAnimation() {
            return Optional.of(new Animation(1978));
        }

        @Override
        public Optional<Projectile> castProjectile(Mobile cast, Mobile castOn) {
            return Optional.empty();
        }

        @Override
        public Optional<Graphic> endGraphic() {
            return Optional.of(new Graphic(367));
        }

        @Override
        public int maximumHit() {
            return 26;
        }

        @Override
        public Optional<Graphic> startGraphic() {
            return Optional.of(new Graphic(366, 6553600));
        }

        @Override
        public int baseExperience() {
            return 46;
        }

        @Override
        public Optional<Item[]> itemsRequired(Player player) {
            return Optional.of(new Item[] { new Item(555, 3), new Item(565, 2), new Item(560, 2) });
        }

        @Override
        public int levelRequired() {
            return 82;
        }

        @Override
        public int spellId() {
            return 12871;
        }
    }),
    SMOKE_BARRAGE(new CombatAncientSpell() {
        @Override
        public void spellEffect(Mobile cast, Mobile castOn, int damage) {
        	CombatFactory.poisonEntity(castOn, PoisonType.SUPER);
        }

        @Override
        public int spellRadius() {
            return 1;
        }

        @Override
        public Optional<Animation> castAnimation() {
            return Optional.of(new Animation(1979));
        }

        @Override
        public Optional<Projectile> castProjectile(Mobile cast, Mobile castOn) {
            return Optional.empty();
        }

        @Override
        public Optional<Graphic> endGraphic() {
            return Optional.of(new Graphic(391));
        }

        @Override
        public int maximumHit() {
            return 27;
        }

        @Override
        public Optional<Graphic> startGraphic() {
            return Optional.empty();
        }

        @Override
        public int baseExperience() {
            return 48;
        }

        @Override
        public Optional<Item[]> itemsRequired(Player player) {
            return Optional.of(new Item[] { new Item(556, 4), new Item(554, 4), new Item(565, 2), new Item(560, 4) });
        }

        @Override
        public int levelRequired() {
            return 86;
        }

        @Override
        public int spellId() {
            return 12975;
        }
    }),
    SHADOW_BARRAGE(new CombatAncientSpell() {
        @Override
        public void spellEffect(Mobile cast, Mobile castOn, int damage) {
        	if (castOn.isPlayer()) {
				Player player = (Player) castOn;

				if (player.getSkillManager().getCurrentLevel(Skill.ATTACK) < player.getSkillManager().getMaxLevel(Skill.ATTACK)) {
					return;
				}

				int decrease = (int) (0.15 * (player.getSkillManager().getCurrentLevel(Skill.ATTACK)));
				player.getSkillManager().setCurrentLevel(Skill.ATTACK, player.getSkillManager().getCurrentLevel(Skill.ATTACK) - decrease);
				player.getSkillManager().updateSkill(Skill.ATTACK);
			}
        }

        @Override
        public int spellRadius() {
            return 1;
        }

        @Override
        public Optional<Animation> castAnimation() {
            return Optional.of(new Animation(1979));
        }

        @Override
        public Optional<Projectile> castProjectile(Mobile cast, Mobile castOn) {
            return Optional.empty();
        }

        @Override
        public Optional<Graphic> endGraphic() {
            return Optional.of(new Graphic(383));
        }

        @Override
        public int maximumHit() {
            return 28;
        }

        @Override
        public Optional<Graphic> startGraphic() {
            return Optional.empty();
        }

        @Override
        public int baseExperience() {
            return 49;
        }

        @Override
        public Optional<Item[]> itemsRequired(Player player) {
            return Optional.of(new Item[] { new Item(556, 4), new Item(566, 3), new Item(565, 2), new Item(560, 4) });
        }

        @Override
        public int levelRequired() {
            return 88;
        }

        @Override
        public int spellId() {
            return 13023;
        }
    }),
    BLOOD_BARRAGE(new CombatAncientSpell() {
        @Override
        public void spellEffect(Mobile cast, Mobile castOn, int damage) {
			cast.heal((int) (damage * 0.20));
        }

        @Override
        public int spellRadius() {
            return 1;
        }

        @Override
        public Optional<Animation> castAnimation() {
            return Optional.of(new Animation(1979));
        }

        @Override
        public Optional<Projectile> castProjectile(Mobile cast, Mobile castOn) {
            return Optional.empty();
        }

        @Override
        public Optional<Graphic> endGraphic() {
            return Optional.of(new Graphic(377));
        }

        @Override
        public int maximumHit() {
            return 29;
        }

        @Override
        public Optional<Graphic> startGraphic() {
            return Optional.empty();
        }

        @Override
        public int baseExperience() {
            return 51;
        }

        @Override
        public Optional<Item[]> itemsRequired(Player player) {
            return Optional.of(new Item[] { new Item(560, 4), new Item(566, 1), new Item(565, 4) });
        }

        @Override
        public int levelRequired() {
            return 92;
        }

        @Override
        public int spellId() {
            return 12929;
        }
    }),
    ICE_BARRAGE(new CombatAncientSpell() {
        @Override
        public void spellEffect(Mobile cast, Mobile castOn, int damage) {
        	CombatFactory.freeze(castOn, 20);
        }

        @Override
        public int spellRadius() {
            return 1;
        }

        @Override
        public Optional<Animation> castAnimation() {
            return Optional.of(new Animation(1979));
        }

        @Override
        public Optional<Projectile> castProjectile(Mobile cast, Mobile castOn) {
            return Optional.empty();
        }

        @Override
        public Optional<Graphic> endGraphic() {
            return Optional.of(new Graphic(369));
        }

        @Override
        public int maximumHit() {
            return 30;
        }

        @Override
        public Optional<Graphic> startGraphic() {
            return Optional.empty();
        }

        @Override
        public int baseExperience() {
            return 52;
        }

        @Override
        public Optional<Item[]> itemsRequired(Player player) {
            return Optional.of(new Item[] { new Item(555, 6), new Item(565, 2), new Item(560, 4) });
        }

        @Override
        public int levelRequired() {
            return 94;
        }

        @Override
        public int spellId() {
            return 12891;
        }
    }),
    TRIDENT_OF_THE_SEAS(new CombatNormalSpell() {
    	 @Override
         public Optional<Animation> castAnimation() {
             return Optional.of(new Animation(1167));
         }

         @Override
         public Optional<Projectile> castProjectile(Mobile cast, Mobile castOn) {
             return Optional.of(new Projectile(cast, castOn, 1252, 0, 20, 43, 31));
         }

         @Override
         public Optional<Graphic> endGraphic() {
             return Optional.of(new Graphic(1253));
         }

         @Override
         public int maximumHit() {
             return 20;
         }

         @Override
         public Optional<Graphic> startGraphic() {
             return Optional.of(new Graphic(1251, GraphicHeight.HIGH));
         }

         @Override
         public int baseExperience() {
             return 50;
         }

         @Override
         public Optional<Item[]> equipmentRequired(Player player) {
             return Optional.empty();
         }

         @Override
         public Optional<Item[]> itemsRequired(Player player) {
             return Optional.empty();
         }

         @Override
         public int levelRequired() {
             return 75;
         }

         @Override
         public int spellId() {
             return 1;
         }
    }),
    TRIDENT_OF_THE_SWAMP(new CombatNormalSpell() {
   	 @Override
        public Optional<Animation> castAnimation() {
            return Optional.of(new Animation(1167));
        }

        @Override
        public Optional<Projectile> castProjectile(Mobile cast, Mobile castOn) {
            return Optional.of(new Projectile(cast, castOn, 1040, 0, 20, 43, 31));
        }

        @Override
        public Optional<Graphic> endGraphic() {
            return Optional.of(new Graphic(1042));
        }

        @Override
        public int maximumHit() {
            return 20;
        }

        @Override
        public Optional<Graphic> startGraphic() {
            return Optional.of(new Graphic(665, GraphicHeight.HIGH));
        }

        @Override
        public int baseExperience() {
            return 50;
        }

        @Override
        public Optional<Item[]> equipmentRequired(Player player) {
            return Optional.empty();
        }

        @Override
        public Optional<Item[]> itemsRequired(Player player) {
            return Optional.empty();
        }

        @Override
        public int levelRequired() {
            return 75;
        }

        @Override
        public int spellId() {
            return 1;
        }
   });

    /**
     * The spell attached to this element.
     */
    private final CombatSpell spell;

    /**
     * Creates a new {@link CombatSpells}.
     *
     * @param spell
     *            the spell attached to this element.
     */
    private CombatSpells(CombatSpell spell) {
        this.spell = spell;
    }

    /**
     * Gets the spell attached to this element.
     *
     * @return the spell.
     */
    public final CombatSpell getSpell() {
        return spell;
    }

    /**
     * Gets the spell with a {@link CombatSpell#spellId()} of {@code id}.
     *
     * @param id
     *            the identification of the combat spell.
     * @return the combat spell with that identification.
     */
    public static Optional<CombatSpells> getCombatSpells(int id) {
        return Arrays.stream(CombatSpells.values()).filter(s -> s != null && s.getSpell().spellId() == id).findFirst();
    }
    
	public static CombatSpell getCombatSpell(int spellId) {
		Optional<CombatSpells> spell = getCombatSpells(spellId);
		if(spell.isPresent()) {
			return spell.get().getSpell();
		}
		return null;
	}
}