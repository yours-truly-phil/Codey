package io.horrorshow.codey.time;

import lombok.Getter;
import net.dv8tion.jda.api.entities.User;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.util.Timer;
import java.util.TimerTask;


public class ReminderTimer extends Timer {

    @Getter
    private final String message;
    @Getter
    private final User user;
    @Getter
    private final Temporal start;
    @Getter
    private final Temporal done;


    public ReminderTimer(String message, User user, Runnable runnable, long delay) {
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
