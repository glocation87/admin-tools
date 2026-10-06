package io.github.glocation87.admintools;

import io.github.glocation87.admintools.data.DataStore;
import io.github.glocation87.admintools.menu.ChatPrompt;
import io.github.glocation87.admintools.menu.MenuService;
import java.io.File;
import org.bukkit.plugin.java.JavaPlugin;

public class AdminToolsPlugin extends JavaPlugin {
    private AdminConfig settings;
    private DataStore data;
    private MenuService menus;
    private ChatPrompt prompts;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        settings = AdminConfig.from(getConfig());
        data = new DataStore(new File(getDataFolder(), "data.yml"), getLogger());
        data.load();
        menus = new MenuService(this);
        prompts = new ChatPrompt(this);

        getServer().getPluginManager().registerEvents(menus, this);
        getServer().getPluginManager().registerEvents(prompts, this);
        getServer().getScheduler().runTaskTimer(this, menus::tick, settings.refreshTicks(), settings.refreshTicks());
    }

    @Override
    public void onDisable() {
        if (data != null) {
            data.save();
        }
    }

    public void reload() {
        reloadConfig();
        settings = AdminConfig.from(getConfig());
    }

    public AdminConfig settings() {
        return settings;
    }

    public DataStore data() {
        return data;
    }

    public MenuService menus() {
        return menus;
    }

    public ChatPrompt prompts() {
        return prompts;
    }
}
