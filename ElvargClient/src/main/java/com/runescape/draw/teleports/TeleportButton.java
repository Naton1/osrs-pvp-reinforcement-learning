package com.runescape.draw.teleports;

public enum TeleportButton {

    HOME("Home teleport", "Teleports you home.", new int[]{19220, 21756}, new int[]{19222, 21757}, new int[]{19210, 21741, 19210}, new int[]{}, 585, 592),
    TRAINING("Training teleports", "Teleports you to training areas.", new int[]{19641, 21833, 30067}, new int[]{19642, 21834, 30068}, new int[]{1164, 13035, 30064}, new int[]{19646, 19647, 19648, 21839, 21840, 21841, 30069, 30070, 30071}, 586, 593),
    MINIGAMES("Minigame teleports", "Teleports you to minigames.", new int[]{19722, 21933, 30078}, new int[]{19723, 21934, 30079}, new int[]{1167, 13045, 30075}, new int[]{19727, 19728, 19729, 21940, 21942, 30080, 30081, 30082}, 587, 594),
    PVP("PvP teleports", "Teleports you to player versus\\nplayer areas.", new int[]{19803, 22052, 30086}, new int[]{19804, 22053, 30087}, new int[]{1170, 13053, 30083}, new int[]{19808, 19809, 19810, 22056, 22057, 30088, 30089, 30090}, 588, 595),
    SLAYER("Slayer teleports", "Teleports you to slayer areas.", new int[]{19960, 22123, 30117}, new int[]{19961, 22124, 30118}, new int[]{1174, 13061, 30114}, new int[]{19964, 19965, 22127, 22128, 30119, 30120, 30121}, 589, 596),
    CITY("City teleports", "Teleports you to cities.", new int[]{20195, 22307, 30149}, new int[]{20196, 22308, 30150}, new int[]{1540, 13079, 30146}, new int[]{20199, 20200, 22311, 22312, 30151, 30152, 30153}, 590, 597),
    SKILLS("Skill teleports", "Teleports you to skill-related\\nareas.", new int[]{20354, 22232, 30109}, new int[]{20355, 22233, 30110}, new int[]{1541, 13069, 30106}, new int[]{20358, 20359, 22237, 22238, 22239, 30111, 30112, 30113}, 589, 596),
    BOSSES("Boss teleports", "Teleports you to bosses.", new int[]{20570, 22415, 30141}, new int[]{20571, 22416, 30142}, new int[]{7455, 13087, 30138}, new int[]{20574, 20575, 22419, 22420, 30143, 30144, 30145}, 591, 598),
    ZMI_ALTAR("Ourania teleport", "Teleports you to the Ourania altar.", new int[]{30165}, new int[]{30166}, new int[]{30162}, new int[]{30167, 30168, 30169}, 599, -1),;

    public final String name;
    public final String tooltip;
    public final int[] nameFrames;
    public final int[] tooltipFrames;
    public final int[] buttonIds;
    public final int[] requiredRunesFrames;
    public final int modernSpriteId;
    public final int ancientSpriteId;

    TeleportButton(String name, String tooltip, int[] nameFrames, int[] tooltipFrames, int[] buttonIds, int[] requiredRunesFrames, int modernSpriteId, int ancientSpriteId) {
        this.name = name;
        this.tooltip = tooltip;
        this.nameFrames = nameFrames;
        this.tooltipFrames = tooltipFrames;
        this.buttonIds = buttonIds;
        this.requiredRunesFrames = requiredRunesFrames;
        this.modernSpriteId = modernSpriteId;
        this.ancientSpriteId = ancientSpriteId;
    }
}

