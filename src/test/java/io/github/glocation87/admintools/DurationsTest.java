package io.github.glocation87.admintools;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Duration;
import org.junit.jupiter.api.Test;

class DurationsTest {

    @Test
    void parsesCompoundDurations() {
        assertEquals(Duration.ofMinutes(30), Durations.parse("30m"));
        assertEquals(Duration.ofHours(36), Durations.parse("1d12h"));
        assertEquals(Duration.ofDays(14), Durations.parse("2w"));
        assertEquals(Duration.ofSeconds(90), Durations.parse(" 1M30S "));
    }

    @Test
    void permanentIsNull() {
        assertNull(Durations.parse("perm"));
        assertNull(Durations.parse("forever"));
    }

    @Test
    void rejectsGarbage() {
        assertThrows(IllegalArgumentException.class, () -> Durations.parse("soon"));
        assertThrows(IllegalArgumentException.class, () -> Durations.parse("10"));
        assertThrows(IllegalArgumentException.class, () -> Durations.parse("5m x"));
    }

    @Test
    void formatsRoundedDown() {
        assertEquals("permanent", Durations.format(null));
        assertEquals("45s", Durations.format(Duration.ofSeconds(45)));
        assertEquals("2h 5m", Durations.format(Duration.ofMinutes(125)));
        assertEquals("3d 4h", Durations.format(Duration.ofHours(76).plusMinutes(20)));
    }
}
