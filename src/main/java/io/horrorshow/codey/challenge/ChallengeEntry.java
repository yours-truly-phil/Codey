package io.horrorshow.codey.challenge;

import io.horrorshow.codey.api.WandboxResponse;
import io.horrorshow.codey.challenge.xml.TestCase;
import io.horrorshow.codey.discordutil.MessagePart;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Message;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Getter
public class ChallengeEntry implements Comparable<ChallengeEntry> {
    private final Challenge challenge;
    private final Message message;
    private final MessagePart codeBlock;
    private final Map<TestCase, Optional<WandboxResponse>> testResults = new HashMap<>();

    private ChallengeEntry(Challenge challenge, Message message, MessagePart codeBlock) {
        this.challenge = challenge;
        this.message = message;
        this.codeBlock = codeBlock;
    }

    public static ChallengeEntry create(TestRunner testRunner, Challenge challenge,
                                        Message message, MessagePart codeBlock) {
        var entry = new ChallengeEntry(challenge, message, codeBlock);
        entry.testResults.putAll(testRunner.runTests(challenge.getProblem(), codeBlock.getText(), codeBlock.getLang()));
        return entry;
    }

    public int getNoTestsPass() {
        int count = 0;
        for (var entry : testResults.entrySet()) {
            if (entry.getValue().isPresent()) {
                var wandboxResult = entry.getValue().get();
                if (wandboxResult.getProgram_output() != null
                        && wandboxResult.getProgram_output().trim().equals(entry.getKey().getOutput())) {
                    count++;
                }
            }
        }
        return count;
    }

    @Override
    public int compareTo(@NotNull ChallengeEntry o) {
        return o.getNoTestsPass() - getNoTestsPass();
    }
}
