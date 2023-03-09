package com.elvarg.game.content.minigames.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import com.elvarg.game.World;
import com.elvarg.game.content.combat.CombatFactory;
import com.elvarg.game.definition.ItemDefinition;
import com.elvarg.game.entity.impl.npc.NPC;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.Boundary;
import com.elvarg.game.model.Item;
import com.elvarg.game.model.Location;
import com.elvarg.util.ItemIdentifiers;
import com.elvarg.util.Misc;
import com.elvarg.util.timers.TimerKey;
import com.google.common.collect.ImmutableList;

import static com.elvarg.util.ItemIdentifiers.*;

public class Barrows {

    public static final Location ENTRANCE = new Location(3565, 3306);
    public static final int KILLCOUNTER_INTERFACE_ID = 4535;
    public static final int KILLCOUNTER_FRAME_ID = 4536;
    private static final Location CHEST_ENTRANCE = new Location(3551, 9691);
    private static final Location BOSS_SPAWN = new Location(3550, 9694);
    private static final int CHEST_OBJECT_ID = 20973;
    private static final int TUNNEL_DIALOGUE_ID = 26;
    private static final ImmutableList<Integer> BARROW_ITEMS = ImmutableList.of(4708, 4710, 4712, 4714, 4716, 4718, 4720, 4722, 4724, 4726, 4728, 4730,
            4732, 4734, 4736, 4738, 4745, 4747, 4749, 4751, 4753, 4755, 4757, 4759);
    private static final ImmutableList<Integer> RUNES = ImmutableList.of(558, 560, 562, 565);
    private static final int REWARDS_INTERFACE_ID = 42560;

    public static void dig(Player player) {
        Optional<Brother> digLocation = getDiggingLocation(player);
        if (digLocation.isPresent()) {
            player.getPacketSender().sendMessage("You've found a crypt!");
            player.moveTo(digLocation.get().getDigSpawn());
        }
    }

    public static boolean handleObject(Player player, int object) {

        // Handle searching crypts..
        Optional<Brother> brother = getBrotherForCrypt(object);
        if (brother.isPresent()) {

            if (CombatFactory.inCombat(player)) {
                player.getPacketSender().sendMessage("You cannot do that whilst in combat.");
                return true;
            }

            if (player.getBarrowsCrypt() <= 0) {
                player.setBarrowsCrypt(getRandomCrypt());
            }

            if (player.getCurrentBrother() != null || player.getKilledBrothers()[brother.get().ordinal()]) {
                player.getPacketSender().sendMessage("The sarcophagus appears to be empty.");
                return false;
            }

            if (player.getBarrowsCrypt() == object) {
                /*player.setDialogueOptions(new DialogueOptions() {
                    @Override
                    public void handleOption(Player player, int option) {
                        switch (option) {
                            case 1:
                                if (getKillcount(player) < 5) {
                                    player.getPacketSender()
                                            .sendMessage("You need a killcount of at least 5 to enter this tunnel.");
                                } else {
                                    player.moveTo(CHEST_ENTRANCE.clone().add(Misc.getRandom(2), Misc.getRandom(1)));
                                }
                                break;
                        }
                        player.getPacketSender().sendInterfaceRemoval();
                    }
                });
                DialogueManager.start(player, TUNNEL_DIALOGUE_ID);*/
                return true;
            }

            if (player.getCurrentBrother() != null || player.getKilledBrothers()[brother.get().ordinal()]) {
                player.getPacketSender().sendMessage("The sarcophagus appears to be empty.");
                return false;
            }

            brotherSpawn(player, brother.get(), brother.get().getSpawn());
            return true;
        }

        // Exit using stairs
        Optional<Brother> stairs = getStairs(object);
        if (stairs.isPresent()) {
            player.moveTo(stairs.get().getStairSpawn());
            return true;
        }

        // Handle chest
        if (object == CHEST_OBJECT_ID) {

            if (CombatFactory.inCombat(player)) {
                player.getPacketSender().sendMessage("You cannot do that whilst in combat.");
                return true;
            }

            if (getKillcount(player) >= 5) {

                final Optional<Brother> boss = getBrotherForCrypt(player.getBarrowsCrypt());
                if (boss.isPresent()) {

                    /** They might have already spawned the boss **/
                    if (player.getCurrentBrother() != null && player.getCurrentBrother().isRegistered()) {
                        player.getPacketSender().sendMessage("You cannot do this right now.");
                        return false;
                    }

                    if (player.getKilledBrothers()[boss.get().ordinal()]) {
                    	player.getPacketSender().clearInterfaceItems(42563, 42568);
                    	List<Item> rewards = new ArrayList<>();
                        if (Misc.randomInclusive(1, 5) == 1) {
                        	rewards.add(new Item(Misc.randomTypeOfList(BARROW_ITEMS)));
                        }
                        for (int i = 0; i < 3; i++) {
                        	rewards.add(new Item(Misc.randomTypeOfList(RUNES), Misc.randomInclusive(50, 300)));
                        }
                        if (Misc.getRandom(1) == 0) {
                        	rewards.add(new Item(ItemIdentifiers.BOLT_RACK, Misc.randomInclusive(50, 150)));
                        }
                        for (int i = 0; i < rewards.size(); i++) {
                        	Item item = rewards.get(i);
                        	player.getInventory().forceAdd(player, item);
                        	player.getPacketSender().sendItemOnInterface(42563 + i, item.getId(), item.getAmount());
                        }
                        player.setBarrowsChestsLooted(player.getBarrowsChestsLooted() + 1);
                        player.getPacketSender().sendInterface(REWARDS_INTERFACE_ID)
                                .sendMessage("@or3@You've looted a total of " + player.getBarrowsChestsLooted() + " chests.");
                        reset(player);
                    } else {
                        brotherSpawn(player, boss.get(), BOSS_SPAWN.clone());
                    }

                } else {
                    reset(player);
                }
            } else {
                player.getPacketSender().sendMessage("The chest appears to be empty.");
            }
            return true;
        }
        return false;
    }

