package com.github.glocation87.util;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class CustomInventoryHolder implements InventoryHolder {

    private final String identifier;
    private CustomInventory customInventory;

    public CustomInventoryHolder(String identifier) {
        this.identifier = identifier;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setCustomInventory(CustomInventory customInventory) {
        this.customInventory = customInventory;
    }

    public CustomInventory getCustomInventory() {
        return customInventory;
    }

    @Override
    public Inventory getInventory() {
        return customInventory != null ? customInventory.getInventory() : null;
    }
}
