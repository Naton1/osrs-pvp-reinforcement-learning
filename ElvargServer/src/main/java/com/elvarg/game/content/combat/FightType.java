package com.elvarg.game.content.combat;

import com.elvarg.game.content.sound.Sound;
import com.elvarg.game.model.equipment.BonusManager;

/**
 * A collection of constants that each represent a different fighting type.
 *
 * @author lare96
 */
public enum FightType {

    STAFF_BASH(401, 43, 0, BonusManager.ATTACK_CRUSH, FightStyle.ACCURATE),
    STAFF_POUND(406, 43, 1, BonusManager.ATTACK_CRUSH, FightStyle.AGGRESSIVE),
    STAFF_FOCUS(406, 43, 2, BonusManager.ATTACK_CRUSH, FightStyle.DEFENSIVE),
    WARHAMMER_POUND(401, 43, 0, BonusManager.ATTACK_CRUSH, FightStyle.ACCURATE),
    WARHAMMER_PUMMEL(401, 43, 1, BonusManager.ATTACK_CRUSH, FightStyle.AGGRESSIVE),
    WARHAMMER_BLOCK(401, 43, 2, BonusManager.ATTACK_CRUSH, FightStyle.DEFENSIVE),
    MAUL_POUND(2661, 43, 0, BonusManager.ATTACK_CRUSH, FightStyle.ACCURATE),
    MAUL_PUMMEL(2661, 43, 1, BonusManager.ATTACK_CRUSH, FightStyle.AGGRESSIVE),
    MAUL_BLOCK(2661, 43, 2, BonusManager.ATTACK_CRUSH, FightStyle.DEFENSIVE),
    ELDER_MAUL_POUND(7516, 43, 0, BonusManager.ATTACK_CRUSH, FightStyle.ACCURATE),
    ELDER_MAUL_PUMMEL(7516, 43, 1, BonusManager.ATTACK_CRUSH, FightStyle.AGGRESSIVE),
    ELDER_MAUL_BLOCK(7516, 43, 2, BonusManager.ATTACK_CRUSH, FightStyle.DEFENSIVE),
    GRANITE_MAUL_POUND(1665, 43, 0, BonusManager.ATTACK_CRUSH, FightStyle.ACCURATE),
    GRANITE_MAUL_PUMMEL(1665, 43, 1, BonusManager.ATTACK_CRUSH, FightStyle.AGGRESSIVE),
    GRANITE_MAUL_BLOCK(1665, 43, 2, BonusManager.ATTACK_CRUSH, FightStyle.DEFENSIVE),
    SCYTHE_REAP(414, 43, 0, BonusManager.ATTACK_SLASH, FightStyle.ACCURATE),
    SCYTHE_CHOP(382, 43, 1, BonusManager.ATTACK_STAB, FightStyle.AGGRESSIVE),
    SCYTHE_JAB(2066, 43, 2, BonusManager.ATTACK_CRUSH, FightStyle.CONTROLLED),
    SCYTHE_BLOCK(382, 43, 3, BonusManager.ATTACK_SLASH, FightStyle.DEFENSIVE),
    BATTLEAXE_CHOP(401, 43, 0, BonusManager.ATTACK_SLASH, FightStyle.ACCURATE),
    BATTLEAXE_HACK(401, 43, 1, BonusManager.ATTACK_SLASH, FightStyle.AGGRESSIVE),
    BATTLEAXE_SMASH(401, 43, 2, BonusManager.ATTACK_CRUSH, FightStyle.AGGRESSIVE),
    BATTLEAXE_BLOCK(401, 43, 3, BonusManager.ATTACK_SLASH, FightStyle.DEFENSIVE),
    GREATAXE_CHOP(2062, 43, 0, BonusManager.ATTACK_SLASH, FightStyle.ACCURATE),
    GREATAXE_HACK(2062, 43, 1, BonusManager.ATTACK_SLASH, FightStyle.AGGRESSIVE),
    GREATAXE_SMASH(2066, 43, 2, BonusManager.ATTACK_CRUSH, FightStyle.AGGRESSIVE),
    GREATAXE_BLOCK(2062, 43, 3, BonusManager.ATTACK_SLASH, FightStyle.DEFENSIVE),
    CROSSBOW_ACCURATE(4230, 43, 0, BonusManager.ATTACK_RANGE, FightStyle.ACCURATE),
    CROSSBOW_RAPID(4230, 43, 1, BonusManager.ATTACK_RANGE, FightStyle.AGGRESSIVE),
    CROSSBOW_LONGRANGE(4230, 43, 2, BonusManager.ATTACK_RANGE, FightStyle.DEFENSIVE),

