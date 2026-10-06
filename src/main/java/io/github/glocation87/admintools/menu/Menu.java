package io.github.glocation87.admintools.menu;

import io.github.glocation87.admintools.AdminToolsPlugin;
import io.github.glocation87.admintools.Text;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

public abstract class Menu implements InventoryHolder {
    protected final AdminToolsPlugin plugin;
    protected final Player viewer;
    private final Menu parent;
    private final Inventory inventory;
    private final int rows;
    private final Map<Integer, Consumer<ClickType>> actions = new HashMap<>();

    @SuppressWarnings("this-escape")
    protected Menu(AdminToolsPlugin plugin, Player viewer, Menu parent, String title, int rows) {
        this.plugin = plugin;
        this.viewer = viewer;
        this.parent = parent;
        this.rows = rows;
        this.inventory = Bukkit.createInventory(this, rows * 9, Text.item(title));
    }

    protected abstract void draw();

    // Live menus get redrawn on the refresh timer while open
    public boolean live() {
        return false;
    }

    public void open() {
        plugin.menus().open(viewer, this);
    }

    public void refresh() {
        inventory.clear();
        actions.clear();
        draw();
    }

    public void onClose() {
    }

    void click(int slot, ClickType type) {
        Consumer<ClickType> action = actions.get(slot);
        if (action != null) {
            action.accept(type);
        }
    }

    protected void set(int slot, ItemStack icon) {
        inventory.setItem(slot, icon);
        actions.remove(slot);
    }

    protected void button(int slot, ItemStack icon, Consumer<ClickType> action) {
        inventory.setItem(slot, icon);
        actions.put(slot, action);
    }

    protected void button(int slot, ItemStack icon, Runnable action) {
        button(slot, icon, type -> action.run());
    }

    protected void border() {
        ItemStack filler = Icons.filler();
        for (int slot = 0; slot < rows * 9; slot++) {
            if (slot < 9 || slot >= rows * 9 - 9 || slot % 9 == 0 || slot % 9 == 8) {
                set(slot, filler);
            }
        }
    }

    protected void fill() {
        ItemStack filler = Icons.filler();
        for (int slot = 0; slot < rows * 9; slot++) {
            if (inventory.getItem(slot) == null) {
                set(slot, filler);
            }
        }
    }

    protected void navigation() {
        int bottom = (rows - 1) * 9;
        if (parent != null) {
            button(bottom, Icons.of(Material.ARROW, "<yellow>Back", "To the previous menu"), this::back);
        }
        button(bottom + 8, Icons.of(Material.BARRIER, "<red>Close"), () -> viewer.closeInventory());
    }

    protected void back() {
        if (parent != null) {
            parent.open();
        } else {
            viewer.closeInventory();
        }
    }

    protected void confirm(String title, ItemStack subject, Runnable action) {
        new ConfirmMenu(plugin, viewer, this, title, subject, action).open();
    }

    protected void message(String mini) {
        Text.send(viewer, mini);
    }

    protected int rows() {
        return rows;
    }

    public Menu parent() {
        return parent;
    }

    public Player viewer() {
        return viewer;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}
