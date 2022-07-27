package com.elvarg.util.chainedwork;

import com.elvarg.game.entity.impl.Mobile;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.Location;
import com.elvarg.game.task.Task;
import com.elvarg.game.task.TaskManager;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * A utlity class to chain code functions together which depend on delays like OSS's {@code delay(1)}.
 * The internal clock/delay system is built upon {@link TaskManager}
 *
 * <br>Has similiar fields to {@link Action} but doesnt have Mob as fixed parent generic type
 *
 * @author Jak | Shadowrs
 * @version 25/4/2020
 */
public class Chain<T> {

    public static boolean DEBUG_CHAIN = false;

    /**
     * A key assocated with this chain, any type is accepted.
     */
    @Nullable
    public T owner;
    /**
     * The name of this task, optional
     */
    @Nullable
    public String name;

    /**
     * the next chain to execute one this one completes.
     */
    @Nullable
    public Chain<T> nextNode;

    /**
     * Evaluated in {@link Task#execute()}, will stop and NOT run the {@link #work} when true.
     */
    @Nullable
    public BooleanSupplier cancelCondition;

    /**
     * Evaluated in {@link Task#execute()}, will only run the {@link #work} when true.
     */
    @Nullable
    public BooleanSupplier executeCondition;

    /**
     * The actual running task this chain represents.
     */
    @Nullable
    public Task task;

    /**
     * Same as {@link Task#delay}
     */
    public int cycleDelay = 1;

    /**
     * The function to run inside {@link Task#execute()}
     * <br>types allowed = {@link Runnable}, Consumer{@link Consumer<Task>}
     */
    @Nullable
    public Object work;

    /**
     * If this task will repeat. Normally this task chain will execute ONCE and stop straight away.
     */
    public boolean repeats = false;

    /**
     * When interrupted, when {@link Task#onStop()} runs it won't call the after hook.
     */
    public boolean interrupted = false;

    public List<StackWalker.StackFrame> fromLocation;

    //public StackTraceElement fromLocation;
    public static <T> Chain<T> bound(T owner) {
        Chain<T> chain = new Chain<>();
        chain.owner = owner;
        if (owner instanceof Mobile) {
            ((Mobile)owner).chains.add(chain);
        }
        chain.findSource();
        return chain;
    }

    private void findSource() {
        fromLocation = StackWalker.getInstance().walk(s -> s.dropWhile(e -> e.getClassName().toLowerCase().contains("chain"))
                .limit(4)
                .collect(Collectors.toList()));
        if (DEBUG_CHAIN) {
            System.out.println("source location: "+Arrays.toString(fromLocation.toArray()));
        }
    }

    public String source() {
        return Arrays.toString(fromLocation.toArray());
    }

    public Chain<T> name(String name) {
        this.name = name;
        return this;
    }

    /**
     * these are predicates which indicate a state in which the chain should break. Example, if an NPC is dead/despawned
     * @param predicates
     */
    public final Chain<T> cancelWhen(BooleanSupplier predicates) {
        cancelCondition = predicates;
        return this;
    }

    /**
     * repeats forever every 1 tick. Only stops when CONDITION evaluates to true or interrupt is called.
     * @param tile
     * @param work
     * @return
     */
    public Chain<T> waitForLocation(Location tile, Runnable work) {
        if (this.work != null) {
            nextNode = bound(owner); // make a new one
            nextNode.work = work; // init work
            nextNode.name = name; // re-use the name
            nextNode.executeCondition = () -> ((Player)owner).getLocation().getX() == tile.getX() && ((Player)owner).getLocation().getY() == tile.getY();
            nextNode.cycleDelay = 1;
            nextNode.repeats = true;
            nextNode.findSource();
            return nextNode;
        }
        executeCondition = () -> ((Mobile)owner).getLocation().getX() == tile.getX() && ((Mobile)owner).getLocation().getY() == tile.getY();
        cycleDelay = 1;
        this.work = work;
        repeats = true;
        startChainExecution();
        return this;
    }

    /**
     * repeats forever every tickBetweenLoop ticks. Only stops when CONDITION evaluates to true or interrupt is called.
     * @param tickBetweenLoop
     * @param condition
     * @param work
     * @return
     */
    public Chain<T> waitUntil(int tickBetweenLoop, BooleanSupplier condition, Runnable work) {
        if (this.work != null) {
            nextNode = bound(owner); // make a new one
            nextNode.work = work; // init work
            nextNode.name = name; // re-use the name
            nextNode.executeCondition = condition;
            nextNode.cycleDelay = tickBetweenLoop;
            nextNode.repeats = true;
            nextNode.findSource();
            return nextNode;
        }
        executeCondition = condition;
        cycleDelay = tickBetweenLoop;
        this.work = work;
        repeats = true;
        startChainExecution();
        return this;
    }

    /**
     * repeats forever every tickBetweenLoop ticks. Must be stopped MANUALLY by calling task.stop from the consumer.
     * <br> Check usages for examples.
     * @param tickBetweenLoop
     * @param work
     * @return
     */
    public Chain<T> repeatingTask(int tickBetweenLoop, Consumer<Task> work) {
        if (this.work != null) {
            nextNode = bound(owner); // make a new one
            nextNode.work = work; // init work
            nextNode.name = name; // re-use the name
            nextNode.cycleDelay = tickBetweenLoop;
            nextNode.repeats = true;
            nextNode.findSource();
            return nextNode;
        }
        this.work = work;
        cycleDelay = tickBetweenLoop;
        repeats = true;
        startChainExecution();
        return this;
    }

