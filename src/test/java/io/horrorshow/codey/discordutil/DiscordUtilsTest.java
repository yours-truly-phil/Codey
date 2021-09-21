package io.horrorshow.codey.discordutil;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutionException;

import static io.horrorshow.codey.discordutil.CommonJDAListener.BASKET;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class DiscordUtilsTest {


    @Test
    void send_removable_message_adds_basket_reaction() {
        var text = "Message text";
        var channel = mock(TextChannel.class, RETURNS_DEEP_STUBS);
        var message = mock(Message.class, RETURNS_DEEP_STUBS);
        when(channel.sendMessage(text).complete()).thenReturn(message);

        DiscordUtils.sendRemovableMessage(text, channel);

        verify(message.addReaction(BASKET)).complete();
    }


    @Test
    void removable_message_async_adds_basket_and_returns_msg() throws ExecutionException, InterruptedException {
        var text = "Message text";
        var channel = mock(TextChannel.class, RETURNS_DEEP_STUBS);
        var message = mock(Message.class, RETURNS_DEEP_STUBS);

        when(channel.sendMessage(text).complete()).thenReturn(message);

        var result = DiscordUtils.sendRemovableMessageAsync(text, channel).get();

        assertThat(result).isEqualTo(message);
        verify(message.addReaction(BASKET)).queue();
    }
}
