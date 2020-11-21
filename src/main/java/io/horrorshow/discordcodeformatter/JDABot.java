package io.horrorshow.discordcodeformatter;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
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
import java.util.stream.Collectors;

@Service
@Slf4j
public class JDABot extends ListenerAdapter {
    private static final String PROP_TOKEN = "${jda.token}";

    private static final String EMOJI = "✨";

    private final JavaFormatter javaFormatter;

    private final Map<String, String> prettyPrintedMessages = new HashMap<>();

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
            var dm = new DiscordMessage(event.getMessage().getContentRaw());
            log.info(dm.getParts().stream()
                    .map(DiscordMessage.MessagePart::toString)
                    .collect(Collectors.joining("\n")));
            boolean isPrettyPrintable = false;
            StringBuilder sb = new StringBuilder();
            for (var part : dm.getParts()) {
                if (part.isCode()) {
                    var parseRes = javaFormatter.format(part.getText());
                    if (parseRes.isChanged()) {
                        isPrettyPrintable = true;
                    }
                    sb.append(codeBlockOf(parseRes.getText(), part.getLang()));
                } else {
                    // not sure if I want to include other stuff in the output other than code
                    sb.append(part.getText());
                }
            }
            if (isPrettyPrintable) {
                messagePrettyPrintable(event.getMessage(), sb.toString());
            }
        }
    }

    private String codeBlockOf(String code, String lang) {
        return "```" + lang + "\n" +
                code +
                "```";
    }

    @Override
    public void onGuildMessageReactionAdd(@NotNull GuildMessageReactionAddEvent event) {
        if (!event.getUser().isBot()) {
            if (EMOJI.equals(event.getReactionEmote().getEmoji())) {
                if (prettyPrintedMessages.containsKey(event.getMessageId())) {
                    var formattedMessage = prettyPrintedMessages.get(event.getMessageId());
                    var channel = event.getChannel();
                    channel.sendMessage(formattedMessage).queue();
                }
            }
        }
    }

    private void messagePrettyPrintable(Message message, String prettyPrinted) {
        prettyPrintedMessages.put(message.getId(), prettyPrinted);
        message.addReaction(EMOJI).queue();
    }
}
