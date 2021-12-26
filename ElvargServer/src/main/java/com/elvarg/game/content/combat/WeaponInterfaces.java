package com.elvarg.game.content.combat;

import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.Item;
import com.elvarg.game.model.container.impl.Equipment;

/**
 * A static utility class that displays holds and displays data for weapon
 * interfaces.
 *
 * @author lare96
 */
public final class WeaponInterfaces {

    /**
     * Assigns an interface to the combat sidebar based on the argued weapon.
     *
     * @param player the player that the interface will be assigned for.
     * @param item   the item that the interface will be chosen for.
     */
    public static void assign(Player player) {
        Item equippedWeapon = player.getEquipment().getItems()[Equipment.WEAPON_SLOT];
        WeaponInterface weapon = WeaponInterface.UNARMED;

        //Get the currently equipped weapon's interface
        if (equippedWeapon.getId() > 0) {
            if (equippedWeapon.getDefinition().getWeaponInterface() != null) {
                weapon = equippedWeapon.getDefinition().getWeaponInterface();
            }
        }

        if (weapon == WeaponInterface.UNARMED) {
            player.getPacketSender().sendTabInterface(0, weapon.getInterfaceId());
            player.getPacketSender().sendString(weapon.getNameLineId(), "Unarmed");
            player.setWeapon(WeaponInterface.UNARMED);
        } else if (weapon == WeaponInterface.CROSSBOW) {
            player.getPacketSender().sendString(weapon.getNameLineId() - 1, "Weapon: ");
        } else if (weapon == WeaponInterface.WHIP) {
            player.getPacketSender().sendString(weapon.getNameLineId() - 1, "Weapon: ");
        }

        //player.getPacketSender().sendItemOnInterface(weapon.getInterfaceId() + 1, 200, item);
        //player.getPacketSender().sendItemOnInterface(weapon.getInterfaceId() + 1, item, 0, 1);

        player.getPacketSender().sendTabInterface(0,
                weapon.getInterfaceId());
        player.getPacketSender().sendString(weapon.getNameLineId(),
                (weapon == WeaponInterface.UNARMED ? "Unarmed" : equippedWeapon.getDefinition().getName()));
        player.setWeapon(weapon);
        CombatSpecial.assign(player);
        CombatSpecial.updateBar(player);

        //Search for an attack style matching ours
    /*	for (FightType type : weapon.getFightType()) {
			if (type.getStyle() == player.getCombat().getFightType().getStyle()) {
				player.setFightType(type);
				player.getPacketSender().sendConfig(player.getCombat().getFightType().getParentId(), player.getCombat().getFightType().getChildId());
				return;
			}
		}*/

        //Set default attack style to aggressive!
        for (FightType type : weapon.getFightType()) {
            if (type.getStyle() == FightStyle.AGGRESSIVE) {
                player.setFightType(type);
                player.getPacketSender().sendConfig(player.getFightType().getParentId(), player.getFightType().getChildId());
                return;
            }
        }

        //Still no proper attack style.
        //Set it to the first one..
        player.setFightType(player.getWeapon().getFightType()[0]);
        player.getPacketSender().sendConfig(player.getFightType().getParentId(), player.getFightType().getChildId());
    }

