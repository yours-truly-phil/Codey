package io.horrorshow.codey.formatter;

import io.horrorshow.codey.data.repository.ElevatedUserRepository;
import io.horrorshow.codey.data.repository.GithubChannelRepository;
import io.horrorshow.codey.data.repository.Repositories;
import io.horrorshow.codey.data.repository.TimerRepository;
import io.horrorshow.codey.discordutil.ApplicationState;
import io.horrorshow.codey.discordutil.CodeyConfig;
import io.horrorshow.codey.discordutil.DiscordMessage;
import io.horrorshow.codey.discordutil.DiscordUtils;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageUpdateEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static io.horrorshow.codey.formatter.DiscordCodeFormatter.STARS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


class DiscordCodeFormatterTest {

    @Mock JDA jda;
    @Mock ElevatedUserRepository elevatedUserRepository;
    @Mock GithubChannelRepository githubChannelRepository;
    @Mock TimerRepository timerRepository;
    JavaFormatter javaFormatter;
    CodeyConfig codeyConfig;
    DiscordUtils utils;
    DiscordCodeFormatter formatter;


    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
        javaFormatter = new JavaFormatter();
        codeyConfig = new CodeyConfig();
        var repositories = new Repositories(timerRepository, githubChannelRepository, elevatedUserRepository);
        var applicationState = new ApplicationState(jda, repositories);
        utils = new DiscordUtils(jda, codeyConfig, applicationState);
        formatter = new DiscordCodeFormatter(jda, javaFormatter, utils);
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
        final String rawContent = """
                ```java
                public class A { public static void main(String[] args)
                { System.out.println("Hello, World!");}}```""";
        final String formattedContent = """
                ```java
                public class A {
                  public static void main(String[] args) {
                    System.out.println("Hello, World!");
                  }
                }
                ```""";
        var event = mock(GuildMessageReactionAddEvent.class, RETURNS_DEEP_STUBS);
        when(event.getUser().isBot()).thenReturn(false);
        when(event.getReactionEmote().getEmoji()).thenReturn(STARS);
        when(event.getMessageId()).thenReturn("messageId");

        var channel = mock(TextChannel.class, RETURNS_DEEP_STUBS);

        var message = mock(Message.class, RETURNS_DEEP_STUBS);
        when(message.getAuthor().isBot()).thenReturn(false);

        when(message.getContentRaw()).thenReturn(rawContent);
        when(message.getTextChannel()).thenReturn(channel);
        when(message.getId()).thenReturn("messageId");
        when(message.getGuild().getId()).thenReturn("guildId");

        when(channel.sendMessage(formattedContent).complete()).thenReturn(message);
        when(event.getChannel().retrieveMessageById("messageId").complete()).thenReturn(message);

        formatter.onReaction(event);
    }


    @Test
    void post_reaction_if_added_message_has_formattable_code() {
        var message = mock(Message.class, RETURNS_DEEP_STUBS);
        when(message.getContentRaw()).thenReturn("""
                ```java
                public class A { public static void main(String[] args)
                { System.out.println("Hello, World!");}}```""");

        formatter.onMessage(message);

        verify(message.addReaction(STARS)).complete();
    }


    @Test
    void post_reaction_if_updated_message_has_formattable_code() {
        var message = mock(Message.class, RETURNS_DEEP_STUBS);
        when(message.getContentRaw()).thenReturn("""
                ```java
                public class A { public static void main(String[] args)
                { System.out.println("Hello, World!");}}```""");

        formatter.onMessage(message);

        verify(message.addReaction(STARS)).complete();
    }


    @Test
    void remove_reaction_if_updated_message_has_no_code() {
        var message = mock(Message.class, RETURNS_DEEP_STUBS);
        when(message.getContentRaw()).thenReturn("""
                I don't contain any code""");

        formatter.onMessage(message);

        verify(message.removeReaction(STARS)).complete();
    }
}
