const DEF_REQUIREMENTS = [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0];

const DEF_ITEM = {
    id: -1,
    name: '',
    examine: '',
    doubleHanded: false,
    stackable: true,
    tradeable: true,
    dropable: true,
    sellable: true,
    noted: false,
    value: 0,
    highAlch: 0,
    lowAlch: 0,
    dropValue: 0,
    noteId: -1,
    blockAnim: 424,
    standAnim: 808,
    walkAnim: 819,
    runAnim: 824,
    standTurnAnim: 823,
    turn180Anim: 820,
    turn90CWAnim: 821,
    turn90CCWAnim: 821,
    weight: 0.0,
};

const GENERATE_FROM_ID = 21392;
const GENERATE_TO_ID = 26562;

const COMPLETE_ITEMS = require('/home/detuks/Projects/runescape/cache/tools/osrsbox-db/docs/items-complete.json');

const slotToEquipmentType = (slot) => {
    switch (slot) {
        case '2h':
            return 'WEAPON';
        case 'ammo':
            return 'ARROWS';
        case 'body':
            return 'PLATEBODY'; // COuld be BODY as well dunno
        case 'cape':
            return 'CAPE';
        case 'feet':
            return 'BOOTS';
        case 'hands':
            return 'GLOVES';
        case 'head':
            return 'FULL_HELMET';
        case 'legs':
            return 'LEGS';
        case 'neck':
            return 'AMULET';
        case 'ring':
            return 'RING';
        case 'shield':
            return 'SHIELD';
        case 'weapon':
            return 'WEAPON';
        default:
            throw new Error(`Failed to get equipment type for ${slot}`);
    }
};

const weaponToWeaponType = (slot) => {
    switch (slot) {
        case 'unarmed':
            return 'UNARMED';
        case 'slash_sword':
            return 'SCIMITAR';
        case 'spear':
            return 'SPEAR'; // COuld be BODY as well dunno
        case 'blunt':
            return 'WARHAMMER';
        case 'crossbow':
            return 'CROSSBOW';
        case 'powered_staff':
            return 'STAFF';
        case 'bladed_staff':
            return 'STAFF';
        case 'stab_sword':
            return 'SWORD';
        case 'scythe':
            return 'SCYTHE';
        case 'bow':
            return 'LONGBOW';
        case 'staff':
            return 'STAFF';
        case 'spiked':
            return 'MACE';
        case 'thrown':
            return 'DART';
        case 'pickaxe':
            return 'PICKAXE';
        case 'axe':
            return 'BATTLEAXE';
        case 'polearm':
            return 'HALBERD';
        case 'banner':
            return undefined;
        case '2h_sword':
            return 'GODSWORD';
        case 'whip':
            return 'WHIP';
        default:
            throw new Error(`Failed to get weapon type for ${slot}`);
    }
};

const generatedItems = [];
const weaponTypes = new Set();

let itemId = GENERATE_FROM_ID;
while (true) {
    itemId++;

    if (itemId > GENERATE_TO_ID) {
        break;
    }

    const inputItem = COMPLETE_ITEMS[itemId];

    if (!inputItem) {
        continue;
    }

    const outputItem = {
        ...DEF_ITEM,
        id: inputItem.id,
        name: inputItem.name,
        examine: inputItem.examine,
        stackable: inputItem.stackable,
        tradeable: inputItem.tradeable,
        dropable: true,
        sellable: inputItem.tradeable,
        noted: inputItem.noted,
        value: inputItem.cost ?? 0,
        lowAlch: inputItem.lowalch ?? 0,
        highAlch: inputItem.highalch ?? 0,
        dropValue: inputItem.dropValue ?? 0,
        noteId: inputItem.linked_id_noted ?? inputItem.linked_id_item ?? -1,
        weight: inputItem.weight,
    };

    const equipment = inputItem.equipment;

    if (equipment) {
        const bonuses = [
            equipment.attack_stab,
            equipment.attack_slash,
            equipment.attack_crush,
            equipment.attack_magic,
            equipment.attack_ranged,
            equipment.defence_stab,
            equipment.defence_slash,
            equipment.defence_crush,
            equipment.defence_magic,
            equipment.defence_ranged,
            equipment.melee_strength,
            equipment.ranged_strength,
            equipment.magic_damage,
            equipment.prayer,
        ];
        outputItem.bonuses = bonuses;

        const slot = equipment.slot;
        if (slot === '2h') {
            outputItem.doubleHanded = true;
        }
        outputItem.equipmentType = slotToEquipmentType(slot);

        if (equipment.requirements) {
            outputItem.requirements = [...DEF_REQUIREMENTS];

            if (equipment.requirements.attack) {
                outputItem.requirements[0] = equipment.requirements.attack;
            }
            if (equipment.requirements.ranged) {
                outputItem.requirements[4] = equipment.requirements.ranged;
            }
            if (equipment.requirements.magic) {
                outputItem.requirements[6] = equipment.requirements.magic;
            }
            if (equipment.requirements.defence) {
                outputItem.requirements[1] = equipment.requirements.defence;
            }
            if (equipment.requirements.strength) {
                outputItem.requirements[2] = equipment.requirements.strength;
            }
            if (equipment.requirements.prayer) {
                outputItem.requirements[5] = equipment.requirements.prayer;
            }
            if (equipment.requirements.hitpoints) {
                outputItem.requirements[3] = equipment.requirements.hitpoints;
            }
        }
    }
    const weapon = inputItem.weapon;

    if (weapon) {
        outputItem.weaponInterface = weaponToWeaponType(weapon.weapon_type);
    }
    generatedItems.push(outputItem);
}

