package io.github.glocation87.admintools.menu.players;

import io.github.glocation87.admintools.AdminToolsPlugin;
import io.github.glocation87.admintools.Text;
import io.github.glocation87.admintools.menu.Icons;
import io.github.glocation87.admintools.menu.Menu;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

// Read only live mirror of any inventory, shift right click deletes a stack
public final class PeekMenu extends Menu {
    private final Supplier<Inventory> source;
    private final String owner;

    private PeekMenu(AdminToolsPlugin plugin, Player viewer, Menu parent, String title, String owner, Supplier<Inventory> source) {
        super(plugin, viewer, parent, title, rowsFor(source.get().getSize()));
        this.source = source;
        this.owner = owner;
    }

    public static PeekMenu inventory(AdminToolsPlugin plugin, Player viewer, Menu parent, Player target) {
        return new PeekMenu(plugin, viewer, parent, "<gold><b>" + target.getName() + "'s inventory", target.getName(), target::getInventory);
    }

    public static PeekMenu enderChest(AdminToolsPlugin plugin, Player viewer, Menu parent, Player target) {
        return new PeekMenu(plugin, viewer, parent, "<dark_purple><b>" + target.getName() + "'s ender chest", target.getName(),
            target::getEnderChest);
    }

    public static PeekMenu container(AdminToolsPlugin plugin, Player viewer, Menu parent, String title, Inventory inventory) {
        return new PeekMenu(plugin, viewer, parent, title, Text.plain(Text.mm(title)), () -> inventory);
    }

    private static int rowsFor(int size) {
        return Math.min(6, (size + 8) / 9 + 1);
    }

    @Override
    public boolean live() {
        return true;
    }

    @Override
    protected void draw() {
        Inventory inventory = source.get();
        ItemStack[] contents = inventory.getContents();
        int shown = Math.min(contents.length, rows() * 9);
        boolean hasNavRow = contents.length <= (rows() - 1) * 9;
        if (hasNavRow) {
            shown = Math.min(shown, (rows() - 1) * 9);
        }
        for (int slot = 0; slot < shown; slot++) {
            ItemStack item = contents[slot];
            if (item == null || item.isEmpty()) {
                continue;
            }
            int index = slot;
            button(slot, hinted(item), type -> {
                if (type.isShiftClick() && type.isRightClick()) {
                    inventory.setItem(index, null);
                    message("<red>Deleted <white>" + Text.plain(item.displayName()) + " <red>from " + owner);
                    refresh();
                }
            });
        }
        if (hasNavRow) {
            int bottom = (rows() - 1) * 9;
            for (int slot = shown; slot < bottom; slot++) {
                set(slot, Icons.filler());
            }
            for (int slot = bottom; slot < bottom + 9; slot++) {
                set(slot, Icons.filler());
            }
            navigation();
            button(bottom + 4, Icons.of(Material.LAVA_BUCKET, "<red>Clear Everything"), () ->
                confirm("<red>Clear " + owner + "?", Icons.of(Material.LAVA_BUCKET, "<red>Clear it all"), () -> {
                    inventory.clear();
                    message("<red>Cleared " + owner);
                }));
        }
    }

    private ItemStack hinted(ItemStack item) {
        ItemStack copy = item.clone();
        copy.editMeta(meta -> {
            List<Component> lore = meta.hasLore() ? new ArrayList<>(meta.lore()) : new ArrayList<>();
            lore.add(Text.item("<dark_gray>Shift + right click to delete"));
            meta.lore(lore);
        });
        return copy;
    }
}
