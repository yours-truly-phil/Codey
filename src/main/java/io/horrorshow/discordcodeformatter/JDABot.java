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
    private static final String PLAY = "▶️";

    private final JavaFormatter javaFormatter;
    private final WandboxApi wandboxApi;

    private final Map<String, Message> formattedCodeStore = new HashMap<>();
    private final Map<String, String> compilationResults = new HashMap<>();

    public JDABot(@Autowired @Value(PROP_TOKEN) String token,
                  @Autowired JavaFormatter javaFormatter,
                  @Autowired WandboxApi wandboxApi) throws LoginException {
        this.javaFormatter = javaFormatter;
        this.wandboxApi = wandboxApi;
        Assert.notNull(token, "Token must not be null, did you forget to set ${%s}?"
                .formatted(PROP_TOKEN));
        JDA jda = JDABuilder.createDefault(token).build();
        jda.setAutoReconnect(true);
        jda.addEventListener(this);
    }

    @Override
    public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent event) {
        if (!event.getAuthor().isBot()) {
            handleMessage(event.getMessage());
        }
    }

    @Override
    public void onGuildMessageReactionAdd(@NotNull GuildMessageReactionAddEvent event) {
        if (!event.getUser().isBot()) {
            event.getChannel()
                    .retrieveMessageById(event.getMessageId())
                    .queue(message -> handleMessageWithNewReaction(event, message));
        }
    }

    @Override
    public void onGuildMessageUpdate(@NotNull GuildMessageUpdateEvent event) {
        if (!event.getAuthor().isBot()) {
            handleMessage(event.getMessage());
        }
    }

    private void handleMessageWithNewReaction(@NotNull GuildMessageReactionAddEvent event, Message message) {
        if (!message.getAuthor().isBot()
                && STARS.equals(event.getReactionEmote().getEmoji())) {
            var dm = new DiscordMessage(message.getContentRaw());
            formatted(dm).ifPresentOrElse(
                    s -> postFormattedCode(message.getTextChannel(), message, s),
                    () -> message.removeReaction(STARS).queue());
        }
        if (BASKET.equals(event.getReactionEmote().getEmoji())
                && event.getJDA().getSelfUser().getId().equals(message.getAuthor().getId())) {

            removeFormattedMessage(message);
        }
    }

    private void removeFormattedMessage(Message message) {
        message.delete().queue();
        formattedCodeStore.values().
                removeIf(storedMsg -> message.getId().equals(storedMsg.getId()));
    }

    private void handleMessage(@NotNull Message message) {
        var dm = new DiscordMessage(message.getContentRaw());
        formatted(dm).ifPresentOrElse(s -> message.addReaction(STARS).queue(),
                () -> message.removeReaction(STARS).queue());

        compileCodeBlocks(message, dm);
    }

    private void compileCodeBlocks(@NotNull Message message, DiscordMessage dm) {
        for (var part : dm.getParts()) {
            if (part.isCode()) {
                wandboxApi.compile(part.getText(), part.getLang(),
                        wandboxOutput -> {
                            compilationResults.put(message.getId(), wandboxOutput.getText());
                            message.addReaction(PLAY).queue();
                        });
            }
        }
    }

    private Optional<String> formatted(DiscordMessage dm) {
        StringBuilder sb = new StringBuilder();
        boolean isFormatted = false;
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