    public static void brotherSpawn(Player player, Brother brother, Location pos) {
        NPC npc = new NPC(brother.getNpcId(), pos) {
            @Override
            public void onAdd() {
                setOwner(player);
                forceChat("You dare disturb my rest!");
                getTimers().extendOrRegister(TimerKey.COMBAT_ATTACK, 3);
                getCombat().attack(player);
                player.getPacketSender().sendEntityHint(this);
            }
        };
        World.getAddNPCQueue().add(npc);
        player.setCurrentBrother(npc);
    }

    public static void brotherDespawn(Player player) {
        final NPC brother = player.getCurrentBrother();
        if (brother != null && brother.isRegistered() && !brother.isDying()) {
            World.getRemoveNPCQueue().add(brother);
            player.setCurrentBrother(null);
            player.getPacketSender().sendEntityHintRemoval(false);
        }
    }

    public static void brotherDeath(Player player, NPC npc) {
        Optional<Brother> brother = getBrotherForNpcId(npc.getId());
        if (brother.isPresent() && player.getCurrentBrother() == npc) {
            player.getPacketSender().sendEntityHintRemoval(false);
            player.getKilledBrothers()[brother.get().ordinal()] = true;
            updateInterface(player);
            player.setCurrentBrother(null);
        }
    }

    public static void reset(Player player) {
        player.setBarrowsCrypt(0);
        player.getPacketSender().sendEntityHintRemoval(false);
        for (int i = 0; i < player.getKilledBrothers().length; i++) {
            player.getKilledBrothers()[i] = false;
        }
        updateInterface(player);
    }

    public static void updateInterface(Player player) {
        player.getPacketSender().sendString(Barrows.KILLCOUNTER_FRAME_ID,
                "Killcount: " + getKillcount(player));
    }
    
    private static int getKillcount(Player player) {
    	int defeated = 0;
    	for (boolean brotherDefeated : player.getKilledBrothers()) {
    		if (brotherDefeated) {
    			defeated++;
    		}
    	}
    	return defeated;
    }

    private static Optional<Brother> getBrotherForCrypt(int crypt) {
        return Arrays.stream(Brother.values()).filter(x -> x.getCoffinId() == crypt).findFirst();
    }

