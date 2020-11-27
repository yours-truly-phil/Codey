package io.horrorshow.discordcodeformatter.challenge;

import net.dv8tion.jda.api.entities.TextChannel;

import java.util.Timer;
import java.util.TimerTask;

public class Challenge {
    private static final long DEFAULT_CHALLENGE_DUR = 1000 * 60 * 60;

    private final Problem problem;
    private final TextChannel channel;
    private final Timer timer;
    private State state = State.ACTIVE;

    public Challenge(Problem problem,
                     TextChannel channel) {
        this.problem = problem;
        this.channel = channel;
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                timeIsUp();
            }
        }, DEFAULT_CHALLENGE_DUR);
    }

    private void timeIsUp() {
        state = State.DONE;
    }
}
