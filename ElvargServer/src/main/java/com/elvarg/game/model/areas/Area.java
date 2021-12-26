package com.elvarg.game.model.areas;

import com.elvarg.game.entity.impl.Mobile;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.Boundary;

import java.util.List;
import java.util.Optional;

public abstract class Area {

    private final List<Boundary> boundaries;

    public Area(List<Boundary> boundaries) {
        this.boundaries = boundaries;
    }

    public abstract void enter(Mobile character);

    public abstract void leave(Mobile character, boolean logout);

    public abstract void process(Mobile character);

    public abstract boolean canTeleport(Player player);

    public abstract boolean canAttack(Mobile attacker, Mobile target);

    public abstract void defeated(Player player, Mobile character);

    public abstract boolean canTrade(Player player, Player target);

    public abstract boolean isMulti(Mobile character);

    public abstract boolean canEat(Player player, int itemId);

    public abstract boolean canDrink(Player player, int itemId);

    public abstract boolean dropItemsOnDeath(Player player, Optional<Player> killer);

    public abstract boolean handleDeath(Player player, Optional<Player> killer);

    public abstract void onPlayerRightClick(Player player, Player rightClicked, int option);

    public abstract boolean handleObjectClick(Player player, int objectId, int type);
    
    public abstract boolean overridesNpcAggressionTolerance(Player player, int npcId);

    public List<Boundary> getBoundaries() {
        return boundaries;
    }
}
