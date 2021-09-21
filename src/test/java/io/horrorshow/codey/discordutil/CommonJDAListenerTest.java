package io.horrorshow.codey.discordutil;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static io.horrorshow.codey.discordutil.CommonJDAListener.BASKET;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class CommonJDAListenerTest {
    @Mock JDA jda;
    CommonJDAListener commonJDAListener;

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
        commonJDAListener = new CommonJDAListener(jda);
    }

    @Test
    void basket_reaction_removes_message() {
        var event = mock(GuildMessageReactionAddEvent.class, RETURNS_DEEP_STUBS);
        when(event.getUser().isBot()).thenReturn(false);
        when(event.getReactionEmote().getEmoji()).thenReturn(BASKET);
        when(event.getReactionEmote().isEmoji()).thenReturn(true);
        when(event.getMessageId()).thenReturn("messageId");

        var message = mock(Message.class, RETURNS_DEEP_STUBS);
        when(message.getAuthor().getId()).thenReturn("botUserId");
        when(event.getJDA().getSelfUser().getId()).thenReturn("botUserId");
        when(event.getChannel().retrieveMessageById("messageId").complete()).thenReturn(message);

        commonJDAListener.onReactionAdd(event);

        verify(message.delete()).complete();
    }
}
