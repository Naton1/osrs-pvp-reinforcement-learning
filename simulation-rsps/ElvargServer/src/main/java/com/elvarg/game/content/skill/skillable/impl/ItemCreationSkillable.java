package com.elvarg.game.content.skill.skillable.impl;

import com.elvarg.game.content.PetHandler;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.AnimationLoop;
import com.elvarg.game.model.Item;
import com.elvarg.game.model.RequiredItem;
import com.elvarg.game.model.Skill;
import com.elvarg.game.task.Task;
import com.elvarg.game.task.TaskManager;
import com.elvarg.util.Misc;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * An implementation of {@link DefaultSkillable}.
 * <p>
 * This sub class handles the creation of an item.
 * It's used by many skills such as Fletching.
 *
 * @author Professor Oak
 */
public class ItemCreationSkillable extends DefaultSkillable {

    /**
     * A {@link List} containing all the {@link RequiredItem}s.
     */
    private final List<RequiredItem> requiredItems;

    /**
     * The item we're making.
     */
    private final Item product;
    /**
     * The {@link AnimationLoop} the player will perform whilst performing this
     * skillable.
     */
    private final Optional<AnimationLoop> animLoop;
    /**
     * The level required to make this item.
     */
    private final int requiredLevel;
    /**
     * The experience a player will receive in the said skill for making this item.
     */
    private final int experience;
    /**
     * The skill to reward the player experience in.
     */
    private final Skill skill;
    /**
     * The amount to make.
     */
    private int amount;

    public ItemCreationSkillable(List<RequiredItem> requiredItems, Item product, int amount,
            Optional<AnimationLoop> animLoop, int requiredLevel, int experience, Skill skill) {
        this.requiredItems = requiredItems;
        this.product = product;
        this.amount = amount;
        this.animLoop = animLoop;
        this.requiredLevel = requiredLevel;
        this.experience = experience;
        this.skill = skill;
    }

    @Override
    public void startAnimationLoop(Player player) {
        if (!animLoop.isPresent()) {
            return;
        }
        Task animLoopTask = new Task(animLoop.get().getLoopDelay(), player, true) {
            @Override
            protected void execute() {
                player.performAnimation(animLoop.get().getAnim());
            }
        };
        TaskManager.submit(animLoopTask);
        getTasks().add(animLoopTask);
    }

    @Override
    public int cyclesRequired(Player player) {
        return 2;
    }

    @Override
    public void onCycle(Player player) {
        PetHandler.onSkill(player, skill);
    }

    @Override
    public void finishedCycle(Player player) {
        // Decrement amount to make and stop if we hit 0.
        if (amount-- <= 0) {
            cancel(player);
        }

        // Delete items required..
        filterRequiredItems(r -> r.isDelete()).forEach(r -> player.getInventory().delete(r.getItem()));

        // Add product..
        player.getInventory().add(product);

        // Add exp..
        player.getSkillManager().addExperience(skill, experience);

        // Send message..
        String name = product.getDefinition().getName();
        String amountPrefix = Misc.anOrA(name);
        if (product.getAmount() > 1) {
            if (!name.endsWith("s")) {
                name += "s";
            }
            amountPrefix = Integer.toString(product.getAmount());
        }

        player.getPacketSender().sendMessage("You make " + amountPrefix + " " + name + ".");
    }

    @Override
    public boolean hasRequirements(Player player) {
        // Validate amount..
        if (amount <= 0) {
            return false;
        }

        // Check if we have required stringing level..
        if (player.getSkillManager().getCurrentLevel(skill) < requiredLevel) {
            player.getPacketSender().sendMessage("You need a " + skill.getName() + " level of at least "
                    + Integer.toString(requiredLevel) + " to do this.");
            return false;
        }

        // Validate required items..
        // Check if we have the required ores..
        boolean hasItems = true;
        for (RequiredItem item : requiredItems) {
            if (!player.getInventory().contains(item.getItem())) {
                String prefix = item.getItem().getAmount() > 1 ? Integer.toString(item.getItem().getAmount()) : "some";
                player.getPacketSender().sendMessage("You " + (!hasItems ? "also need" : "need") + " " + prefix + " "
                        + item.getItem().getDefinition().getName() + ".");
                hasItems = false;
            }
        }
        if (!hasItems) {
            return false;
        }

        return super.hasRequirements(player);
    }

    @Override
    public boolean loopRequirements() {
        return true;
    }

    @Override
    public boolean allowFullInventory() {
        return true;
    }

    public void decrementAmount() {
        amount--;
    }

    public int getAmount() {
        return amount;
    }

    public List<RequiredItem> filterRequiredItems(Predicate<RequiredItem> criteria) {
        return requiredItems.stream().filter(criteria).collect(Collectors.<RequiredItem>toList());
    }

    public List<RequiredItem> getRequiredItems() {
        return requiredItems;
    }
}
