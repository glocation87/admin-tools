package io.github.glocation87.admintools.staff;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.glocation87.admintools.AdminToolsPlugin;
import io.github.glocation87.admintools.data.StaffSnapshot;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

class StaffModeServiceTest {

    private ServerMock server;
    private AdminToolsPlugin plugin;
    private PlayerMock player;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        plugin = MockBukkit.load(AdminToolsPlugin.class);
        player = server.addPlayer();
        player.setOp(true);
        player.setGameMode(GameMode.SURVIVAL);
        player.getInventory().setItem(0, ItemStack.of(Material.DIAMOND_PICKAXE));
        player.getInventory().setItem(20, ItemStack.of(Material.COBBLESTONE, 64));
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void enterSwapsInventoryForToolsAndExitHandsItBack() {
        plugin.staffMode().enter(player);

        assertTrue(plugin.staffMode().active(player));
        assertEquals(GameMode.CREATIVE, player.getGameMode());
        assertTrue(plugin.staffMode().items().of(player.getInventory().getItem(0)).isPresent());
        assertEquals(StaffItem.EXIT, plugin.staffMode().items().of(player.getInventory().getItem(8)).orElseThrow());
        assertNull(player.getInventory().getItem(20));
        assertNotNull(plugin.data().snapshot(player.getUniqueId()));
        assertTrue(plugin.vanish().vanished(player));

        plugin.staffMode().exit(player);

        assertFalse(plugin.staffMode().active(player));
        assertEquals(GameMode.SURVIVAL, player.getGameMode());
        assertEquals(Material.DIAMOND_PICKAXE, player.getInventory().getItem(0).getType());
        assertEquals(64, player.getInventory().getItem(20).getAmount());
        assertNull(plugin.data().snapshot(player.getUniqueId()));
        assertFalse(plugin.vanish().vanished(player));
    }

    @Test
    void quittingRestoresTheInventory() {
        plugin.staffMode().enter(player);
        player.disconnect();

        assertFalse(plugin.staffMode().active(player.getUniqueId()));
        assertEquals(Material.DIAMOND_PICKAXE, player.getInventory().getItem(0).getType());
    }

    @Test
    void leftoverSnapshotIsRestoredOnJoin() {
        // a crash leaves the snapshot on disk while the service has forgotten the player
        plugin.data().snapshot(player.getUniqueId(), StaffSnapshot.capture(player));
        player.getInventory().clear();
        player.setGameMode(GameMode.CREATIVE);

        plugin.staffMode().restoreCrashed(player);

        assertEquals(Material.DIAMOND_PICKAXE, player.getInventory().getItem(0).getType());
        assertEquals(GameMode.SURVIVAL, player.getGameMode());
        assertNull(plugin.data().snapshot(player.getUniqueId()));
    }

    @Test
    void staffCannotChangeGamemodeByHand() {
        plugin.staffMode().enter(player);
        player.setGameMode(GameMode.SURVIVAL);
        assertEquals(GameMode.CREATIVE, player.getGameMode());
    }

    @Test
    void vanishHidesFromPlayersWithoutPermission() {
        PlayerMock other = server.addPlayer();
        other.setOp(false);
        plugin.vanish().vanish(player);

        assertFalse(other.canSee(player));
        plugin.vanish().reveal(player);
        assertTrue(other.canSee(player));
    }
}
