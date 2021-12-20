package io.horrorshow.codey.discordutil;

import io.horrorshow.codey.data.repository.ElevatedUserRepository;
import io.horrorshow.codey.data.repository.GithubChannelRepository;
import io.horrorshow.codey.data.repository.Repositories;
import io.horrorshow.codey.data.repository.TimerRepository;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Objects;

import static io.horrorshow.codey.discordutil.CommonJDAListener.BASKET;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class CommonJDAListenerTest {

    @Mock JDA jda;
    @Mock TimerRepository timerRepository;
    @Mock GithubChannelRepository githubChannelRepository;
    @Mock ElevatedUserRepository elevatedUserRepository;
    CommonJDAListener commonJDAListener;


    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
        var repositories = new Repositories(timerRepository, githubChannelRepository, elevatedUserRepository);
        var applicationState = new ApplicationState(jda, repositories);
        commonJDAListener = new CommonJDAListener(jda, applicationState);
    }


    @Test
    void basket_reaction_removes_message() {
        var event = mock(MessageReactionAddEvent.class, RETURNS_DEEP_STUBS);
        when(Objects.requireNonNull(event.getUser()).isBot()).thenReturn(false);
        when(event.getReactionEmote().getEmoji()).thenReturn(BASKET);
        when(event.getReactionEmote().isEmoji()).thenReturn(true);
        when(event.getMessageId()).thenReturn("messageId");

        var message = mock(Message.class, RETURNS_DEEP_STUBS);
        when(message.getAuthor().getId()).thenReturn("botUserId");
        when(event.getJDA().getSelfUser().getId()).thenReturn("botUserId");
        when(message.getChannel().getId()).thenReturn("channelId");
        when(event.getChannel().retrieveMessageById("messageId").complete()).thenReturn(message);
        when(message.getContentRaw()).thenReturn("message content");

        commonJDAListener.onReactionAdd(event);

        verify(message.delete()).complete();
    }

}
