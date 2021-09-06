package io.horrorshow.codey.time;

import io.horrorshow.codey.discordutil.CodeyConfig;
import io.horrorshow.codey.discordutil.DiscordMessage;
import io.horrorshow.codey.discordutil.DiscordUtils;
import io.horrorshow.codey.formatter.DiscordCodeFormatter;
import io.horrorshow.codey.formatter.JavaFormatter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.DayOfWeek;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class DiscordTimezoneTest {
    @Mock
    JDA jda;
    CodeyConfig codeyConfig;
    DiscordUtils utils;
    DiscordTimezone discordTimezone;

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
        codeyConfig = new CodeyConfig();
        utils = new DiscordUtils(jda, codeyConfig);
        discordTimezone = new DiscordTimezone(jda, utils);
    }

    @Test
    void test_no_weekday()
    {
        String[] tests = {
                "18:00 Europe/Amsterdam", // match
                "15:011 America/Chicago", // no match
                "12:35 Am/Chic", // no match
                "11:35 what are you doing?" // no match
        };

        assertEquals(discordTimezone.matchTimes(tests[0])[0].parseTime()[0], 18);
        assertEquals(discordTimezone.matchTimes(tests[0])[0].parseTime()[1], 0);
        assertEquals(discordTimezone.matchTimes(tests[0])[0].region(), "Europe/Amsterdam");

        assertEquals(discordTimezone.matchTimes(tests[1]).length, 0);

        assertEquals(discordTimezone.matchTimes(tests[2]).length, 0);

        assertEquals(discordTimezone.matchTimes(tests[3]).length, 0);
    }

    @Test
    void test_with_weekday()
    {
        String[] tests = {
                "friday 18:00 Europe/Amsterdam", // match
                "d 15:01 America/Chicago", // no match
                "tourney at 12:35 Am/Chic", // no match
                "d 11:35 what are you doing?" // no match
        };

        assertTrue(discordTimezone.matchTimes(tests[0])[0].hasWeekDay());
        assertEquals(discordTimezone.matchTimes(tests[0])[0].weekDay(), DayOfWeek.FRIDAY);
        assertEquals(discordTimezone.matchTimes(tests[0])[0].parseTime()[0], 18);
        assertEquals(discordTimezone.matchTimes(tests[0])[0].parseTime()[1], 0);
        assertEquals(discordTimezone.matchTimes(tests[0])[0].region(), "Europe/Amsterdam");

        assertFalse(discordTimezone.matchTimes(tests[1])[0].hasWeekDay());

        assertEquals(discordTimezone.matchTimes(tests[2]).length, 0);

        assertEquals(discordTimezone.matchTimes(tests[3]).length, 0);
    }
}
