package io.horrorshow.discordcodeformatter;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageUpdateEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.security.auth.login.LoginException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
public class JDABot extends ListenerAdapter {
    private static final String PROP_TOKEN = "${jda.token}";

    private static final String STARS = "✨";
    private static final String BASKET = "\uD83D\uDDD1️";

    private final JavaFormatter javaFormatter;

    private final Map<String, Message> formattedCodeStore = new HashMap<>();

    public JDABot(@Autowired @Value(PROP_TOKEN) String token,
                  @Autowired JavaFormatter javaFormatter) throws LoginException {
        this.javaFormatter = javaFormatter;
        Assert.notNull(token, "Token must not be null, did you forget to set ${%s}?"
                .formatted(PROP_TOKEN));
        JDA jda = JDABuilder.createDefault(token).build();
        jda.setAutoReconnect(true);
        jda.addEventListener(this);
    }

    @Override
    public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent event) {
        if (!event.getAuthor().isBot()) {
            addReactionIfPrettyPrintable(event.getMessage());
        }
    }

    @Override
    public void onGuildMessageReactionAdd(@NotNull GuildMessageReactionAddEvent event) {
        if (!event.getUser().isBot()) {
            event.getChannel()
                    .retrieveMessageById(event.getMessageId())
                    .queue(message -> {
                        handleUserMessage(event, message);
                    });
        }
    }

    private void handleUserMessage(@NotNull GuildMessageReactionAddEvent event, Message message) {
        if (!message.getAuthor().isBot()) {
            createFormattedMessageIfParsable(event);
        }
        if (BASKET.equals(event.getReactionEmote().getEmoji())
                && event.getJDA().getSelfUser().getId()
                .equals(message.getAuthor().getId())) {
            removeFormattedMessage(message);
        }
    }

    private void removeFormattedMessage(Message message) {
        message.delete().queue();
        formattedCodeStore.values().
                removeIf(storedMsg ->
                        message.getId().equals(storedMsg.getId()));
    }

    @Override
    public void onGuildMessageUpdate(@NotNull GuildMessageUpdateEvent event) {
        if (!event.getAuthor().isBot()) {
            addReactionIfPrettyPrintable(event.getMessage());
        }
    }

    private void addReactionIfPrettyPrintable(@NotNull Message message) {
        formatted(message.getContentRaw())
                .ifPresentOrElse(s -> message.addReaction(STARS).queue(),
                        () -> message.removeReaction(STARS).queue());
    }

    private Optional<String> formatted(String rawMessage) {
        StringBuilder sb = new StringBuilder();
        boolean isFormatted = false;
        var dm = new DiscordMessage(rawMessage);
        for (var part : dm.getParts()) {
            if (part.isCode()) {
                var parseRes = javaFormatter.format(part.getText());
                if (parseRes.isPresent()) {
                    isFormatted = true;
                    sb.append(codeBlockOf(parseRes.get().getText(), parseRes.get().getLang()));
                } else {
                    sb.append(codeBlockOf(part.getText(), part.getLang()));
                }
            } else {
                sb.append(part.getText());
            }
        }

        if (!isFormatted) return Optional.empty();
        else return Optional.of(sb.toString());
    }

    private String codeBlockOf(String code, String lang) {
        return "```" + lang + "\n" +
                code +
                "```";
    }

    private void createFormattedMessageIfParsable(@NotNull GuildMessageReactionAddEvent event) {
        if (STARS.equals(event.getReactionEmote().getEmoji())) {
            var channel = event.getChannel();
            channel.retrieveMessageById(event.getMessageId())
                    .queue(message -> {
                        var formatted = formatted(message.getContentRaw());
                        formatted.ifPresentOrElse(
                                s -> postFormattedCode(channel, message, s),
                                () -> message.removeReaction(STARS).queue());
                    });
        }
    }

    private void postFormattedCode(TextChannel channel, Message message, String s) {
        if (!formattedCodeStore.containsKey(message.getId())
                || !formattedCodeStore.get(message.getId()).getContentRaw().equals(s)) {
            channel.sendMessage(s)
                    .queue(m -> {
                        formattedCodeStore.put(message.getId(), m);
                        m.addReaction(BASKET).queue();
                    });
        }
    }
}
