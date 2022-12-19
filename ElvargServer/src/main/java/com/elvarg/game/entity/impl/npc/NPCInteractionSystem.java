package com.elvarg.game.entity.impl.npc;

import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.InteractIds;
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

    public static boolean handleFirstOption(Player player, NPC npc) {
        NPCInteraction npcInteraction = NPC_INTERACT_MAP.get(npc.getId());
        if (npcInteraction == null) {
            return false;
        }

        npcInteraction.firstOptionClick(player, npc);
        return true;
    }

    public static boolean handleSecondOption(Player player, NPC npc) {
        NPCInteraction npcInteraction = NPC_INTERACT_MAP.get(npc.getId());
        if (npcInteraction == null) {
            return false;
        }

        npcInteraction.secondOptionClick(player, npc);
        return true;
    }

    public static boolean handleThirdOption(Player player, NPC npc) {
        NPCInteraction npcInteraction = NPC_INTERACT_MAP.get(npc.getId());
        if (npcInteraction == null) {
            return false;
        }

        npcInteraction.thirdOptionClick(player, npc);
        return true;
    }

    public static boolean handleForthOption(Player player, NPC npc) {
        NPCInteraction npcInteraction = NPC_INTERACT_MAP.get(npc.getId());
        if (npcInteraction == null) {
            return false;
        }

        npcInteraction.forthOptionClick(player, npc);
        return true;
    }

    public static boolean handleUseItem(Player player, NPC npc, int itemId, int slot) {
        NPCInteraction npcInteraction = NPC_INTERACT_MAP.get(npc.getId());
        if (npcInteraction == null) {
            return false;
        }

        npcInteraction.useItemOnNpc(player, npc, itemId, slot);
        return true;
    }

}
