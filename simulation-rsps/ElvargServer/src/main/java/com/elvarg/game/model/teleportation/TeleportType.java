package com.elvarg.game.model.teleportation;

import com.elvarg.game.model.Animation;
import com.elvarg.game.model.Graphic;
import com.elvarg.game.model.GraphicHeight;
import com.elvarg.game.model.Priority;

public enum TeleportType {

	// Spellbooks
    NORMAL(3, new Animation(714, Priority.HIGH), null, new Animation(715, Priority.HIGH), new Graphic(308, 50, GraphicHeight.HIGH), null, null),
    ANCIENT(5, new Animation(1979, Priority.HIGH), null, Animation.DEFAULT_RESET_ANIMATION, new Graphic(392, Priority.HIGH), null, null),
    LUNAR(4, new Animation(1816, Priority.HIGH), null, new Animation(715, Priority.HIGH), new Graphic(308, Priority.HIGH), null, null),
    // Ladders
    LADDER_DOWN(1, new Animation(827, Priority.HIGH), null, Animation.DEFAULT_RESET_ANIMATION, null, null, null),
	LADDER_UP(1, new Animation(828, Priority.HIGH), null, Animation.DEFAULT_RESET_ANIMATION, null, null, null),
	
    // Misc
	LEVER(3, new Animation(2140, Priority.HIGH), new Animation(714), new Animation(715, Priority.HIGH), null, new Graphic(308, 50, GraphicHeight.HIGH), null),
    TELE_TAB(3, new Animation(4071, Priority.HIGH), null, Animation.DEFAULT_RESET_ANIMATION, new Graphic(678, Priority.HIGH), null, null),
    PURO_PURO(9, new Animation(6601, Priority.HIGH), null, Animation.DEFAULT_RESET_ANIMATION, new Graphic(1118, Priority.HIGH), null, null),
	;

    private final Animation startAnim, middleAnim, endAnim;
    private final Graphic startGraphic, middleGraphic, endGraphic;
    private final int startTick;
    
    TeleportType(int startTick, Animation startAnim, Animation middleAnim, Animation endAnim, Graphic startGraphic, Graphic middleGraphic, Graphic endGraphic) {
        this.startTick = startTick;
        this.startAnim = startAnim;
        this.middleAnim = middleAnim;
        this.endAnim = endAnim;
        this.startGraphic = startGraphic;
        this.middleGraphic = middleGraphic;
        this.endGraphic = endGraphic;
    }

    public Animation getStartAnimation() {
        return startAnim;
    }

    public Animation getEndAnimation() {
        return endAnim;
    }

    public Graphic getStartGraphic() {
        return startGraphic;
    }

    public Graphic getEndGraphic() {
        return endGraphic;
    }

    public int getStartTick() {
        return startTick;
    }

	public Animation getMiddleAnim() {
		return middleAnim;
	}

	public Graphic getMiddleGraphic() {
		return middleGraphic;
	}
}
