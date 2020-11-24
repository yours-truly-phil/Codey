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
    private static final String CLEAR_CACHE_CMD = "$cf-clear-cache";

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

    private void handleMessageWithNewReaction(@NotNull GuildMessageReactionAddEvent event,
                                              Message message) {
        final String emoji = event.getReactionEmote().getEmoji();
        if (!message.getAuthor().isBot()) {
            if (STARS.equals(emoji)) {
                var dm = new DiscordMessage(message.getContentRaw());
                formatted(dm).ifPresentOrElse(
                        s -> postFormattedCode(message.getTextChannel(), message, s),
                        () -> message.removeReaction(STARS).queue());
            } else if (PLAY.equals(emoji)) {
                printOnceCompilationResultIfPresent(message);
            }
        }
        if (BASKET.equals(emoji)
                && event.getJDA().getSelfUser().getId().equals(message.getAuthor().getId())) {
            removeFormattedMessage(message);
        }
    }

    private void printOnceCompilationResultIfPresent(Message message) {
        if (compilationResults.containsKey(message.getId())) {
            message.getChannel()
                    .sendMessage(compilationResults
                            .getOrDefault(message.getId(), "no result"))
                    .queue(sentMsg -> sentMsg.addReaction(BASKET).queue());
            compilationResults.remove(message.getId());
        }
    }

    private void removeFormattedMessage(Message message) {
        message.delete().queue();
        formattedCodeStore.values().
                removeIf(storedMsg -> message.getId().equals(storedMsg.getId()));
    }

    private void handleMessage(@NotNull Message message) {
        final String contentRaw = message.getContentRaw();

        var dm = new DiscordMessage(contentRaw);
        formatted(dm).ifPresentOrElse(s -> message.addReaction(STARS).queue(),
                () -> message.removeReaction(STARS).queue());

        compileCodeBlocks(message, dm);

        if (CLEAR_CACHE_CMD.equals(contentRaw.trim())) {
            this.compilationResults.clear();
            this.formattedCodeStore.clear();
        }
    }

    private void compileCodeBlocks(@NotNull Message message, DiscordMessage dm) {
        dm.getParts().stream()
                .filter(DiscordMessage.MessagePart::isCode)
                .findFirst()
                .ifPresent(part ->
                        wandboxApi.compile(part.getText(), part.getLang(),
                                wandboxOutput -> {
                                    compilationResults.put(message.getId(), wandboxOutput.getText());
                                    message.addReaction(PLAY).queue();
                                }));
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
