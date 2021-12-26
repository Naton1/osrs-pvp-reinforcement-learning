package com.elvarg.game.content.combat;

import com.elvarg.game.model.Skill;

/**
 * A collection of constants that each represent a different fighting style.
 *
 * @author lare96
 */
public enum FightStyle {
    ACCURATE() {
        @Override
        public int[] skill(CombatType type) {
            return type == CombatType.RANGED ? new int[]{Skill.RANGED.ordinal()}
                    : new int[]{Skill.ATTACK.ordinal()};
        }
    },
    AGGRESSIVE() {
        @Override
        public int[] skill(CombatType type) {
            return type == CombatType.RANGED ? new int[]{Skill.RANGED.ordinal()}
                    : new int[]{Skill.STRENGTH.ordinal()};
        }
    },
    DEFENSIVE() {
        @Override
        public int[] skill(CombatType type) {
            return type == CombatType.RANGED ? new int[]{Skill.RANGED.ordinal(),
                    Skill.DEFENCE.ordinal()} : new int[]{Skill.DEFENCE.ordinal()};
        }
    },
    CONTROLLED() {
        @Override
        public int[] skill(CombatType type) {
            return new int[]{Skill.ATTACK.ordinal(), Skill.STRENGTH.ordinal(), Skill.DEFENCE.ordinal()};
        }
    };

    /**
     * Determines the Skill trained by this fighting style based on the
     * {@link CombatType}.
     *
     * @param type the combat type to determine the Skill trained with.
     * @return the Skill trained by this fighting style.
     */
    public abstract int[] skill(CombatType type);
}