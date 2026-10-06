package io.github.glocation87.admintools.menu;

import io.github.glocation87.admintools.AdminToolsPlugin;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public final class ConfirmMenu extends Menu {
    private final ItemStack subject;
    private final Runnable confirmed;

    public ConfirmMenu(AdminToolsPlugin plugin, Player viewer, Menu parent, String title, ItemStack subject, Runnable confirmed) {
        super(plugin, viewer, parent, title, 3);
        this.subject = subject;
        this.confirmed = confirmed;
    }

    @Override
    protected void draw() {
        set(13, subject);
        button(11, Icons.of(Material.LIME_WOOL, "<green><b>Confirm"), () -> {
            confirmed.run();
            back();
        });
        button(15, Icons.of(Material.RED_WOOL, "<red><b>Cancel"), this::back);
        fill();
    }
}