    KARILS_CROSSBOW_ACCURATE(2075, 43, 0, BonusManager.ATTACK_RANGE, FightStyle.ACCURATE),
    KARILS_CROSSBOW_RAPID(2075, 43, 1, BonusManager.ATTACK_RANGE, FightStyle.AGGRESSIVE),
    KARILS_CROSSBOW_LONGRANGE(2075, 43, 2, BonusManager.ATTACK_RANGE, FightStyle.DEFENSIVE),

    BALLISTA_ACCURATE(7218, 43, 0, BonusManager.ATTACK_RANGE, FightStyle.ACCURATE),
    BALLISTA_RAPID(7218, 43, 1, BonusManager.ATTACK_RANGE, FightStyle.AGGRESSIVE),
    BALLISTA_LONGRANGE(7218, 43, 2, BonusManager.ATTACK_RANGE, FightStyle.DEFENSIVE),
    BLOWPIPE_ACCURATE(5061, 43, 0, BonusManager.ATTACK_RANGE, FightStyle.ACCURATE),
    BLOWPIPE_RAPID(5061, 43, 1, BonusManager.ATTACK_RANGE, FightStyle.AGGRESSIVE),
    BLOWPIPE_LONGRANGE(5061, 43, 2, BonusManager.ATTACK_RANGE, FightStyle.DEFENSIVE),

    ABYSSAL_BLUDGEON_CHOP(7054, 43, 0, BonusManager.ATTACK_SLASH, FightStyle.ACCURATE),
    ABYSSAL_BLUDGEON_SLASH(7054, 43, 1, BonusManager.ATTACK_SLASH, FightStyle.AGGRESSIVE),
    ABYSSAL_BLUDGEON_SMASH(7054, 43, 2, BonusManager.ATTACK_CRUSH, FightStyle.AGGRESSIVE),
    ABYSSAL_BLUDGEON_BLOCK(7054, 43, 3, BonusManager.ATTACK_SLASH, FightStyle.DEFENSIVE),

