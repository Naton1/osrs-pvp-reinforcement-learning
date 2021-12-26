package com.elvarg.game.content;

import com.elvarg.game.content.combat.CombatFactory;
import com.elvarg.game.content.skill.SkillManager;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.Animation;
import com.elvarg.game.model.Graphic;
import com.elvarg.game.model.Skill;
import com.elvarg.game.model.Skillcape;
import com.elvarg.game.model.container.impl.Equipment;
import com.elvarg.util.Misc;

import java.util.HashMap;
import java.util.Map;

public class Emotes {

    public static boolean doEmote(Player player, int button) {
        EmoteData data = EmoteData.forId(button);
        if (data != null) {
            animation(player, data.animation, data.graphic);
            return true;
        }

        //Skill cape button
        if (button == 19052) {
            Skillcape cape = Skillcape.forId(player.getEquipment().getItems()[Equipment.CAPE_SLOT].getId());
            if (cape != null) {

                if (cape != Skillcape.QUEST_POINT) {

                    if (cape.ordinal() < Skill.values().length) {

                        //Check if player is maxed in skill
                        Skill skill = Skill.values()[cape.ordinal()];
                        int level = SkillManager.getMaxAchievingLevel(skill);
                        if (player.getSkillManager().getMaxLevel(skill) < level) {
                            player.getPacketSender().sendMessage("You need " + Misc.anOrA(skill.getName()) + " " + Misc.formatPlayerName(skill.getName().toLowerCase()) + " level of at least " + level + " to do this emote.");
                            return false;
                        }

                    } else {

                        //Custom capes..
						/*if(cape == Skillcape.MAX_CAPE) {
							//Check if all level 99s
							for(Skill skill : Skill.values()) {
								int level = SkillManager.getMaxAchievingLevel(skill);
								if (player.getSkillManager().getMaxLevel(skill) < level) {
									player.getPacketSender().sendMessage("You need "+Misc.anOrA(skill.getName())+" " + Misc.formatPlayerName(skill.getName().toLowerCase()) + " level of at least "+ level + " to do this emote.");
									return false;
								}
							}
						}*/
                    }
                }
                animation(player, cape.getAnimation(), cape.getGraphic());
            }
            return true;
        }

        return false;
    }

    private static void animation(Player player, Animation anim, Graphic graphic) {
        if (CombatFactory.inCombat(player)) {
            player.getPacketSender().sendMessage("You cannot do this right now.");
            return;
        }

        //Stop skilling..
        player.getSkillManager().stopSkillable();

        //Stop movement..
        player.getMovementQueue().reset();

        if (anim != null)
            player.performAnimation(anim);
        if (graphic != null)
            player.performGraphic(graphic);
    }

    private enum EmoteData {
        YES(168, new Animation(855), null),
        NO(169, new Animation(856), null),
        BOW(164, new Animation(858), null),
        ANGRY(165, new Animation(859), null),
        THINK(162, new Animation(857), null),
        WAVE(163, new Animation(863), null),
        SHRUG(13370, new Animation(2113), null),
        CHEER(171, new Animation(862), null),
        BECKON(167, new Animation(864), null),
        LAUGH(170, new Animation(861), null),
        JUMP_FOR_JOY(13366, new Animation(2109), null),
        YAWN(13368, new Animation(2111), null),
        DANCE(166, new Animation(866), null),
        JIG(13363, new Animation(2106), null),
        SPIN(13364, new Animation(2107), null),
        HEADBANG(13365, new Animation(2108), null),
        CRY(161, new Animation(860), null),
        KISS(11100, new Animation(1374), new Graphic(574, 25)),
        PANIC(13362, new Animation(2105), null),
        RASPBERRY(13367, new Animation(2110), null),
        CRAP(172, new Animation(865), null),
        SALUTE(13369, new Animation(2112), null),
        GOBLIN_BOW(13383, new Animation(2127), null),
        GOBLIN_SALUTE(13384, new Animation(2128), null),
        GLASS_BOX(667, new Animation(1131), null),
        CLIMB_ROPE(6503, new Animation(1130), null),
        LEAN(6506, new Animation(1129), null),
        GLASS_WALL(666, new Animation(1128), null),
        ZOMBIE_WALK(18464, new Animation(3544), null),
        ZOMBIE_DANCE(18465, new Animation(3543), null),
        SCARED(15166, new Animation(2836), null),
        RABBIT_HOP(18686, new Animation(6111), null),

		/*ZOMBIE_HAND(15166, new Animation(7272), new Graphic(1244)),
		SAFETY_FIRST(6540, new Animation(8770), new Graphic(1553)),
		AIR_GUITAR(11101, new Animation(2414), new Graphic(1537)),
		SNOWMAN_DANCE(11102, new Animation(7531), null),
		FREEZE(11103, new Animation(11044), new Graphic(1973))*/;

        private static Map<Integer, EmoteData> emotes = new HashMap<Integer, EmoteData>();

        static {
            for (EmoteData t : EmoteData.values()) {
                emotes.put(t.button, t);
            }
        }

        public Animation animation;
        public Graphic graphic;
        private int button;

        EmoteData(int button, Animation animation, Graphic graphic) {
            this.button = button;
            this.animation = animation;
            this.graphic = graphic;
        }

        public static EmoteData forId(int button) {
            return emotes.get(button);
        }
    }
}
