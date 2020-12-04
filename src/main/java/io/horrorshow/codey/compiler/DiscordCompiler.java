package io.horrorshow.codey.compiler;

import io.horrorshow.codey.api.WandboxApi;
import io.horrorshow.codey.discordutil.DiscordMessage;
import io.horrorshow.codey.discordutil.DiscordUtils;
import io.horrorshow.codey.discordutil.MessagePart;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class DiscordCompiler extends ListenerAdapter {

    private static final String PLAY = "▶️";

    private final WandboxApi wandboxApi;

    private final Map<String, List<String>> compilationResults = new HashMap<>();
    private final DiscordUtils utils;

    public DiscordCompiler(@Autowired JDA jda,
                           @Autowired WandboxApi wandboxApi,
                           @Autowired DiscordUtils utils) {
        this.wandboxApi = wandboxApi;
        this.utils = utils;

        jda.addEventListener(this);
    }

    @Override
    public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;

        handleMessage(event.getMessage());
    }

    @Override
    public void onGuildMessageUpdate(@NotNull GuildMessageUpdateEvent event) {
        if (event.getAuthor().isBot()) return;

        handleMessage(event.getMessage());
    }

    @Override
    public void onGuildMessageReactionAdd(@NotNull GuildMessageReactionAddEvent event) {
        if (event.getUser().isBot()) return;

        final String emoji = event.getReactionEmote().getEmoji();
        if (PLAY.equals(emoji)) {
            event.getChannel()
                    .retrieveMessageById(event.getMessageId())
                    .queue(this::printCompilationResultIfPresent);
        }
    }

    public void printCompilationResultIfPresent(Message message) {
        if (compilationResults.containsKey(message.getId())) {
            for (var msg : compilationResults.getOrDefault(message.getId(),
                    List.of("compilation result unavailable"))) {
                log.debug("print compilation result");
                utils.sendRemovableMessage(msg, message.getTextChannel());
            }
        } else {
            log.debug("compilation result not present");
            utils.sendRemovableMessage("compilation result removed from cache, " +
                    "repost message with code block.", message.getTextChannel());
        }
    }

    private void handleMessage(Message message) {
        var contentRaw = message.getContentRaw();
        var dm = DiscordMessage.of(contentRaw);
        compileCodeBlocks(message, dm);
    }

    private void compileCodeBlocks(@NotNull Message message, DiscordMessage dm) {
        dm.getParts().stream()
                .filter(MessagePart::isCode)
                .findFirst()
                .ifPresent(part -> wandboxApi.compileAsync(part.getText(), part.getLang(),
                        wandboxResponse -> {
                            compilationResults.put(message.getId(),
                                    WandboxDiscordUtils.formatWandboxResponse(wandboxResponse));
                            message.addReaction(PLAY).queue();
                        }));
    }
}
