package io.github.glocation87.admintools.staff;

import java.util.List;
import org.bukkit.Material;

public enum StaffItem {
    PLAYER_LIST(0, Material.ENCHANTED_BOOK, "<light_purple><b>Player List", "Right click to browse everyone online"),
    INSPECTOR(1, Material.SPYGLASS, "<aqua><b>Inspector", "Right click a block or entity", "to see what makes it tick"),
    FREEZE_WAND(2, Material.BLUE_ICE, "<aqua><b>Freeze Wand", "Hit a player to freeze them,", "hit them again to thaw"),
    PEEK(3, Material.CHEST, "<gold><b>Inventory Peek", "Right click a player to watch", "their inventory live"),
    VANISH(4, Material.LIME_DYE, "<green><b>Vanish", "Right click to toggle"),
    RANDOM_TP(5, Material.ENDER_PEARL, "<dark_purple><b>Random Teleport", "Right click to drop in on", "a random player"),
    DASHBOARD(6, Material.CLOCK, "<yellow><b>Server Dashboard", "Right click for tps, worlds,", "entities, chunks and plugins"),
    COMMAND_SPY(7, Material.PAPER, "<gray><b>Command Spy", "Right click to toggle seeing", "the commands players run"),
    EXIT(8, Material.BARRIER, "<red><b>Exit Staff Mode", "Right click to get your", "own inventory back");

    private final int slot;
    private final Material material;
    private final String title;
    private final List<String> lore;

    StaffItem(int slot, Material material, String title, String... lore) {
        this.slot = slot;
        this.material = material;
        this.title = title;
        this.lore = List.of(lore);
    }

    public int slot() {
        return slot;
    }

    public Material material() {
        return material;
    }

    public String title() {
        return title;
    }

    public List<String> lore() {
        return lore;
    }

    public boolean toggle() {
        return this == VANISH || this == COMMAND_SPY;
    }
}
