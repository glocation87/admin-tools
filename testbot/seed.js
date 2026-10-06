// Ops the admin bot before the dev server starts, so nobody has to type in the console
const fs = require("fs");
const path = require("path");
const crypto = require("crypto");

const names = process.argv.slice(2);
if (names.length === 0) {
    names.push("ATAdmin");
}

// Offline mode UUIDs are version 3 UUIDs of "OfflinePlayer:<name>", same as the server computes
function offlineUuid(name) {
    const hash = crypto.createHash("md5").update("OfflinePlayer:" + name, "utf8").digest();
    hash[6] = (hash[6] & 0x0f) | 0x30;
    hash[8] = (hash[8] & 0x3f) | 0x80;
    const hex = hash.toString("hex");
    return `${hex.slice(0, 8)}-${hex.slice(8, 12)}-${hex.slice(12, 16)}-${hex.slice(16, 20)}-${hex.slice(20)}`;
}

const runDir = path.join(__dirname, "..", "run");
fs.mkdirSync(runDir, { recursive: true });
const opsFile = path.join(runDir, "ops.json");
const ops = fs.existsSync(opsFile) ? JSON.parse(fs.readFileSync(opsFile, "utf8")) : [];
for (const name of names) {
    if (!ops.some(op => op.name === name)) {
        ops.push({ uuid: offlineUuid(name), name, level: 4, bypassesPlayerLimit: false });
    }
}
fs.writeFileSync(opsFile, JSON.stringify(ops, null, 2));
console.log("opped " + names.join(", ") + " in " + opsFile);
