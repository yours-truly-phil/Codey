package io.horrorshow.codey.challenge;

import java.time.format.DateTimeFormatter;

public class DiscordFormat {
    public static String noActiveChallenge() {
        return "No active challenge";
    }

    public static String testResults(Challenge challenge, ChallengeEntry entry) {
        return "%d/%d test cases passed"
                .formatted(entry.getNoTestsPass(), challenge.getTestsTotal());
    }

    public static String presentNewChallenge(Challenge challenge) {
        return "*New Challenge! Good luck*\n\n%s"
                .formatted(challenge);
    }

    public static String noChallengeInChannelMsg() {
        return "No challenge in this channel available";
    }

    public static String challengeDone(Challenge challenge) {
        return "Challenge DONE";
    }

    public static String showCurChallenge(Challenge challenge) {
        return "Challenge ACTIVE:%n%s"
                .formatted(challenge);
    }

    public static String challengeFinishedMsg(Challenge challenge) {
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
        return sb.toString();
    }
}
