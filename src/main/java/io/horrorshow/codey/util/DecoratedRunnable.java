package io.horrorshow.codey.util;

import org.slf4j.MDC;

import java.util.concurrent.CompletableFuture;


public class DecoratedRunnable implements Runnable {

    private final Runnable delegate;
    private final TaskInfo taskInfo;


    public DecoratedRunnable(Runnable delegate, TaskInfo taskInfo) {
        this.delegate = delegate;
        this.taskInfo = taskInfo;
    }


    @Override
    public void run() {
        try (var ignored = MDC.putCloseable(LogbackConverter.MDC_CHANNEL_KEY,
                "%s: %s".formatted(taskInfo.channel.getId(), taskInfo.channel.getName()));
             var ignored2 = MDC.putCloseable(LogbackConverter.MDC_USER_KEY,
                     "%s: %s".formatted(taskInfo.user.getId(), taskInfo.user.getName()));
             var ignored3 = MDC.putCloseable(LogbackConverter.MDC_GUILD_KEY,
                     "%s: %s".formatted(taskInfo.guild.getId(), taskInfo.guild.getName()));
             var ignored4 = MDC.putCloseable(LogbackConverter.MDC_TASK_KEY, taskInfo.id)) {
            delegate.run();
        }
    }


    public static CompletableFuture<Void> runAsync(Runnable runnable, TaskInfo taskInfo) {
        return CompletableFuture.runAsync(new DecoratedRunnable(runnable, taskInfo));
    }
}
