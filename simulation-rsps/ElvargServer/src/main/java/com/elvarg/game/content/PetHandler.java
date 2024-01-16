package com.elvarg.game.content;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.elvarg.game.World;
import com.elvarg.game.collision.RegionManager;
import com.elvarg.game.entity.impl.grounditem.ItemOnGroundManager;
import com.elvarg.game.entity.impl.npc.NPC;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.Animation;
import com.elvarg.game.model.Item;
import com.elvarg.game.model.Location;
import com.elvarg.game.model.Skill;
import com.elvarg.game.model.container.impl.Bank;
import com.elvarg.util.Misc;
import com.google.common.collect.ImmutableSet;

/**
 * Handles pets. Allows them to be dropped on the ground aswell as picked up.
 * Credits to Lumiere from R-S for the huge {@link Pet} data.
 *
 * @author Professor Oak
 */
public class PetHandler {

    /**
     * The {@link Animation} for interacting with a pet.
     */
    private static final Animation INTERACTION_ANIM = new Animation(827);

    /**
     * Randomly gives player pets when skilling.
     *
     * @param player
     * @param skill
     */
    public static void onSkill(Player player, Skill skill) {
        for (Pet pet : Pet.SKILLING_PETS) {
            if (pet.getSkill().isPresent() && pet.getSkill().get() == skill) {
                if (Misc.getRandom(pet.getChance()) == 1) {
                    World.sendMessage("@dre@" + player.getUsername() + " just found a stray " + pet.getName()
                            + " while " + Misc.formatName(skill.toString().toLowerCase()) + "!");
                    drop(player, pet.getItemId(), true);
                    return;
                }
            }
        }
    }

    /**
     * Attempts to drop a pet.
     *
     * @param player The player to spawn a pet for.
     * @param id     The pet-to-spawn's identifier.
     * @param reward Is this pet spawn a reward?
     * @return
     */
    public static boolean drop(Player player, int id, boolean reward) {
        Optional<Pet> pet = Pet.getPetForItem(id);
        if (pet.isPresent()) {

            // Check if we already have a pet..
            if (player.getCurrentPet() == null) {

                if (player.getArea() != null) {
                    if (!player.getArea().allowSummonPet(player) && !reward) {
                        return false;
                    }
                }

                // Spawn the pet..
                List<Location> tiles = new ArrayList<>();
                for (Location tile : player.outterTiles()) {
                    if (RegionManager.blocked(tile, player.getPrivateArea())) {
                        continue;
                    }
                    tiles.add(tile);
                }
                Location location = tiles.isEmpty() ? player.getLocation().clone() : tiles.get(Misc.getRandom(tiles.size() - 1));
                NPC npc = NPC.create(pet.get().getId(), location);
                npc.setPet(true);
                npc.setOwner(player);
                npc.setFollowing(player);
                npc.setMobileInteraction(player);
                npc.setArea(player.getArea());
                World.getAddNPCQueue().add(npc);
                
                // Set the player's current pet to this one.
                player.setCurrentPet(npc);

                // If this is a reward, congratulate them.
                // Otherwise simply drop it on the ground.
                if (reward) {
                    player.getPacketSender().sendMessage("You have a funny feeling like you're being followed.");
                } else {
                    player.getInventory().delete(pet.get().getItemId(), 1);
                    player.getPacketSender().sendMessage("You drop your pet..");
                    player.performAnimation(INTERACTION_ANIM);
                    player.setPositionToFace(npc.getLocation());
                }
            } else {
                // We might have to add to bank if inventory is full!
                if (reward) {
                    if (!player.getInventory().isFull()) {
                        player.getInventory().add(pet.get().getItemId(), 1);
                    } else {
                        ItemOnGroundManager.registerNonGlobal(player, new Item(pet.get().getItemId()));
                    }
                    player.getPacketSender().sendMessage("@dre@You've received a pet!");
                } else {
                    player.getPacketSender().sendMessage("You already have a pet following you.");
                }
            }
            return true;
        }
        return false;
    }

    /**
     * Attempts to pick up a pet.
     *
     * @param player The player picking up the pet.
     * @param npc    The pet to pick up.
     * @return
     */
    public static boolean pickup(Player player, NPC npc) {
        if (npc == null || player.getCurrentPet() == null) {
            return false;
        }
        // Make sure npc is a pet..
        Optional<Pet> pet = Pet.getPet(npc.getId());
        if (!pet.isPresent()) {
            return false;
        }

        // Make sure we're picking up our pet!
        if (player.getCurrentPet().equals(npc)) {

            player.getMovementQueue().reset();
            
            // Perform animation..
            player.performAnimation(INTERACTION_ANIM);

            // Remove the npc from the world
            World.getRemoveNPCQueue().add(player.getCurrentPet());

            // Add pet to inventory or bank
            if (!player.getInventory().isFull()) {
                player.getInventory().add(pet.get().getItemId(), 1);
            } else {
                player.getBank(Bank.getTabForItem(player, pet.get().getItemId())).add(pet.get().getItemId(), 1);
            }

            // Send message
            player.getPacketSender().sendMessage("You pick up your pet..");

            // Reset pet
            player.setCurrentPet(null);
            return true;
        }
        return false;
    }

