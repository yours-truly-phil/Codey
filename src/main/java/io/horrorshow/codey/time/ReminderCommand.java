package io.horrorshow.codey.time;

import io.horrorshow.codey.discordutil.DiscordUtils;
import io.horrorshow.codey.discordutil.MessageStore;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.util.Map;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;


@Service
@Slf4j
public class ReminderCommand extends ListenerAdapter {

    private static final String ALARM = "⏰";

    private final DiscordUtils utils;
    private final Map<String, Reminder> timerMap;


    @Autowired
    public ReminderCommand(JDA jda, DiscordUtils utils, MessageStore store) {
        this.utils = utils;
        this.timerMap = store.getTimerMap();

        var remindMe = new CommandData("remind-me", "Set a reminder")
                .addOptions(new OptionData(OptionType.INTEGER, "in", "how many minutes from now?", true),
                        new OptionData(OptionType.STRING, "m", "what should it say?", true));
        var showReminders = new CommandData("show-reminders", "Show your running reminders")
                .addOptions(new OptionData(OptionType.BOOLEAN, "all", "Show reminders of all users"));
        var stopReminder = new CommandData("stop-reminder", "Stop a running reminder")
                .addOptions(new OptionData(OptionType.STRING, "id", "Id of the reminder to stop", true));

        jda.addEventListener(this);
        jda.updateCommands().addCommands(remindMe, showReminders, stopReminder).queue();
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
        final BiFunction<String, Reminder, String> formatEntry =
                (id, reminder) -> "%s by %s (%ds remaining) -> %s".formatted(
                        id,
                        reminder.getUser().getName(),
                        getSecondsUntil(reminder),
                        reminder.getMessage());

        var allOption = event.getOption("all");
        if (allOption != null && allOption.getAsBoolean() && utils.isElevatedMember(event.getMember())) {
            event.reply("***All currently running timers***\n" + timerMap.entrySet().stream()
                    .map(entry -> formatEntry.apply(entry.getKey(), entry.getValue()))
                    .collect(Collectors.joining("\n"))).complete();
        } else {
            event.reply("***Currently running timers by " + event.getUser().getName() + "***\n" + timerMap.entrySet().stream()
                    .filter(entry -> entry.getValue().getUser().getIdLong() == event.getUser().getIdLong())
                    .map(entry -> formatEntry.apply(entry.getKey(), entry.getValue()))
                    .collect(Collectors.joining("\n"))).complete();
        }
    }


    private long getSecondsUntil(Reminder reminder) {
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
        } else if (inMinutes > 60 * 24 * 365 * 20) {
            event.reply("can't remind you on "
                        + LocalDateTime.ofInstant(Instant.now()
                    .plus(60 * 24 * 365 * 20, ChronoUnit.MINUTES), ZoneId.systemDefault())
                        + ". Too far in the future.").complete();
            return;
        }
        var message = options.get("m").getAsString();

        var embed = new EmbedBuilder()
                .setColor(event.getMember() != null ? event.getMember().getColor() : Color.MAGENTA)
                .setDescription("%s%s%s\n%s".formatted(ALARM, ALARM, ALARM, message))
                .setThumbnail(event.getUser().getAvatarUrl())
                .setAuthor(event.getUser().getName())
                .setFooter("reminder set at")
                .setTimestamp(Instant.now())
                .build();

        var timer = new Reminder(message, event.getUser(), () -> {
            utils.sendRemovableEmbed(embed, event.getTextChannel());
            timerMap.remove(event.getId());
        }, inMinutes * 60 * 1000);

        timerMap.put(event.getId(), timer);

        event.reply("reminder set %dmin from now".formatted(inMinutes)).complete();
    }


    static public class Reminder extends Timer {

        @Getter
        private final String message;
        @Getter
        private final User user;
        @Getter
        private final Temporal start;
        @Getter
        private final Temporal done;


        public Reminder(String message, User user, Runnable runnable, long delay) {
            super();
            this.message = message;
            this.user = user;
            start = Instant.now();
            done = start.plus(delay, ChronoUnit.MILLIS);

            var task = new TimerTask() {
                @Override
                public void run() {
                    runnable.run();
                }
            };
            schedule(task, delay);
        }
    }
}
