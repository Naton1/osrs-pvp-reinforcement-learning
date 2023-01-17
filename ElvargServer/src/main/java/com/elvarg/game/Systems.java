package com.elvarg.game;

import com.elvarg.game.entity.impl.npc.NPC;
import com.elvarg.game.entity.impl.npc.NPCInteraction;
import com.elvarg.game.entity.impl.npc.NPCInteractionSystem;
import com.elvarg.game.model.Ids;
import com.google.common.reflect.ClassPath;

import java.io.IOException;
import java.util.stream.Collectors;

public class Systems {

    public static void init() throws IOException {

        // Firstly, gather all the classes inside the npc.impl package
        var npcOverrideClasses = ClassPath.from(ClassLoader.getSystemClassLoader())
                .getAllClasses()
                .stream()
                .filter(clazz -> clazz.getPackageName().startsWith("com.elvarg.game.entity.impl.npc.impl"))
                .map(clazz -> clazz.load());

        // Filter all classes which have @Ids annotation defined on them
        var npcClasses = npcOverrideClasses
                .filter(clazz -> clazz.getAnnotation(Ids.class) != null)
                .collect(Collectors.toList());

        // Filter all classes which extend NPC
        var implementationClasses = npcClasses.stream().filter(NPC.class::isAssignableFrom).toList();
        NPC.initImplementations(implementationClasses);
    }
}

