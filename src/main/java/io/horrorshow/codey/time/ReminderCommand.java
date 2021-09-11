package io.horrorshow.codey.time;

import io.horrorshow.codey.discordutil.DiscordUtils;
import io.horrorshow.codey.discordutil.DataStore;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;


@Service
@Slf4j
public class ReminderCommand extends ListenerAdapter {

    private static final String ALARM = "⏰";
    private static final long MAX_DURATION_MINS = 60 * 24 * 365 * 20;

    private final DiscordUtils utils;
    private final Map<String, ReminderTimer> timerMap;


    @Autowired
    public ReminderCommand(JDA jda, DiscordUtils utils, DataStore store) {
        this.utils = utils;
        this.timerMap = store.getTimerMap();

        jda.addEventListener(this);
    }


    @Override
    public void onSlashCommand(SlashCommandEvent event) {
        final var cmd = event.getName();
        switch (cmd) {
            case "remind-me" -> CompletableFuture.runAsync(() -> onRemindCommand(event));
            case "show-reminders" -> CompletableFuture.runAsync(() -> onShowReminders(event));
            case "stop-reminder" -> CompletableFuture.runAsync(() -> onStopReminder(event));
        }
    }


    public void onShowReminders(SlashCommandEvent event) {
        var allOption = event.getOption("all");
        if (allOption != null && allOption.getAsBoolean() && utils.isElevatedMember(event.getMember())) {
            event.reply("***All currently running timers***\n" + timerMap.entrySet().stream()
                    .map(entry -> formatReminder(entry.getKey(), entry.getValue()))
                    .collect(Collectors.joining("\n"))).complete();
        } else {
            event.reply("***Currently running timers by " + event.getUser().getName() + "***\n" + timerMap.entrySet().stream()
                    .filter(entry -> entry.getValue().getUser().getIdLong() == event.getUser().getIdLong())
                    .map(entry -> formatReminder(entry.getKey(), entry.getValue()))
                    .collect(Collectors.joining("\n"))).complete();
        }
    }


    private String formatReminder(String id, ReminderTimer reminder) {
        return "%s by %s (%ds remaining) -> %s".formatted(
                id,
                reminder.getUser().getName(),
                getSecondsUntil(reminder),
                reminder.getMessage());
    }


    private long getSecondsUntil(ReminderTimer reminder) {
        return Instant.now().until(reminder.getDone(), ChronoUnit.SECONDS);
    }


    public void onStopReminder(SlashCommandEvent event) {
        var id = Objects.requireNonNull(event.getOption("id")).getAsString();
        var reminder = timerMap.get(id);
        if (reminder != null
            && (event.getUser().getIdLong() == reminder.getUser().getIdLong() || utils.isElevatedMember(event.getMember()))) {
            reminder.cancel();
            timerMap.remove(id);
            event.reply("Timer %s cancelled".formatted(id)).complete();
        } else {
            event.reply("Timer %s might not exist or you don't have the necessary permissions".formatted(id)).complete();
        }
    }


    public void onRemindCommand(SlashCommandEvent event) {
        var options = event.getOptions().stream()
                .collect(Collectors.toMap(OptionMapping::getName, Function.identity()));

        var inMinutes = options.get("in").getAsLong();
        if (inMinutes < 1) {
            event.reply("That's in the past.").complete();
            return;
        } else {
            if (inMinutes > MAX_DURATION_MINS) {
                event.reply("can't remind you on "
                            + LocalDateTime.ofInstant(Instant.now()
                        .plus(MAX_DURATION_MINS, ChronoUnit.MINUTES), ZoneId.systemDefault())
                            + ". Too far in the future.").complete();
                return;
            }
        }
        var message = options.get("m").getAsString();

        var embed = new EmbedBuilder()
                .setColor(event.getMember() != null ? event.getMember().getColor() : Color.MAGENTA)
                .setDescription("%s%s%s\n%s\n<@%s>".formatted(ALARM, ALARM, ALARM, message, event.getUser().getId()))
                .setThumbnail(event.getUser().getAvatarUrl())
                .setAuthor(event.getUser().getName())
                .setFooter("reminder set at")
                .setTimestamp(Instant.now())
                .build();

        var timer = new ReminderTimer(message, event.getUser(), () -> {
            utils.sendRemovableEmbed(embed, event.getTextChannel());
            timerMap.remove(event.getId());
        }, inMinutes * 60 * 1000);

        timerMap.put(event.getId(), timer);

        event.reply("reminder set %dmin from now".formatted(inMinutes)).complete();
    }
}
