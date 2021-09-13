package io.horrorshow.codey.time;

import net.dv8tion.jda.api.entities.User;

import java.time.temporal.Temporal;
import java.util.TimerTask;


public record ReminderTask(Long id, User user, String message, Temporal done, TimerTask task) {

}