    SHORTBOW_ACCURATE(426, 43, 0, BonusManager.ATTACK_RANGE, FightStyle.ACCURATE),
    SHORTBOW_RAPID(426, 43, 1, BonusManager.ATTACK_RANGE, FightStyle.AGGRESSIVE),
    SHORTBOW_LONGRANGE(426, 43, 2, BonusManager.ATTACK_RANGE, FightStyle.DEFENSIVE),
    LONGBOW_ACCURATE(426, 43, 0, BonusManager.ATTACK_RANGE, FightStyle.ACCURATE),
    LONGBOW_RAPID(426, 43, 1, BonusManager.ATTACK_RANGE, FightStyle.AGGRESSIVE),
    LONGBOW_LONGRANGE(426, 43, 2, BonusManager.ATTACK_RANGE, FightStyle.DEFENSIVE),
    DAGGER_STAB(400, 43, 0, BonusManager.ATTACK_STAB, FightStyle.ACCURATE),
    DAGGER_LUNGE(400, 43, 1, BonusManager.ATTACK_STAB, FightStyle.AGGRESSIVE),
    DAGGER_SLASH(400, 43, 2, BonusManager.ATTACK_STAB, FightStyle.AGGRESSIVE),
    DAGGER_BLOCK(400, 43, 3, BonusManager.ATTACK_STAB, FightStyle.DEFENSIVE),
    DRAGON_DAGGER_STAB(376, 43, 0, BonusManager.ATTACK_STAB, FightStyle.ACCURATE),
    DRAGON_DAGGER_LUNGE(376, 43, 1, BonusManager.ATTACK_STAB, FightStyle.AGGRESSIVE),
    DRAGON_DAGGER_SLASH(377, 43, 2, BonusManager.ATTACK_STAB, FightStyle.AGGRESSIVE),
    DRAGON_DAGGER_BLOCK(376, 43, 3, BonusManager.ATTACK_STAB, FightStyle.DEFENSIVE),
    SWORD_STAB(412, 43, 0, BonusManager.ATTACK_STAB, FightStyle.ACCURATE),
    SWORD_LUNGE(412, 43, 1, BonusManager.ATTACK_STAB, FightStyle.AGGRESSIVE),
    SWORD_SLASH(390, 43, 2, BonusManager.ATTACK_SLASH, FightStyle.AGGRESSIVE),
    SWORD_BLOCK(412, 43, 3, BonusManager.ATTACK_STAB, FightStyle.DEFENSIVE),
    SCIMITAR_CHOP(390, 43, 0, BonusManager.ATTACK_SLASH, FightStyle.ACCURATE),
    SCIMITAR_SLASH(390, 43, 1, BonusManager.ATTACK_SLASH, FightStyle.AGGRESSIVE),
    SCIMITAR_LUNGE(390, 43, 2, BonusManager.ATTACK_STAB, FightStyle.CONTROLLED),
    SCIMITAR_BLOCK(390, 43, 3, BonusManager.ATTACK_SLASH, FightStyle.DEFENSIVE),
    LONGSWORD_CHOP(390, 43, 0, BonusManager.ATTACK_SLASH, FightStyle.ACCURATE),
    LONGSWORD_SLASH(390, 43, 1, BonusManager.ATTACK_SLASH, FightStyle.AGGRESSIVE),
    LONGSWORD_LUNGE(412, 43, 2, BonusManager.ATTACK_STAB, FightStyle.CONTROLLED),
    LONGSWORD_BLOCK(390, 43, 3, BonusManager.ATTACK_SLASH, FightStyle.DEFENSIVE),
    MACE_POUND(401, 43, 0, BonusManager.ATTACK_CRUSH, FightStyle.ACCURATE),
    MACE_PUMMEL(401, 43, 1, BonusManager.ATTACK_CRUSH, FightStyle.AGGRESSIVE),
    MACE_SPIKE(401, 43, 2, BonusManager.ATTACK_STAB, FightStyle.CONTROLLED),
    MACE_BLOCK(401, 43, 3, BonusManager.ATTACK_CRUSH, FightStyle.DEFENSIVE),
    KNIFE_ACCURATE(806, 43, 0, BonusManager.ATTACK_RANGE, FightStyle.ACCURATE),
    KNIFE_RAPID(806, 43, 1, BonusManager.ATTACK_RANGE, FightStyle.AGGRESSIVE),
    KNIFE_LONGRANGE(806, 43, 2, BonusManager.ATTACK_RANGE, FightStyle.DEFENSIVE),
    OBBY_RING_ACCURATE(2614, 43, 0, BonusManager.ATTACK_RANGE, FightStyle.ACCURATE),
    OBBY_RING_RAPID(2614, 43, 1, BonusManager.ATTACK_RANGE, FightStyle.AGGRESSIVE),
    OBBY_RING_LONGRANGE(2614, 43, 2, BonusManager.ATTACK_RANGE, FightStyle.DEFENSIVE),
    SPEAR_LUNGE(2080, 43, 0, BonusManager.ATTACK_STAB, FightStyle.CONTROLLED),
    SPEAR_SWIPE(2081, 43, 1, BonusManager.ATTACK_SLASH, FightStyle.CONTROLLED),
    SPEAR_POUND(2082, 43, 2, BonusManager.ATTACK_CRUSH, FightStyle.CONTROLLED),
    SPEAR_BLOCK(2080, 43, 3, BonusManager.ATTACK_STAB, FightStyle.DEFENSIVE),
    TWOHANDEDSWORD_CHOP(407, 43, 0, BonusManager.ATTACK_SLASH, FightStyle.ACCURATE),
    TWOHANDEDSWORD_SLASH(407, 43, 1, BonusManager.ATTACK_SLASH, FightStyle.AGGRESSIVE),
    TWOHANDEDSWORD_SMASH(406, 43, 2, BonusManager.ATTACK_CRUSH, FightStyle.AGGRESSIVE),
    TWOHANDEDSWORD_BLOCK(407, 43, 3, BonusManager.ATTACK_SLASH, FightStyle.DEFENSIVE),

