const items = require("../data/definitions/items.json");

const usedNames = new Set();

const isNumber = (char) => {
    if (typeof char !== 'string') {
        return false;
    }

    if (char.trim() === '') {
        return false;
    }

    return !isNaN(char);
}

const toJavaName = (name) => {
    const newName = name.trim().toUpperCase()
        .replaceAll(" (", "_")
        .replaceAll(") ", "_")
        .replaceAll("(", "_")
        .replaceAll(")", "_")
        .replaceAll(" ", "_")
        .replaceAll("'", "")
        .replaceAll("+", "_PLUS")
        .replaceAll("-", "_")
        .replaceAll(".", "_")
        .replaceAll("/", "_")
        .replaceAll("?", "_")
        .replaceAll("&", "_")

    return numberify(newName, 0);
}

const numberify = (name, num) => {
    let n = (!num) ? name : name + "_" + (num + 1);

    n = n.replaceAll("__", "_");

    if (isNumber(n[0])) {
        n = "_" + n;
    }

    if (!usedNames.has(n)) {
        usedNames.add(n);
        return n;
    }
    return numberify(name, num + 1);
}

for (const item of items) {

    const name = toJavaName(item.name);

    console.log(`public static final int ${name} = ${item.id};`)

}
