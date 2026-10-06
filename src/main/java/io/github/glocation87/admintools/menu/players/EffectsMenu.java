package io.github.glocation87.admintools.menu.players;

import io.github.glocation87.admintools.AdminToolsPlugin;
import io.github.glocation87.admintools.menu.Icons;
import io.github.glocation87.admintools.menu.ListMenu;
import io.github.glocation87.admintools.menu.Menu;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

public final class EffectsMenu extends ListMenu<PotionEffect> {
    private final LivingEntity target;

    public EffectsMenu(AdminToolsPlugin plugin, org.bukkit.entity.Player viewer, Menu parent, LivingEntity target) {
        super(plugin, viewer, parent, "<light_purple><b>Effects", 4);
        this.target = target;
    }

    @Override
    public boolean live() {
        return true;
    }

    @Override
    protected List<PotionEffect> items() {
        List<PotionEffect> effects = new ArrayList<>(target.getActivePotionEffects());
        effects.sort(Comparator.comparing(effect -> effect.getType().getKey().getKey()));
        return effects;
    }

    @Override
    protected ItemStack icon(PotionEffect effect) {
        String duration = effect.isInfinite() ? "infinite" : effect.getDuration() / 20 + "s";
        return Icons.of(Material.POTION, "<light_purple>" + effect.getType().getKey().getKey().replace('_', ' '),
            "Level: <white>" + (effect.getAmplifier() + 1), "Left: <white>" + duration,
            "Particles: <white>" + effect.hasParticles() + " <gray>Ambient: <white>" + effect.isAmbient(), "", "<yellow>Click to remove");
    }

    @Override
    protected void onClick(PotionEffect effect, ClickType type) {
        target.removePotionEffect(effect.getType());
        refresh();
    }

    @Override
    protected void extras() {
        button(bottom() + 1, Icons.of(Material.MILK_BUCKET, "<red>Clear All"), () -> {
            for (PotionEffect effect : target.getActivePotionEffects()) {
                target.removePotionEffect(effect.getType());
            }
            refresh();
        });
        button(bottom() + 2, Icons.of(Material.BREWING_STAND, "<green>Give Effect"), () -> new GiveEffectMenu(plugin, viewer, this, target).open());
    }
}
