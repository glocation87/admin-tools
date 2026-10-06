// Two bots run through the suite and print PASS/FAIL per check. ATAdmin must be op, `node seed.js` does that before the server starts.
const { connect, spawned, waitForChat, sawChat, nextWindow, windowTitle, sleep, log } = require("./lib");

const admin = connect("ATAdmin");
const target = connect("ATTarget");
const results = [];

function check(label, ok, detail) {
    results.push(ok);
    log("selftest", `${ok ? "PASS" : "FAIL"} ${label}${detail ? `  (${detail})` : ""}`);
}

const now = () => Date.now();
const hotbar = (bot, slot) => bot.inventory.slots[36 + slot];
const itemName = item => (item ? item.name : "empty");
const distance = () => {
    const other = admin.players[target.username]?.entity;
    return other ? other.position.distanceTo(admin.entity.position) : Infinity;
};

async function main() {
    await Promise.all([spawned(admin), spawned(target)]);
    await sleep(1500);
    admin.chat("/gamemode survival");
    admin.chat("/clear");
    admin.chat("/give ATAdmin minecraft:diamond 3");
    admin.chat("/tp ATTarget ATAdmin");
    await sleep(1500);

    // staff mode
    let since = now();
    admin.chat("/staff");
    check("staff mode on", (await waitForChat(admin, /Staff mode on/, since)) !== null);
    await sleep(500);
    check("hotbar holds the staff tools", itemName(hotbar(admin, 0)) === "enchanted_book" && itemName(hotbar(admin, 8)) === "barrier",
        `slot0=${itemName(hotbar(admin, 0))} slot8=${itemName(hotbar(admin, 8))}`);
    check("own items are gone", !admin.inventory.items().some(item => item.name === "diamond"));
    check("entering staff mode vanishes you", !target.players[admin.username]?.entity, "target cannot see the admin entity");

    since = now();
    admin.chat("/vanish");
    await waitForChat(admin, /visible again/, since);
    await sleep(800);
    check("unvanish shows you again", !!target.players[admin.username]?.entity);

    // menus
    admin.chat("/players");
    let window = await nextWindow(admin);
    check("player list opens", window !== null && /Players/.test(windowTitle(window)), window && windowTitle(window));
    if (window) {
        const slot = [...Array(window.slots.length).keys()].find(i => window.slots[i] && window.slots[i].name === "player_head");
        const next = nextWindow(admin);
        admin.clickWindow(slot, 0, 0);
        window = await next;
        check("clicking a head opens the player menu", window !== null && /ATTarget|ATAdmin/.test(windowTitle(window)), window && windowTitle(window));
        admin.closeWindow(window);
    }
    admin.chat("/admin");
    window = await nextWindow(admin);
    check("dashboard opens", window !== null && /Dashboard/.test(windowTitle(window)), window && windowTitle(window));
    if (window) {
        admin.closeWindow(window);
    }
    admin.chat("/punish ATTarget");
    window = await nextWindow(admin);
    check("punish menu opens", window !== null && /Punish/.test(windowTitle(window)), window && windowTitle(window));
    if (window) {
        admin.closeWindow(window);
    }

    // freeze
    since = now();
    admin.chat("/freeze ATTarget");
    check("target hears about the freeze", (await waitForChat(target, /frozen/i, since)) !== null);
    const before = target.entity.position.clone();
    target.setControlState("forward", true);
    await sleep(1500);
    target.setControlState("forward", false);
    await sleep(300);
    check("frozen player cannot move", target.entity.position.distanceTo(before) < 1, `moved ${target.entity.position.distanceTo(before).toFixed(2)}`);
    since = now();
    target.chat("/gamemode creative");
    check("frozen player cannot run commands", (await waitForChat(target, /cannot use commands/, since)) !== null);
    admin.chat("/freeze ATTarget");
    await waitForChat(target, /unfrozen/, since);

    // mute
    since = now();
    admin.chat("/mute ATTarget 5m spamming");
    check("target hears about the mute", (await waitForChat(target, /muted for 5m/, since)) !== null);
    since = now();
    target.chat("hello from the muted one");
    await sleep(800);
    check("muted chat is blocked", !sawChat(admin, /hello from the muted one/, since) && sawChat(target, /You are muted/, since));
    since = now();
    admin.chat("/unmute ATTarget");
    await waitForChat(target, /unmuted/, since);
    since = now();
    target.chat("hello after the mute");
    check("chat works again", (await waitForChat(admin, /hello after the mute/, since)) !== null);

    // warn and history
    since = now();
    admin.chat("/warn ATTarget be nice");
    check("warning reaches the target", (await waitForChat(target, /warned: be nice/, since)) !== null);
    admin.chat("/history ATTarget");
    window = await nextWindow(admin);
    check("history menu lists the record", window !== null && window.slots.some(item => item && item.name === "name_tag")
        && window.slots.some(item => item && item.name === "yellow_wool"), window && windowTitle(window));
    if (window) {
        admin.closeWindow(window);
    }

    // staff chat and command spy
    since = now();
    admin.chat("/sc testing staff chat");
    await sleep(800);
    check("staff chat reaches staff only", sawChat(admin, /\[SC\].*testing staff chat/, since) && !sawChat(target, /testing staff chat/, since));
    since = now();
    admin.chat("/cspy");
    await waitForChat(admin, /Command spy/, since);
    since = now();
    target.chat("/list");
    check("command spy shows player commands", (await waitForChat(admin, /\[Spy\].*ATTarget.*\/list/, since)) !== null);
    admin.chat("/cspy");

    // chat control
    since = now();
    admin.chat("/chatlock");
    await waitForChat(target, /Chat has been locked/, since);
    since = now();
    target.chat("locked out");
    check("chat lock blocks players", (await waitForChat(target, /Chat is locked/, since)) !== null && !sawChat(admin, /locked out/, since));
    admin.chat("/chatlock");

    // reports
    since = now();
    target.chat("/report ATAdmin abusing powers");
    check("report is acknowledged", (await waitForChat(target, /staff have been notified/, since)) !== null);
    check("staff get the report", (await waitForChat(admin, /Report.*ATTarget.*abusing powers/, since)) !== null);

    // helpers
    since = now();
    admin.chat("/gm creative ATTarget");
    await sleep(800);
    check("gamemode command", target.game.gameMode === "creative", target.game.gameMode);
    admin.chat("/gm survival ATTarget");
    admin.chat("/tp ATTarget ~ ~ ~20");
    await sleep(800);
    admin.chat("/tphere ATTarget");
    await sleep(1000);
    check("tphere pulls the target over", distance() < 3, `distance ${distance().toFixed(1)}`);
    since = now();
    admin.chat("/whois ATTarget");
    check("whois prints a summary", (await waitForChat(admin, /First joined/, since)) !== null);
    since = now();
    admin.chat("/seen ATTarget");
    check("seen knows they are online", (await waitForChat(admin, /online right now/, since)) !== null);

    // leaving staff mode gives the inventory back
    since = now();
    admin.chat("/staff");
    check("staff mode off", (await waitForChat(admin, /Staff mode off/, since)) !== null);
    await sleep(800);
    check("own items come back", admin.inventory.items().some(item => item.name === "diamond" && item.count === 3));

    // kick last, it ends the target's session
    since = now();
    const kicked = new Promise(resolve => target.once("kicked", reason => resolve(JSON.stringify(reason))));
    admin.chat("/kick ATTarget test over");
    const reason = await Promise.race([kicked, sleep(3000).then(() => null)]);
    check("kick disconnects with the reason", reason !== null && /test over/.test(reason), reason);

    const passed = results.filter(Boolean).length;
    log("selftest", `${passed}/${results.length} checks passed`);
    admin.quit();
    target.quit();
    process.exit(passed === results.length ? 0 : 1);
}

main().catch(err => {
    log("selftest", "crashed: " + err.stack);
    process.exit(2);
});
