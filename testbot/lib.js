const mineflayer = require("mineflayer");

const HOST = process.env.HOST ?? "localhost";
const PORT = Number(process.env.PORT ?? 25569);

const sleep = ms => new Promise(resolve => setTimeout(resolve, ms));
const log = (who, msg) => console.log(`[${new Date().toISOString().slice(11, 23)}] ${who}: ${msg}`);

// mineflayer is on 26.1, ViaBackwards on the dev server translates to 26.2
function connect(username) {
    const bot = mineflayer.createBot({ host: HOST, port: PORT, username, version: "26.1", auth: "offline" });
    bot.chatLog = [];
    bot.on("message", (message, position) => {
        const text = message.toString();
        bot.chatLog.push({ text, position, time: Date.now() });
    });
    bot.on("kicked", reason => log(username, "kicked " + JSON.stringify(reason)));
    bot.on("error", err => log(username, "error " + err.message));
    return bot;
}

function spawned(bot) {
    return new Promise((resolve, reject) => {
        bot.once("spawn", resolve);
        bot.once("end", () => reject(new Error(bot.username + " disconnected before spawning")));
    });
}

// Waits for a chat line matching the pattern that arrived after `since`
async function waitForChat(bot, pattern, since = 0, timeout = 4000) {
    const deadline = Date.now() + timeout;
    while (Date.now() < deadline) {
        const hit = bot.chatLog.find(line => line.time >= since && pattern.test(line.text));
        if (hit) {
            return hit.text;
        }
        await sleep(50);
    }
    return null;
}

function sawChat(bot, pattern, since = 0) {
    return bot.chatLog.some(line => line.time >= since && pattern.test(line.text));
}

// Resolves with the next inventory window the server opens for the bot
function nextWindow(bot, timeout = 4000) {
    return new Promise(resolve => {
        const timer = setTimeout(() => resolve(null), timeout);
        bot.once("windowOpen", window => {
            clearTimeout(timer);
            resolve(window);
        });
    });
}

const windowTitle = window => {
    try {
        return JSON.stringify(window.title);
    } catch (e) {
        return String(window.title);
    }
};

module.exports = { HOST, PORT, connect, spawned, waitForChat, sawChat, nextWindow, windowTitle, sleep, log };
