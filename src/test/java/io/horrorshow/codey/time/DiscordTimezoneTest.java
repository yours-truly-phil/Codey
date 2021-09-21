package io.horrorshow.codey.time;

import io.horrorshow.codey.discordutil.CodeyConfig;
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
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.time.DayOfWeek;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;


class DiscordTimezoneTest {

    @Mock JDA jda;
    DiscordTimezone discordTimezone;
    @Captor ArgumentCaptor<MessageEmbed> embedCaptor;
    CodeyConfig codeyConfig;


    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
        codeyConfig = new CodeyConfig();
        discordTimezone = new DiscordTimezone(jda, codeyConfig);
    }


    @Test
    void test_parsing() {
        assertThat(TimeParser.toOffsetDateTime("18:00 Europe/Amsterdam").getHour()).isEqualTo(18);
        assertThat(TimeParser.toOffsetDateTime("18:00 Europe/Amsterdam").getMinute()).isZero();

        assertThat(TimeParser.toOffsetDateTime("friday 18:00 Europe/Amsterdam").getHour()).isEqualTo(18);
        assertThat(TimeParser.toOffsetDateTime("friday 18:00 Europe/Amsterdam").getMinute()).isZero();
        assertThat(TimeParser.toOffsetDateTime("friday 18:00 Europe/Amsterdam").getDayOfWeek().getValue()).isEqualTo(5);
    }


    @Test
    void finds_time_in_message_and_replies_with_local_time() {
        var message = mock(Message.class, RETURNS_DEEP_STUBS);
        when(message.getContentStripped()).thenReturn("""
                this is a discord message, here
                is a time: Friday 15:00 Europe/Berlin cool cool
                here is some invalid time fRidai 24:60 Europe/Los_Angeles""");
        codeyConfig.setEmbedColor("#ffffff");

        try (var discordUtilsMock = Mockito.mockStatic(DiscordUtils.class)) {

            discordTimezone.onMessage(message);

            discordUtilsMock.verify(() -> DiscordUtils.sendRemovableMessageReply(eq(message), embedCaptor.capture()), times(1));

            var sentEmbed = embedCaptor.getValue();
            var time = sentEmbed.getTimestamp();
            assertThat(time).isNotNull();
            assertThat(time.getHour()).isEqualTo(15);
            assertThat(time.getMinute()).isZero();
            assertThat(time.getDayOfWeek()).isEqualTo(DayOfWeek.FRIDAY);
        }
    }
}
