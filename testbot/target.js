// A dummy player to practise on: wanders, chats now and then, runs the odd command.
// node target.js [name] [count]
const { connect, spawned, sleep, log } = require("./lib");

const name = process.argv[2] ?? "Dummy";
const count = Number(process.argv[3] ?? 1);
const LINES = ["anyone selling diamonds?", "lag?", "how do i get to spawn", "gg", "brb", "nice base"];

async function run(username) {
    const bot = connect(username);
    await spawned(bot);
    log(username, "spawned, say !come to call it over or !stop to park it");
    let wander = true;
    bot.on("chat", (from, message) => {
        if (from === username) {
            return;
        }
        if (message === "!come") {
            const player = bot.players[from]?.entity;
            if (player) {
                wander = false;
                bot.lookAt(player.position);
                bot.setControlState("forward", true);
                setTimeout(() => bot.setControlState("forward", false), 1500);
            }
        } else if (message === "!stop") {
            wander = false;
            bot.clearControlStates();
        } else if (message === "!go") {
            wander = true;
        }
    });
    for (let tick = 0; ; tick++) {
        await sleep(3000);
        if (!wander) {
            continue;
        }
        bot.look(Math.random() * Math.PI * 2, 0);
        bot.setControlState("forward", true);
        await sleep(800);
        bot.setControlState("forward", false);
        if (tick % 10 === 5) {
            bot.chat(LINES[Math.floor(Math.random() * LINES.length)]);
        }
        if (tick % 25 === 12) {
            bot.chat("/list");
        }
    }
}

for (let i = 0; i < count; i++) {
    run(count === 1 ? name : name + (i + 1)).catch(err => log(name, err.message));
}
