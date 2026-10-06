package io.github.glocation87.admintools.staff;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.glocation87.admintools.AdminToolsPlugin;
import io.github.glocation87.admintools.data.Punishment;
import java.time.Duration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

class PunishmentServiceTest {

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
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void ladderDoublesWithEveryPriorAndGoesPermanent() {
        PunishmentService punishments = plugin.punishments();
        assertEquals(Duration.ofHours(1), punishments.ladder(target.getUniqueId(), Punishment.Category.CHAT, 1));
        assertEquals(Duration.ofDays(7), punishments.ladder(target.getUniqueId(), Punishment.Category.CHAT, 3));

        punishments.mute(target, Duration.ofHours(1), "spam", staff, Punishment.Category.CHAT, 1);
        assertEquals(Duration.ofHours(2), punishments.ladder(target.getUniqueId(), Punishment.Category.CHAT, 1));
        assertEquals(Duration.ofDays(1), punishments.ladder(target.getUniqueId(), Punishment.Category.GAMEPLAY, 1));

        for (int i = 0; i < 3; i++) {
            punishments.mute(target, Duration.ofHours(1), "spam again", staff, Punishment.Category.CHAT, 1);
        }
        assertEquals(4, punishments.priors(target.getUniqueId(), Punishment.Category.CHAT));
        assertNull(punishments.ladder(target.getUniqueId(), Punishment.Category.CHAT, 1));
    }

    @Test
    void mutesAreEnforcedAndRecorded() {
        plugin.punishments().mute(target, Duration.ofMinutes(10), "caps", staff, Punishment.Category.CHAT, 1);

        assertTrue(plugin.mutes().mute(target.getUniqueId()).isPresent());
        assertEquals(1, plugin.punishments().history(target.getUniqueId()).size());
        assertEquals("caps", plugin.punishments().history(target.getUniqueId()).getFirst().reason());
        assertTrue(plugin.punishments().history(target.getUniqueId()).getFirst().active());

        assertTrue(plugin.mutes().unmute(target, staff.getName()));
        assertFalse(plugin.mutes().mute(target.getUniqueId()).isPresent());
    }

    @Test
    void warnsDoNotCountAsPriors() {
        plugin.punishments().warn(target, "be nice", staff);
        assertEquals(0, plugin.punishments().priors(target.getUniqueId(), Punishment.Category.CHAT));
        assertEquals(Punishment.Type.WARN, plugin.punishments().history(target.getUniqueId()).getFirst().type());
    }

    @Test
    void historyIsNewestFirst() {
        plugin.punishments().warn(target, "first", staff);
        plugin.data().addPunishment(target.getUniqueId(),
            new Punishment(Punishment.Type.KICK, Punishment.Category.OTHER, 0, "x", System.currentTimeMillis() + 1000, 0, "second"));
        assertEquals("second", plugin.punishments().history(target.getUniqueId()).getFirst().reason());
    }

    @Test
    void reportsAreStoredForStaff() {
        plugin.punishments().report(target, staff, "flying around");
        assertEquals(1, plugin.data().reports().size());
        assertEquals(staff.getUniqueId(), plugin.data().reports().getFirst().target());
    }

    @Test
    void altsShareAnAddress() {
        plugin.data().recordAddress(target.getUniqueId(), "10.0.0.5");
        plugin.data().recordAddress(staff.getUniqueId(), "10.0.0.5");
        plugin.data().recordAddress(staff.getUniqueId(), "10.0.0.9");
        PlayerMock stranger = server.addPlayer();
        plugin.data().recordAddress(stranger.getUniqueId(), "10.0.0.9");

        assertEquals(java.util.Set.of(staff.getUniqueId()), plugin.data().alts(target.getUniqueId()));
        assertEquals(java.util.Set.of(target.getUniqueId(), stranger.getUniqueId()), plugin.data().alts(staff.getUniqueId()));
    }
}
