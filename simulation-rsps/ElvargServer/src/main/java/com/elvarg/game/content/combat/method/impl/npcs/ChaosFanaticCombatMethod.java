package com.elvarg.game.content.combat.method.impl.npcs;

import java.util.ArrayList;
import java.util.List;

import com.elvarg.game.content.combat.CombatType;
import com.elvarg.game.content.combat.hit.HitDamage;
import com.elvarg.game.content.combat.hit.HitMask;
import com.elvarg.game.content.combat.hit.PendingHit;
import com.elvarg.game.content.combat.method.CombatMethod;
import com.elvarg.game.entity.impl.Mobile;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.Animation;
import com.elvarg.game.model.Graphic;
import com.elvarg.game.model.GraphicHeight;
import com.elvarg.game.model.Location;
import com.elvarg.game.model.Projectile;
import com.elvarg.game.task.Task;
import com.elvarg.game.task.TaskManager;
import com.elvarg.util.Misc;
import com.elvarg.util.timers.TimerKey;

public class ChaosFanaticCombatMethod extends CombatMethod {

    private static final String[] QUOTES = { "Burn!", "WEUGH!", "Develish Oxen Roll!",
            "All your wilderness are belong to them!", "AhehHeheuhHhahueHuUEehEahAH",
            "I shall call him squidgy and he shall be my squidgy!", };

    private static enum Attack {
        SPECIAL_ATTACK, DEFAULT_MAGIC_ATTACK;
    }

    private Attack attack = Attack.DEFAULT_MAGIC_ATTACK;
    private static final Graphic ATTACK_END_GFX = new Graphic(305, GraphicHeight.HIGH);
    private static final Graphic EXPLOSION_END_GFX = new Graphic(157, GraphicHeight.MIDDLE);
    private static final Animation MAGIC_ATTACK_ANIM = new Animation(811);
    private static final Projectile EXPLOSION_PROJECTILE = new Projectile(551, 31, 43, 40, 80);
    private static final Projectile MAGIC_PROJECTILE = new Projectile(554, 31, 43, 62, 80);
    
    @Override
    public PendingHit[] hits(Mobile character, Mobile target) {
        if (attack == Attack.SPECIAL_ATTACK) {
            return null;
        }
        return new PendingHit[] { new PendingHit(character, target, this, 2) };
    }

    @Override
    public void start(Mobile character, Mobile target) {
        if (!character.isNpc() || !target.isPlayer())
            return;

        character.performAnimation(MAGIC_ATTACK_ANIM);

        attack = Attack.DEFAULT_MAGIC_ATTACK;

        if (Misc.getRandom(9) < 3) {
            attack = Attack.SPECIAL_ATTACK;
        }

        character.forceChat(QUOTES[Misc.getRandom(QUOTES.length - 1)]);

        if (attack == Attack.DEFAULT_MAGIC_ATTACK) {
            Projectile.sendProjectile(character, target, MAGIC_PROJECTILE);
            if (Misc.getRandom(1) == 0) {
                TaskManager.submit(new Task(3, target, false) {
                    @Override
                    public void execute() {
                        target.performGraphic(ATTACK_END_GFX);
                        stop();
                    }
                });
            }
        } else if (attack == Attack.SPECIAL_ATTACK) {
            Location targetPos = target.getLocation();
            List<Location> attackPositions = new ArrayList<>();
            attackPositions.add(targetPos);
            for (int i = 0; i < 3; i++) {
                attackPositions.add(new Location((targetPos.getX() - 1) + Misc.getRandom(3),
                        (targetPos.getY() - 1) + Misc.getRandom(3)));
            }
            for (Location pos : attackPositions) {
                Projectile.sendProjectile(character, pos, EXPLOSION_PROJECTILE);
            }
            TaskManager.submit(new Task(4) {
                @Override
                public void execute() {
                    for (Location pos : attackPositions) {
                        target.getAsPlayer().getPacketSender().sendGlobalGraphic(EXPLOSION_END_GFX, pos);
                        for (Player player : character.getAsNpc().getPlayersWithinDistance(10)) {
                            if (player.getLocation().equals(pos)) {
                                player.getCombat().getHitQueue()
                                        .addPendingDamage(new HitDamage(Misc.getRandom(25), HitMask.RED));
                            }
                        }
                    }
                    finished(character, target);
                    stop();
                }
            });
            character.getTimers().register(TimerKey.COMBAT_ATTACK, 5);
        }
    }

    @Override
    public int attackDistance(Mobile character) {
        return 8;
    }

    @Override
    public void finished(Mobile character, Mobile target) {
        if (Misc.getRandom(10) == 1) {
            if (target.isPlayer()) {
                ChaosElementalCombatMethod.disarmAttack(target.getAsPlayer());
            }
        }
    }

    @Override
    public CombatType type() {
        return CombatType.MAGIC;
    }
}
