package io.horrorshow.codey.challenge;

import io.horrorshow.codey.challenge.xml.Problem;
import lombok.Getter;
import net.dv8tion.jda.api.entities.TextChannel;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Getter
public class Challenge {
    private final Problem problem;
    private final TextChannel channel;
    private final LocalDateTime startTime;
    private final LocalDateTime endTime;
    private final CodingCompetition codingCompetition;
    private final DateTimeFormatter df = DateTimeFormatter.ofPattern("HH:mm");
    private final List<ChallengeEntry> entries = Collections.synchronizedList(new ArrayList<>());
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
        }, (long) problem.getMinutes() * 60 * 1000);
    }

    public int getTestsTotal() {
        return problem.getTestcases().getTestcase().size();
    }

    private void timeIsUp() {
        state = State.DONE;
        codingCompetition.onChallengeTimeUp(this);
    }

    @Override
    public String toString() {
        return "*%s* [%s]\nstart: %s end: %s\n%s\n%s"
                .formatted(problem.getName(), state.toString(),
                        df.format(startTime), df.format(endTime),
                        problem.getDescription(), problem.getTemplate());
    }
}
