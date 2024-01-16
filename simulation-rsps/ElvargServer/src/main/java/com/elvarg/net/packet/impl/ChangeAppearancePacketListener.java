package com.elvarg.net.packet.impl;

import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.Appearance;
import com.elvarg.game.model.Flag;
import com.elvarg.net.packet.Packet;
import com.elvarg.net.packet.PacketExecutor;

public class ChangeAppearancePacketListener implements PacketExecutor {

    private static final int[][] ALLOWED_COLORS = {
            {0, 11}, // hair color
            {0, 15}, // torso color
            {0, 15}, // legs color
            {0, 5}, // feet color
            {0, 7} // skin color
    };
    private static final int[][] FEMALE_VALUES = {
            {45, 54}, // head
            {-1, -1}, // jaw
            {56, 60}, // torso
            {61, 65}, // arms
            {67, 68}, // hands
            {70, 77}, // legs
            {79, 80}, // feet
    };
    private static final int[][] MALE_VALUES = {
            {0, 8}, // head
            {10, 17}, // jaw
            {18, 25}, // torso
            {26, 31}, // arms
            {33, 34}, // hands
            {36, 40}, // legs
            {42, 43}, // feet
    };

    @Override
    public void execute(Player player, Packet packet) {
        try {
            int gender = packet.readByte();
            if (gender != 0 && gender != 1) {
                return;
            }
            final int[] apperances = new int[MALE_VALUES.length];
            final int[] colors = new int[ALLOWED_COLORS.length];
            for (int i = 0; i < apperances.length; i++) {
                int value = packet.readByte();
                if (value < (gender == 0 ? MALE_VALUES[i][0] : FEMALE_VALUES[i][0]) || value > (gender == 0 ? MALE_VALUES[i][1] : FEMALE_VALUES[i][1]))
                    value = (gender == 0 ? MALE_VALUES[i][0] : FEMALE_VALUES[i][0]);
                apperances[i] = value;
            }
            for (int i = 0; i < colors.length; i++) {
                int value = packet.readByte();
                if (value < ALLOWED_COLORS[i][0] || value > ALLOWED_COLORS[i][1])
                    value = ALLOWED_COLORS[i][0];
                colors[i] = value;
            }
            if (player.getAppearance().canChangeAppearance() && player.getInterfaceId() > 0) {
                //Appearance looks

                player.getAppearance().set(Appearance.GENDER, gender);
                player.getAppearance().set(Appearance.HEAD, apperances[0]);
                player.getAppearance().set(Appearance.CHEST, apperances[2]);
                player.getAppearance().set(Appearance.ARMS, apperances[3]);
                player.getAppearance().set(Appearance.HANDS, apperances[4]);
                player.getAppearance().set(Appearance.LEGS, apperances[5]);
                player.getAppearance().set(Appearance.FEET, apperances[6]);
                player.getAppearance().set(Appearance.BEARD, apperances[1]);

                //Colors
                player.getAppearance().set(Appearance.HAIR_COLOUR, colors[0]);
                player.getAppearance().set(Appearance.TORSO_COLOUR, colors[1]);
                player.getAppearance().set(Appearance.LEG_COLOUR, colors[2]);
                player.getAppearance().set(Appearance.FEET_COLOUR, colors[3]);
                player.getAppearance().set(Appearance.SKIN_COLOUR, colors[4]);

                player.getUpdateFlag().flag(Flag.APPEARANCE);
            }
        } catch (Exception e) {
            player.getAppearance().set();
            //e.printStackTrace();
        }
        player.getPacketSender().sendInterfaceRemoval();
        player.getAppearance().setCanChangeAppearance(false);
    }

}
