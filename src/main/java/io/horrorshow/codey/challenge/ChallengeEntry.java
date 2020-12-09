package io.horrorshow.codey.challenge;

import io.horrorshow.codey.api.WandboxApi;
import io.horrorshow.codey.api.WandboxResponse;
import io.horrorshow.codey.challenge.xml.TestCase;
import io.horrorshow.codey.discordutil.MessagePart;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Message;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.client.RestClientException;

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

    public static ChallengeEntry create(WandboxApi wandboxApi, Challenge challenge,
                                        Message message, MessagePart codeBlock) {
        var entry = new ChallengeEntry(challenge, message, codeBlock);
        entry.compileAndRunTests(wandboxApi);
        return entry;
    }

    public boolean isAllTestsPass() {
        var testCases = challenge.getProblem().getTestcases().getTestcase();
        return testCases.size() == getNoTestsPass();
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

    private void compileAndRunTests(WandboxApi wandboxApi) {
        var caseList = challenge.getProblem().getTestcases().getTestcase();
        for (var test : caseList) {
            try {

                var wandboxResult = wandboxApi.compile(codeBlock.getText(),
                        codeBlock.getLang(), test.getInput(), "");
                testResults.put(test, Optional.of(wandboxResult));

            } catch (RestClientException e) {
                log.error("exception compiling for problem {} in lang '{}' stdin '{}' code: {}",
                        challenge.getProblem().getName(), codeBlock.getLang(), test.getInput(), codeBlock, e);

                testResults.put(test, Optional.empty());
            }
        }
    }

    @Override
    public int compareTo(@NotNull ChallengeEntry o) {
        return o.getNoTestsPass() - getNoTestsPass();
    }
}
