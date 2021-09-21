package io.horrorshow.codey.formatter;

import com.google.common.annotations.VisibleForTesting;
import io.horrorshow.codey.discordutil.DiscordMessage;
import io.horrorshow.codey.discordutil.DiscordUtils;
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

import java.util.Optional;
import java.util.concurrent.CompletableFuture;


@Service
@Slf4j
public class DiscordCodeFormatter extends ListenerAdapter {

    public static final String STARS = "✨";

    private final JavaFormatter javaFormatter;


    @Autowired
    public DiscordCodeFormatter(JDA jda, JavaFormatter javaFormatter) {
        this.javaFormatter = javaFormatter;

        jda.addEventListener(this);
    }


    @Override
    public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent event) {
        if (!event.getAuthor().isBot()) {
            CompletableFuture.runAsync(() -> onMessage(event.getMessage()));
        }
    }


    @Override
    public void onGuildMessageUpdate(@NotNull GuildMessageUpdateEvent event) {
        if (!event.getAuthor().isBot()) {
            CompletableFuture.runAsync(() -> onMessage(event.getMessage()));
        }
    }


    @Override
    public void onGuildMessageReactionAdd(@NotNull GuildMessageReactionAddEvent event) {
        if (!event.getUser().isBot()) {
            CompletableFuture.runAsync(() -> onReaction(event));
        }
    }


    public void onReaction(@NotNull GuildMessageReactionAddEvent event) {
        final String emoji = event.getReactionEmote().getEmoji();
        if (STARS.equals(emoji)) {
            var message = event.getChannel().retrieveMessageById(event.getMessageId()).complete();
            starsReaction(message);
        }
    }


    private void starsReaction(Message message) {
        if (message.getAuthor().isBot()) {
            return;
        }

        var dm = DiscordMessage.of(message.getContentRaw());
        formatted(dm).ifPresentOrElse(
                s -> DiscordUtils.sendRemovableMessage(s, message.getTextChannel()),
                () -> message.removeReaction(STARS).complete());
    }


    public void onMessage(Message message) {
        final String contentRaw = message.getContentRaw();

        var dm = DiscordMessage.of(contentRaw);
        formatted(dm).ifPresentOrElse(s -> message.addReaction(STARS).complete(),
                () -> message.removeReaction(STARS).complete());
    }


    @VisibleForTesting
    Optional<String> formatted(DiscordMessage dm) {
        StringBuilder sb = new StringBuilder();
        boolean isFormatted = false;
        for (var part : dm.getParts()) {
            if (part.isCode()) {
                var parseRes = javaFormatter.format(part.text());
                if (parseRes.isPresent()) {
                    isFormatted = true;
                    sb.append(codeBlockOf(parseRes.get().text(),
                            parseRes.get().lang()));
                } else {
                    sb.append(codeBlockOf(part.text(), part.lang()));
                }
            } else {
                sb.append(part.text());
            }
        }

        if (!isFormatted) {
            return Optional.empty();
        } else {
            return Optional.of(sb.toString());
        }
    }


    private String codeBlockOf(String code, String lang) {
        return "```" + lang + "\n" +
               code +
               "```";
    }
}
