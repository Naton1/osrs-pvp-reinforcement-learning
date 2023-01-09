package com.elvarg.game.entity.impl.npc.impl;

import com.elvarg.game.content.combat.method.CombatMethod;
import com.elvarg.game.content.combat.method.impl.npcs.BanditCombtMethod;
import com.elvarg.game.entity.impl.npc.NPC;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.Ids;
import com.elvarg.game.model.Location;
import com.elvarg.game.model.container.impl.Equipment;

import static com.elvarg.util.NpcIdentifiers.*;

@Ids({BANDIT})
public class Bandit extends NPC {

    private static final CombatMethod COMBAT_METHOD = new BanditCombtMethod();

    /**
     * Constructs a new npc.
     *
     * @param id       The npc id.
     * @param position
     */
    public Bandit(int id, Location position) {
        super(id, position);
    }

    @Override
    public boolean isAggressiveTo(Player player) {
        // Bandits are only aggressive towards players who have god affiliated items
        int saradominItemCount = Equipment.getItemCount(player, "Saradomin", true);
        int zamorakItemCount = Equipment.getItemCount(player, "Zamorak", true);

        return saradominItemCount > 0 || zamorakItemCount > 0;
    }

    @Override
    public CombatMethod getCombatMethod() {
        return COMBAT_METHOD;
    }

}
