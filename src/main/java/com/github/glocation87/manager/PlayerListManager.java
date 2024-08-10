package com.github.glocation87.manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.inventory.Inventory;

import com.github.glocation87.util.CustomInventory;
import com.github.glocation87.util.CustomInventoryHolder;

public class PlayerListManager {

    private static final int SLOTS_COUNT = 5;
    private static final String TITLE = "      §l§k|||§r§l" + "  §lPlayer List  " + "§l§k|||§r§l";
    private static final CustomInventoryHolder HOLDER = new CustomInventoryHolder("player_list");
    private static final Map<Player, Integer> playerPages = new HashMap<>();

    private static ItemStack getPlayerHead(Player player) {
        ItemStack playerHead = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta skullMeta = (SkullMeta) playerHead.getItemMeta();
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "UUID: " + ChatColor.GREEN + player.getUniqueId().toString());
        lore.add(ChatColor.GRAY + "Ping: " + ChatColor.GREEN + player.getPing() + "ms");
        lore.add(ChatColor.GRAY + "Rank: " + ChatColor.GREEN + "Member");
        if (skullMeta != null) {
            skullMeta.setOwningPlayer(player);
            skullMeta.setDisplayName(ChatColor.GREEN + player.getName());
            skullMeta.setLore(lore);
            playerHead.setItemMeta(skullMeta);
        }
        return playerHead;
    }

    public static void openPlayerListUI(Player player, int page) {
        CustomInventory playerListInventory = new CustomInventory(HOLDER, SLOTS_COUNT, TITLE);
        Player[] onlinePlayers = Bukkit.getOnlinePlayers().toArray(new Player[0]);

        // Add player heads
        int startIndex = page * playerListInventory.getAvailableSlots();
        int endIndex = Math.min(startIndex + playerListInventory.getAvailableSlots(), onlinePlayers.length);

        for (int i = startIndex; i < endIndex; i++) {
            Player indexedPlayer = onlinePlayers[i];

            int slotNumber = 10 + (i - startIndex) % 7 + ((i - startIndex) / 7) * 9;
            playerListInventory.setItem(slotNumber, getPlayerHead(indexedPlayer));
        }

        // Add navigation buttons
        if (page > 0) {
            int slotNumber = SLOTS_COUNT * 9 - 9;
            playerListInventory.addInventoryItem(Material.RED_STAINED_GLASS_PANE, ChatColor.GREEN + "Previous Page", slotNumber);
        }

        if (endIndex < onlinePlayers.length) {
            int slotNumber = SLOTS_COUNT * 9 - 1;
            playerListInventory.addInventoryItem(Material.GREEN_STAINED_GLASS_PANE, ChatColor.GREEN + "Next Page", slotNumber);
        }

        playerPages.put(player, page);
        player.openInventory(playerListInventory.getInventory());
    }

    public static void handleInventoryClick(Player admin, InventoryHolder inventoryHolder, int slot) {
        if (!(inventoryHolder instanceof CustomInventoryHolder)) return;

        CustomInventoryHolder holder = (CustomInventoryHolder) inventoryHolder;
        Inventory playerInventory = holder.getInventory();
        if (playerInventory == null || slot >= playerInventory.getSize()) {
            System.out.println("Invalid slot: " + slot);
            return;
        }

        ItemStack item = playerInventory.getItem(slot);
        if (item == null) {
            System.out.println("No item found in slot: " + slot);
            return;
        }

        ItemMeta itemMeta = item.getItemMeta();
        String itemName = ChatColor.stripColor(itemMeta.getDisplayName());
        Material itemMaterial = item.getType();
        int currentPage = playerPages.getOrDefault(admin, 0);

        if (itemMaterial == Material.PLAYER_HEAD) {
            SkullMeta skullMeta = (SkullMeta) itemMeta;
            OfflinePlayer targetPlayer = skullMeta.getOwningPlayer();
            if (targetPlayer != null && targetPlayer.isOnline()) {
                Player onlinePlayer = Bukkit.getPlayer(targetPlayer.getUniqueId());
                PlayerProfileManager playerProfile = new PlayerProfileManager(admin, onlinePlayer);
                admin.closeInventory();
                playerProfile.openPlayerMenuUI();
            } else {
                admin.sendMessage(ChatColor.YELLOW + "Selected player is currently offline");
            }
        } else if ("Previous Page".equals(itemName) && currentPage > 0) {
            currentPage--;
            playerPages.put(admin, currentPage);
            admin.closeInventory();
            openPlayerListUI(admin, currentPage);
        } else if ("Next Page".equals(itemName) && (currentPage + 1) * 21 < 100) { //100 being an arbitruary value for the max players online
            currentPage++;
            playerPages.put(admin, currentPage);
            admin.closeInventory();
            openPlayerListUI(admin, currentPage);
        }
    }
}
