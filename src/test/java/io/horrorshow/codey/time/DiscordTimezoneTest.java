package io.horrorshow.codey.time;

import io.horrorshow.codey.discordutil.CodeyConfig;
import io.horrorshow.codey.discordutil.DiscordUtils;
import io.horrorshow.codey.parser.TimeParser;
import net.dv8tion.jda.api.JDA;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DiscordTimezoneTest {
    @Mock
    JDA jda;
    CodeyConfig codeyConfig;
    DiscordUtils utils;

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
        codeyConfig = new CodeyConfig();
        utils = new DiscordUtils(jda, codeyConfig);
    }

    @Test
    void test_parsing() {
        assertEquals(TimeParser.toOffsetDateTime("18:00 Europe/Amsterdam").getHour(), 18);
        assertEquals(TimeParser.toOffsetDateTime("18:00 Europe/Amsterdam").getMinute(), 0);

        assertEquals(TimeParser.toOffsetDateTime("friday 18:00 Europe/Amsterdam").getHour(), 18);
        assertEquals(TimeParser.toOffsetDateTime("friday 18:00 Europe/Amsterdam").getMinute(), 0);
        assertEquals(TimeParser.toOffsetDateTime("friday 18:00 Europe/Amsterdam").getDayOfWeek().getValue(), 5);
    }
}
