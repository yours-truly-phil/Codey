package io.horrorshow.codey.discordutil;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.concurrent.ExecutionException;

import static io.horrorshow.codey.discordutil.DiscordUtils.BASKET;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


class DiscordUtilsTest {

    @Mock
    JDA jda;
    DiscordUtils discordUtils;
    CodeyConfig codeyConfig;


    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
        codeyConfig = new CodeyConfig();
        discordUtils = new DiscordUtils(jda, codeyConfig);
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
        when(event.getChannel().retrieveMessageById("messageId").complete()).thenReturn(message);

        discordUtils.onReactionAdd(event);

        verify(message.delete()).complete();
    }


    @Test
    void send_removable_message_adds_basket_reaction() {
        var text = "Message text";
        var channel = mock(TextChannel.class, RETURNS_DEEP_STUBS);
        var message = mock(Message.class, RETURNS_DEEP_STUBS);
        when(channel.sendMessage(text).complete()).thenReturn(message);

        discordUtils.sendRemovableMessage(text, channel);

        verify(message.addReaction(BASKET)).complete();
    }


    @Test
    void removable_message_async_adds_basket_and_returns_msg() throws ExecutionException, InterruptedException {
        var text = "Message text";
        var channel = mock(TextChannel.class, RETURNS_DEEP_STUBS);
        var message = mock(Message.class, RETURNS_DEEP_STUBS);

        when(channel.sendMessage(text).complete()).thenReturn(message);

        var result = discordUtils.sendRemovableMessageAsync(text, channel).get();

        assertThat(result).isEqualTo(message);
        verify(message.addReaction(BASKET)).queue();
    }


    @Test
    void isElevatedMember() {
        codeyConfig.getRoles().add("CodeyRole");
        var member = Mockito.mock(Member.class);

        when(member.getRoles()).thenReturn(List.of());
        assertThat(discordUtils.isElevatedMember(member)).isFalse();

        var role = Mockito.mock(Role.class);
        when(role.getName()).thenReturn("CodeyRole");
        when(member.getRoles()).thenReturn(List.of(role));
        assertThat(discordUtils.isElevatedMember(member)).isTrue();
    }
}