    GODSWORD_CHOP(7046, 43, 0, BonusManager.ATTACK_SLASH, FightStyle.ACCURATE),
    GODSWORD_SLASH(7045, 43, 1, BonusManager.ATTACK_SLASH, FightStyle.AGGRESSIVE),
    GODSWORD_SMASH(7054, 43, 2, BonusManager.ATTACK_CRUSH, FightStyle.AGGRESSIVE),
    GODSWORD_BLOCK(7055, 43, 3, BonusManager.ATTACK_SLASH, FightStyle.DEFENSIVE),

    VERACS_FLAIL_POUND(1658, 43, 0, BonusManager.ATTACK_CRUSH, FightStyle.ACCURATE),
    VERACS_FLAIL_PUMMEL(1658, 43, 1, BonusManager.ATTACK_CRUSH, FightStyle.AGGRESSIVE),
    VERACS_FLAIL_SPIKE(1658, 43, 2, BonusManager.ATTACK_STAB, FightStyle.CONTROLLED),
    VERACS_FLAIL_BLOCK(1658, 43, 3, BonusManager.ATTACK_CRUSH, FightStyle.DEFENSIVE),
    PICKAXE_SPIKE(401, 43, 0, BonusManager.ATTACK_STAB, FightStyle.ACCURATE),
    PICKAXE_IMPALE(401, 43, 1, BonusManager.ATTACK_STAB, FightStyle.AGGRESSIVE),
    PICKAXE_SMASH(401, 43, 2, BonusManager.ATTACK_CRUSH, FightStyle.AGGRESSIVE),
    PICKAXE_BLOCK(400, 43, 3, BonusManager.ATTACK_STAB, FightStyle.DEFENSIVE),
    CLAWS_CHOP(393, 43, 0, BonusManager.ATTACK_SLASH, FightStyle.ACCURATE),
    CLAWS_SLASH(393, 43, 1, BonusManager.ATTACK_SLASH, FightStyle.AGGRESSIVE),
    CLAWS_LUNGE(393, 43, 2, BonusManager.ATTACK_STAB, FightStyle.CONTROLLED),
    CLAWS_BLOCK(393, 43, 3, BonusManager.ATTACK_SLASH, FightStyle.DEFENSIVE),
    HALBERD_JAB(440, 43, 0, BonusManager.ATTACK_STAB, FightStyle.CONTROLLED),
    HALBERD_SWIPE(440, 43, 1, BonusManager.ATTACK_SLASH, FightStyle.AGGRESSIVE),
    HALBERD_FEND(440, 43, 2, BonusManager.ATTACK_STAB, FightStyle.DEFENSIVE),
    UNARMED_PUNCH(422, 43, 0, BonusManager.ATTACK_CRUSH, FightStyle.ACCURATE),
    UNARMED_KICK(423, 43, 1, BonusManager.ATTACK_CRUSH, FightStyle.AGGRESSIVE),
    UNARMED_BLOCK(422, 43, 2, BonusManager.ATTACK_CRUSH, FightStyle.DEFENSIVE),
    WHIP_FLICK(1658, 43, 0, BonusManager.ATTACK_SLASH, FightStyle.ACCURATE, Sound.WEAPON_WHIP),
    WHIP_LASH(1658, 43, 1, BonusManager.ATTACK_SLASH, FightStyle.CONTROLLED, Sound.WEAPON_WHIP),
    WHIP_DEFLECT(1658, 43, 2, BonusManager.ATTACK_SLASH, FightStyle.DEFENSIVE, Sound.WEAPON_WHIP),
    THROWNAXE_ACCURATE(806, 43, 0, BonusManager.ATTACK_RANGE, FightStyle.ACCURATE),
    THROWNAXE_RAPID(806, 43, 1, BonusManager.ATTACK_RANGE, FightStyle.AGGRESSIVE),
    THROWNAXE_LONGRANGE(806, 43, 2, BonusManager.ATTACK_RANGE, FightStyle.DEFENSIVE),
    DART_ACCURATE(806, 43, 0, BonusManager.ATTACK_RANGE, FightStyle.ACCURATE),
    DART_RAPID(806, 43, 1, BonusManager.ATTACK_RANGE, FightStyle.AGGRESSIVE),
    DART_LONGRANGE(806, 43, 2, BonusManager.ATTACK_RANGE, FightStyle.DEFENSIVE),
    JAVELIN_ACCURATE(806, 43, 0, BonusManager.ATTACK_RANGE, FightStyle.ACCURATE),
    JAVELIN_RAPID(806, 43, 2, BonusManager.ATTACK_RANGE, FightStyle.AGGRESSIVE),
    JAVELIN_LONGRANGE(806, 43, 3, BonusManager.ATTACK_RANGE, FightStyle.DEFENSIVE),
    GHRAZI_RAPIER_STAB(8145, 43, 0, BonusManager.ATTACK_STAB, FightStyle.ACCURATE),
    GHRAZI_RAPIER_LUNGE(8145, 43, 1, BonusManager.ATTACK_STAB, FightStyle.AGGRESSIVE),
    GHRAZI_RAPIER_SLASH(390, 43, 2, BonusManager.ATTACK_STAB, FightStyle.AGGRESSIVE),
    GHRAZI_RAPIER_BLOCK(8145, 43, 3, BonusManager.ATTACK_STAB, FightStyle.DEFENSIVE);

