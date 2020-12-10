package io.horrorshow.codey.challenge;

import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

@Slf4j
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
        return "Challenge %s DONE".formatted(challenge.getProblem().getName());
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

    public static String noChallengesFound() {
        return "no challenges found";
    }

    public static BufferedImage drawSomeAvatarsToSeeHowThatLooksLike(Challenge challenge) {
        var entries = challenge.getEntries();
        var size = entries.size();
        var res = new BufferedImage(400, 100 + size * 100, BufferedImage.TYPE_INT_RGB);
        var g = res.createGraphics();
        try {
            var congratz = ImageIO.read(new File("images/congratz.png"));
            g.drawImage(congratz, 0, 0, 400, 100, null);
        } catch (IOException e) {
            log.error("error loading graphic from file", e);
        }
        for (int i = 0; i < size; i++) {
            var entry = entries.get(i);
            var user = entry.getMessage().getAuthor();
            if (i == 0) {
                // first place
                try {
                    var trophy = ImageIO.read(new File("images/trophy.png"));
                    g.drawImage(trophy, 100, 100, 100, 100, null);
                } catch (IOException e) {
                    log.error("error reading trophy image", e);
                }
            }
            try {
                // avatar
                var url = new URL(Objects.requireNonNull(user.getAvatarUrl()));
                var img = ImageIO.read(url);
                g.drawImage(img, 0, 100 + i * 100, 100, 100, Color.WHITE, null);
                // rank + user name
                int rank = i + 1;
                var text = rank + ". " + user.getName();
                g.setColor(Color.WHITE);
                var font = g.getFont().deriveFont(20.f);
                drawCenteredString(g,
                        text,
                        new Rectangle(200, 100 + i * 100, 200, 100),
                        font);

            } catch (IOException e) {
                log.error("Error drawing winner graphic", e);
            }
        }
        drawDecorator(g, res.getHeight());
        g.dispose();
        return res;
    }

    private static void drawDecorator(Graphics2D g, int height) {
        g.setColor(new Color(247, 44, 123));
        g.fillRoundRect(0, 0, 10 - 5, height, 5, 5);
        g.fillRect(5, 0, 10 - 5, height);
    }

    private static void drawCenteredString(Graphics2D g, String text, Rectangle rect, Font font) {
        var metrics = g.getFontMetrics(font);
        var x = rect.x + (rect.width - metrics.stringWidth(text)) / 2;
        var y = rect.y + ((rect.height - metrics.getHeight()) / 2) + metrics.getAscent();
        g.setFont(font);
        g.drawString(text, x, y);
    }
}