console.log(JSON.stringify(generatedItems, null, 2));


const examples = [
    {
        id: 11834,
        name: 'Bandos tassets',
        examine: 'A sturdy pair of tassets.',
        equipmentType: 'LEGS',
        doubleHanded: false,
        stackable: false,
        tradeable: true,
        dropable: true,
        sellable: true,
        noted: false,
        value: 23639087,
        highAlch: 173946,
        lowAlch: 115964,
        dropValue: 289910,
        noteId: 11835,
        blockAnim: 424,
        standAnim: 808,
        walkAnim: 819,
        runAnim: 824,
        standTurnAnim: 823,
        turn180Anim: 820,
        turn90CWAnim: 821,
        turn90CCWAnim: 821,
        weight: 8.0,
        bonuses: [0.0, 0.0, 0.0, -21.0, -7.0, 71.0, 63.0, 66.0, -4.0, 93.0, 2.0, 0.0, 0.0, 1.0],
        requirements: [0, 65, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0],
    },
    {
        id: 11835,
        name: 'Bandos tassets',
        examine: 'Swap this note at any bank for the equivalent item.',
        equipmentType: 'LEGS',
        doubleHanded: false,
        stackable: true,
        tradeable: true,
        dropable: true,
        sellable: true,
        noted: true,
        value: 23639087,
        highAlch: 173946,
        lowAlch: 115964,
        dropValue: 289910,
        noteId: 11834,
        blockAnim: 424,
        standAnim: 808,
        walkAnim: 819,
        runAnim: 824,
        standTurnAnim: 823,
        turn180Anim: 820,
        turn90CWAnim: 821,
        turn90CCWAnim: 821,
        weight: 0.0,
    },
];