    private static Optional<Brother> getBrotherForNpcId(int npcId) {
        return Arrays.stream(Brother.values()).filter(x -> x.getNpcId() == npcId).findFirst();
    }

    private static Optional<Brother> getStairs(int object) {
        return Arrays.stream(Brother.values()).filter(x -> x.getStairs() == object).findFirst();
    }

    private static Optional<Brother> getDiggingLocation(Player player) {
        return Arrays.stream(Brother.values()).filter(x -> x.getBoundary().inside(player.getLocation())).findFirst();
    }

    private static int getRandomCrypt() {
        return Brother.values()[Misc.getRandom(Brother.values().length - 1)].getCoffinId();
    }

    public static int getBrokenId(Item item) {
        if(item.getDefinition().isNoted())
            return item.getId();
        String name = item.getDefinition().getName();
        if(name.endsWith(" 100"))
            return item.getId() + 4;
        if(name.endsWith(" 75"))
            return item.getId() + 3;
        if(name.endsWith(" 50"))
            return item.getId() + 2;
        if(name.endsWith(" 25"))
            return item.getId() + 1;
        if(name.endsWith(" 0"))
            return item.getId();
        return getFirstBrokenId(item.getId()) + 4;
    }

    public static short getFirstBrokenId(int id) {
        switch(id) {
            case AHRIMS_HOOD:
                return AHRIMS_HOOD_100;
            case AHRIMS_STAFF:
                return AHRIMS_STAFF_100;
            case AHRIMS_ROBETOP:
                return AHRIMS_ROBETOP_100;
            case AHRIMS_ROBESKIRT:
                return AHRIMS_ROBESKIRT_100;
            case DHAROKS_HELM:
                return DHAROKS_HELM_100;
            case DHAROKS_GREATAXE:
                return DHAROKS_GREATAXE_100;
            case DHAROKS_PLATEBODY:
                return DHAROKS_PLATEBODY_100;
            case DHAROKS_PLATELEGS:
                return DHAROKS_PLATELEGS_100;
            case GUTHANS_HELM:
                return GUTHANS_HELM_100;
            case GUTHANS_WARSPEAR:
                return GUTHANS_WARSPEAR_100;
            case GUTHANS_PLATEBODY:
                return GUTHANS_PLATEBODY_100;
            case GUTHANS_CHAINSKIRT:
                return GUTHANS_CHAINSKIRT_100;
            case KARILS_COIF:
                return KARILS_COIF_100;
            case KARILS_CROSSBOW:
                return KARILS_CROSSBOW_100;
            case KARILS_LEATHERTOP:
                return KARILS_LEATHERTOP_100;
            case KARILS_LEATHERSKIRT:
                return KARILS_LEATHERSKIRT_100;
            case TORAGS_HELM:
                return TORAGS_HELM_100;
            case TORAGS_HAMMERS:
                return TORAGS_HAMMERS_100;
            case TORAGS_PLATEBODY:
                return TORAGS_PLATEBODY_100;
            case TORAGS_PLATELEGS:
                return TORAGS_PLATELEGS_100;
            case VERACS_HELM:
                return VERACS_HELM_100;
            case VERACS_FLAIL:
                return VERACS_FLAIL_100;
            case VERACS_BRASSARD:
                return VERACS_BRASSARD_100;
            case VERACS_PLATESKIRT:
                return VERACS_PLATESKIRT_100;
        }
        return -1;
    }

