package com.github.glocation87.manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import com.github.glocation87.util.CustomInventoryHolder;

public class PlayerListManager {
    private static final int SLOTS_COUNT = 5;
    private static final int PLAYERS_PER_PAGE = 21;
    private static final String TITLE = ChatColor.GOLD + "§l§k|||§r§l" + "  §lPlayer List  " + "§l§k|||§r§l";
    private static final CustomInventoryHolder HOLDER = new CustomInventoryHolder("player_list");
    private static List<ItemStack> dummyPlayers;
    private static Map<Player, Integer> playerPages = new HashMap<>();

    // Initialize dummy players for testing
    static {
        dummyPlayers = new ArrayList<>();
        for (int i = 1; i <= 100; i++) {
            ItemStack playerHead = new ItemStack(Material.PLAYER_HEAD, 1);
            SkullMeta skullMeta = (SkullMeta) playerHead.getItemMeta();
            skullMeta.setDisplayName(ChatColor.YELLOW + "§lDummyPlayer" + i);

            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "UUID: " + UUID.randomUUID().toString());
            lore.add(ChatColor.GRAY + "Ping: " + (int) (Math.random() * 100) + "ms");
            skullMeta.setLore(lore);

            playerHead.setItemMeta(skullMeta);
            dummyPlayers.add(playerHead);
        }
    }

    public static void openPlayerListUI(Player player, int page) {
        Inventory playerListInventory = Bukkit.createInventory(HOLDER, SLOTS_COUNT * 9, TITLE);

        // Fill the borders with colored glass panes
        ItemStack glassPane = new ItemStack(Material.BLACK_STAINED_GLASS_PANE, 1);
        ItemMeta glassMeta = glassPane.getItemMeta();
        glassMeta.setDisplayName(" ");
        glassPane.setItemMeta(glassMeta);

        for (int i = 0; i < SLOTS_COUNT * 9; i++) {
            if (i < 9 || i >= SLOTS_COUNT * 9 - 9 || i % 9 == 0 || i % 9 == 8) {
                playerListInventory.setItem(i, glassPane);
            }
        }

        // Add player heads
        int startIndex = page * PLAYERS_PER_PAGE;
        int endIndex = Math.min(startIndex + PLAYERS_PER_PAGE, dummyPlayers.size());

        for (int i = startIndex; i < endIndex; i++) {
            int slotNumber = 10 + (i - startIndex) % 7 + ((i - startIndex) / 7) * 9;
            playerListInventory.setItem(slotNumber, dummyPlayers.get(i));
        }

        // Add navigation buttons
        if (page > 0) {
            ItemStack previousPage = new ItemStack(Material.RED_STAINED_GLASS_PANE);
            ItemMeta previousMeta = previousPage.getItemMeta();
            previousMeta.setDisplayName(ChatColor.GREEN + "Previous Page");
            previousPage.setItemMeta(previousMeta);
            playerListInventory.setItem(SLOTS_COUNT * 9 - 9, previousPage);
        }

        if (endIndex < dummyPlayers.size()) {
            ItemStack nextPage = new ItemStack(Material.GREEN_STAINED_GLASS_PANE);
            ItemMeta nextMeta = nextPage.getItemMeta();
            nextMeta.setDisplayName(ChatColor.GREEN + "Next Page");
            nextPage.setItemMeta(nextMeta);
            playerListInventory.setItem(SLOTS_COUNT * 9 - 1, nextPage);
        }

        playerPages.put(player, page);
        player.openInventory(playerListInventory);
    }

    public static void handleInventoryClick(Player player, InventoryHolder inventoryHolder, int slot) {
        CustomInventoryHolder holder = (CustomInventoryHolder) inventoryHolder;
        if (holder != null && !"player_list".equals(holder.getIdentifier()))
            return;

        int currentPage = playerPages.getOrDefault(player, 0);

        if (slot == SLOTS_COUNT * 9 - 9 && currentPage > 0) {
            // Previous page button clicked
            currentPage--;
            playerPages.put(player, currentPage);
            player.closeInventory();
            openPlayerListUI(player, currentPage);
        } else if (slot == SLOTS_COUNT * 9 - 1 && (currentPage + 1) * PLAYERS_PER_PAGE < dummyPlayers.size()) {
            // Next page button clicked
            currentPage++;
            playerPages.put(player, currentPage);
            player.closeInventory();
            openPlayerListUI(player, currentPage);
        }
    }
}
