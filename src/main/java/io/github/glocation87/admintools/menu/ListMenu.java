package io.github.glocation87.admintools.menu;

import io.github.glocation87.admintools.AdminToolsPlugin;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

// Pages of items in a 7 wide grid inside the border, nav row at the bottom
public abstract class ListMenu<T> extends Menu {
    private int page;

    protected ListMenu(AdminToolsPlugin plugin, Player viewer, Menu parent, String title, int rows) {
        super(plugin, viewer, parent, title, rows);
    }

    protected abstract List<T> items();

    protected abstract ItemStack icon(T item);

    protected abstract void onClick(T item, ClickType type);

    // Extra buttons in the bottom row, slots 1, 2, 6 and 7 are free
    protected void extras() {
    }

    protected int perPage() {
        return 7 * (rows() - 2);
    }

    protected int bottom() {
        return (rows() - 1) * 9;
    }

    @Override
    protected void draw() {
        border();
        navigation();
        List<T> all = items();
        int pages = Math.max(1, (all.size() + perPage() - 1) / perPage());
        page = Math.min(page, pages - 1);
        int start = page * perPage();
        int end = Math.min(start + perPage(), all.size());
        for (int index = start; index < end; index++) {
            int offset = index - start;
            int slot = 10 + offset % 7 + offset / 7 * 9;
            T item = all.get(index);
            button(slot, icon(item), type -> onClick(item, type));
        }
        if (page > 0) {
            button(bottom() + 3, Icons.of(Material.RED_STAINED_GLASS_PANE, "<yellow>Previous page"), () -> {
                page--;
                refresh();
            });
        }
        set(bottom() + 4, Icons.of(Material.PAPER, "<white>Page " + (page + 1) + " of " + pages, all.size() + " entries"));
        if (end < all.size()) {
            button(bottom() + 5, Icons.of(Material.LIME_STAINED_GLASS_PANE, "<yellow>Next page"), () -> {
                page++;
                refresh();
            });
        }
        extras();
    }
}