    public static short getRepairedId(int id) {
        String name = ItemDefinition.forId(id).getName();
        if(name.endsWith(" 75"))
            id--;
        else if(name.endsWith(" 50"))
            id -= 2;
        else if(name.endsWith(" 25"))
            id -= 3;
        else if(name.endsWith(" 0"))
            id -= 4;
        switch(id) {
            case AHRIMS_HOOD_100:
                return AHRIMS_HOOD;
            case AHRIMS_STAFF_100:
                return AHRIMS_STAFF;
            case AHRIMS_ROBETOP_100:
                return AHRIMS_ROBETOP;
            case AHRIMS_ROBESKIRT_100:
                return AHRIMS_ROBESKIRT;
            case DHAROKS_HELM_100:
                return DHAROKS_HELM;
            case DHAROKS_GREATAXE_100:
                return DHAROKS_GREATAXE;
            case DHAROKS_PLATEBODY_100:
                return DHAROKS_PLATEBODY;
            case DHAROKS_PLATELEGS_100:
                return DHAROKS_PLATELEGS;
            case GUTHANS_HELM_100:
                return GUTHANS_HELM;
            case GUTHANS_WARSPEAR_100:
                return GUTHANS_WARSPEAR;
            case GUTHANS_PLATEBODY_100:
                return GUTHANS_PLATEBODY;
            case GUTHANS_CHAINSKIRT_100:
                return GUTHANS_CHAINSKIRT;
            case KARILS_COIF_100:
                return KARILS_COIF;
            case KARILS_CROSSBOW_100:
                return KARILS_CROSSBOW;
            case KARILS_LEATHERTOP_100:
                return KARILS_LEATHERTOP;
            case KARILS_LEATHERSKIRT_100:
                return KARILS_LEATHERSKIRT;
            case TORAGS_HELM_100:
                return TORAGS_HELM;
            case TORAGS_HAMMERS_100:
                return TORAGS_HAMMERS;
            case TORAGS_PLATEBODY_100:
                return TORAGS_PLATEBODY;
            case TORAGS_PLATELEGS_100:
                return TORAGS_PLATELEGS;
            case VERACS_HELM_100:
                return VERACS_HELM;
            case VERACS_FLAIL_100:
                return VERACS_FLAIL;
            case VERACS_BRASSARD_100:
                return VERACS_BRASSARD;
            case VERACS_PLATESKIRT_100:
                return VERACS_PLATESKIRT;
        }
        return -1;
    }

    public enum Brother {
        AHRIM_THE_BLIGHTED(1672, 20770, new Location(3557, 9701, 3), new Boundary(3562, 3568, 3285, 3292),
                new Location(3557, 9703, 3), 20667, new Location(3565, 3288, 0)), DHAROK_THE_WRETCHED(1673, 20720,
                new Location(3553, 9716, 3), new Boundary(3572, 3578, 3294, 3301), new Location(3556, 9718, 3),
                20668, new Location(3574, 3297, 0)), GUTHAN_THE_INFESTED(1674, 20722,
                new Location(3540, 9705, 3), new Boundary(3574, 3584, 3279, 3285),
                new Location(3534, 9704, 3), 20669, new Location(3577, 3282, 0)), KARIL_THE_TAINTED(
                1675, 20771, new Location(3549, 9685, 3), new Boundary(3564, 3568, 3273, 3278),
                new Location(3546, 9684, 3), 20670,
                new Location(3566, 3275, 0)), TORAG_THE_CORRUPTED(1676, 20721,
                new Location(3568, 9688, 3), new Boundary(3550, 3556, 3280, 3284),
                new Location(3568, 9683, 3), 20671,
                new Location(3554, 3282, 0)), VERAC_THE_DEFILED(1677, 20772,
                new Location(3575, 9708, 3),
                new Boundary(3553, 3560, 3294, 3301),
                new Location(3578, 9706, 3), 20672,
                new Location(3557, 3297, 0));

        private int npcId, coffinId, stairs;

        private Location spawn, digSpawn, stairSpawn;
        private Boundary boundary;

        Brother(final int npcId, final int coffin, final Location brotherSpawn, final Boundary boundary,
                        final Location digSpawn, final int stairs, final Location stairSpawn) {
            this.npcId = npcId;
            this.coffinId = coffin;
            this.spawn = brotherSpawn;
            this.boundary = boundary;
            this.digSpawn = digSpawn;
            this.stairs = stairs;
            this.stairSpawn = stairSpawn;
        }

        public int getNpcId() {
            return npcId;
        }

        public int getCoffinId() {
            return coffinId;
        }

        public Location getSpawn() {
            return spawn;
        }

        public Boundary getBoundary() {
            return boundary;
        }

        public Location getDigSpawn() {
            return digSpawn;
        }

        public int getStairs() {
            return stairs;
        }

        public Location getStairSpawn() {
            return stairSpawn;
        }
    }
}
