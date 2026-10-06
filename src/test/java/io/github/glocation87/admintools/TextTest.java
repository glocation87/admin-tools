package io.github.glocation87.admintools;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.Test;

class TextTest {

    @Test
    void historyColorsEachSample() {
        assertEquals("<green>|<yellow>|<red>|", Text.history(List.of(10.0, 40.0, 60.0), 35, 50));
    }

    @Test
    void gaugeFillsProportionally() {
        assertEquals("<green>|||||<dark_gray>|||||", Text.gauge(0.5, 10));
        assertEquals("<red>||||||||||<dark_gray>", Text.gauge(1.5, 10));
    }

    @Test
    void lowIsBadColorsInvert() {
        assertEquals("<green>", Text.colorLow(20, 18, 15));
        assertEquals("<yellow>", Text.colorLow(17, 18, 15));
        assertEquals("<red>", Text.colorLow(12, 18, 15));
    }

    @Test
    void plainStripsFormatting() {
        assertEquals("Hello world", Text.plain(Text.mm("<red><b>Hello</b> <i>world")));
    }
}
