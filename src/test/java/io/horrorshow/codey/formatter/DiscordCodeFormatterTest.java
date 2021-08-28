package io.horrorshow.codey.formatter;

import io.horrorshow.codey.discordutil.CodeyConfig;
import io.horrorshow.codey.discordutil.DiscordMessage;
import io.horrorshow.codey.discordutil.DiscordUtils;
import io.horrorshow.codey.discordutil.MessageStore;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageUpdateEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.function.Consumer;

import static io.horrorshow.codey.formatter.DiscordCodeFormatter.STARS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class DiscordCodeFormatterTest {

    @Mock
    JDA jda;
    JavaFormatter javaFormatter;
    MessageStore messageStore;
    CodeyConfig codeyConfig;
    DiscordUtils utils;
    DiscordCodeFormatter formatter;

    @Captor
    ArgumentCaptor<Consumer<Message>> consumerMessageCaptor;

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
        javaFormatter = new JavaFormatter();
        messageStore = new MessageStore();
        codeyConfig = new CodeyConfig();
        utils = new DiscordUtils(jda, messageStore, codeyConfig);
        formatter = new DiscordCodeFormatter(jda, javaFormatter, messageStore, utils);
    }

    @Test
    void format_java_code() {
        var dm = DiscordMessage.of("""
                Hello
                ```java
                public class A
                {
                public static void main(String[] args)
                {
                System.out.println("Hello, World!");
                }
                }```
                what's up?
                ```another code block```
                ```scala scala```
                bla""");
        var result = formatter.formatted(dm);
        assertThat(result).get().isEqualTo("""
                Hello
                ```java
                public class A {
                  public static void main(String[] args) {
                    System.out.println("Hello, World!");
                  }
                }
                ```
                what's up?
                ```null
                another code block```
                ```java
                scala```
                bla""");
    }

    @Test
    void dont_react_to_added_messages_by_bots() {
        var event = mock(GuildMessageReceivedEvent.class, RETURNS_DEEP_STUBS);
        when(event.getAuthor().isBot()).thenReturn(true);
        formatter.onGuildMessageReceived(event);
        verify(event, never()).getMessage();
    }

    @Test
    void dont_react_to_updated_messages_by_bots() {
        var event = mock(GuildMessageUpdateEvent.class, RETURNS_DEEP_STUBS);
        when(event.getAuthor().isBot()).thenReturn(true);
        formatter.onGuildMessageUpdate(event);
        verify(event, never()).getMessage();
    }

    @Test
    void dont_react_to_reactions_by_bots() {
        var event = mock(GuildMessageReactionAddEvent.class, RETURNS_DEEP_STUBS);
        when(event.getUser().isBot()).thenReturn(true);
        formatter.onGuildMessageReactionAdd(event);
        verify(event, never()).getReactionEmote();
    }

    @Test
    void post_formatted_code_on_stars_reaction() {
        var event = mock(GuildMessageReactionAddEvent.class, RETURNS_DEEP_STUBS);
        when(event.getUser().isBot()).thenReturn(false);
        when(event.getReactionEmote().getEmoji()).thenReturn(STARS);
        when(event.getMessageId()).thenReturn("messageId");

        var channel = mock(TextChannel.class, RETURNS_DEEP_STUBS);

        var message = mock(Message.class, RETURNS_DEEP_STUBS);
        when(message.getAuthor().isBot()).thenReturn(false);
        when(message.getContentRaw()).thenReturn("""
                ```java
                public class A { public static void main(String[] args)
                { System.out.println("Hello, World!");}}```""");
        when(message.getTextChannel()).thenReturn(channel);
        when(message.getId()).thenReturn("messageId");

        formatter.onGuildMessageReactionAdd(event);

        verify(event.getChannel().retrieveMessageById("messageId"))
                .queue(consumerMessageCaptor.capture());

        consumerMessageCaptor.getValue().accept(message);

        verify(channel).sendMessage("""
                ```java
                public class A {
                  public static void main(String[] args) {
                    System.out.println("Hello, World!");
                  }
                }
                ```""");
    }

    @Test
    void post_reaction_if_added_message_has_formattable_code() {
        var event = mock(GuildMessageReceivedEvent.class, RETURNS_DEEP_STUBS);
        when(event.getAuthor().isBot()).thenReturn(false);
        var message = mock(Message.class, RETURNS_DEEP_STUBS);
        when(event.getMessage()).thenReturn(message);
        when(message.getContentRaw()).thenReturn("""
                ```java
                public class A { public static void main(String[] args)
                { System.out.println("Hello, World!");}}```""");

        formatter.onGuildMessageReceived(event);

        verify(message.addReaction(STARS)).queue();
    }

    @Test
    void post_reaction_if_updated_message_has_formattable_code() {
        var event = mock(GuildMessageUpdateEvent.class, RETURNS_DEEP_STUBS);
        when(event.getAuthor().isBot()).thenReturn(false);
        var message = mock(Message.class, RETURNS_DEEP_STUBS);
        when(event.getMessage()).thenReturn(message);
        when(message.getContentRaw()).thenReturn("""
                ```java
                public class A { public static void main(String[] args)
                { System.out.println("Hello, World!");}}```""");

        formatter.onGuildMessageUpdate(event);

        verify(message.addReaction(STARS)).queue();
    }

    @Test
    void remove_reaction_if_updated_message_has_no_code() {
        var event = mock(GuildMessageUpdateEvent.class, RETURNS_DEEP_STUBS);
        when(event.getAuthor().isBot()).thenReturn(false);
        var message = mock(Message.class, RETURNS_DEEP_STUBS);
        when(event.getMessage()).thenReturn(message);
        when(message.getContentRaw()).thenReturn("""
                I don't contain any code""");

        formatter.onGuildMessageUpdate(event);

        verify(message.removeReaction(STARS)).queue();
    }
}
