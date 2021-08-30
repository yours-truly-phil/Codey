package io.horrorshow.codey.compiler;

import io.horrorshow.codey.api.CompilerApi;
import io.horrorshow.codey.api.wandbox.WandboxResponse;
import io.horrorshow.codey.discordutil.CodeyConfig;
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
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.concurrent.CompletableFuture;

import static io.horrorshow.codey.compiler.DiscordCompiler.PLAY;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


class DiscordCompilerTest {

    @Mock
    JDA jda;
    @Mock
    CompilerApi compilerApi;

    DiscordUtils utils;
    DiscordCompiler discordCompiler;
    MessageStore messageStore;
    CodeyConfig codeyConfig;


    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
        messageStore = new MessageStore();
        codeyConfig = new CodeyConfig();
        utils = new DiscordUtils(jda, codeyConfig);
        discordCompiler = new DiscordCompiler(jda, compilerApi, utils, messageStore);
    }


    @Test
    void doesnt_react_to_bot_messages() {
        var event =
                mock(GuildMessageReceivedEvent.class, RETURNS_DEEP_STUBS);
        when(event.getAuthor().isBot()).thenReturn(true);
        discordCompiler.onGuildMessageReceived(event);
        verify(event, never()).getMessage();
    }


    @Test
    void doesnt_react_to_bot_message_updates() {
        var event =
                mock(GuildMessageUpdateEvent.class, RETURNS_DEEP_STUBS);
        when(event.getAuthor().isBot()).thenReturn(true);
        discordCompiler.onGuildMessageUpdate(event);
        verify(event, never()).getMessage();
    }


    @Test
    void doesnt_react_to_bot_reactions() {
        var event =
                mock(GuildMessageReactionAddEvent.class, RETURNS_DEEP_STUBS);
        when(event.getUser().isBot()).thenReturn(true);
        discordCompiler.onGuildMessageReactionAdd(event);
        verify(event, never()).getReactionEmote();
    }


    @Test
    void on_add_message_compile_code_from_codeblocks_and_add_PLAY_reaction() {
        var message = mock(Message.class, RETURNS_DEEP_STUBS);
        when(message.getContentRaw()).thenReturn("""
                Hello, I'm a discord message
                ```java
                public class A {
                    public static void main(String[] args) {
                        System.out.println("Hello, World!");
                    }
                }```""");
        when(message.getGuild().getId()).thenReturn("guildId");
        when(message.getId()).thenReturn("messageId");

        var wandboxResponse = new WandboxResponse();
        wandboxResponse.setStatus(0);

        when(compilerApi.compile(eq("""
                                
                public class A {
                    public static void main(String[] args) {
                        System.out.println("Hello, World!");
                    }
                }"""), eq("java"), any(), any()))
                .thenReturn(CompletableFuture.completedFuture(new Output("sysOut", 0, null, null)));

        discordCompiler.onMessage(message);

        verify(message.addReaction(PLAY)).complete();
    }


    @Test
    void on_update_message_compile_code_from_codeblocks_and_add_PLAY_reaction() {
        var message = mock(Message.class, RETURNS_DEEP_STUBS);
        when(message.getContentRaw()).thenReturn("""
                Hello, I'm a discord message
                ```java
                public class A {
                    public static void main(String[] args) {
                        System.out.println("Hello, World!");
                    }
                }```""");
        when(message.getId()).thenReturn("messageId");
        when(message.getGuild().getId()).thenReturn("guildId");

        when(compilerApi.compile(any(), eq("java"), any(), any()))
                .thenReturn(CompletableFuture.completedFuture(new Output("sysOut", 0, null, null)));

        discordCompiler.onMessage(message);

        verify(message.addReaction(PLAY)).complete();
    }


    @Test
    void on_play_reaction_print_compilation_results_after_compilable_message_received() {
        var message = mock(Message.class, RETURNS_DEEP_STUBS);

        when(message.getContentRaw()).thenReturn("""
                Hello, I'm a discord message
                ```java
                public class A {
                    public static void main(String[] args) {
                        System.out.println("Hello, World!");
                    }
                }```""");
        when(message.getId()).thenReturn("messageId");
        when(message.getGuild().getId()).thenReturn("guildId");

        var channel = mock(TextChannel.class, RETURNS_DEEP_STUBS);
        when(message.getTextChannel()).thenReturn(channel);

        when(compilerApi.compile(eq("""
                                
                public class A {
                    public static void main(String[] args) {
                        System.out.println("Hello, World!");
                    }
                }"""), eq("java"), any(), any()))
                .thenReturn(CompletableFuture.completedFuture(new Output("sysOut", 0, null, null)));

        discordCompiler.onMessage(message);

        var event =
                mock(GuildMessageReactionAddEvent.class, RETURNS_DEEP_STUBS);
        when(event.getUser().isBot()).thenReturn(false);
        when(event.getReactionEmote().getEmoji()).thenReturn(PLAY);
        when(event.getMessageId()).thenReturn("messageId");
        when(event.getChannel().retrieveMessageById("messageId").complete()).thenReturn(message);

        discordCompiler.onReactionAdd(event);

        verify(channel).sendMessage("""
                ```
                sysOut```
                """);
    }
}