    /***
     * Attempts to morph a pet.
     *
     * @param player
     *            The player morphing a pet.
     * @param npc
     *            The pet being morphed.
     * @return
     */
    public static boolean morph(Player player, NPC npc) {
        if (npc == null || player.getCurrentPet() == null) {
            return false;
        }

        // Make sure npc is a pet..
        Optional<Pet> pet = Pet.getPet(npc.getId());
        if (!pet.isPresent()) {
            return false;
        }

        // Make sure we're picking up our own pet!
        if (player.getCurrentPet().equals(npc)) {

            // If this pet can morph..
            if (pet.get().canMorph()) {
                npc.setNpcTransformationId(pet.get().getMorphId());
                player.getPacketSender().sendMessage("Your pet endures metamorphosis and transforms.");
            }
            return true;
        }
        return false;
    }

    /**
     * Attempts to interact with the given pet.
     *
     * @param player
     * @param npc
     * @return
     */
    public static boolean interact(Player player, NPC npc) {
        if (npc == null || player.getCurrentPet() == null) {
            return false;
        }

        // Make sure npc is a pet..
        Optional<Pet> pet = Pet.getPet(npc.getId());
        if (!pet.isPresent() || pet.get().getDialogue(player) == -1) {
            return false;
        }

        // Make sure we're interacting with our own pet!
        if (player.getCurrentPet().equals(npc)) {
            if (player.getCurrentPet().getId() == Pet.OLMLET.getId()) {
               /* DialogueManager.start(player, 298);
                player.setDialogueOptions(new DialogueOptions() {
                    @Override
                    public void handleOption(Player player, int option) {
                        switch (option) {
                            case 1:
                                DialogueManager.start(player, 300);
                                break;
                            case 2:
                                DialogueManager.start(player, 303);
                                break;
                            case 3:
                                DialogueManager.start(player, 308);
                                break;
                            case 4:
                                player.getPacketSender().sendInterfaceRemoval();
                                break;
                        }
                    }
                });*/
            } else {
             //   DialogueManager.start(player, pet.get().getDialogue(player));
            }
            return true;
        }
        return false;
    }

    /**
     * Contains all data related to pets.
     *
     * @author Lumiere
     */
    public enum Pet {
        // BOSS & SLAYER PETS
        DARK_CORE(318, 0, 12816, 123), VENENATIS_SPIDERLING(495, 0, 13177, 126), CALLISTO_CUB(497, 0, 13178,
                130), HELLPUPPY(964, 0, 13247, 138) {
            @Override
            public int getDialogue(Player player) {
                int[] dialogueIds = new int[]{138, 143, 145, 150, 154};
                return dialogueIds[Misc.getRandom(dialogueIds.length - 1)];
            }
        },
        CHAOS_ELEMENTAL_JR(2055, 0, 11995, 158), SNAKELING(2130, 2131, 12921, 162), MAGMA_SNAKELING(2131, 2132, 12921,
                169), TANZANITE_SNAKELING(2132, 2130, 12921, 176), VETION_JR(5536, 5537, 13179, 183), VETION_JR_REBORN(
                5537, 5536, 13179,
                189), SCORPIAS_OFFSPRING(5561, 0, 13181, 195), ABYSSAL_ORPHAN(5884, 0, 13262, 202) {
            @Override
            public int getDialogue(Player player) {
                if (!player.getAppearance().isMale()) {
                    return 206;
                } else {
                    int[] dialogueIds = new int[]{202, 209};
                    return dialogueIds[Misc.getRandom(dialogueIds.length - 1)];
                }
            }
        },
        TZREK_JAD(5892, 0, 13225, 212) {
            @Override
            public int getDialogue(Player player) {
                int[] dialogueIds = new int[]{212, 217};
                return dialogueIds[Misc.getRandom(dialogueIds.length - 1)];
            }
        },
        SUPREME_HATCHLING(6628, 0, 12643, 220), PRIME_HATCHLING(6629, 0, 12644, 223), REX_HATCHLING(6630, 0, 12645,
                231), CHICK_ARRA(6631, 0, 12649,
                239), GENERAL_AWWDOR(6632, 0, 12650, 247), COMMANDER_MINIANA(6633, 0, 12651, 250) {
            @Override
            public int getDialogue(Player player) {
                if (player.getEquipment().contains(11806)) {
                    return 252;
                } else
                    return 250;
            }
        },
        KRIL_TINYROTH(6634, 0, 12652, 254), BABY_MOLE(6635, 0, 12646, 261), PRINCE_BLACK_DRAGON(6636, 0, 12653,
                267), KALPHITE_PRINCESS(6637, 6638, 12654, 271), MORPHED_KALPHITE_PRINCESS(6638, 6637, 12654,
                279), SMOKE_DEVIL(6639, 0, 12648, 288), KRAKEN(6640, 0, 12655, 291), PENANCE_PRINCESS(6642, 0,
                12703, 296), OLMLET(7520, 0, 20851, 298), Skotos(425, 0, 21273, 298), // TODO whats the
        // last thing?

