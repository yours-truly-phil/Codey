package io.horrorshow.codey.challenge;

import io.horrorshow.codey.api.WandboxApi;
import io.horrorshow.codey.challenge.xml.Problem;
import io.horrorshow.codey.discordutil.DiscordMessage;
import io.horrorshow.codey.discordutil.DiscordUtils;
import io.horrorshow.codey.discordutil.MessagePart;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@Slf4j
public class CodingCompetition extends ListenerAdapter {

    private static final String CREATE_CHALLENGE = "$create";
    private static final String SHOW_CHALLENGE = "$show";

    private static final String VERIFY = "\uD83C\uDF00";

    private final WandboxApi wandboxApi;

    private final List<Problem> problemList;

    private final Map<TextChannel, Challenge> challenges = new HashMap<>();

    private final Random random = new Random();
    private final DiscordUtils utils;

    public CodingCompetition(@Autowired JDA jda,
                             @Autowired WandboxApi wandboxApi,
                             @Autowired @Value("${challenge.path}") String challengePath,
                             @Autowired DiscordUtils utils,
                             @Autowired ChallengeRepository challengeRepository) {
        this.wandboxApi = wandboxApi;
        this.utils = utils;

        jda.addEventListener(this);

        problemList = challengeRepository.findAllProblems(challengePath);
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

        getActiveChallenge(channel).flatMap(challenge -> DiscordMessage.of(raw)
                .getParts().stream()
                .filter(MessagePart::isCode)
                .findAny())
                .ifPresent(p -> event.getMessage().addReaction(VERIFY).queue());
    }

    private Optional<Challenge> getActiveChallenge(TextChannel channel) {
        return Optional.ofNullable(challenges.get(channel))
                .filter(challenge -> challenge.getState() == State.ACTIVE);
    }

    @Override
    public void onGuildMessageReactionAdd(@NotNull GuildMessageReactionAddEvent event) {
        if (event.getUser().isBot()) return;

        var channel = event.getChannel();
        if (VERIFY.equals(event.getReactionEmote().getEmoji())) {
            getActiveChallenge(channel).ifPresentOrElse(challenge ->
                            challengePresent(event, channel, challenge)
                    , () -> utils.sendRemovableMessage("No active challenge", channel));
        }
    }

    private void challengePresent(@NotNull GuildMessageReactionAddEvent event,
                                  TextChannel channel,
                                  Challenge challenge) {
        channel.retrieveMessageById(event.getMessageId())
                .queue(message -> DiscordMessage.of(message.getContentRaw())
                        .getParts().stream()
                        .filter(MessagePart::isCode)
                        .forEach(part -> {
                                    var entry = ChallengeEntry.create(wandboxApi, challenge, message, part);
                                    challenge.getEntries().add(entry);
                                    utils.sendRemovableMessage("%d/%d test cases passed"
                                            .formatted(entry.getNoTestsPass(), challenge.getTestsTotal()), channel);
                                }
                        ));
    }

    private void onCreateChallenge(TextChannel channel) {
        if (problemList.isEmpty()) {
            utils.sendRemovableMessage("No challenges found", channel);
        } else {
            var randProblem = problemList.get(random.nextInt(problemList.size()));
            var challenge = new Challenge(randProblem, channel, this);
            challenges.put(channel, challenge);
            utils.sendRemovableMessage("*New Challenge! Good luck*\n\n%s"
                    .formatted(challenge), channel);
        }
    }

    private void onShowChallenge(TextChannel channel) {
        if (challenges.containsKey(channel)) {
            var challenge = challenges.get(channel);
            switch (challenge.getState()) {
                case ACTIVE -> utils.sendRemovableMessage("Challenge ACTIVE:%n%s"
                        .formatted(challenge), channel);
                case DONE -> utils.sendRemovableMessage("Challenge DONE", channel);
            }
        } else {
            utils.sendRemovableMessage("No challenge in this channel available", channel);
        }
    }

    public void onChallengeTimeUp(Challenge challenge) {
        var sb = new StringBuilder();
        sb.append("Time is up for challenge:\n")
                .append(challenge);
        for (var entry : challenge.getEntries()) {
            sb.append("Entry by ")
                    .append(entry.getMessage().getAuthor().getName())
                    .append(" %d/%d ".formatted(entry.getNoTestsPass(),
                            challenge.getProblem().getTestcases().getTestcase().size()))
                    .append("tests passed at ")
                    .append(entry.getMessage().getTimeCreated().
                            format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                    .append("\n");
        }
        utils.sendRemovableMessage(sb.toString(), challenge.getChannel());
    }
}
