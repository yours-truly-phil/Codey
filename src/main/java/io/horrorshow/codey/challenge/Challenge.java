package io.horrorshow.codey.challenge;

import io.horrorshow.codey.challenge.xml.Problem;
import lombok.Getter;
import net.dv8tion.jda.api.entities.TextChannel;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Timer;
import java.util.TimerTask;

@Getter
public class Challenge {
    private final Problem problem;
    private final TextChannel channel;
    private final LocalDateTime startTime;
    private final LocalDateTime endTime;
    private final CodingCompetition codingCompetition;
    private final DateTimeFormatter df = DateTimeFormatter.ofPattern("HH:mm");
    private State state = State.ACTIVE;

    public Challenge(Problem problem,
                     TextChannel channel,
                     CodingCompetition codingCompetition) {
        this.problem = problem;
        this.channel = channel;
        this.codingCompetition = codingCompetition;

        startTime = LocalDateTime.now();
        endTime = startTime.plusMinutes(problem.getMinutes());

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                timeIsUp();
            }
        }, problem.getMinutes() * 60 * 1000);
    }

    private void timeIsUp() {
        state = State.DONE;
        codingCompetition.onChallengeTimeUp(this);
        channel.sendMessage("time is up!").queue();
    }

    @Override
    public String toString() {
        return "*" + problem.getName() + "* [" + state.toString() + "]" + "\n" +
                "start: " + df.format(startTime) + " end: " + df.format(endTime) + "\n" +
                problem.getDescription();
    }
}
