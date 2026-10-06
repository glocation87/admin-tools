package io.github.glocation87.admintools.menu;

import io.github.glocation87.admintools.Text;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

public final class Icons {
    private Icons() {
    }

    public static ItemStack of(Material material, String name, String... lore) {
        return of(material, name, Arrays.asList(lore));
    }

    public static ItemStack of(Material material, String name, List<String> lore) {
        ItemStack item = ItemStack.of(material);
        item.editMeta(meta -> {
            meta.displayName(Text.item(name));
            meta.lore(lines(lore));
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS);
        });
        return item;
    }

    public static ItemStack glowing(Material material, String name, String... lore) {
        ItemStack item = of(material, name, lore);
        item.editMeta(meta -> meta.setEnchantmentGlintOverride(true));
        return item;
    }

    public static ItemStack toggle(boolean on, String name, String... lore) {
        List<String> lines = new ArrayList<>(Arrays.asList(lore));
        lines.add("");
        lines.add(on ? "<green>Enabled <gray>- click to disable" : "<red>Disabled <gray>- click to enable");
        return of(on ? Material.LIME_DYE : Material.GRAY_DYE, name, lines);
    }

    public static ItemStack head(OfflinePlayer owner, String name, List<String> lore) {
        ItemStack item = of(Material.PLAYER_HEAD, name, lore);
        item.editMeta(SkullMeta.class, meta -> meta.setOwningPlayer(owner));
        return item;
    }

    public static ItemStack filler() {
        ItemStack item = ItemStack.of(Material.BLACK_STAINED_GLASS_PANE);
        item.editMeta(meta -> meta.setHideTooltip(true));
        return item;
    }

    public static List<Component> lines(List<String> lore) {
        List<Component> out = new ArrayList<>(lore.size());
        for (String line : lore) {
            out.add(Text.item("<gray>" + line));
        }
        return out;
    }
}
