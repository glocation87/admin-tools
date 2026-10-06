package io.github.glocation87.admintools.debug;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;

class ServerStatsTest {

    @BeforeEach
    void setUp() {
        MockBukkit.mock();
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void samplingSurvivesServersWithoutTickTiming() {
        ServerStats stats = new ServerStats();
        for (int i = 0; i < 40; i++) {
            stats.sample();
        }
        assertTrue(stats.mspt().size() <= 30);
        assertEquals(0, stats.peakMspt());
    }
}