    public static boolean changeCombatSettings(Player player, int button) {
        switch (button) {
            case 1772: // shortbow & longbow
                if (player.getWeapon() == WeaponInterface.SHORTBOW) {
                    player.setFightType(FightType.SHORTBOW_ACCURATE);
                } else if (player.getWeapon() == WeaponInterface.LONGBOW
                        || player.getWeapon() == WeaponInterface.DARK_BOW) {
                    player.setFightType(FightType.LONGBOW_ACCURATE);
                } else if (player.getWeapon() == WeaponInterface.CROSSBOW) {
                    player.setFightType(FightType.CROSSBOW_ACCURATE);
                } else if (player.getWeapon() == WeaponInterface.KARILS_CROSSBOW) {
                    player.setFightType(FightType.KARILS_CROSSBOW_ACCURATE);
                }
                return true;
            case 1771:
                if (player.getWeapon() == WeaponInterface.SHORTBOW) {
                    player.setFightType(FightType.SHORTBOW_RAPID);
                } else if (player.getWeapon() == WeaponInterface.LONGBOW
                        || player.getWeapon() == WeaponInterface.DARK_BOW) {
                    player.setFightType(FightType.LONGBOW_RAPID);
                } else if (player.getWeapon() == WeaponInterface.CROSSBOW) {
                    player.setFightType(FightType.CROSSBOW_RAPID);
                } else if (player.getWeapon() == WeaponInterface.KARILS_CROSSBOW) {
                    player.setFightType(FightType.KARILS_CROSSBOW_RAPID);
                }
                return true;
            case 1770:
                if (player.getWeapon() == WeaponInterface.SHORTBOW) {
                    player.setFightType(FightType.SHORTBOW_LONGRANGE);
                } else if (player.getWeapon() == WeaponInterface.LONGBOW
                        || player.getWeapon() == WeaponInterface.DARK_BOW) {
                    player.setFightType(FightType.LONGBOW_LONGRANGE);
                } else if (player.getWeapon() == WeaponInterface.CROSSBOW) {
                    player.setFightType(FightType.CROSSBOW_LONGRANGE);
                } else if (player.getWeapon() == WeaponInterface.KARILS_CROSSBOW) {
                    player.setFightType(FightType.KARILS_CROSSBOW_LONGRANGE);
                }
                return true;
            case 2282: // dagger & sword
                if (player.getWeapon() == WeaponInterface.DAGGER) {
                    player.setFightType(FightType.DAGGER_STAB);
                } else if (player.getWeapon() == WeaponInterface.DRAGON_DAGGER) {
                    player.setFightType(FightType.DRAGON_DAGGER_STAB);
                } else if (player.getWeapon() == WeaponInterface.SWORD) {
                    player.setFightType(FightType.SWORD_STAB);
                }
                return true;
            case 2285:
                if (player.getWeapon() == WeaponInterface.DAGGER) {
                    player.setFightType(FightType.DAGGER_LUNGE);
                } else if (player.getWeapon() == WeaponInterface.DRAGON_DAGGER) {
                    player.setFightType(FightType.DRAGON_DAGGER_LUNGE);
                } else if (player.getWeapon() == WeaponInterface.SWORD) {
                    player.setFightType(FightType.SWORD_LUNGE);
                }
                return true;
            case 2284:
                if (player.getWeapon() == WeaponInterface.DAGGER) {
                    player.setFightType(FightType.DAGGER_SLASH);
                } else if (player.getWeapon() == WeaponInterface.DRAGON_DAGGER) {
                    player.setFightType(FightType.DRAGON_DAGGER_SLASH);
                } else if (player.getWeapon() == WeaponInterface.SWORD) {
                    player.setFightType(FightType.SWORD_SLASH);
                }
                return true;
            case 2283:
                if (player.getWeapon() == WeaponInterface.DAGGER) {
                    player.setFightType(FightType.DAGGER_BLOCK);
                } else if (player.getWeapon() == WeaponInterface.DRAGON_DAGGER) {
                    player.setFightType(FightType.DRAGON_DAGGER_BLOCK);
                } else if (player.getWeapon() == WeaponInterface.SWORD) {
                    player.setFightType(FightType.SWORD_BLOCK);
                }
                return true;
            case 2429: // scimitar & longsword
                if (player.getWeapon() == WeaponInterface.SCIMITAR) {
                    player.setFightType(FightType.SCIMITAR_CHOP);
                } else if (player.getWeapon() == WeaponInterface.LONGSWORD) {
                    player.setFightType(FightType.LONGSWORD_CHOP);
                }
                return true;
            case 2432:
                if (player.getWeapon() == WeaponInterface.SCIMITAR) {
                    player.setFightType(FightType.SCIMITAR_SLASH);
                } else if (player.getWeapon() == WeaponInterface.LONGSWORD) {
                    player.setFightType(FightType.LONGSWORD_SLASH);
                }
                return true;
            case 2431:
                if (player.getWeapon() == WeaponInterface.SCIMITAR) {
                    player.setFightType(FightType.SCIMITAR_LUNGE);
                } else if (player.getWeapon() == WeaponInterface.LONGSWORD) {
                    player.setFightType(FightType.LONGSWORD_LUNGE);
                }
                return true;
            case 2430:
                if (player.getWeapon() == WeaponInterface.SCIMITAR) {
                    player.setFightType(FightType.SCIMITAR_BLOCK);
                } else if (player.getWeapon() == WeaponInterface.LONGSWORD) {
                    player.setFightType(FightType.LONGSWORD_BLOCK);
                }
                return true;
            case 3802: // mace
                if (player.getWeapon() == WeaponInterface.VERACS_FLAIL) {
                    player.setFightType(FightType.VERACS_FLAIL_POUND);
                } else {
                    player.setFightType(FightType.MACE_POUND);
                }
                return true;
            case 3805:
                if (player.getWeapon() == WeaponInterface.VERACS_FLAIL) {
                    player.setFightType(FightType.VERACS_FLAIL_PUMMEL);
                } else {
                    player.setFightType(FightType.MACE_PUMMEL);
                }
                return true;
            case 3804:
                if (player.getWeapon() == WeaponInterface.VERACS_FLAIL) {
                    player.setFightType(FightType.VERACS_FLAIL_SPIKE);
                } else {
                    player.setFightType(FightType.MACE_SPIKE);
                }
                return true;
            case 3803:
                if (player.getWeapon() == WeaponInterface.VERACS_FLAIL) {
                    player.setFightType(FightType.VERACS_FLAIL_BLOCK);
                } else {
                    player.setFightType(FightType.MACE_BLOCK);
                }
                return true;
            case 4454: // knife, thrownaxe, dart & javelin
                if (player.getWeapon() == WeaponInterface.KNIFE) {
                    player.setFightType(FightType.KNIFE_ACCURATE);
                } else if (player.getWeapon() == WeaponInterface.OBBY_RINGS) {
                    player.setFightType(FightType.OBBY_RING_ACCURATE);
                } else if (player.getWeapon() == WeaponInterface.THROWNAXE) {
                    player.setFightType(FightType.THROWNAXE_ACCURATE);
                } else if (player.getWeapon() == WeaponInterface.DART) {
                    player.setFightType(FightType.DART_ACCURATE);
                } else if (player.getWeapon() == WeaponInterface.JAVELIN) {
                    player.setFightType(FightType.JAVELIN_ACCURATE);
                }
                return true;
            case 4453:
                if (player.getWeapon() == WeaponInterface.KNIFE) {
                    player.setFightType(FightType.KNIFE_RAPID);
                } else if (player.getWeapon() == WeaponInterface.OBBY_RINGS) {
                    player.setFightType(FightType.OBBY_RING_RAPID);
                } else if (player.getWeapon() == WeaponInterface.THROWNAXE) {
                    player.setFightType(FightType.THROWNAXE_RAPID);
                } else if (player.getWeapon() == WeaponInterface.DART) {
                    player.setFightType(FightType.DART_RAPID);
                } else if (player.getWeapon() == WeaponInterface.JAVELIN) {
                    player.setFightType(FightType.JAVELIN_RAPID);
                }
                return true;
            case 4452:
                if (player.getWeapon() == WeaponInterface.KNIFE) {
                    player.setFightType(FightType.KNIFE_LONGRANGE);
                } else if (player.getWeapon() == WeaponInterface.OBBY_RINGS) {
                    player.setFightType(FightType.OBBY_RING_LONGRANGE);
                } else if (player.getWeapon() == WeaponInterface.THROWNAXE) {
                    player.setFightType(FightType.THROWNAXE_LONGRANGE);
                } else if (player.getWeapon() == WeaponInterface.DART) {
                    player.setFightType(FightType.DART_LONGRANGE);
                } else if (player.getWeapon() == WeaponInterface.JAVELIN) {
                    player.setFightType(FightType.JAVELIN_LONGRANGE);
                }
                return true;
            case 4685: // spear
                player.setFightType(FightType.SPEAR_LUNGE);
                return true;
            case 4688:
                player.setFightType(FightType.SPEAR_SWIPE);
                return true;
            case 4687:
                player.setFightType(FightType.SPEAR_POUND);
                return true;
            case 4686:
                player.setFightType(FightType.SPEAR_BLOCK);
                return true;
            case 4711: // 2h sword
                player.setFightType(FightType.TWOHANDEDSWORD_CHOP);
                return true;
            case 4714:
                player.setFightType(FightType.TWOHANDEDSWORD_SLASH);
                return true;
            case 4713:
                player.setFightType(FightType.TWOHANDEDSWORD_SMASH);
                return true;
            case 4712:
                player.setFightType(FightType.TWOHANDEDSWORD_BLOCK);
                return true;
            case 5576: // pickaxe
                player.setFightType(FightType.PICKAXE_SPIKE);
                return true;
            case 5579:
                player.setFightType(FightType.PICKAXE_IMPALE);
                return true;
            case 5578:
                player.setFightType(FightType.PICKAXE_SMASH);
                return true;
            case 5577:
                player.setFightType(FightType.PICKAXE_BLOCK);
                return true;
            case 7768: // claws
                player.setFightType(FightType.CLAWS_CHOP);
                return true;
            case 7771:
                player.setFightType(FightType.CLAWS_SLASH);
                return true;
            case 7770:
                player.setFightType(FightType.CLAWS_LUNGE);
                return true;
            case 7769:
                player.setFightType(FightType.CLAWS_BLOCK);
                return true;
            case 8466: // halberd
                player.setFightType(FightType.HALBERD_JAB);
                return true;
            case 8468:
                player.setFightType(FightType.HALBERD_SWIPE);
                return true;
            case 8467:
                player.setFightType(FightType.HALBERD_FEND);
                return true;
            case 5861: // unarmed
                player.setFightType(FightType.UNARMED_BLOCK);
                return true;
            case 5862:
                player.setFightType(FightType.UNARMED_KICK);
                return true;
            case 5860:
                player.setFightType(FightType.UNARMED_PUNCH);
                return true;
            case 12298: // whip
                player.setFightType(FightType.WHIP_FLICK);
                return true;
            case 12297:
                player.setFightType(FightType.WHIP_LASH);
                return true;
            case 12296:
                player.setFightType(FightType.WHIP_DEFLECT);
                return true;
            case 336: // staff
                player.setFightType(FightType.STAFF_BASH);
                return true;
            case 335:
                player.setFightType(FightType.STAFF_POUND);
                return true;
            case 334:
                player.setFightType(FightType.STAFF_FOCUS);
                return true;
            case 433: // warhammer
                if (player.getWeapon() == WeaponInterface.GRANITE_MAUL) {
                    player.setFightType(FightType.GRANITE_MAUL_POUND);
                } else if (player.getWeapon() == WeaponInterface.MAUL) {
                    player.setFightType(FightType.MAUL_POUND);
                } else if (player.getWeapon() == WeaponInterface.WARHAMMER) {
                    player.setFightType(FightType.WARHAMMER_POUND);
                } else if (player.getWeapon() == WeaponInterface.ELDER_MAUL) {
                    player.setFightType(FightType.ELDER_MAUL_POUND);
                }
                return true;
            case 432:
                if (player.getWeapon() == WeaponInterface.GRANITE_MAUL) {
                    player.setFightType(FightType.GRANITE_MAUL_PUMMEL);
                } else if (player.getWeapon() == WeaponInterface.MAUL) {
                    player.setFightType(FightType.MAUL_PUMMEL);
                } else if (player.getWeapon() == WeaponInterface.WARHAMMER) {
                    player.setFightType(FightType.WARHAMMER_PUMMEL);
                } else if (player.getWeapon() == WeaponInterface.ELDER_MAUL) {
                    player.setFightType(FightType.ELDER_MAUL_PUMMEL);
                }
                return true;
            case 431:
                if (player.getWeapon() == WeaponInterface.GRANITE_MAUL) {
                    player.setFightType(FightType.GRANITE_MAUL_BLOCK);
                } else if (player.getWeapon() == WeaponInterface.MAUL) {
                    player.setFightType(FightType.MAUL_BLOCK);
                } else if (player.getWeapon() == WeaponInterface.WARHAMMER) {
                    player.setFightType(FightType.WARHAMMER_BLOCK);
                } else if (player.getWeapon() == WeaponInterface.ELDER_MAUL) {
                    player.setFightType(FightType.ELDER_MAUL_BLOCK);
                }
                return true;
            case 782: // scythe
                player.setFightType(FightType.SCYTHE_REAP);
                return true;
            case 784:
                player.setFightType(FightType.SCYTHE_CHOP);
                return true;
            case 785:
                player.setFightType(FightType.SCYTHE_JAB);
                return true;
            case 783:
                player.setFightType(FightType.SCYTHE_BLOCK);
                return true;
            case 1704: // battle axe
                if (player.getWeapon() == WeaponInterface.GREATAXE) {
                    player.setFightType(FightType.GREATAXE_CHOP);
                } else {
                    player.setFightType(FightType.BATTLEAXE_CHOP);
                }
                return true;
            case 1707:
                if (player.getWeapon() == WeaponInterface.GREATAXE) {
                    player.setFightType(FightType.GREATAXE_HACK);
                } else {
                    player.setFightType(FightType.BATTLEAXE_HACK);
                }
                return true;
            case 1706:
                if (player.getWeapon() == WeaponInterface.GREATAXE) {
                    player.setFightType(FightType.GREATAXE_SMASH);
                } else {
                    player.setFightType(FightType.BATTLEAXE_SMASH);
                }
                return true;
            case 1705:
                if (player.getWeapon() == WeaponInterface.GREATAXE) {
                    player.setFightType(FightType.GREATAXE_BLOCK);
                } else {
                    player.setFightType(FightType.BATTLEAXE_BLOCK);
                }
                return true;
            case 29138:
            case 29038:
            case 29063:
            case 29113:
            case 29163:
            case 29188:
            case 29213:
            case 29238:
            case 30007:
            case 48023:
            case 33033:
            case 30108:
            case 7473:
            case 7562:
            case 7487:
            case 7788:
            case 8481:
            case 7612:
            case 7587:
            case 7662:
            case 7462:
            case 7548:
            case 7687:
            case 7537:
            case 7623:
            case 12322:
            case 7637:
            case 12311:
            case 155:
                CombatSpecial.activate(player);
                return true;
        }
        return false;
    }

