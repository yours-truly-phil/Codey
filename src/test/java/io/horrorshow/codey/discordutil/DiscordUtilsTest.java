package io.horrorshow.codey.discordutil;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.function.Consumer;

import static io.horrorshow.codey.discordutil.DiscordUtils.BASKET;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.*;

class DiscordUtilsTest {

    @Mock
    JDA jda;
    MessageStore messageStore;
    DiscordUtils discordUtils;

    @Captor
    ArgumentCaptor<Consumer<Message>> messageCaptor;
    @Captor
    ArgumentCaptor<Consumer<Void>> voidCaptor;

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
        messageStore = new MessageStore();
        discordUtils = new DiscordUtils(jda, messageStore);
    }

    @Test
    void basket_reaction_removes_message() {
        var event = mock(GuildMessageReactionAddEvent.class, RETURNS_DEEP_STUBS);
        when(event.getUser().isBot()).thenReturn(false);
        when(event.getReactionEmote().getEmoji()).thenReturn(BASKET);
        when(event.getMessageId()).thenReturn("messageId");
        var message = mock(Message.class, RETURNS_DEEP_STUBS);
        when(message.getAuthor().getId()).thenReturn("botUserId");
        when(event.getJDA().getSelfUser().getId()).thenReturn("botUserId");

        discordUtils.onGuildMessageReactionAdd(event);

        verify(event.getChannel().retrieveMessageById("messageId"))
                .queue(messageCaptor.capture());

        messageCaptor.getValue().accept(message);

        verify(message.delete()).queue();
    }

    @Test
    void send_removable_message_adds_basket_reaction() {
        var text = "Message text";
        var channel = mock(TextChannel.class, RETURNS_DEEP_STUBS);
        var message = mock(Message.class, RETURNS_DEEP_STUBS);

        discordUtils.sendRemovableMessage(text, channel);

        verify(channel.sendMessage(text))
                .queue(messageCaptor.capture());

        messageCaptor.getValue().accept(message);

        verify(message.addReaction(BASKET)).queue();
    }

    @Test
    void removable_message_async_adds_basket_and_gives_msg_to_consumer() {
        var text = "Message text";
        var channel = mock(TextChannel.class, RETURNS_DEEP_STUBS);
        var message = mock(Message.class, RETURNS_DEEP_STUBS);
        Consumer<Message> consumer = message1 -> assertThat(message1).isEqualTo(message);

        discordUtils.sendRemovableMessage(text, channel, consumer);

        verify(channel.sendMessage(text))
                .queue(messageCaptor.capture());

        messageCaptor.getValue().accept(message);

        verify(message.addReaction(BASKET))
                .queue(voidCaptor.capture());
        voidCaptor.getValue().accept(null);
    }
}