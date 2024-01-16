package com.github.naton1.rl.env;

import com.elvarg.game.content.combat.hit.HitDamage;
import com.elvarg.game.content.combat.hit.PendingHit;
import com.elvarg.game.entity.impl.player.Player;
import java.util.List;

public interface AgentEnvironment {

    Player getAgent();

    Player getTarget();

    void processAction(List<Integer> action);

    List<Number> getObs();

    List<List<Boolean>> getActionMasks();

    // Called whenever a hit is calculated for either the agent or target
    default void onHitCalculated(PendingHit pendingHit) {}

    // Called whenever a hit is applied to a target (not a traditional attack), ex. poison
    default void onHitApplied(HitDamage hit) {}

    // Called before action is processed for this environment
    default void onTickStart() {}

    // Called when all ticks have been processed for all players
    default void onTickProcessed() {}

    // Called after obs/masks have been used for this env, for tick cleanup
    default void onTickEnd() {}
}
