package com.github.naton1.rl.util;

import com.elvarg.game.content.Food;
import com.elvarg.game.content.skill.skillable.impl.Herblore;
import com.elvarg.game.entity.impl.player.Player;
import com.github.naton1.rl.env.EnvironmentCallback;

public class NoOpEnvironmentCallback implements EnvironmentCallback {

    @Override
    public void onEat(final Player agent, final Food.Edible food) {}

    @Override
    public void onDrink(final Player agent, final Herblore.PotionDose potionDose) {}
}
