package io.horrorshow.codey.challenge;

import io.horrorshow.codey.api.WandboxApi;
import io.horrorshow.codey.challenge.xml.Problem;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Slf4j
public class CodingCompetition extends ListenerAdapter {

    private static final String CREATE_CHALLENGE = "$create";
    private static final String SHOW_CHALLENGE = "$show";

    private final WandboxApi wandboxApi;

    private final List<Problem> problemList = new ArrayList<>();

    private final Map<TextChannel, Challenge> challenges = new HashMap<>();

    private final Random random = new Random();

    public CodingCompetition(@Autowired JDA jda,
                             @Autowired WandboxApi wandboxApi,
                             @Autowired @Value("${challenge.path}") String challengePath) throws IOException, JAXBException {
        this.wandboxApi = wandboxApi;

        jda.addEventListener(this);

        loadProblemList(challengePath);
    }

    private void loadProblemList(String challengePath) throws JAXBException, IOException {
        var context = JAXBContext.newInstance(Problem.class);
        var um = context.createUnmarshaller();

        try (Stream<Path> stream = Files.walk(Paths.get(challengePath), 1)) {
            var files = stream.filter(file -> !Files.isDirectory(file))
                    .map(Path::toFile)
                    .collect(Collectors.toList());
            for (File file : files) {
                problemList.add((Problem) um.unmarshal(file));
            }
        }
    }

    @Override
    public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;

        final var raw = event.getMessage().getContentRaw();
        final var channel = event.getChannel();
        if (CREATE_CHALLENGE.equals(raw)) {
            onCreateChallenge(channel);
        } else if (SHOW_CHALLENGE.equals(raw)) {
            onShowChallenge(channel);
        }
    }

    private void onCreateChallenge(TextChannel channel) {
        var randProblem = problemList.get(random.nextInt(problemList.size()));
        var randChallenge = new Challenge(randProblem, channel, this);
        challenges.put(channel, randChallenge);
        channel.sendMessage("started Challenge:\n%s"
                .formatted(randProblem.getName())).queue();
    }

    private void onShowChallenge(TextChannel channel) {
        if (challenges.containsKey(channel)) {
            var challenge = challenges.get(channel);
            switch (challenge.getState()) {
                case ACTIVE -> channel.sendMessage("Challenge ACTIVE:%n%s"
                        .formatted(challenge)).queue();
                case DONE -> channel.sendMessage("Challenge DONE").queue();
            }
        } else {
            channel.sendMessage("No challenge in this channel available").queue();
        }
    }

    public void onChallengeTimeUp(Challenge challenge) {
        challenge.getChannel().sendMessage("Time is up for challenge:%n%s"
                .formatted(challenge)).queue();
    }
}
