package com.elvarg.util;

import java.util.HashMap;
import java.util.Map;

public class FrameUpdater {

    /**
     * System to optimize sendFrame126 performance.
     *
     * @author MikeRSPS
     * UltimateScape
     * http://ultimatescape2.com
     */
    public Map<Integer, Frame126> interfaceTextMap = new HashMap<Integer, Frame126>();

    public boolean shouldUpdate(String text, int id) {
        if (!interfaceTextMap.containsKey(id)) {
            interfaceTextMap.put(id, new Frame126(text, id));
        } else {
            Frame126 t = interfaceTextMap.get(id);
            if (text.equals(t.currentState)) {
                return false;
            }
            t.currentState = text;
        }
        return true;
    }

    public class Frame126 {
        public int id;
        public String currentState;

        public Frame126(String s, int id) {
            this.currentState = s;
            this.id = id;
        }

    }
}
