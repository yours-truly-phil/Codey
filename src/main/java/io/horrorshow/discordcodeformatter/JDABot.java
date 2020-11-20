package io.horrorshow.discordcodeformatter;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.security.auth.login.LoginException;
import java.util.stream.Collectors;

@Service
@Slf4j
public class JDABot extends ListenerAdapter {
    private static final String PROP_TOKEN = "${jda.token}";

    private final JavaFormatter javaFormatter;

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
            StringBuilder sb = new StringBuilder();
            dm.getParts().forEach(part -> {
                if (part.isCode()) {
                    sb.append("```").append(part.getLang()).append("\n");
                    javaFormatter.googleFormat(part.getText())
                            .ifPresentOrElse(sb::append,
                                    () -> sb.append(part.getText()));
                    sb.append("```");
                } else {
                    sb.append(part.getText());
                }
            });
            event.getChannel().sendMessage(sb.toString()).queue();
        }
    }
}