    /**
     * no context/owner task.
     * @param startAfterTicks
     * @param work
     * @return
     */
    public static Chain runGlobal(int startAfterTicks, Runnable work) {
        return bound(null).runFn(startAfterTicks, work);
    }

    /**
     * The first function to run, kicks off the internal {@link Task} via {@link TaskManager}. Only runs ONCE.
     */
    public Chain<T> runFn(int startAfterTicks, Runnable work) {
        if (this.work != null) {
            return then(startAfterTicks, work);
        }
        cycleDelay = startAfterTicks;
        this.work = work;
        startChainExecution();
        return this;

    }

    // this is private on purpose, internal class use only
    private void startChainExecution() {
        if (cycleDelay == 0) {
            // run instantly
            attemptWork();
        } else {
            task = new Task(name != null ? name : "", cycleDelay, false) {
                @Override
                protected void execute() {
                    attemptWork();
                    if (!repeats)
                        stop();
                }

                @Override
                public void onStop() {
                    if (interrupted) {
                        return;
                    }
                    super.onStop();
                    if (nextNode != null) {
                        nextNode.startChainExecution();
                    }
                }
            }.bind(owner);
            // just cloning exists fromLocation which should filter properly already
            task.codeOrigin = Arrays.toString(fromLocation.stream().map(s1 -> s1.toString())
                    .map(s2 -> {
                        // kotlin.KtCommands$init$26.invoke(KtCommands.kt:293)
                        // .setTimer(Poison.java:54)
                        int dotfile = s2.lastIndexOf(".");
                        int endfile = s2.substring(0, dotfile).lastIndexOf(".");
                        int startfile = s2.substring(0, endfile).lastIndexOf(".");
                        return s2.substring(startfile+1);
                    }).toArray());
            TaskManager.submit(task);
        }
    }

    public void __TESTING_ONLY_doWork() {
        attemptWork();
    }

    private void attemptWork() {
        if (interrupted)
            return;
        if (cancelCondition != null && cancelCondition.getAsBoolean()) {
            if (DEBUG_CHAIN) {
                System.out.println("[DEBUG_CHAIN] Cancel condition was True, stopping work for "+owner);
            }
            repeats = false; // condition to cancel was true, stop looping
            return;
        }
        if (executeCondition != null) {
            if (!executeCondition.getAsBoolean()) {
                if (DEBUG_CHAIN) {
                    System.out.println("[DEBUG_CHAIN] execution condition false. Won't run for " + owner);
                }
                return;
            }
            repeats = false; // condition to execute the task (aka stop looping) is true
        }
        if (DEBUG_CHAIN) {
            System.out.println("Running " + fromLocation + " task for " + owner);
        }
        if (work != null) {
            if (work instanceof Runnable)
                ((Runnable)work).run();
            else if (work instanceof Consumer)
                ((Consumer<Task>)work).accept(task);
            else {
                System.err.println("Unknown workload type: "+work.getClass());
            }
        }
    }

    /**
     * Adds a function which is run immidiately after the previous chain completes
     */
    public Chain<T> then(Runnable nextWork) {
        if (this.work == null) {
            return runFn(1, nextWork);
        }
        nextNode = bound(owner); // make a new one
        nextNode.work = nextWork; // init work
        nextNode.name = name; // re-use the name
        nextNode.findSource();
        return nextNode;
    }

    /**
     * Adds a function which will execute X ticks after the previous work completes.
     */
    public Chain<T> then(int startDelay, Runnable nextWork) {
        if (this.work == null) {
            return runFn(startDelay, nextWork);
        }
        nextNode = bound(owner); // make a new one
        nextNode.work = nextWork; // init work
        nextNode.name = name; // re-use the name
        nextNode.cycleDelay = startDelay;
        nextNode.findSource();
        return nextNode;
    }

    /**
     * see {@link Chain#repeatingTask(int, Consumer)}.
     * @param tickBetweenLoop
     * @param condition The condition TRUE when the task will stop/complete. Runs forever until true.
     * @return
     */
    public Chain<T> repeatIf(int tickBetweenLoop, BooleanSupplier condition/* Runnable WORK is integrated into CONDITION*/) {
        if (this.work == null) {
            work = null; // SEE CONDITION - condition IS the workload! intrgrated into one method for execute+evaluate
            name = name; // re-use the name
            cancelCondition = condition; // NOTE : this is actually a 2 in 1 version of work.
            // cancel condition will evaluate and itself is the Runnable Work.
            cycleDelay = tickBetweenLoop;
            repeats = true;
            findSource();
            return this;
        }
        nextNode = bound(owner); // make a new one
        nextNode.work = null; // SEE CONDITION - condition IS the workload! intrgrated into one method for execute+evaluate
        nextNode.name = name; // re-use the name
        nextNode.cancelCondition = condition; // NOTE : this is actually a 2 in 1 version of work.
        // cancel condition will evaluate and itself is the Runnable Work.
        nextNode.cycleDelay = tickBetweenLoop;
        nextNode.repeats = true;
        nextNode.findSource();
        return nextNode;
    }

}
