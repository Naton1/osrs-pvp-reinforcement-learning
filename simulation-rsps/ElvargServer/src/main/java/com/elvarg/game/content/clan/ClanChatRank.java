package com.elvarg.game.content.clan;

public enum ClanChatRank {

    FRIEND(-1, 197),
    RECRUIT(0, 198),
    CORPORAL(1, 199),
    SERGEANT(2, 200),
    LIEUTENANT(3, 201),
    CAPTAIN(4, 202),
    GENERAL(5, 203),
    OWNER(-1, 204),
    STAFF(-1, 203);

    private final int actionMenuId;
    private final int spriteId;
    ClanChatRank(int actionMenuId, int spriteId) {
        this.actionMenuId = actionMenuId;
        this.spriteId = spriteId;
    }

    public static ClanChatRank forId(int id) {
        for (ClanChatRank rank : ClanChatRank.values()) {
            if (rank.ordinal() == id) {
                return rank;
            }
        }
        return null;
    }

    public static ClanChatRank forMenuId(int id) {
        for (ClanChatRank rank : ClanChatRank.values()) {
            if (rank.actionMenuId == id) {
                return rank;
            }
        }
        return null;
    }

    public int getSpriteId() {
        return spriteId;
    }
}
