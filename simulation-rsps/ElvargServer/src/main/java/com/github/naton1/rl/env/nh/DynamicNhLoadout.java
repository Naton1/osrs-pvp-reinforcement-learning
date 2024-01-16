package com.github.naton1.rl.env.nh;

import static com.elvarg.util.ItemIdentifiers.ANGLERFISH;
import static com.elvarg.util.ItemIdentifiers.RANGING_POTION_4_;
import static com.elvarg.util.ItemIdentifiers.RUNE_POUCH;
import static com.elvarg.util.ItemIdentifiers.SARADOMIN_BREW_4_;
import static com.elvarg.util.ItemIdentifiers.SUPER_COMBAT_POTION_4_;
import static com.elvarg.util.ItemIdentifiers.SUPER_RESTORE_4_;

import com.elvarg.game.content.Food;
import com.elvarg.game.content.PrayerHandler;
import com.elvarg.game.content.skill.skillable.impl.Herblore;
import com.elvarg.game.definition.ItemDefinition;
import com.elvarg.game.model.EquipmentType;
import com.elvarg.game.model.Item;
import com.elvarg.game.model.MagicSpellbook;
import com.elvarg.game.model.container.impl.Equipment;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

public abstract class DynamicNhLoadout implements NhLoadout {

    private Item[] inventory;
    private Item[] equipment;

    @Override
    public Item[] getInventory() {
        if (inventory == null) {
            computeLoadout();
        }
        return inventory;
    }

    @Override
    public Item[] getEquipment() {
        if (equipment == null) {
            computeLoadout();
        }
        return equipment;
    }

    @Override
    public int[] getTankGear() {
        // Use range gear, and then staff if the staff gives better bonuses
        final int[] rangeGear = getRangedGear();
        final int[] mageGear = getMageGear();
        final ItemDefinition staff = Arrays.stream(mageGear)
                .mapToObj(ItemDefinition::forId)
                .filter(i -> i != ItemDefinition.DEFAULT)
                .filter(i -> i.getEquipmentType() == EquipmentType.WEAPON)
                .filter(i -> !i.isDoubleHanded())
                .findFirst()
                .orElse(null);
        if (staff != null) {
            final ItemDefinition rangedWeapon = Arrays.stream(rangeGear)
                    .mapToObj(ItemDefinition::forId)
                    .filter(i -> i != ItemDefinition.DEFAULT)
                    .filter(i -> i.getEquipmentType() == EquipmentType.WEAPON)
                    .findFirst()
                    .orElseThrow();
            // Average defense bonuses, and use staff if it's better
            final double rangeScore = (rangedWeapon.getBonuses()[6]
                            + rangedWeapon.getBonuses()[8]
                            + rangedWeapon.getBonuses()[9])
                    / 3D;
            final double staffScore = (staff.getBonuses()[6] + staff.getBonuses()[8] + staff.getBonuses()[9]) / 3D;
            if (staffScore > rangeScore) {
                final int[] newGear = Arrays.copyOf(rangeGear, rangeGear.length);
                for (int i = 0; i < newGear.length; i++) {
                    if (newGear[i] == rangedWeapon.getId()) {
                        newGear[i] = staff.getId();
                        break;
                    }
                }
                return newGear;
            }
        }
        return rangeGear;
    }

    private void computeLoadout() {
        final int[] mageGear = getMageGear();
        final List<int[]> remainingGearSetups = new ArrayList<>();
        final int[] startGear;
        if (mageGear.length == 0) {
            // Not using mage, wear range by default instead
            startGear = getRangedGear();
            Collections.addAll(remainingGearSetups, getMeleeGear(), getMeleeSpecGear());
        } else {
            // Using mage
            startGear = mageGear;
            Collections.addAll(remainingGearSetups, getRangedGear(), getMeleeGear(), getMeleeSpecGear());
        }

        this.equipment = Arrays.stream(startGear).mapToObj(Item::new).toArray(Item[]::new);
        for (Item equipped : this.equipment) {
            if (ItemDefinition.forId(equipped.getId()).isStackable()) {
                equipped.setAmount(10000);
            }
        }
        final List<Item> inventory = new ArrayList<>();
        final Set<Integer> addedItems = Arrays.stream(startGear).boxed().collect(Collectors.toCollection(HashSet::new));
        for (int[] equipmentSet : remainingGearSetups) {
            for (int i : equipmentSet) {
                if (!addedItems.contains(i)) {
                    final int quantity = ItemDefinition.forId(i).isStackable() ? 10000 : 1;
                    inventory.add(new Item(i, quantity));
                    addedItems.add(i);
                }
            }
        }
        inventory.addAll(
                Arrays.stream(getDefaultInventoryItems()).mapToObj(Item::new).toList());
        final int foodToAdd = 28 - inventory.size();
        for (int i = 0; i < foodToAdd; i++) {
            inventory.add(new Item(getFillItem()));
        }
        this.inventory = inventory.toArray(Item[]::new);
        if (this.inventory.length > 28) {
            throw new IllegalStateException("Generated inventory length: " + this.inventory.length);
        }
        Arrays.sort(this.inventory, Comparator.comparing(i -> {
            // Rune pouch last
            if (i.getId() == RUNE_POUCH) {
                return 10;
            }
            // Potions second to last
            for (Herblore.PotionDose potion : Herblore.PotionDose.values()) {
                if (potion.getDoseForID(i.getId()) > 0) {
                    return 9;
                }
            }
            // Food before potions
            for (Food.Edible food : Food.Edible.values()) {
                if (food.getItem().getId() == i.getId()) {
                    return 8;
                }
            }
            // Then everything else as it was
            return 0;
        }));
    }

