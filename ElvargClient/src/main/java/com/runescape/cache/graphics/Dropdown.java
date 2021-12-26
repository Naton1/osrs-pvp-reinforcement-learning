package com.runescape.cache.graphics;

import com.runescape.Configuration;
import com.runescape.cache.graphics.widget.Widget;
import com.runescape.model.content.Keybinding;

public enum Dropdown {


    KEYBIND_SELECTION() {
        @Override
        public void selectOption(int selected, Widget dropdown) {
            Keybinding.bind((dropdown.id - Keybinding.MIN_FRAME) / 3, selected);
        }
    },

    TELEPORT_SELECTION() {
        @Override
        public void selectOption(int selected, Widget r) {
            int[] teleports = {28103, 28200, 28250, 28400, 28150, 28300, 28350};
            Widget.interfaceCache[28100].children[2] = teleports[selected];
        }
    },

    PLAYER_ATTACK_OPTION_PRIORITY() {
        @Override
        public void selectOption(int selected, Widget r) {
            Configuration.playerAttackOptionPriority = selected;
        }
    },

    NPC_ATTACK_OPTION_PRIORITY() {
        @Override
        public void selectOption(int selected, Widget r) {
            Configuration.npcAttackOptionPriority = selected;
        }
    };

    private Dropdown() {
    }

    public abstract void selectOption(int selected, Widget r);
}
