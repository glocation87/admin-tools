package io.github.glocation87.admintools.staff;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.glocation87.admintools.AdminToolsPlugin;
import org.bukkit.Location;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

class FreezeServiceTest {

    private ServerMock server;
    private AdminToolsPlugin plugin;
    private PlayerMock staff;
    private PlayerMock target;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        plugin = MockBukkit.load(AdminToolsPlugin.class);
        staff = server.addPlayer();
        target = server.addPlayer();
        target.setOp(false);
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void togglePersistsAndThaws() {
        assertTrue(plugin.freeze().toggle(target, staff));
        assertTrue(plugin.data().frozen(target.getUniqueId()));
        assertFalse(plugin.freeze().toggle(target, staff));
        assertFalse(plugin.freeze().frozen(target));
    }

    @Test
    void frozenPlayersSnapBackButCanLookAround() {
        plugin.freeze().freeze(target, staff);
        Location from = target.getLocation();
        Location to = from.clone().add(3, 0, 1);
        to.setYaw(90);

        PlayerMoveEvent event = new PlayerMoveEvent(target, from, to);
        server.getPluginManager().callEvent(event);

        assertEquals(from.getX(), event.getTo().getX());
        assertEquals(from.getZ(), event.getTo().getZ());
        assertEquals(90, event.getTo().getYaw());
    }

    @Test
    void frozenPlayersOnlyRunAllowedCommands() {
        plugin.freeze().freeze(target, staff);

        PlayerCommandPreprocessEvent blocked = new PlayerCommandPreprocessEvent(target, "/gamemode creative");
        server.getPluginManager().callEvent(blocked);
        PlayerCommandPreprocessEvent allowed = new PlayerCommandPreprocessEvent(target, "/msg staff help");
        server.getPluginManager().callEvent(allowed);
        PlayerCommandPreprocessEvent namespaced = new PlayerCommandPreprocessEvent(target, "/minecraft:tp 0 0 0");
        server.getPluginManager().callEvent(namespaced);

        assertTrue(blocked.isCancelled());
        assertFalse(allowed.isCancelled());
        assertTrue(namespaced.isCancelled());
    }
}
