package io.github.glocation87.admintools.inspect;

import io.github.glocation87.admintools.AdminToolsPlugin;
import io.github.glocation87.admintools.Text;
import io.github.glocation87.admintools.menu.Icons;
import io.github.glocation87.admintools.menu.Menu;
import io.github.glocation87.admintools.menu.players.NotesMenu;
import io.github.glocation87.admintools.menu.players.PeekMenu;
import java.util.ArrayList;
import java.util.List;
import net.kyori.adventure.text.event.ClickEvent;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.block.Sign;
import org.bukkit.block.TileState;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public final class BlockInspectMenu extends Menu {
    private final Block block;

    public BlockInspectMenu(AdminToolsPlugin plugin, Player viewer, Menu parent, Block block) {
        super(plugin, viewer, parent, "<aqua><b>" + name(block), 4);
        this.block = block;
    }

    private static String name(Block block) {
        return block.getType().getKey().getKey().replace('_', ' ');
    }

    @Override
    public boolean live() {
        return true;
    }

    @Override
    protected void draw() {
        border();
        navigation();
        Material icon = block.getType().isItem() && !block.getType().isAir() ? block.getType() : Material.BARRIER;
        set(4, Icons.of(icon, "<aqua>" + name(block), "At: <white>" + Text.coords(block.getLocation())));

        set(10, Icons.of(Material.STONE, "<aqua>Block", blockInfo()));
        button(11, Icons.of(Material.COMPASS, "<aqua>Position", position()), this::teleport);
        set(12, Icons.of(Material.TORCH, "<aqua>Light", "Total: <white>" + block.getLightLevel(), "Sky: <white>" + block.getLightFromSky(),
            "Block: <white>" + block.getLightFromBlocks()));
        set(13, Icons.of(Material.REDSTONE, "<aqua>Redstone", "Power: <white>" + block.getBlockPower(),
            "Powered: <white>" + block.isBlockPowered(), "Indirectly: <white>" + block.isBlockIndirectlyPowered()));
        BlockState state = block.getState();
        if (state instanceof Container container) {
            List<String> lore = new ArrayList<>(tile(state));
            lore.add("");
            lore.add("<yellow>Click to peek inside");
            button(14, Icons.of(Material.CHEST, "<aqua>Container", lore),
                () -> PeekMenu.container(plugin, viewer, this, "<gold><b>" + name(block), container.getInventory()).open());
        } else {
            set(14, Icons.of(Material.CHEST, "<aqua>Tile Entity", tile(state)));
        }
        set(15, Icons.of(Material.MAP, "<aqua>Chunk", chunk()));

        button(19, Icons.of(Material.ENDER_PEARL, "<green>Teleport On Top"), this::teleport);
        button(20, Icons.of(Material.IRON_PICKAXE, "<red>Break", "Drops like a player broke it"), () -> {
            block.breakNaturally();
            message("<red>Broke the block");
            back();
        });
        button(21, Icons.of(Material.STRUCTURE_VOID, "<red>Set To Air", "No drops"), () -> {
            block.setType(Material.AIR);
            message("<red>Removed the block");
            back();
        });
        button(22, Icons.of(Material.WRITABLE_BOOK, "<yellow>Copy Block Data", "Puts the data string in chat,", "click it there to copy"), () -> {
            String data = block.getBlockData().getAsString();
            viewer.sendMessage(Text.mm(Text.PREFIX + "<white>" + Text.escape(data) + " <dark_gray>(click to copy)")
                .clickEvent(ClickEvent.copyToClipboard(data)));
        });
        button(23, Icons.of(Material.COMMAND_BLOCK, "<yellow>Copy /setblock", "Suggests the command in chat"), () -> {
            String command = "/setblock " + block.getX() + " " + block.getY() + " " + block.getZ() + " " + block.getBlockData().getAsString();
            viewer.sendMessage(Text.mm(Text.PREFIX + "<white>" + Text.escape(command) + " <dark_gray>(click to paste)")
                .clickEvent(ClickEvent.suggestCommand(command)));
        });
    }

    private void teleport() {
        viewer.teleportAsync(block.getLocation().add(0.5, 1, 0.5));
    }

    private List<String> blockInfo() {
        List<String> lore = new ArrayList<>();
        lore.add("Type: <white>" + block.getType().getKey());
        lore.addAll(NotesMenu.wrap(Text.escape(block.getBlockData().getAsString(true))));
        lore.add("Hardness: <white>" + Text.number(block.getType().getHardness()) + " <gray>Blast: <white>" + Text.number(block.getType().getBlastResistance()));
        lore.add("Solid: <white>" + block.isSolid() + " <gray>Liquid: <white>" + block.isLiquid() + " <gray>Replaceable: <white>" + block.isReplaceable());
        return lore;
    }

    private List<String> position() {
        List<String> lore = new ArrayList<>();
        lore.add("World: <white>" + block.getWorld().getName());
        lore.add("XYZ: <white>" + block.getX() + ", " + block.getY() + ", " + block.getZ());
        lore.add("Biome: <white>" + block.getBiome().getKey().getKey());
        lore.add("Temperature: <white>" + Text.number(block.getTemperature()) + " <gray>Humidity: <white>" + Text.number(block.getHumidity()));
        lore.add("");
        lore.add("<yellow>Click to teleport on top");
        return lore;
    }

    private List<String> tile(BlockState state) {
        List<String> lore = new ArrayList<>();
        if (!(state instanceof TileState tile)) {
            lore.add("<dark_gray>Not a tile entity");
            return lore;
        }
        lore.add("Kind: <white>" + state.getClass().getSimpleName().replace("Craft", ""));
        if (state instanceof Container container) {
            int stacks = 0;
            for (ItemStack item : container.getInventory().getContents()) {
                if (item != null && !item.isEmpty()) {
                    stacks++;
                }
            }
            lore.add("Stacks: <white>" + stacks + " <gray>of " + container.getInventory().getSize());
        }
        if (state instanceof Sign sign) {
            for (var line : sign.getSide(org.bukkit.block.sign.Side.FRONT).lines()) {
                lore.add("<white>" + Text.escape(Text.plain(line)));
            }
        }
        List<String> keys = new ArrayList<>();
        for (NamespacedKey key : tile.getPersistentDataContainer().getKeys()) {
            keys.add(key.toString());
        }
        lore.add("Plugin data: <white>" + (keys.isEmpty() ? "none" : String.join(", ", keys)));
        return lore;
    }

    private List<String> chunk() {
        Chunk chunk = block.getChunk();
        List<String> lore = new ArrayList<>();
        lore.add("Chunk: <white>" + chunk.getX() + ", " + chunk.getZ());
        lore.add("Entities: <white>" + chunk.getEntities().length + " <gray>Tiles: <white>" + chunk.getTileEntities(false).length);
        lore.add("Inhabited: <white>" + chunk.getInhabitedTime() / 20 / 60 + " min");
        lore.add("Force loaded: <white>" + chunk.isForceLoaded() + " <gray>Level: <white>" + chunk.getLoadLevel().name().toLowerCase(java.util.Locale.ROOT));
        return lore;
    }
}
