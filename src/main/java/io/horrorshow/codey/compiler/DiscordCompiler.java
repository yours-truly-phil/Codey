package io.horrorshow.codey.compiler;

import io.horrorshow.codey.api.CompilerApi;
import io.horrorshow.codey.discordutil.DiscordMessage;
import io.horrorshow.codey.discordutil.DiscordUtils;
import io.horrorshow.codey.discordutil.MessagePart;
import io.horrorshow.codey.discordutil.MessageStore;
import io.horrorshow.codey.parser.SourceProcessing;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageUpdateEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

import static io.horrorshow.codey.discordutil.DiscordUtils.CHAR_LIMIT;
import static io.horrorshow.codey.discordutil.DiscordUtils.toCodeBlock;


@Service
@Slf4j
public class DiscordCompiler extends ListenerAdapter {

    public static final String PLAY = "▶️";

    private final CompilerApi compiler;
    private final MessageStore.CompilationCache compilationCache;
    private final DiscordUtils utils;


    public DiscordCompiler(@Autowired JDA jda,
            @Autowired @Qualifier("piston") CompilerApi compiler,
            @Autowired DiscordUtils utils,
            @Autowired MessageStore messageStore) {
        this.utils = utils;
        this.compilationCache = messageStore.getCompilationCache();
        this.compiler = compiler;

        jda.addEventListener(this);
    }


    @Override
    public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent event) {
        if (!event.getAuthor().isBot()) {
            onMessage(event.getMessage());
        }
    }


    @Override
    public void onGuildMessageUpdate(@NotNull GuildMessageUpdateEvent event) {
        if (!event.getAuthor().isBot()) {
            onMessage(event.getMessage());
        }
    }


    @Override
    public void onGuildMessageReactionAdd(@NotNull GuildMessageReactionAddEvent event) {
        if (!event.getUser().isBot()) {
            onReactionAdd(event);
        }
    }


    @Async
    public void onReactionAdd(@NotNull GuildMessageReactionAddEvent event) {
        if (DiscordUtils.hasEmoji(PLAY, event)) {
            var message = event.getChannel().retrieveMessageById(event.getMessageId()).complete();
            sendCompilationResults(message);
        }
    }


    public void sendCompilationResults(Message message) {
        if (compilationCache.hasResult(message)) {
            var futures = compilationCache
                    .get(message).stream()
                    .map(msg -> {
                        if (msg.length() > CHAR_LIMIT) {
                            return utils.sendTextFile("codey-compiler-output.txt", msg, message.getTextChannel());
                        } else {
                            return utils.sendRemovableMessageAsync(toCodeBlock(msg, true), message.getTextChannel());
                        }
                    })
                    .toList();
            CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
        } else {
            utils.sendRemovableMessageAsync("compilation result removed from cache, " +
                                            "repost message with code block.", message.getTextChannel());
        }
    }


    @Async
    public void onMessage(Message message) {
        var contentRaw = message.getContentRaw();
        var dm = DiscordMessage.of(contentRaw);
        compileCodeBlocks(message, dm);
    }


    private void compileCodeBlocks(@NotNull Message message, DiscordMessage dm) {
        dm.getParts().stream()
                .filter(MessagePart::isCode)
                .findFirst()
                .ifPresent(part -> compiler.compile(SourceProcessing.processSource(part.text(), part.lang()), part.lang(), null, null)
                        .thenAccept(output -> {
                            compilationCache.cache(message, DiscordUtils.toDiscordMessages(output));
                            message.addReaction(PLAY).complete();
                        }));
    }
}
