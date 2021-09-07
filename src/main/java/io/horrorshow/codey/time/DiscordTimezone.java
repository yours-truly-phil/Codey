package io.horrorshow.codey.time;

import io.horrorshow.codey.discordutil.DiscordUtils;
import io.horrorshow.codey.parser.TimeParser;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.DateFormatSymbols;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class DiscordTimezone extends ListenerAdapter {
    private static final Pattern timeMatcher = Pattern.compile(createTimeMatchPattern(), Pattern.CASE_INSENSITIVE);
    private final DiscordUtils utils;

    public DiscordTimezone(@Autowired JDA jda, @Autowired DiscordUtils utils) {
        this.utils = utils;

        jda.addEventListener(this);
    }

    @Override
    public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent event) {
        if (!event.getAuthor().isBot()) {
            CompletableFuture.runAsync(() -> onMessage(event.getMessage()));
        }
    }

    void onMessage(Message message) {
        var matcher = timeMatcher.matcher(message.getContentRaw());

        while (matcher.find()) {
            OffsetDateTime timestamp = TimeParser.toOffsetDateTime(matcher.group());
            var reply = new EmbedBuilder()
                    .setFooter("Time in your timezone: ")
                    .setTimestamp(timestamp)
                    .setColor(utils.getColor()).build();
            utils.sendRemovableMessageReply(message, reply);
        }
    }

    private static String createTimeMatchPattern() {
        return Arrays.stream(new DateFormatSymbols().getWeekdays()).collect(Collectors.joining("|", "(", ")?"))
                + "( )?([0-9]|0[0-9]|1[0-9]|2[0-3]):[0-5][0-9] "
                + ZoneId.getAvailableZoneIds().stream()
                .map(zone -> zone.replaceAll("\\+", "\\\\+"))
                .collect(Collectors.joining("|", "(", ")"));
    }
}
