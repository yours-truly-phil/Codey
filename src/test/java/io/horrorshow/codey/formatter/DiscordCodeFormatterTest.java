package io.horrorshow.codey.formatter;

import io.horrorshow.codey.discordutil.DiscordMessage;
import io.horrorshow.codey.discordutil.DiscordUtils;
import io.horrorshow.codey.discordutil.MessageStore;
import net.dv8tion.jda.api.JDA;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.assertThat;

class DiscordCodeFormatterTest {

    @Mock
    JDA jda;
    JavaFormatter javaFormatter;
    MessageStore messageStore;
    DiscordUtils utils;
    DiscordCodeFormatter formatter;

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
        javaFormatter = new JavaFormatter();
        messageStore = new MessageStore();
        utils = new DiscordUtils(jda, messageStore);
        formatter = new DiscordCodeFormatter(jda, javaFormatter, messageStore, utils);
    }

    @Test
    void format_java_code() {
        var dm = DiscordMessage.of("""
                Hello
                ```java
                public class A 
                { 
                public static void main(String[] args) 
                { 
                System.out.println("Hello, World!");
                }
                }```
                what's up?
                ```another code block```
                ```scala scala```
                bla""");
        var result = formatter.formatted(dm);
        assertThat(result).get().isEqualTo("""
                Hello
                ```java
                public class A {
                  public static void main(String[] args) {
                    System.out.println("Hello, World!");
                  }
                }
                ```
                what's up?
                ```null
                another code block```
                ```java
                scala```
                bla""");
    }
}