    protected int[] getDefaultInventoryItems() {
        return new int[] {
            SUPER_COMBAT_POTION_4_,
            RANGING_POTION_4_,
            SUPER_RESTORE_4_,
            SUPER_RESTORE_4_,
            SUPER_RESTORE_4_,
            SARADOMIN_BREW_4_,
            SARADOMIN_BREW_4_,
            SARADOMIN_BREW_4_,
            SARADOMIN_BREW_4_,
            SARADOMIN_BREW_4_,
            RUNE_POUCH,
        };
    }

    protected int getFillItem() {
        return ANGLERFISH;
    }

    @Override
    public NhLoadout randomize(long seed) {
        final RandomizerContext randomizerContext = new RandomizerContext(
                Arrays.stream(getMageGear()).boxed().collect(Collectors.toList()),
                Arrays.stream(getMeleeGear()).boxed().collect(Collectors.toList()),
                Arrays.stream(getRangedGear()).boxed().collect(Collectors.toList()),
                Arrays.stream(getMeleeSpecGear()).boxed().collect(Collectors.toList()),
                Arrays.stream(getDefaultInventoryItems()).boxed().collect(Collectors.toList()),
                new Random(seed));
        applyRandomization(randomizerContext);
        Stream.of(
                        randomizerContext.getUpdatedMageItems(),
                        randomizerContext.getUpdatedMeleeItems(),
                        randomizerContext.getUpdatedRangedItems(),
                        randomizerContext.getUpdatedMeleeItems())
                .flatMapToInt(Arrays::stream)
                .forEach(id -> {
                    if (ItemDefinition.forId(id).getBonuses() == null) {
                        throw new IllegalStateException("Missing bonuses for " + id);
                    }
                });
        return new RandomizedDynamicNhLoadout(randomizerContext, this);
    }

