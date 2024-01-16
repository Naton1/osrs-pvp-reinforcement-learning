package com.elvarg.game.model.dialogues;

/**
 * The enumerated type whose elements represent the expressions a character can
 * take on.
 *
 * @author lare96 <http://github.com/lare96>
 */
public enum DialogueExpression {
    HAPPY(588),
    CALM(589),
    CALM_CONTINUED(590),
    DEFAULT(591),
    EVIL(592),
    EVIL_CONTINUED(593),
    DELIGHTED_EVIL(594),
    ANNOYED(595),
    DISTRESSED(596),
    DISTRESSED_CONTINUED(597),
    DISORIENTED_LEFT(600),
    DISORIENTED_RIGHT(601),
    UNINTERESTED(602),
    SLEEPY(603),
    PLAIN_EVIL(604),
    LAUGHING(605),
    LAUGHING_2(608),
    LONGER_LAUGHING(606),
    LONGER_LAUGHING_2(607),
    EVIL_LAUGH_SHORT(609),
    SLIGHTLY_SAD(610),
    SAD(599),
    VERY_SAD(611),
    OTHER(612),
    NEAR_TEARS(598),
    NEAR_TEARS_2(613),
    ANGRY_1(614),
    ANGRY_2(615),
    ANGRY_3(616),
    ANGRY_4(617);

    /**
     * The identification for this expression.
     */
    private final int expression;

    /**
     * Creates a new {@link DialogueExpression}.
     *
     * @param expression
     *            the identification for this expression.
     */
    private DialogueExpression(int expression) {
        this.expression = expression;
    }

    /**
     * Gets the identification for this expression.
     *
     * @return the expression.
     */
    public final int getExpression() {
        return expression;
    }
}

