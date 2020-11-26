package io.horrorshow.discordcodeformatter;

import lombok.AllArgsConstructor;
import lombok.Data;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CodingCompetition extends ListenerAdapter {

    private static final String SHOW_CUR_PROBLEM_CMD = "$challenge";
    private static CodingChallenge sampleChallenge = new CodingChallenge();

    static {
        sampleChallenge.description = """
                Current Coding challenge:
                                
                Write a program that reads a number from system.in \
                and writes its square to System.out.
                                
                Sample:
                `Input: 4`
                `Output: 8`
                                
                ```java
                import java.util.Scanner;
                                
                public class F {
                    public static void square(int num) {
                        // write your code here
                    }
                    
                    public static void main(String[] args) {
                        Scanner scanner = new Scanner(System.in);
                        square(Integer.parseInt(scanner.nextLine()));
                        scanner.close();
                    }
                }```""";

        sampleChallenge.testList.addAll(List.of(
                new ChallengeTest("4", "8"),
                new ChallengeTest(String.valueOf(Integer.MAX_VALUE),
                        String.valueOf((long) Integer.MAX_VALUE * (long) Integer.MAX_VALUE))));
    }

    private final WandboxApi wandboxApi;

    public CodingCompetition(@Autowired WandboxApi wandboxApi) {
        this.wandboxApi = wandboxApi;
    }

    @Override
    public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;

        if (SHOW_CUR_PROBLEM_CMD.equals(event.getMessage().getContentRaw())) {
            event.getChannel().sendMessage(sampleChallenge.getDescription())
                    .queue();
        }
    }

    @Data
    static class CodingChallenge {
        private final List<ChallengeTest> testList = new ArrayList<>();
        private String description;
    }

    @Data
    @AllArgsConstructor
    private static class ChallengeTest {
        private String input;
        private String expected;
    }
}
