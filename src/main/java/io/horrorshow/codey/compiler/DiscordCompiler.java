package io.horrorshow.codey.compiler;

import io.horrorshow.codey.api.WandboxApi;
import io.horrorshow.codey.discordutil.DiscordMessage;
import io.horrorshow.codey.discordutil.DiscordUtils;
import io.horrorshow.codey.discordutil.MessagePart;
import io.horrorshow.codey.discordutil.MessageStore;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageUpdateEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;


@Service
@Slf4j
public class DiscordCompiler extends ListenerAdapter {

    public static final String PLAY = "▶️";

    private final WandboxApi wandboxApi;

    private final MessageStore.CompilationCache compilationCache;
    private final DiscordUtils utils;


    public DiscordCompiler(@Autowired JDA jda,
            @Autowired WandboxApi wandboxApi,
            @Autowired DiscordUtils utils,
            @Autowired MessageStore messageStore) {
        this.wandboxApi = wandboxApi;
        this.utils = utils;
        this.compilationCache = messageStore.getCompilationCache();

        jda.addEventListener(this);
    }


    @Override
    public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent event) {
        if (event.getAuthor().isBot()) {
            return;
        }

        CompletableFuture.runAsync(() -> onMessage(event.getMessage()));
    }


    @Override
    public void onGuildMessageUpdate(@NotNull GuildMessageUpdateEvent event) {
        if (event.getAuthor().isBot()) {
            return;
        }
        CompletableFuture.runAsync(() -> onMessage(event.getMessage()));
    }


    @Override
    public void onGuildMessageReactionAdd(@NotNull GuildMessageReactionAddEvent event) {
        if (event.getUser().isBot()) {
            return;
        }
        CompletableFuture.runAsync(() -> onReactionAdd(event));
    }


    public void onReactionAdd(@NotNull GuildMessageReactionAddEvent event) {
        String emoji = event.getReactionEmote().getEmoji();
        if (PLAY.equals(emoji)) {
            var message = event.getChannel().retrieveMessageById(event.getMessageId()).complete();
            printCompilationResultIfPresent(message);
        }
    }


    public void printCompilationResultIfPresent(Message message) {
        if (compilationCache.hasResult(message)) {
            for (var msg : compilationCache.get(message)) {
                utils.sendRemovableMessage(msg, message.getTextChannel());

            }
        } else {
            utils.sendRemovableMessage("compilation result removed from cache, " +
                                       "repost message with code block.", message.getTextChannel());
        }
    }


    public void onMessage(Message message) {
        var contentRaw = message.getContentRaw();
        var dm = DiscordMessage.of(contentRaw);
        compileCodeBlocks(message, dm);
    }


    private void compileCodeBlocks(@NotNull Message message, DiscordMessage dm) {
        dm.getParts().stream()
                .filter(MessagePart::isCode)
                .findFirst()
                .ifPresent(part -> wandboxApi.compileAsync(part.text(), part.lang())
                        .thenAccept(response -> {
                            compilationCache.cache(message, WandboxDiscordUtils.formatWandboxResponse(response));
                            message.addReaction(PLAY).complete();
                        }));
    }
}
