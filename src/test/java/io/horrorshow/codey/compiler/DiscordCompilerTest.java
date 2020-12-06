package io.horrorshow.codey.compiler;

import io.horrorshow.codey.api.WandboxApi;
import io.horrorshow.codey.api.WandboxConfiguration;
import io.horrorshow.codey.api.WandboxRequest;
import io.horrorshow.codey.api.WandboxResponse;
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
import org.springframework.web.client.RestTemplate;

import java.util.function.Consumer;

import static io.horrorshow.codey.compiler.DiscordCompiler.PLAY;
import static org.mockito.Mockito.*;

class DiscordCompilerTest {

    @Mock
    JDA jda;
    @Mock
    RestTemplate restTemplate;
    WandboxApi wandboxApi;
    DiscordUtils utils;
    DiscordCompiler discordCompiler;
    MessageStore messageStore;
    WandboxConfiguration config;

    @Captor
    ArgumentCaptor<Consumer<Message>> consumerArgumentCaptor;

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
        config = new WandboxConfiguration();
        config.setUrl("WandboxURL");
        wandboxApi = new WandboxApi(restTemplate, config);
        messageStore = new MessageStore();
        utils = new DiscordUtils(jda, messageStore);
        discordCompiler = new DiscordCompiler(jda, wandboxApi, utils);
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
        var event =
                mock(GuildMessageReceivedEvent.class, RETURNS_DEEP_STUBS);
        var message = mock(Message.class, RETURNS_DEEP_STUBS);
        when(event.getAuthor().isBot()).thenReturn(false);
        when(event.getMessage()).thenReturn(message);
        when(message.getContentRaw()).thenReturn("""
                Hello, I'm a discord message
                ```java
                public class A {
                    public static void main(String[] args) {
                        System.out.println("Hello, World!");
                    }
                }```""");

        var wandboxResponse = new WandboxResponse();
        wandboxResponse.setStatus("0");
        when(restTemplate.postForObject(
                eq(config.getUrl()), any(WandboxRequest.class), eq(WandboxResponse.class)))
                .thenReturn(wandboxResponse);

        discordCompiler.onGuildMessageReceived(event);

        verify(message.addReaction(PLAY)).queue();
    }

    @Test
    void on_update_message_compile_code_from_codeblocks_and_add_PLAY_reaction() {
        var event =
                mock(GuildMessageUpdateEvent.class, RETURNS_DEEP_STUBS);
        var message = mock(Message.class, RETURNS_DEEP_STUBS);
        when(event.getAuthor().isBot()).thenReturn(false);
        when(event.getMessage()).thenReturn(message);
        when(message.getContentRaw()).thenReturn("""
                Hello, I'm a discord message
                ```java
                public class A {
                    public static void main(String[] args) {
                        System.out.println("Hello, World!");
                    }
                }```""");

        var wandboxResponse = new WandboxResponse();
        wandboxResponse.setStatus("0");
        when(restTemplate.postForObject(
                eq(config.getUrl()), any(WandboxRequest.class), eq(WandboxResponse.class)))
                .thenReturn(wandboxResponse);

        discordCompiler.onGuildMessageUpdate(event);

        verify(message.addReaction(PLAY)).queue();
    }

    @Test
    void on_play_reaction_print_compilation_results_after_compilable_message_received() {
        var msgReceivedEvent =
                mock(GuildMessageReceivedEvent.class, RETURNS_DEEP_STUBS);
        var message = mock(Message.class, RETURNS_DEEP_STUBS);
        when(msgReceivedEvent.getAuthor().isBot()).thenReturn(false);
        when(msgReceivedEvent.getMessage()).thenReturn(message);
        when(message.getContentRaw()).thenReturn("""
                Hello, I'm a discord message
                ```java
                public class A {
                    public static void main(String[] args) {
                        System.out.println("Hello, World!");
                    }
                }```""");
        when(message.getId()).thenReturn("messageId");
        var channel = mock(TextChannel.class, RETURNS_DEEP_STUBS);
        when(message.getTextChannel()).thenReturn(channel);
        var wandboxResponse = new WandboxResponse();
        wandboxResponse.setStatus("0");
        wandboxResponse.setCompiler_message("compiler message");
        wandboxResponse.setCompiler_error("compiler error");
        wandboxResponse.setProgram_message("program message");
        wandboxResponse.setProgram_output("program output");
        when(restTemplate.postForObject(
                eq(config.getUrl()), any(WandboxRequest.class), eq(WandboxResponse.class)))
                .thenReturn(wandboxResponse);
        discordCompiler.onGuildMessageReceived(msgReceivedEvent);

        var event =
                mock(GuildMessageReactionAddEvent.class, RETURNS_DEEP_STUBS);
        when(event.getUser().isBot()).thenReturn(false);
        when(event.getReactionEmote().getEmoji()).thenReturn(PLAY);
        when(event.getMessageId()).thenReturn("messageId");
        messageStore.getFormattedCodeStore().put(message.getId(), message);

        discordCompiler.onGuildMessageReactionAdd(event);

        verify(event.getChannel().retrieveMessageById("messageId"))
                .queue(consumerArgumentCaptor.capture());

        consumerArgumentCaptor.getValue().accept(message);

        verify(channel).sendMessage("""
                ```
                compiler error```
                """);
        verify(channel).sendMessage("""
                ```
                program message```
                """);
    }
}