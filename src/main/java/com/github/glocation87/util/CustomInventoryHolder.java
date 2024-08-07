package com.github.glocation87.util;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class CustomInventoryHolder implements InventoryHolder {
    private final String identifier;

    public CustomInventoryHolder(String identifier) {
        this.identifier = identifier;
    }

    public String getIdentifier() {
        return identifier;
    }

    @Override
    public Inventory getInventory() { // no implementation
        return null;
    }
}
