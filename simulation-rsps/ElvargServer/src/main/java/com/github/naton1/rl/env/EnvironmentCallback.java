package com.github.naton1.rl.env;

import com.elvarg.game.content.Food;
import com.elvarg.game.content.skill.skillable.impl.Herblore;
import com.elvarg.game.entity.impl.player.Player;

public interface EnvironmentCallback {

    /**
     * Called before the agent eats food. The agent must tell the environment when it is eating
     * food.
     *
     * <p>Primarily used to track wasted food.
     *
     * @param agent the agent
     * @param food the food being eaten
     */
    void onEat(Player agent, Food.Edible food);

    /**
     * Called before the agent drinks a potion. The agent must tell the environment when it is
     * drinking a potion.
     *
     * <p>Primarily used to track wasted potions.
     *
     * @param agent the agent
     * @param potionDose the potion being drank
     */
    void onDrink(Player agent, Herblore.PotionDose potionDose);
}
