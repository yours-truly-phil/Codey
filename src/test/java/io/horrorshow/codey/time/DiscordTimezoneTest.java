package io.horrorshow.codey.time;

import io.horrorshow.codey.discordutil.DiscordUtils;
import io.horrorshow.codey.parser.TimeParser;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.DayOfWeek;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class DiscordTimezoneTest {
    @Mock
    JDA jda;
    @Mock
    DiscordUtils utils;
    DiscordTimezone discordTimezone;
    @Captor
    ArgumentCaptor<MessageEmbed> embedCaptor;

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
        discordTimezone = new DiscordTimezone(jda, utils);
    }

    @Test
    void test_parsing() {
        assertEquals(TimeParser.toOffsetDateTime("18:00 Europe/Amsterdam").getHour(), 18);
        assertEquals(TimeParser.toOffsetDateTime("18:00 Europe/Amsterdam").getMinute(), 0);

        assertEquals(TimeParser.toOffsetDateTime("friday 18:00 Europe/Amsterdam").getHour(), 18);
        assertEquals(TimeParser.toOffsetDateTime("friday 18:00 Europe/Amsterdam").getMinute(), 0);
        assertEquals(TimeParser.toOffsetDateTime("friday 18:00 Europe/Amsterdam").getDayOfWeek().getValue(), 5);
    }

    @Test
    void finds_time_in_message_and_replies_with_local_time() {
        var message = mock(Message.class, RETURNS_DEEP_STUBS);
        when(message.getContentRaw()).thenReturn("""
                this is a discord message, here
                is a time: Friday 15:00 Europe/Berlin cool cool
                here is some invalid time fRidai 24:60 Europe/Los_Angeles""");

        discordTimezone.onMessage(message);
        verify(utils).sendRemovableMessageReply(eq(message), embedCaptor.capture());

        var sentEmbed = embedCaptor.getValue();
        var time = sentEmbed.getTimestamp();
        assertThat(time).isNotNull();
        assertThat(time.getHour()).isEqualTo(15);
        assertThat(time.getMinute()).isZero();
        assertThat(time.getDayOfWeek()).isEqualTo(DayOfWeek.FRIDAY);
    }
}
