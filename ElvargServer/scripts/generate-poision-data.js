const readline = require('readline');
const fs = require('fs');

const items = require('../data/definitions/items.json');

const rangedInterfaces = new Set([
    'CROSSBOW',
    'THROWNAXE',
    'DART',
    'SHORTBOW',
    'LONGBOW',
    'KNIFE',
    'KARILS_CROSSBOW',
    'OBBY_RINGS',
    'DARK_BOW',
    'BLOWPIPE',
    'BALLISTA',
]);

const readInterface = readline.createInterface({
    input: fs.createReadStream('../src/main/java/com/elvarg/util/ItemIdentifiers.java'),
    console: false,
});

const itemIdentifierNameMap = {};

readInterface.on('line', (line) => {
    if (!line.includes('public static final int')) {
        return;
    }
    const cleanLine = line.replace('public static final int', '').replaceAll(';', '').replaceAll(' ', '');

    const parts = cleanLine.split('=');
    const name = parts[0];
    const id = parts[1];

    itemIdentifierNameMap[id] = name;
});

readInterface.on('close', () => {
    for (const item of items) {
        if (item.noted || (item.equipmentType !== "WEAPON" && item.equipmentType !== "ARROWS")) {
            continue;
        }
        const isRanged = item.equipmentType === "ARROWS" || rangedInterfaces.has(item.weaponInterface);
        if (item.name?.includes('(p)')) {
            if (!isRanged) {
                console.log(`types.put(ItemIdentifiers.${itemIdentifierNameMap['' + item.id]}, PoisonType.MILD);`);
            } else {
                console.log(`types.put(ItemIdentifiers.${itemIdentifierNameMap['' + item.id]}, PoisonType.VERY_WEAK);`);
            }
        }
        if (item.name?.includes('(p+)')) {
            if (!isRanged) {
                console.log(`types.put(ItemIdentifiers.${itemIdentifierNameMap['' + item.id]}, PoisonType.EXTRA);`);
            } else {
                console.log(`types.put(ItemIdentifiers.${itemIdentifierNameMap['' + item.id]}, PoisonType.WEAK);`);
            }
        }
        if (item.name?.includes('(p++)')) {
            if (!isRanged) {
                console.log(`types.put(ItemIdentifiers.${itemIdentifierNameMap['' + item.id]}, PoisonType.SUPER);`);
            } else {
                console.log(`types.put(ItemIdentifiers.${itemIdentifierNameMap['' + item.id]}, PoisonType.MILD);`);
            }
        }
    }
});

// console.log(itemIdentifierFile);
