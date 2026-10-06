# AdminTools
Staff mode, moderation and server debugging through chest menus, for Paper 26.2

`/staff` swaps your inventory for a hotbar of tools and keeps you safe: no damage, no hunger, no block edits, mobs ignore you, and your real inventory is written to disk so a crash mid-shift cannot lose it.

| Slot | Tool | What it does |
| --- | --- | --- |
| 1 | Player List | Everyone online with ping, world, badges, click to manage |
| 2 | Inspector | Right click a block or mob for its data, light, redstone, AI, equipment, plugin data |
| 3 | Freeze Wand | Hit a player to freeze them in place, hit again to thaw |
| 4 | Inventory Peek | Right click a player to watch their inventory live, shift right click deletes a stack |
| 5 | Vanish | Hide from everyone without `admintools.vanish.see` |
| 6 | Random Teleport | Drop in on a random player |
| 7 | Server Dashboard | TPS and tick time bars, memory, worlds, entities, hot chunks, plugins, tick control |
| 8 | Command Spy | See the commands other players run |
| 9 | Exit | Back to your own inventory and game mode |

## Menus

Every menu is a chest inventory with a back button, and live ones redraw every second.

| Menu | Highlights |
| --- | --- |
| Player | Teleport, peek, ender chest, debug info, effects, notes, spectate, freeze, mute, kick, punish, kill, heal, clear, game mode, fly, god, smite, op, history |
| Punish | Mineplex style: pick chat or gameplay offense and a severity, the ladder sets the length and doubles it for every prior in that category, permanent after enough strikes |
| History | Warns, kicks, mutes and bans with who, when, why, still active |
| Reports | What players sent with `/report`, teleport to them, punish, or close |
| Dashboard | TPS, tick time and memory with 30 second history bars, uptime, load, chat lock, slow chat, clear chat |
| Worlds | Time and weather presets, difficulty, border size, game rules editor, save |
| Entities | Counts by type across worlds, teleport to the nearest one, wipe a type |
| Hot Chunks | The loaded chunks with the most entities and tile entities, click to go there |
| Plugins | Versions and authors, enable and disable at runtime |
| Tick Control | Freeze the game, step 1, 20 or 100 ticks, change the tick rate, sprint |
| Diagnostics | Run GC, write thread dumps and heap reports to `plugins/AdminTools/dumps`, save all, whitelist, broadcast |

## Commands

| Command | Does |
| --- | --- |
| `/staff`, `/staff list` | Toggle staff mode, see who is on duty |
| `/admin`, `/players`, `/reports` | Open the dashboard, player list or reports |
| `/manage <p>`, `/punish <p>`, `/history <p>`, `/notes <p>` | Open that player's menus, the last three work offline |
| `/warn <p> <reason>`, `/kick <p> [reason]` | Record and apply |
| `/mute <p> [time] [reason]`, `/unmute <p>` | Chat mute, `30m`, `2h`, `7d` or `perm` |
| `/ban <p> [time] [reason]`, `/unban <p>` | Vanilla ban list plus the history record |
| `/freeze <p>` | Lock them in place, only `freeze.allowed-commands` still work |
| `/vanish`, `/cspy`, `/sc [message]` | Vanish, command spy, staff chat (no message toggles it) |
| `/invsee <p>`, `/ecsee <p>`, `/spectate <p>`, `/tphere <p>`, `/tpall` | Looking and moving |
| `/whois <p>`, `/seen <p>`, `/alts <p>`, `/ping <p>` | Info, alts are accounts that shared an address |
| `/inspect` | Inspect whatever you are looking at |
| `/fly`, `/god`, `/heal`, `/feed`, `/gm <mode>`, `/gmc`, `/gms`, `/gma`, `/gmsp`, `/speed <1-10>` | Take an optional player |
| `/clearchat`, `/chatlock`, `/slowchat <seconds>`, `/broadcast <message>` | Chat control |
| `/report <p> <reason>` | For players, staff get pinged |
| `/admintools reload` | Reread `config.yml` |

`admintools.staff` unlocks everything above, `admintools.admin` adds the dashboard, `/tpall` and erasing history. Both default to op.

## Config

Offense ladders, escalation, the default mute length, the staff game mode and the frozen command whitelist all live in `config.yml`. Mutes, freezes, notes, history, reports, addresses and staff inventory snapshots are in `plugins/AdminTools/data.yml`.

## Running it

```
./gradlew runServer
```

Starts a dev server on port 25569 with ViaVersion and ViaBackwards, in offline mode so bots can join.

## Test bots

Needs Node 22. Install once with `npm install` inside `testbot/`.

- `node seed.js` ops `ATAdmin` before the server starts
- `node target.js [name] [count]` joins dummy players that wander and chat, say `!come` to call one over
- `node selftest.js` runs an admin bot and a target bot through staff mode, menus, freeze, mute, warn, staff chat, command spy, chat lock, reports and the helpers, printing PASS or FAIL per check
