package io.horrorshow.codey.compiler;

import io.horrorshow.codey.api.WandboxApi;
import io.horrorshow.codey.discordutil.DiscordMessageParser;
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

import static io.horrorshow.codey.discordutil.RemoveMessageListener.BASKET;

@Service
@Slf4j
public class DiscordCompiler extends ListenerAdapter {

    private static final String PLAY = "▶️";

    private final WandboxApi wandboxApi;

    private final Map<String, List<String>> compilationResults = new HashMap<>();

    public DiscordCompiler(@Autowired JDA jda,
                           @Autowired WandboxApi wandboxApi) {
        this.wandboxApi = wandboxApi;

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
                message.getChannel().sendMessage(msg)
                        .queue(sentMsg -> sentMsg.addReaction(BASKET).queue());
            }
        } else {
            log.debug("compilation result not present");
            message.getChannel()
                    .sendMessage("compilation result removed from cache, " +
                            "repost message with code block.").queue();
        }
    }

    private void handleMessage(Message message) {
        var contentRaw = message.getContentRaw();
        var dm = DiscordMessageParser.of(contentRaw);
        compileCodeBlocks(message, dm);
    }

    private void compileCodeBlocks(@NotNull Message message, DiscordMessageParser dm) {
        dm.getParts().stream()
                .filter(MessagePart::isCode)
                .findFirst()
                .ifPresent(part -> wandboxApi.compile(part.getText(), part.getLang(),
                        wandboxResponse -> {
                            compilationResults.put(message.getId(),
                                    WandboxDiscordUtils.formatWandboxResponse(wandboxResponse));
                            message.addReaction(PLAY).queue();

                        }));
    }
}
