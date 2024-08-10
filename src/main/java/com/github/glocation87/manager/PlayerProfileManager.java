package com.github.glocation87.manager;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import com.github.glocation87.util.CustomInventory;
import com.github.glocation87.util.CustomInventoryHolder;

public class PlayerProfileManager {
    //Maps
    private static final Map<UUID, PlayerProfileManager> profileManagers = new HashMap<>();

    //Properties
    private final Player selectedPlayer;
    private final Player currentAdmin;

    //Main Menu Constants
    private static final int MAIN_MENU_SLOTS_COUNT = 3;
    private static final int TELEPORT_SLOT = 10;
    private static final int MUTE_SLOT = 11;
    private static final int KICK_SLOT = 12;
    private static final int BAN_SLOT = 13;
    private static final int KILL_SLOT = 14;
    private static final int FREEZE_SLOT = 16;
    private static final CustomInventoryHolder MAIN_MENU_HOLDER = new CustomInventoryHolder("player_main_menu");

    //Teleport Menu Constants
    private static final int TELEPORT_MENU_SLOTS_COUNT = 3;
    private static final int TELEPORT_GOTO_SLOT = 10;
    private static final int TELEPORT_BRING_SLOT = 16;
    private static final CustomInventoryHolder TELEPORT_MENU_HOLDER = new CustomInventoryHolder("player_teleport_menu");

    private void initProfile() {
        profileManagers.put(this.currentAdmin.getUniqueId(), this);
    }

    public PlayerProfileManager(Player _currentAdmin, Player _selectedPlayer) {
        this.currentAdmin = _currentAdmin;
        this.selectedPlayer = _selectedPlayer;
        initProfile();
    }

    public static PlayerProfileManager getProfileManager(Player admin) {
        return profileManagers.get(admin.getUniqueId());
    }

    public static void removeProfileManager(Player admin) {
        profileManagers.remove(admin.getUniqueId());
    }

    public Player getSelectedPlayer() {
        return selectedPlayer;
    }

    public void openPlayerTeleportMenu() {
        CustomInventory playerMenuInventory = new CustomInventory(TELEPORT_MENU_HOLDER, TELEPORT_MENU_SLOTS_COUNT, "§l§k|||§r§l" + "  §lTeleport Menu"  + "  §l§k|||§r§l");

        // Add items to the inventory
        playerMenuInventory.addInventoryItem(Material.ENDER_PEARL, ChatColor.GREEN + "§lGo To Player", TELEPORT_GOTO_SLOT);
        playerMenuInventory.addInventoryItem(Material.BLACK_STAINED_GLASS_PANE, "", 11);
        playerMenuInventory.addInventoryItem(Material.BLACK_STAINED_GLASS_PANE, "", 12);
        playerMenuInventory.addInventoryItem(Material.BLACK_STAINED_GLASS_PANE, "", 13);
        playerMenuInventory.addInventoryItem(Material.BLACK_STAINED_GLASS_PANE, "", 14);
        playerMenuInventory.addInventoryItem(Material.BLACK_STAINED_GLASS_PANE, "", 15);
        playerMenuInventory.addInventoryItem(Material.ENDER_EYE, ChatColor.GREEN + "§lBring Player", TELEPORT_BRING_SLOT);

        this.currentAdmin.openInventory(playerMenuInventory.getInventory());
    }

    public void openPlayerMenuUI() {
        CustomInventory playerMenuInventory = new CustomInventory(MAIN_MENU_HOLDER, MAIN_MENU_SLOTS_COUNT, "§l§k|||§r§l" + "  §l" + ChatColor.stripColor(selectedPlayer.getName()) + "  §l§k|||§r§l");

        // Add items to the inventory
        playerMenuInventory.addInventoryItem(Material.COMPASS, ChatColor.GREEN + "§lTeleport", TELEPORT_SLOT);
        playerMenuInventory.addInventoryItem(Material.NAME_TAG, ChatColor.GREEN + "§lMute", MUTE_SLOT);
        playerMenuInventory.addInventoryItem(Material.IRON_DOOR, ChatColor.GREEN + "§lKick", KICK_SLOT);
        playerMenuInventory.addInventoryItem(Material.ANVIL, ChatColor.GREEN + "§lBan", BAN_SLOT);
        playerMenuInventory.addInventoryItem(Material.SKELETON_SKULL, ChatColor.GREEN + "§lKill", KILL_SLOT);
        playerMenuInventory.addInventoryItem(Material.PAPER, ChatColor.GREEN + "§lPlayer Information", 15);
        playerMenuInventory.addInventoryItem(Material.COBWEB, ChatColor.GREEN + "§lFreeze", FREEZE_SLOT);

        this.currentAdmin.openInventory(playerMenuInventory.getInventory());
    }

    public static void handlePlayerMenuClick(Player admin, InventoryHolder inventoryHolder, int slot) {
        PlayerProfileManager profileManager = getProfileManager(admin);
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

        if (itemName == null) {
            System.out.println("Item Name is null");
            return;
        }

        switch (itemName) {
            case "Teleport":
                admin.closeInventory();
                profileManager.openPlayerTeleportMenu();
                break;
            
            default:
                break;
        }
        System.out.println(itemName);
    }
}