    protected abstract void applyRandomization(RandomizerContext randomizerContext);

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    protected static class RandomizerContext {
        private final List<Integer> mageItems;
        private final List<Integer> meleeItems;
        private final List<Integer> rangeItems;
        private final List<Integer> specItems;
        private final List<Integer> defaultInventoryItems;

        @Getter
        @Setter
        private Integer fillItemOverride;

        @Getter
        @Setter
        private MagicSpellbook spellbookOverride;

        @Getter
        private final Random random;

        public void disableMage() {
            mageItems.clear();
        }

        public int[] getUpdatedMageItems() {
            return mageItems.stream().mapToInt(Integer::intValue).toArray();
        }

        public int[] getUpdatedMeleeItems() {
            return meleeItems.stream().mapToInt(Integer::intValue).toArray();
        }

        public int[] getUpdatedRangedItems() {
            return rangeItems.stream().mapToInt(Integer::intValue).toArray();
        }

        public int[] getUpdatedSpecItems() {
            return specItems.stream().mapToInt(Integer::intValue).toArray();
        }

        public int[] getUpdatedDefaultInventoryItems() {
            return defaultInventoryItems.stream().mapToInt(Integer::intValue).toArray();
        }

        public void swapRange(int id, int... newChoices) {
            swap(List.of(rangeItems), id, newChoices);
        }

        public void swapMage(int id, int... newChoices) {
            swap(List.of(mageItems), id, newChoices);
        }

        public void swapMelee(int id, int... newChoices) {
            swap(List.of(meleeItems), id, newChoices);
        }

        public void setAll(int slot, int id) {
            setRange(slot, id);
            setMelee(slot, id);
            setMage(slot, id);
            setSpec(slot, id);
        }

        public void setRange(int slot, int id) {
            setSlot(slot, id, rangeItems, this::swapRange);
        }

        public void setMelee(int slot, int id) {
            setSlot(slot, id, meleeItems, this::swapMelee);
        }

        public void setMage(int slot, int id) {
            setSlot(slot, id, mageItems, this::swapMage);
        }

        public void setSpec(int slot, int id) {
            setSlot(slot, id, specItems, this::swapSpec);
        }

        public void swapSpec(int id, int... newChoices) {
            swap(List.of(specItems), id, newChoices);
        }

        public void swapInventory(int id, int... newChoices) {
            swap(List.of(defaultInventoryItems), id, newChoices);
        }

        public void swapInventoryQuantity(int id, int newQuantity) {
            final int currentQuantity =
                    (int) defaultInventoryItems.stream().filter(i -> i == id).count();
            if (currentQuantity > newQuantity) {
                for (int i = 0; i < currentQuantity - newQuantity; i++) {
                    defaultInventoryItems.remove(Integer.valueOf(id));
                }
            } else if (currentQuantity < newQuantity) {
                for (int i = 0; i < newQuantity - currentQuantity; i++) {
                    defaultInventoryItems.add(id);
                }
            }
        }

        public void swapAll(int id, int... newChoices) {
            swap(List.of(mageItems, rangeItems, meleeItems, specItems, defaultInventoryItems), id, newChoices);
        }

        private void setSlot(int slot, int id, List<Integer> source, BiConsumer<Integer, Integer> swapFunction) {
            if (Arrays.stream(EquipmentType.values()).noneMatch(i -> i.getSlot() == slot)) {
                throw new IllegalArgumentException("Unknown slot: " + slot);
            }
            final ItemDefinition corresponding = source.stream()
                    .map(ItemDefinition::forId)
                    .filter(i -> i.getEquipmentType().getSlot() == slot)
                    .findFirst()
                    .orElse(null);
            if (corresponding != null) {
                swapFunction.accept(corresponding.getId(), id);
            } else if (id != -1) {
                source.add(id);
            }
            // Special case for setting weapon, remove shield
            final ItemDefinition def = ItemDefinition.forId(id);
            if (def != null && def != ItemDefinition.DEFAULT) {
                if (def.getEquipmentType().getSlot() != slot) {
                    throw new IllegalArgumentException("Slot " + slot + " is not valid for " + id);
                }
                if (def.getEquipmentType() == EquipmentType.WEAPON && def.isDoubleHanded()) {
                    setSlot(Equipment.SHIELD_SLOT, -1, source, swapFunction);
                }
            }
        }

        private void swap(List<List<Integer>> items, int id, int... newChoices) {
            final int index = random.nextInt(newChoices.length);
            final int replacement = newChoices[index];
            for (List<Integer> list : items) {
                if (replacement == -1) {
                    list.removeIf(i -> i == id);
                } else {
                    list.replaceAll(i -> {
                        if (i == id) {
                            return replacement;
                        }
                        return i;
                    });
                }
            }
        }
    }

    @RequiredArgsConstructor
    private static class RandomizedDynamicNhLoadout extends DynamicNhLoadout {

        private final RandomizerContext randomizerContext;
        private final DynamicNhLoadout parent;

        @Override
        public int[] getRangedGear() {
            return randomizerContext.getUpdatedRangedItems();
        }

        @Override
        public int[] getMageGear() {
            if (getMagicSpellbook() == MagicSpellbook.LUNAR) {
                // No mage spell to cast, so don't use magic
                return new int[0];
            }
            return randomizerContext.getUpdatedMageItems();
        }

        @Override
        public int[] getMeleeGear() {
            return randomizerContext.getUpdatedMeleeItems();
        }

        @Override
        public int[] getMeleeSpecGear() {
            return randomizerContext.getUpdatedSpecItems();
        }

        @Override
        public int[] getDefaultInventoryItems() {
            return randomizerContext.getUpdatedDefaultInventoryItems();
        }

        @Override
        public PrayerHandler.PrayerData[] getRangedPrayers() {
            return parent.getRangedPrayers();
        }

        @Override
        public PrayerHandler.PrayerData[] getMagePrayers() {
            return parent.getMagePrayers();
        }

        @Override
        public PrayerHandler.PrayerData[] getMeleePrayers() {
            return parent.getMeleePrayers();
        }

        @Override
        public CombatStats getCombatStats() {
            return parent.getCombatStats();
        }

        @Override
        public NhEnvironmentParams.FightType getFightType() {
            return parent.getFightType();
        }

        @Override
        public MagicSpellbook getMagicSpellbook() {
            if (randomizerContext.getSpellbookOverride() != null) {
                return randomizerContext.getSpellbookOverride();
            }
            return parent.getMagicSpellbook();
        }

        @Override
        protected int getFillItem() {
            if (randomizerContext.getFillItemOverride() != null) {
                return randomizerContext.getFillItemOverride();
            }
            return parent.getFillItem();
        }

        @Override
        protected void applyRandomization(final RandomizerContext randomizerContext) {
            throw new IllegalStateException("Loadout already randomized");
        }
    }
}
