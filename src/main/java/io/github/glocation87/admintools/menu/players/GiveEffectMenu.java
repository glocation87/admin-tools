package io.github.glocation87.admintools.menu.players;

import io.github.glocation87.admintools.AdminToolsPlugin;
import io.github.glocation87.admintools.menu.Icons;
import io.github.glocation87.admintools.menu.ListMenu;
import io.github.glocation87.admintools.menu.Menu;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public final class GiveEffectMenu extends ListMenu<PotionEffectType> {
    private static final List<PotionEffectType> COMMON = List.of(PotionEffectType.SPEED, PotionEffectType.HASTE, PotionEffectType.STRENGTH,
        PotionEffectType.REGENERATION, PotionEffectType.RESISTANCE, PotionEffectType.FIRE_RESISTANCE, PotionEffectType.JUMP_BOOST,
        PotionEffectType.INVISIBILITY, PotionEffectType.NIGHT_VISION, PotionEffectType.WATER_BREATHING, PotionEffectType.SATURATION,
        PotionEffectType.ABSORPTION, PotionEffectType.SLOWNESS, PotionEffectType.WEAKNESS, PotionEffectType.BLINDNESS,
        PotionEffectType.NAUSEA, PotionEffectType.GLOWING, PotionEffectType.LEVITATION, PotionEffectType.SLOW_FALLING,
        PotionEffectType.DARKNESS, PotionEffectType.POISON);
    private static final int MINUTES = 5;

    private final LivingEntity target;

    public GiveEffectMenu(AdminToolsPlugin plugin, Player viewer, Menu parent, LivingEntity target) {
        super(plugin, viewer, parent, "<green><b>Give Effect", 5);
        this.target = target;
    }

    @Override
    protected List<PotionEffectType> items() {
        return COMMON;
    }

    @Override
    protected ItemStack icon(PotionEffectType type) {
        return Icons.of(Material.SPLASH_POTION, "<green>" + type.getKey().getKey().replace('_', ' '),
            "Lasts " + MINUTES + " minutes", "", "Left click: level 1", "Right click: level 2", "Shift click: level 5");
    }

    @Override
    protected void onClick(PotionEffectType type, ClickType click) {
        int amplifier = click.isShiftClick() ? 4 : click.isRightClick() ? 1 : 0;
        target.addPotionEffect(new PotionEffect(type, MINUTES * 60 * 20, amplifier));
        message("<green>Gave <white>" + type.getKey().getKey() + " " + (amplifier + 1));
        back();
    }
}
