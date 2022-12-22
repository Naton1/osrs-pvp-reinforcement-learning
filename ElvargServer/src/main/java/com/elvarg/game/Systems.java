package com.elvarg.game;

import com.elvarg.game.entity.impl.npc.NPCInteractionSystem;
import com.elvarg.game.model.InteractIds;
import com.google.common.reflect.ClassPath;

import java.io.IOException;
import java.util.stream.Collectors;

public class Systems {

    public static void init() throws IOException {

        var interactClasses = ClassPath.from(ClassLoader.getSystemClassLoader())
                .getAllClasses()
                .stream()
                .filter(clazz -> clazz.getPackageName()
                        .startsWith("com.elvarg.game.entity.impl.npc.impl"))
                .map(clazz -> clazz.load())
                .filter(clazz -> clazz.getAnnotation(InteractIds.class) != null)
                .collect(Collectors.toList());

        NPCInteractionSystem.init(interactClasses);

    }
}

