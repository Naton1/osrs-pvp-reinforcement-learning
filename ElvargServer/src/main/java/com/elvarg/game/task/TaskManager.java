package com.elvarg.game.task;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public final class TaskManager {

    private final static Queue<Task> pendingTasks = new LinkedList<>();

    private final static List<Task> activeTasks = new LinkedList<>();

    private TaskManager() {
        throw new UnsupportedOperationException(
                "This class cannot be instantiated!");
    }

    public static void process() {
        try {
            Task t;
            while ((t = pendingTasks.poll()) != null) {
                if (t.isRunning()) {
                    activeTasks.add(t);
                }
            }

            Iterator<Task> it = activeTasks.iterator();

            while (it.hasNext()) {
                t = it.next();
                if (!t.tick())
                    it.remove();
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public static void submit(Task task) {
        if (task.isRunning()) {
            // Task is already running
            return;
        }

        task.setRunning(true);

        if (task.isImmediate()) {
            task.execute();
        }

        pendingTasks.add(task);
    }

    /**
     * Used to cancel multiple tasks at once.
     * @param keys
     */
    public static void cancelTasks(Object[] keys) {
        for (Object key: keys) {
            cancelTasks(key);
        }
    }

    public static void cancelTasks(Object key) {
        try {
            pendingTasks.stream().filter(t -> t.getKey().equals(key)).forEach(t -> t.stop());
            activeTasks.stream().filter(t -> t.getKey().equals(key)).forEach(t -> t.stop());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static int getTaskAmount() {
        return (pendingTasks.size() + activeTasks.size());
    }
}