    /**
     * All of the interfaces for weapons and the data needed to display these
     * interfaces properly.
     *
     * @author lare96
     */
    public enum WeaponInterface {
        STAFF(328, 355, 5, new FightType[]{FightType.STAFF_BASH, FightType.STAFF_POUND, FightType.STAFF_FOCUS}),
        WARHAMMER(425, 428, 6, new FightType[]{FightType.WARHAMMER_POUND,
                FightType.WARHAMMER_PUMMEL, FightType.WARHAMMER_BLOCK}, 7474, 7486),
        MAUL(425, 428, 7, new FightType[]{FightType.MAUL_POUND,
                FightType.MAUL_PUMMEL, FightType.MAUL_BLOCK}, 7474, 7486),
        GRANITE_MAUL(425, 428, 7, new FightType[]{FightType.GRANITE_MAUL_POUND,
                FightType.GRANITE_MAUL_PUMMEL, FightType.GRANITE_MAUL_BLOCK}, 7474, 7486),
        VERACS_FLAIL(3796, 3799, 5, new FightType[]{FightType.VERACS_FLAIL_POUND,
                FightType.VERACS_FLAIL_PUMMEL, FightType.VERACS_FLAIL_SPIKE,
                FightType.VERACS_FLAIL_BLOCK}, 7624, 7636),
        SCYTHE(776, 779, 4, new FightType[]{FightType.SCYTHE_REAP,
                FightType.SCYTHE_CHOP, FightType.SCYTHE_JAB,
                FightType.SCYTHE_BLOCK}),
        BATTLEAXE(1698, 1701, 5, new FightType[]{FightType.BATTLEAXE_CHOP,
                FightType.BATTLEAXE_HACK, FightType.BATTLEAXE_SMASH,
                FightType.BATTLEAXE_BLOCK}, 7499, 7511),
        GREATAXE(1698, 1701, 7, new FightType[]{FightType.GREATAXE_CHOP,
                FightType.GREATAXE_HACK, FightType.GREATAXE_SMASH,
                FightType.GREATAXE_BLOCK}, 7499, 7511),
        CROSSBOW(1764, 1767, 6, new FightType[]{FightType.CROSSBOW_ACCURATE,
                FightType.CROSSBOW_RAPID, FightType.CROSSBOW_LONGRANGE}, 7549, 7561),
        BALLISTA(1764, 1767, 7, new FightType[]{FightType.BALLISTA_ACCURATE,
                FightType.BALLISTA_RAPID, FightType.BALLISTA_LONGRANGE}, 7549, 7561),
        BLOWPIPE(1764, 1767, 3, new FightType[]{FightType.BLOWPIPE_ACCURATE,
                FightType.BLOWPIPE_RAPID, FightType.BLOWPIPE_LONGRANGE}, 7549, 7561),
        KARILS_CROSSBOW(1764, 1767, 4, new FightType[]{FightType.KARILS_CROSSBOW_ACCURATE,
                FightType.KARILS_CROSSBOW_RAPID, FightType.KARILS_CROSSBOW_LONGRANGE}, 7549, 7561),
        SHORTBOW(1764, 1767, 4, new FightType[]{FightType.SHORTBOW_ACCURATE,
                FightType.SHORTBOW_RAPID, FightType.SHORTBOW_LONGRANGE}, 7549, 7561),
        LONGBOW(1764, 1767, 6, new FightType[]{FightType.LONGBOW_ACCURATE,
                FightType.LONGBOW_RAPID, FightType.LONGBOW_LONGRANGE}, 7549, 7561),
        DRAGON_DAGGER(2276, 2279, 4, new FightType[]{FightType.DRAGON_DAGGER_STAB,
                FightType.DRAGON_DAGGER_LUNGE, FightType.DRAGON_DAGGER_SLASH,
                FightType.DRAGON_DAGGER_BLOCK}, 7574, 7586),
        ABYSSAL_DAGGER(2276, 2279, 4, new FightType[]{FightType.DRAGON_DAGGER_STAB,
                FightType.DRAGON_DAGGER_LUNGE, FightType.DRAGON_DAGGER_SLASH,
                FightType.DRAGON_DAGGER_BLOCK}, 7574, 7586),
        DAGGER(2276, 2279, 4, new FightType[]{FightType.DAGGER_STAB,
                FightType.DAGGER_LUNGE, FightType.DAGGER_SLASH,
                FightType.DAGGER_BLOCK}, 7574, 7586),
        SWORD(2276, 2279, 5, new FightType[]{FightType.SWORD_STAB,
                FightType.SWORD_LUNGE, FightType.SWORD_SLASH,
                FightType.SWORD_BLOCK}, 7574, 7586),
        SCIMITAR(2423, 2426, 4, new FightType[]{FightType.SCIMITAR_CHOP,
                FightType.SCIMITAR_SLASH, FightType.SCIMITAR_LUNGE,
                FightType.SCIMITAR_BLOCK}, 7599, 7611),
        LONGSWORD(2423, 2426, 5, new FightType[]{FightType.LONGSWORD_CHOP,
                FightType.LONGSWORD_SLASH, FightType.LONGSWORD_LUNGE,
                FightType.LONGSWORD_BLOCK}, 7599, 7611),
        MACE(3796, 3799, 5, new FightType[]{FightType.MACE_POUND,
                FightType.MACE_PUMMEL, FightType.MACE_SPIKE,
                FightType.MACE_BLOCK}, 7624, 7636),
        KNIFE(4446, 4449, 3, new FightType[]{FightType.KNIFE_ACCURATE,
                FightType.KNIFE_RAPID, FightType.KNIFE_LONGRANGE}, 7649, 7661),
        OBBY_RINGS(4446, 4449, 4, new FightType[]{FightType.OBBY_RING_ACCURATE,
                FightType.OBBY_RING_RAPID, FightType.OBBY_RING_LONGRANGE}, 7649, 7661),
        SPEAR(4679, 4682, 5, new FightType[]{FightType.SPEAR_LUNGE,
                FightType.SPEAR_SWIPE, FightType.SPEAR_POUND,
                FightType.SPEAR_BLOCK}, 7674, 7686),
        TWO_HANDED_SWORD(4705, 4708, 7, new FightType[]{
                FightType.TWOHANDEDSWORD_CHOP, FightType.TWOHANDEDSWORD_SLASH,
                FightType.TWOHANDEDSWORD_SMASH, FightType.TWOHANDEDSWORD_BLOCK}, 7699, 7711),
        PICKAXE(5570, 5573, 5, new FightType[]{FightType.PICKAXE_SPIKE,
                FightType.PICKAXE_IMPALE, FightType.PICKAXE_SMASH,
                FightType.PICKAXE_BLOCK}),
        CLAWS(7762, 7765, 4, new FightType[]{FightType.CLAWS_CHOP,
                FightType.CLAWS_SLASH, FightType.CLAWS_LUNGE,
                FightType.CLAWS_BLOCK}, 7800, 7812),
        HALBERD(8460, 8463, 7, new FightType[]{FightType.HALBERD_JAB,
                FightType.HALBERD_SWIPE, FightType.HALBERD_FEND}, 8493, 8505),
        UNARMED(5855, 5857, 4, new FightType[]{FightType.UNARMED_PUNCH,
                FightType.UNARMED_KICK, FightType.UNARMED_BLOCK}),
        WHIP(12290, 12293, 4, new FightType[]{FightType.WHIP_FLICK,
                FightType.WHIP_LASH, FightType.WHIP_DEFLECT}, 12323, 12335),
        THROWNAXE(4446, 4449, 4, new FightType[]{
                FightType.THROWNAXE_ACCURATE, FightType.THROWNAXE_RAPID,
                FightType.THROWNAXE_LONGRANGE}, 7649, 7661),
        DART(4446, 4449, 3, new FightType[]{FightType.DART_ACCURATE,
                FightType.DART_RAPID, FightType.DART_LONGRANGE}, 7649, 7661),
        JAVELIN(4446, 4449, 4, new FightType[]{FightType.JAVELIN_ACCURATE,
                FightType.JAVELIN_RAPID, FightType.JAVELIN_LONGRANGE}, 7649, 7661),
        ANCIENT_STAFF(328, 355, 4, new FightType[]{FightType.STAFF_BASH, FightType.STAFF_POUND, FightType.STAFF_FOCUS}),
        DARK_BOW(1764, 1767, 8, new FightType[]{FightType.LONGBOW_ACCURATE,
                FightType.LONGBOW_RAPID, FightType.LONGBOW_LONGRANGE}, 7549, 7561),
        GODSWORD(4705, 4708, 6, new FightType[]{
                FightType.TWOHANDEDSWORD_CHOP, FightType.TWOHANDEDSWORD_SLASH,
                FightType.TWOHANDEDSWORD_SMASH, FightType.TWOHANDEDSWORD_BLOCK}, 7699, 7711),
        ABYSSAL_BLUDGEON(4705, 4708, 4, new FightType[]{
                FightType.ABYSSAL_BLUDGEON_CHOP, FightType.ABYSSAL_BLUDGEON_SLASH,
                FightType.ABYSSAL_BLUDGEON_SMASH, FightType.ABYSSAL_BLUDGEON_BLOCK}, 7699, 7711),
        SARADOMIN_SWORD(4705, 4708, 4, new FightType[]{
                FightType.TWOHANDEDSWORD_CHOP, FightType.TWOHANDEDSWORD_SLASH,
                FightType.TWOHANDEDSWORD_SMASH, FightType.TWOHANDEDSWORD_BLOCK}, 7699, 7711),
        ELDER_MAUL(425, 428, 6, new FightType[]{FightType.ELDER_MAUL_POUND,
                FightType.ELDER_MAUL_PUMMEL, FightType.ELDER_MAUL_BLOCK}, 7474, 7486);

