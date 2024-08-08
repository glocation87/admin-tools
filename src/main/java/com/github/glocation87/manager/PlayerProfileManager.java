package com.github.glocation87.manager;

import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import com.github.glocation87.util.CustomInventory;
import com.github.glocation87.util.CustomInventoryHolder;

public class PlayerProfileManager {

    //Main Menu Constants
    private static final int MAIN_MENU_SLOTS_COUNT = 3;
    private static final CustomInventoryHolder MAIN_MENU_HOLDER = new CustomInventoryHolder("player_main_menu");

    public static void openPlayerMenuUI(Player admin, String playerName, UUID playerUUID) {
        CustomInventory playerMenuInventory = new CustomInventory(MAIN_MENU_HOLDER, MAIN_MENU_SLOTS_COUNT, "    §l§k|||§r§l" + "  §l" + ChatColor.stripColor(playerName) + "  §l§k|||§r§l");
        admin.openInventory(playerMenuInventory.getInventory());
    }

}