const imputExmaple = {
    11834: {
        id: 11834,
        name: 'Bandos tassets',
        last_updated: '2021-08-05',
        incomplete: false,
        members: true,
        tradeable: true,
        tradeable_on_ge: true,
        stackable: false,
        stacked: null,
        noted: false,
        noteable: true,
        linked_id_item: null,
        linked_id_noted: 11835,
        linked_id_placeholder: 18356,
        placeholder: false,
        equipable: true,
        equipable_by_player: true,
        equipable_weapon: false,
        cost: 289910,
        lowalch: 115964,
        highalch: 173946,
        weight: 8,
        buy_limit: 8,
        quest_item: false,
        release_date: '2013-10-17',
        duplicate: false,
        examine: 'A sturdy pair of tassets.',
        icon: 'iVBORw0KGgoAAAANSUhEUgAAACQAAAAgCAYAAAB6kdqOAAAC/UlEQVR4Xu3WUWsaQRAH8NznyIuQh0JAkIKU4EuKSBBFKopiREmUoMQqBmPUakRrYk2TNDZNbFKRVBpaKP2OU/8re+6tZ6u5q09dGPTQu/3d3OzOraz8H6YMhYyFqUMhy+qqoTARNcakQqGZkYmEaS8YoITfT1Gvh8JuNwVdLvJubpLb4SDXxoaZKIXysdjMyEa3GYh/Aghc3OdTcUA5bDYzUArV0ikqJ5O6IYLwvbATp2JiV4MDyu98ybJkt1qNohRqVavUqFRY1EslNXrXr+nxa4eG/RMa3DXp4UuLvg3a9GN4RjfdDIv9EToRjVIkGKRXHg+9sNsZxmaxsJBnm2MoKkYE1YpFNiEAgPR7DRaAAdi9SFEunaa9eJxi4TAFRo/Pu7WlAckzzTkmIDE7eqDbbpXub+rs+/m7pJqd0KjQkR2Ano/qaGmgT1cVFp+va3T6dleTHQ6yrq+bDwJGBiEzHy9KdNk5ZJ+N4xjtbEc02XE7neaB5OyIINQNB3VaB3TeLlC1HJnKDkDP1taWD2o1snRUCKrZMR2kh+EgvuRRN1fvj6jdzFGjmqZ81j+VHQTfg0wBiRgRhIKWQdl93xTINWojiKWC8Miax5kpkIj6ZyDs1LyGAOI1BBB/ZHpZWioIKwxFDRCKeqmgu9sc62XYhwDCpoh9CKBCPqABcZRB0LixzgPCsucgFDYHyVkCiC99ebY5hhYkw/q9POvsIoivNOxD6PBylgAy0GD1QRw1C4TCFkEiCnVkCCTv0mIM7g8YCEsfIHR7gFDY5WJYN0umg8bPXqHh4JB+ff+ggvjrB0CoI/QyoPBfMUsAOZ7+fj0GccTkrmaDsPSBqr+JMhQ/TwY9sesrdNZsqCfywPFDv6ALQh0BhQBKvAkxDPS0CYYfA4P4+XipAWEvQnAY3onEScUbMgAaX2hyNAahoEUQdmu8tnZOEizwxojQn9QgSDsUhkFg2WNzRAtBo5Ufy3R2tdeZ/dvCY3picQI55LP5+NNvCw950r9Nvuj4DcReG8eswMDzAAAAAElFTkSuQmCC',
        wiki_name: 'Bandos tassets',
        wiki_url: 'https://oldschool.runescape.wiki/w/Bandos_tassets',
        equipment: {
            attack_stab: 0,
            attack_slash: 0,
            attack_crush: 0,
            attack_magic: -21,
            attack_ranged: -7,
            defence_stab: 71,
            defence_slash: 63,
            defence_crush: 66,
            defence_magic: -4,
            defence_ranged: 93,
            melee_strength: 2,
            ranged_strength: 0,
            magic_damage: 0,
            prayer: 1,
            slot: 'legs',
            requirements: {
                defence: 65,
            },
        },
        weapon: null,
    },
    11835: {
        id: 11835,
        name: 'Bandos tassets',
        last_updated: '2021-08-05',
        incomplete: false,
        members: true,
        tradeable: true,
        tradeable_on_ge: false,
        stackable: false,
        stacked: null,
        noted: true,
        noteable: true,
        linked_id_item: 11834,
        linked_id_noted: null,
        linked_id_placeholder: null,
        placeholder: false,
        equipable: false,
        equipable_by_player: false,
        equipable_weapon: false,
        cost: 289910,
        lowalch: 115964,
        highalch: 173946,
        weight: 8,
        buy_limit: null,
        quest_item: false,
        release_date: '2013-10-17',
        duplicate: true,
        examine: 'A sturdy pair of tassets.',
        icon: 'iVBORw0KGgoAAAANSUhEUgAAACQAAAAgCAYAAAB6kdqOAAADfklEQVR4Xs3WW0sbQRQH8ORz+CL40Ne+ii8WEQkWqSgGKxEvFEttRYnRajQkTZuq9X6JlzT1irfWtiLWXhQUq71Ralvp1zn1P8tZZyeTktVYHDhkd2eX+eXMzGEcjsvfnKQPXf+FNye9mgvQ2kyHiOfTflp52i6CEXsbvbS73kM7r7s1KPVPnBtugBgFEIeK2X75mN6uPrIMjHv0qWFF2UI6aX0+mIDCLyAcDNpaCdPGYki8i18dCO/L6OPDOfq5HzsbikG45uww6P1aRID43WQgfPf7YIb+fFk6iWUBwhhXMzNFqKMrzUg7/2sGYVAdaHPpgQnCtQ60v9lnAeGaQeroSnNapgED8eLGPUBy6lMJgA63BtMH4izhGfrrysosUV/uplulJVRZVERul4uK866RKyeH8rOzKTMjQ3zz+Z0MWrIH4nWhgng3easqzfDVVAtQo8cj7mUcYAz6+mHIAvr18dn5QXiO/mBbGwVaW0VEh+9SfCJE01Nhmo09pPl4hEYH66muupo8bjfdKCwU33zbHkkvCMEYBnX4fGJwgMYG/TQ5GqBYNEh3amup6mY5lZxk6XpBgfjux27UBB1/WjAxtkEySs2ODBoZaKOBnhYa6m2lmooKKisuFtlx5eWJ7452x7UgdfQkzdgd2MJ2QF3hJhHlpaUCg+yYoL2ps4KMGoQM6UAyRp2yJxEvBf23xVRxdvJzcxNBUlFUR1easeV1IESqIDk7aQcxCpEMhB02PtxBfV3NFOqsv7gMyVnCrw6EbY/tDhAWdTh4z1zQ+jW0bGJSAmGH6UBcFFXQxFiDBRQJNQgQZ4m3vZyhFLODdgpS1xH6wn5/AigWbRTFEDUIW553GaPSDmKUCmJUfLIpAdTiPQVxpZZBjFJH1zQDpJs29Mk1CPez8WZ6sdhPCzNdokKjFmFh3/e5RT+jrmRliXuAUINs1CErSJ42Bhn/zIjpmJdWF/pMELY+shRor7C8J2fINgi7LBmoO2TUIpxv8IvpAghThlqEaQMq2OkR/Tg74WD2fWf0/CB12vAc/RhABmGXoRapGcGJEqCDN/1W0EkNsl2HdNOG5xgEIFzzzuPg4onv+FCPd9MC0k2bDEKffCzBNYPwHh/q0wBCs6Y+WcgYOVQQztIyiMMGyOHgsv6vUIFyyOsnLaBUm4pMBctx+u5/aipSF+o3l7r9BT42Fjh1E5vJAAAAAElFTkSuQmCC',
        wiki_name: 'Bandos tassets',
        wiki_url: 'https://oldschool.runescape.wiki/w/Bandos_tassets',
        equipment: null,
        weapon: null,
    },
};
