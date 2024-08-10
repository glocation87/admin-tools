package com.github.glocation87.util;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.google.common.collect.Multimap;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

//wrapper class
public class CustomInventory {

    private final CustomInventoryHolder customInventoryHolder;
    private final Inventory virtualInventory;
    private final int SLOT_COUNT;
    private final int ITEMS_PER_PAGE;
    private final String TITLE;

    public CustomInventory(CustomInventoryHolder _customInventoryHolder, int _slotCount, String _title) {
        this.SLOT_COUNT = _slotCount;
        this.TITLE = _title;
        this.ITEMS_PER_PAGE = (7 * (_slotCount - 2));
        this.virtualInventory = Bukkit.createInventory(_customInventoryHolder, _slotCount * 9, _title);
        this.customInventoryHolder = _customInventoryHolder;

        initialize();
    }

    private void initialize() {
        ItemStack glassPane = new ItemStack(Material.BLACK_STAINED_GLASS_PANE, 1);
        ItemMeta glassMeta = glassPane.getItemMeta();
        glassMeta.setDisplayName(" ");
        glassPane.setItemMeta(glassMeta);

        for (int i = 0; i < SLOT_COUNT * 9; i++) {
            if (i < 9 || i >= SLOT_COUNT * 9 - 9 || i % 9 == 0 || i % 9 == 8) {
                virtualInventory.setItem(i, glassPane);
            }
        }

        customInventoryHolder.setCustomInventory(this);
    }

    private void removeAllAttributeModifiers(ItemStack item) {
        if (item == null) return;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        Multimap<Attribute, AttributeModifier> modifiers = meta.getAttributeModifiers();

        if (modifiers != null) {
            for (Attribute attribute : modifiers.keySet()) {
                meta.removeAttributeModifier(attribute);
            }
        }

        item.setItemMeta(meta);
    }

    public void setItem(int slot, ItemStack item) {
        virtualInventory.setItem(slot, item);
    }

    public int getAvailableSlots() {
        return ITEMS_PER_PAGE;
    }

    public Inventory getInventory() {
        return virtualInventory;
    }

    public ItemStack addInventoryItem(Material material, String name, int slot) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        item.setItemMeta(meta);
        removeAllAttributeModifiers(item);
        virtualInventory.setItem(slot, item);
        return item;
    }

}
