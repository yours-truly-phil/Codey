package io.horrorshow.codey.time;

import io.horrorshow.codey.discordutil.DiscordUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class DiscordTimezone extends ListenerAdapter {
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
        String rawContent = message.getContentRaw();
        TimeMatch[] matches = matchTimes(rawContent);

        if(matches.length == 0)
            return;

        // Retrieve only one match
        TimeMatch match = matches[0];

        int[] time = match.parseTime();
        OffsetDateTime timeStamp;
        if(match.hasWeekDay())
            timeStamp = parseTime(time[0], time[1], match.region, match.weekDay);
        else
            timeStamp = parseTime(time[0], time[1], match.region);

        utils.sendRemovableMessageReply(message, new EmbedBuilder()
                .setFooter("Time in your timezone: ")
                .setTimestamp(timeStamp)
                .setColor(utils.getColor())
                .build());
    }

    private OffsetDateTime parseTime(int hour, int minute, String zoneId, DayOfWeek weekDay)
    {
        return OffsetDateTime.now(ZoneId.of(zoneId)).with(ChronoField.DAY_OF_WEEK, weekDay.getValue()).withHour(hour).withMinute(minute);
    }

    private OffsetDateTime parseTime(int hour, int minute, String zoneId)
    {
        return OffsetDateTime.now(ZoneId.of(zoneId)).withHour(hour).withMinute(minute);
    }

    private final Pattern[] patterns = new Pattern[]{
            Pattern.compile("[\\d]{1,2}:[\\d]{1,2} +(\\w+/\\w+|\\w{2,5})"),
            Pattern.compile("([^\\s]+) +[\\d]{1,2}:[\\d]{1,2} +(\\w+/\\w+|\\w{2,5})")
    };

    TimeMatch[] matchTimes(String messageContent)
    {
        List<TimeMatch> matches = new ArrayList<>();

        for(Pattern pattern : patterns) {
            Matcher matcher = pattern.matcher(messageContent);
            while (matcher.find()) {
                if(!startsWithWeekDay(matcher.group().toUpperCase(Locale.ROOT))) {
                    String time = stripRegion(matcher.group());
                    String region = stripTime(matcher.group());

                    if (ZoneId.getAvailableZoneIds().contains(region))
                        matches.add(new TimeMatch(time, region, false, null, matcher.start()));
                } else
                {
                    DayOfWeek weekOfDay = getWeekOfDay(matcher.group().toUpperCase(Locale.ROOT));
                    String time = getTime(matcher.group());
                    String region = getRegion(matcher.group());

                    if (ZoneId.getAvailableZoneIds().contains(region)) {
                        // Rather hacky solution to remove the match if it was already detected without the week day
                        matches.remove(matches.size() - 1);
                        matches.add(new TimeMatch(time, region, true, weekOfDay, matcher.start()));
                    }
                }
            }
        }

        return matches.toArray(new TimeMatch[0]);
    }

    private String getTime(String group) {
        return group.split("\\s+")[1];
    }

    private String getRegion(String group) {
        return group.split("\\s+")[2];
    }

    private DayOfWeek getWeekOfDay(String group) {
        return DayOfWeek.valueOf(group.split("\\s+")[0]);
    }

    private final List<String> weekDays = Arrays.asList("MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY");

    private boolean startsWithWeekDay(String group)
    {
        return weekDays.contains(group.split("\\s+")[0]);
    }

    private String stripTime(String group) {
        String[] data = group.split("\\s+");

        if(data.length == 1)
            return data[0];

        return String.join(" ", Arrays.copyOfRange(data, 1, data.length));
    }


    private String stripRegion(String group) {
        return group.split("\\s+")[0];
    }

    record TimeMatch(String time, String region, boolean hasWeekDay, DayOfWeek weekDay, int index) {
        public int[] parseTime() {
            String[] time = time().split(":");
            int hour = Integer.parseInt(time[0].trim());
            int minute = Integer.parseInt(time[1].trim());

            return new int[]{hour, minute};
        }
    }
}