    /**
     * The animation this fight type holds.
     */
    private int animation;
    /**
     * Sound for attack
     */
    private Sound attackSound;

    /**
     * The parent config id.
     */
    private int parentId;

    /**
     * The child config id.
     */
    private int childId;

    /**
     * The bonus type.
     */
    private int bonusType;

    /**
     * The fighting style.
     */
    private FightStyle style;

    /**
     * Create a new {@link FightType}.
     *
     * @param animation  the animation this fight type holds.
     * @param trainType  the train type this fight type holds.
     * @param parentId   the parent config id.
     * @param childId    the child config id.
     * @param bonusType  the bonus type.
     * @param fightStyle the fighting style.
     */
    private FightType(int animation, int parentId, int childId, int bonusType,
                      FightStyle style) {
        this.animation = animation;
        this.parentId = parentId;
        this.childId = childId;
        this.bonusType = bonusType;
        this.style = style;
        this.attackSound = Sound.WEAPON;
    }

    private FightType(int animation, int parentId, int childId, int bonusType,
                      FightStyle style, Sound sound) {
        this.animation = animation;
        this.parentId = parentId;
        this.childId = childId;
        this.bonusType = bonusType;
        this.style = style;
        this.attackSound = sound;
    }

    /**
     * Gets the animation this fight type holds.
     *
     * @return the animation.
     */
    public int getAnimation() {
        return animation;
    }

    /**
     * Gets the parent config id.
     *
     * @return the parent id.
     */
    public int getParentId() {
        return parentId;
    }

    /**
     * Gets the child config id.
     *
     * @return the child id.
     */
    public int getChildId() {
        return childId;
    }

    /**
     * Gets the bonus type.
     *
     * @return the bonus type.
     */
    public int getBonusType() {
        return bonusType;
    }

    /**
     * Gets the fighting style.
     *
     * @return the fighting style.
     */
    public FightStyle getStyle() {
        return style;
    }

    /**
     * Determines the corresponding bonus for this fight type.
     *
     * @return the corresponding bonus for this fight type.
     */
    public int getCorrespondingBonus() {
        switch (bonusType) {
            case BonusManager.ATTACK_CRUSH:
                return BonusManager.DEFENCE_CRUSH;
            case BonusManager.ATTACK_MAGIC:
                return BonusManager.DEFENCE_MAGIC;
            case BonusManager.ATTACK_RANGE:
                return BonusManager.DEFENCE_RANGE;
            case BonusManager.ATTACK_SLASH:
                return BonusManager.DEFENCE_SLASH;
            case BonusManager.ATTACK_STAB:
                return BonusManager.DEFENCE_STAB;
            default:
                return BonusManager.DEFENCE_CRUSH;
        }
    }

    public Sound getAttackSound() {
        return attackSound;
    }
}