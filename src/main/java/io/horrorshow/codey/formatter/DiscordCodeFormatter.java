package io.horrorshow.codey.formatter;

import com.google.common.annotations.VisibleForTesting;
import io.horrorshow.codey.discordutil.DiscordMessage;
import io.horrorshow.codey.discordutil.DiscordUtils;
import io.horrorshow.codey.discordutil.MessageStore;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageUpdateEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;


@Service
@Slf4j
public class DiscordCodeFormatter extends ListenerAdapter {

    public static final String STARS = "✨";

    private final JavaFormatter javaFormatter;
    private final MessageStore messageStore;
    private final DiscordUtils utils;


    public DiscordCodeFormatter(@Autowired JDA jda,
            @Autowired JavaFormatter javaFormatter,
            @Autowired MessageStore messageStore,
            @Autowired DiscordUtils utils) {
        this.javaFormatter = javaFormatter;
        this.messageStore = messageStore;
        this.utils = utils;

        jda.addEventListener(this);
    }


    @Override
    public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent event) {
        if (event.getAuthor().isBot()) {
            return;
        }

        handleMessage(event.getMessage());
    }


    @Override
    public void onGuildMessageUpdate(@NotNull GuildMessageUpdateEvent event) {
        if (event.getAuthor().isBot()) {
            return;
        }

        handleMessage(event.getMessage());
    }


    @Override
    public void onGuildMessageReactionAdd(@NotNull GuildMessageReactionAddEvent event) {
        if (event.getUser().isBot()) {
            return;
        }

        final String emoji = event.getReactionEmote().getEmoji();
        if (STARS.equals(emoji)) {
            event.getChannel()
                    .retrieveMessageById(event.getMessageId())
                    .queue(this::starsReaction);
        }
    }


    private void starsReaction(Message message) {
        if (message.getAuthor().isBot()) {
            return;
        }

        var dm = DiscordMessage.of(message.getContentRaw());
        formatted(dm).ifPresentOrElse(
                s -> postFormattedCode(message.getTextChannel(), message, s),
                () -> message.removeReaction(STARS).queue());
    }


    private void handleMessage(Message message) {
        final String contentRaw = message.getContentRaw();

        var dm = DiscordMessage.of(contentRaw);
        formatted(dm).ifPresentOrElse(s -> message.addReaction(STARS).queue(),
                () -> message.removeReaction(STARS).queue());
    }


    @VisibleForTesting
    Optional<String> formatted(DiscordMessage dm) {
        StringBuilder sb = new StringBuilder();
        boolean isFormatted = false;
        for (var part : dm.getParts()) {
            if (part.isCode()) {
                var parseRes = javaFormatter.format(part.getText());
                if (parseRes.isPresent()) {
                    isFormatted = true;
                    sb.append(codeBlockOf(parseRes.get().text(),
                            parseRes.get().lang()));
                } else {
                    sb.append(codeBlockOf(part.getText(), part.getLang()));
                }
            } else {
                sb.append(part.getText());
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


    private void postFormattedCode(TextChannel channel, Message message, String s) {
        if (noDuplicatePost(message, s)) {
            utils.sendRemovableMessage(s, channel,
                    m -> messageStore.getFormattedCodeStore().put(message.getId(), m));
        }
    }


    private boolean noDuplicatePost(Message message, String s) {
        return !messageStore.getFormattedCodeStore().containsKey(message.getId())
               || !messageStore.getFormattedCodeStore().get(
                message.getId()).getContentRaw().equals(s);
    }
}
