package com.elvarg.game.system;

import com.elvarg.game.entity.impl.npc.NPC;
import com.elvarg.game.entity.impl.player.Player;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class NPCInteractionSystem {

    private static Map<Integer, NPCInteraction> NPC_INTERACT_MAP;

    public static void init(List<? extends Class<?>> interactClasses) {
        NPC_INTERACT_MAP = interactClasses.stream()
                .filter(clazz -> NPCInteraction.class.isAssignableFrom(clazz))
                .flatMap(clazz -> {
                    try {
                        var instance = (NPCInteraction) clazz.getDeclaredConstructor().newInstance();
                        return Arrays.stream(clazz.getAnnotation(InteractIds.class).value())
                                .mapToObj(id -> new ImmutablePair<>(id, instance));

                    } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                        throw new RuntimeException(e);
                    }
                }).collect(Collectors.toMap(ImmutablePair::getLeft, ImmutablePair::getRight));
    }

    public static void handleFirstOption(Player player, NPC npc) {
        NPCInteraction npcInteraction = NPC_INTERACT_MAP.get(npc.getId());
        if (npcInteraction == null) {
            return;
        }

        npcInteraction.firstOptionClick(player, npc);
    }

    public static void handleSecondOption(Player player, NPC npc) {
        NPCInteraction npcInteraction = NPC_INTERACT_MAP.get(npc.getId());
        if (npcInteraction == null) {
            return;
        }

        npcInteraction.secondOptionClick(player, npc);
    }

    public static void handleThirdOption(Player player, NPC npc) {
        NPCInteraction npcInteraction = NPC_INTERACT_MAP.get(npc.getId());
        if (npcInteraction == null) {
            return;
        }

        npcInteraction.thirdOptionClick(player, npc);
    }

    public static void handleForthOption(Player player, NPC npc) {
        NPCInteraction npcInteraction = NPC_INTERACT_MAP.get(npc.getId());
        if (npcInteraction == null) {
            return;
        }

        npcInteraction.forthOptionClick(player, npc);
    }

}