        // SKILL PETS
        HERON(6715, 0, 13320, -1, Skill.FISHING, 5000), BEAVER(6717, 0, 13322, -1, Skill.WOODCUTTING,
                5000), GREY_CHINCHOMPA(6719, 6720, 13324, -1, Skill.HUNTER, 3000), RED_CHINCHOMPA(6718, 6719, 13323, -1,
                Skill.HUNTER, 4000), BLACK_CHINCHOMPA(6720, 6718, 13325, -1, Skill.HUNTER, 5000), ROCK_GOLEM(
                6723, 0, 13321, -1, Skill.MINING,
                5000), GIANT_SQUIRREL(7334, 0, 20659, -1, Skill.AGILITY, 5000), TANGLEROOT(7335, 0, 0,
                -1, Skill.FARMING, 5000), ROCKY(7336, 0, 0, -1, Skill.THIEVING, 5000),

        // RIFT GUARDIANS (SKILL PETS)
        FIRE_RIFT_GAURDIAN(7337, 7338, 20665, -1, Skill.RUNECRAFTING, 8000), AIR_RIFT_GUARDIAN(7338, 7339, 20667, -1,
                Skill.RUNECRAFTING,
                8000), MIND_RIFT_GUARDIAN(7339, 7340, 20669, -1, Skill.RUNECRAFTING, 8000), WATER_RIFT_GUARDIAN(7340,
                7341, 20671, -1, Skill.RUNECRAFTING,
                8000), EARTH_RIFT_GUARDIAN(7341, 7342, 20673, -1, Skill.RUNECRAFTING, 8000), BODY_RIFT_GUARDIAN(
                7342, 7343, 20675, -1, Skill.RUNECRAFTING, 8000), COSMIC_RIFT_GUARDIAN(7343, 7344,
                20677, -1, Skill.RUNECRAFTING, 8000), CHAOS_RIFT_GUARDIAN(7344, 7345, 20679, -1,
                Skill.RUNECRAFTING, 8000), NATURE_RIFT_GUARDIAN(7345, 7346, 20681, -1,
                Skill.RUNECRAFTING, 8000), LAW_RIFT_GUARDIAN(7346, 7347, 20683,
                -1, Skill.RUNECRAFTING, 8000), DEATH_RIFT_GUARDIAN(7347,
                7348, 20685, -1, Skill.RUNECRAFTING,
                8000), SOUL_RIFT_GUARDIAN(7348, 7349, 20687, -1,
                Skill.RUNECRAFTING,
                8000), ASTRAL_RIFT_GUARDIAN(7349, 7350,
                20689, -1, Skill.RUNECRAFTING,
                8000), BLOOD_RIFT_GUARDIAN(7350,
                7337, 20691, -1,
                Skill.RUNECRAFTING,
                8000);

        public static final Set<Pet> SKILLING_PETS = ImmutableSet.of(HERON, BEAVER, GREY_CHINCHOMPA, RED_CHINCHOMPA,
                BLACK_CHINCHOMPA, ROCK_GOLEM, GIANT_SQUIRREL, TANGLEROOT, ROCKY);
        private final int petId, morphId, itemId, dialogue;
        public Optional<Skill> skill = Optional.empty();
        private int chance;

        private Pet(int petNpcId, int morphId, int itemId, int dialogue) {
            this.petId = petNpcId;
            this.morphId = morphId;
            this.itemId = itemId;
            this.dialogue = dialogue;
        }

        private Pet(int petNpcId, int morphId, int itemId, int dialogue, Skill skill, int chance) {
            this(petNpcId, morphId, itemId, dialogue);
            this.skill = Optional.of(skill);
            this.chance = chance;
        }

        public static Optional<Pet> getPet(int identifier) {
            return Arrays.stream(values()).filter(s -> s.petId == identifier).findFirst();
        }

        public static Optional<Pet> getPetForItem(int identifier) {
            return Arrays.stream(values()).filter(s -> s.itemId == identifier).findFirst();
        }

        public int getId() {
            return petId;
        }

        public int getMorphId() {
            return morphId;
        }

        public boolean canMorph() {
            return (morphId != 0);
        }

        public int getItemId() {
            return itemId;
        }

        public int getDialogue(Player player) {
            return dialogue;
        }

        public Optional<Skill> getSkill() {
            return skill;
        }

        public int getChance() {
            return chance;
        }

        public String getName() {
            String name = name().toLowerCase().replaceAll("_", " ");
            return Misc.capitalizeWords(name);
        }
    }
}