        /**
         * The interface that will be displayed on the sidebar.
         */
        private int interfaceId;

        /**
         * The line that the name of the item will be printed to.
         */
        private int nameLineId;

        /**
         * The attack speed of weapons using this interface.
         */
        private int speed;

        /**
         * The fight types that correspond with this interface.
         */
        private FightType[] fightType;

        /**
         * The id of the special bar for this interface.
         */
        private int specialBar;

        /**
         * The id of the special meter for this interface.
         */
        private int specialMeter;

        /**
         * Creates a new weapon interface.
         *
         * @param interfaceId  the interface that will be displayed on the sidebar.
         * @param nameLineId   the line that the name of the item will be printed to.
         * @param speed        the attack speed of weapons using this interface.
         * @param fightType    the fight types that correspond with this interface.
         * @param specialBar   the id of the special bar for this interface.
         * @param specialMeter the id of the special meter for this interface.
         */
        private WeaponInterface(int interfaceId, int nameLineId, int speed,
                                FightType[] fightType, int specialBar, int specialMeter) {
            this.interfaceId = interfaceId;
            this.nameLineId = nameLineId;
            this.speed = speed;
            this.fightType = fightType;
            this.specialBar = specialBar;
            this.specialMeter = specialMeter;
        }

        /**
         * Creates a new weapon interface.
         *
         * @param interfaceId the interface that will be displayed on the sidebar.
         * @param nameLineId  the line that the name of the item will be printed to.
         * @param speed       the attack speed of weapons using this interface.
         * @param fightType   the fight types that correspond with this interface.
         */
        private WeaponInterface(int interfaceId, int nameLineId, int speed,
                                FightType[] fightType) {
            this(interfaceId, nameLineId, speed, fightType, -1, -1);
        }

        /**
         * Gets the interface that will be displayed on the sidebar.
         *
         * @return the interface id.
         */
        public int getInterfaceId() {
            return interfaceId;
        }

        /**
         * Gets the line that the name of the item will be printed to.
         *
         * @return the name line id.
         */
        public int getNameLineId() {
            return nameLineId;
        }

        /**
         * Gets the attack speed of weapons using this interface.
         *
         * @return the attack speed of weapons using this interface.
         */
        public int getSpeed() {
            return speed;
        }

        /**
         * Gets the fight types that correspond with this interface.
         *
         * @return the fight types that correspond with this interface.
         */
        public FightType[] getFightType() {
            return fightType;
        }

        /**
         * Gets the id of the special bar for this interface.
         *
         * @return the id of the special bar for this interface.
         */
        public int getSpecialBar() {
            return specialBar;
        }

        /**
         * Gets the id of the special meter for this interface.
         *
         * @return the id of the special meter for this interface.
         */
        public int getSpecialMeter() {
            return specialMeter;
        }
    }
}
