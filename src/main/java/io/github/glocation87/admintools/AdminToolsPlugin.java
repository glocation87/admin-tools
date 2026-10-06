package io.github.glocation87.admintools;

import io.github.glocation87.admintools.data.DataStore;
import io.github.glocation87.admintools.menu.ChatPrompt;
import io.github.glocation87.admintools.menu.MenuService;
import io.github.glocation87.admintools.staff.CommandSpy;
import io.github.glocation87.admintools.staff.FreezeService;
import io.github.glocation87.admintools.staff.MuteService;
import io.github.glocation87.admintools.staff.StaffChat;
import io.github.glocation87.admintools.staff.StaffItems;
import io.github.glocation87.admintools.staff.StaffListener;
import io.github.glocation87.admintools.staff.StaffModeService;
import io.github.glocation87.admintools.staff.VanishService;
import java.io.File;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public class AdminToolsPlugin extends JavaPlugin {
    private AdminConfig settings;
    private DataStore data;
    private MenuService menus;
    private ChatPrompt prompts;
    private StaffChat staffChat;
    private CommandSpy commandSpy;
    private VanishService vanish;
    private FreezeService freeze;
    private MuteService mutes;
    private StaffModeService staffMode;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        settings = AdminConfig.from(getConfig());
        data = new DataStore(new File(getDataFolder(), "data.yml"), getLogger());
        data.load();

        menus = new MenuService(this);
        prompts = new ChatPrompt(this);
        staffChat = new StaffChat(this);
        commandSpy = new CommandSpy(this);
        vanish = new VanishService(this);
        freeze = new FreezeService(this);
        mutes = new MuteService(this);
        staffMode = new StaffModeService(this, new StaffItems(this), vanish, commandSpy, staffChat);

        for (Listener listener : new Listener[] {menus, prompts, staffChat, commandSpy, freeze, mutes, new StaffListener(this)}) {
            getServer().getPluginManager().registerEvents(listener, this);
        }
        getServer().getScheduler().runTaskTimer(this, () -> {
            menus.tick();
            vanish.tick();
        }, settings.refreshTicks(), settings.refreshTicks());
    }

    @Override
    public void onDisable() {
        if (staffMode != null) {
            staffMode.exitAll();
        }
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

    public StaffChat staffChat() {
        return staffChat;
    }

    public CommandSpy commandSpy() {
        return commandSpy;
    }

    public VanishService vanish() {
        return vanish;
    }

    public FreezeService freeze() {
        return freeze;
    }

    public MuteService mutes() {
        return mutes;
    }

    public StaffModeService staffMode() {
        return staffMode;
    }
}
