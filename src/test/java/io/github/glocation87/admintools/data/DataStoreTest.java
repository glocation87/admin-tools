package io.github.glocation87.admintools.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.file.Path;
import java.util.UUID;
import java.util.logging.Logger;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

class DataStoreTest {

    @TempDir
    Path dir;

    private ServerMock server;
    private File file;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        file = dir.resolve("data.yml").toFile();
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    private DataStore store() {
        DataStore store = new DataStore(file, Logger.getAnonymousLogger());
        store.load();
        return store;
    }

    @Test
    void mutesNotesAndFreezesSurviveReload() {
        UUID id = UUID.randomUUID();
        DataStore first = store();
        first.mute(id, new Mute(-1, "spam", "admin"));
        first.frozen(id, true);
        first.addNote(id, new StaffNote("admin", 1234L, "watch this one"));

        DataStore second = store();
        assertEquals("spam", second.mute(id).orElseThrow().reason());
        assertTrue(second.frozen(id));
        assertEquals(1, second.notes(id).size());
        assertEquals("watch this one", second.notes(id).getFirst().text());
    }

    @Test
    void expiredMutesDropOnRead() {
        UUID id = UUID.randomUUID();
        DataStore store = store();
        store.mute(id, new Mute(System.currentTimeMillis() - 1, "old", "admin"));
        assertTrue(store.mute(id).isEmpty());
        assertFalse(store.unmute(id));
    }

    @Test
    void snapshotRestoresInventoryAndGamemode() {
        PlayerMock player = server.addPlayer();
        player.setGameMode(GameMode.ADVENTURE);
        player.getInventory().setItem(3, ItemStack.of(Material.DIAMOND_SWORD));
        StaffSnapshot snapshot = StaffSnapshot.capture(player);

        player.getInventory().clear();
        player.setGameMode(GameMode.CREATIVE);
        snapshot.restore(player);

        assertEquals(GameMode.ADVENTURE, player.getGameMode());
        assertEquals(Material.DIAMOND_SWORD, player.getInventory().getItem(3).getType());
    }

    @Test
    void snapshotSurvivesReload() {
        PlayerMock player = server.addPlayer();
        player.getInventory().setItem(0, ItemStack.of(Material.STONE, 7));
        UUID id = player.getUniqueId();
        DataStore first = store();
        first.snapshot(id, StaffSnapshot.capture(player));

        StaffSnapshot loaded = store().snapshot(id);
        assertNotNull(loaded);
        assertEquals(7, loaded.contents()[0].getAmount());
        assertEquals(Material.STONE, loaded.contents()[0].getType());

        first.clearSnapshot(id);
        assertNull(store().snapshot(id));
    }
}
