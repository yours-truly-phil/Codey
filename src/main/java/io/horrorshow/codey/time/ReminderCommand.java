package io.horrorshow.codey.time;

import io.horrorshow.codey.data.TimerData;
import io.horrorshow.codey.data.TimerRepository;
import io.horrorshow.codey.discordutil.DataStore;
import io.horrorshow.codey.discordutil.DiscordUtils;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
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
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;


@Service
@Slf4j
public class ReminderCommand extends ListenerAdapter {

    private static final String ALARM = "⏰";
    private static final long MAX_DURATION_MINS = 60 * 24 * 365 * 20;

    private final DiscordUtils utils;
    private final Map<Long, ReminderTask> timerMap;
    private final TimerRepository timerRepository;


    @Autowired
    public ReminderCommand(JDA jda, DiscordUtils utils, DataStore store, TimerRepository timerRepository) {
        this.utils = utils;
        this.timerMap = store.getTimerMap();
        this.timerRepository = timerRepository;

        try {
            jda.awaitReady();
            for (var timerData : timerRepository.findAll()) {
                scheduleTimer(jda, utils, timerRepository, timerData);
            }
        } catch (InterruptedException e) {
            log.error("unable to wait for jda to startup", e);
        }

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
                    .map(entry -> formatReminder(entry.getKey().toString(), entry.getValue()))
                    .collect(Collectors.joining("\n"))).complete();
        } else {
            event.reply("***Currently running timers by " + event.getUser().getName() + "***\n" + timerMap.entrySet().stream()
                    .filter(entry -> entry.getValue().user().getIdLong() == event.getUser().getIdLong())
                    .map(entry -> formatReminder(entry.getKey().toString(), entry.getValue()))
                    .collect(Collectors.joining("\n"))).complete();
        }
    }


    private String formatReminder(String id, ReminderTask reminder) {
        return "%s by %s (%ds remaining) -> %s".formatted(
                id,
                reminder.user().getName(),
                getSecondsUntil(reminder),
                reminder.message());
    }


    private long getSecondsUntil(ReminderTask reminder) {
        return Instant.now().until(reminder.done(), ChronoUnit.SECONDS);
    }


    public void onStopReminder(SlashCommandEvent event) {
        var id = Objects.requireNonNull(event.getOption("id")).getAsLong();
        var reminder = timerMap.get(id);
        if (reminder != null
            && (event.getUser().getId().equals(reminder.user().getId()) || utils.isElevatedMember(event.getMember()))) {

            reminder.task().cancel();
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
        } else if (inMinutes > MAX_DURATION_MINS) {
            event.reply("can't remind you on "
                        + LocalDateTime.ofInstant(Instant.now()
                    .plus(MAX_DURATION_MINS, ChronoUnit.MINUTES), ZoneId.systemDefault())
                        + ". Too far in the future.").complete();
            return;
        }

        var requestPing = !options.containsKey("ping") || options.get("ping").getAsBoolean();

        var message = options.get("m").getAsString();

        var newTimerData = new TimerData(null,
                event.getTextChannel().getId(),
                message, event.getUser().getId(),
                Instant.now().toEpochMilli(),
                Instant.now().plus(inMinutes * 60, ChronoUnit.SECONDS).toEpochMilli(),
                requestPing);
        var timerData = timerRepository.save(newTimerData);

        scheduleTimer(event.getJDA(), utils, timerRepository, timerData);

        event.reply("reminder set %dmin from now".formatted(inMinutes)).complete();
    }


    private void scheduleTimer(JDA jda, DiscordUtils utils, TimerRepository timerRepository, TimerData timerData) {
        var reminderTask = createTimer(timerData, jda,
                (embed, channel) -> runTimer(timerData, embed, channel, utils, timerRepository));

        if (Instant.now().isAfter(Instant.from(reminderTask.done()))) {
            timerRepository.deleteById(reminderTask.id());
        } else {
            var timer = new Timer();
            timer.schedule(reminderTask.task(), timerData.getDone() - Instant.now().toEpochMilli());
            timerMap.put(reminderTask.id(), reminderTask);
        }
    }


    private void runTimer(TimerData timerData, MessageEmbed embed, TextChannel channel, DiscordUtils utils,
            TimerRepository timerRepository) {

        if (timerData.getIsPingUser()) {
            utils.sendRemovableMessageAsync("%s <@%s>".formatted(ALARM, timerData.getUserId()), channel);
        }
        utils.sendRemovableEmbed(embed, channel);
        timerMap.remove(timerData.getId());
        timerRepository.deleteById(timerData.getId());
    }


    public ReminderTask createTimer(TimerData timerData, JDA jda, BiConsumer<MessageEmbed, TextChannel> onRun) {
        var channel = jda.getTextChannelById(timerData.getChannelId());
        var user = jda.getUserById(timerData.getUserId());
        if (user == null) {
            user = jda.retrieveUserById(timerData.getUserId()).complete();
        }
        var member = Objects.requireNonNull(channel,
                        "Channel must not be null, channelId=" + timerData.getChannelId())
                .getGuild().getMember(user);
        var color = member != null ? member.getColor() : Color.MAGENTA;

        var embed = new EmbedBuilder()
                .setColor(color)
                .setTitle(ALARM + ALARM + ALARM)
                .setDescription(timerData.getMessage())
                .setThumbnail(user.getAvatarUrl())
                .setAuthor(user.getName())
                .setFooter("reminder set at")
                .setTimestamp(Instant.now())
                .build();

        var task = new TimerTask() {
            @Override
            public void run() {
                onRun.accept(embed, channel);
            }
        };

        return new ReminderTask(timerData.getId(), user, timerData.getMessage(), Instant.ofEpochMilli(timerData.getDone()), task);
    }
}
