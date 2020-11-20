package io.horrorshow.discordcodeformatter;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DiscordMessageTest {
    @Test
    void parse_no_code_blocks() {
        var msg = """
                Hello, what's going on?
                                
                Cheers :)
                                
                try `lol lol`""";
        assertThat(new DiscordMessage(msg).getParts())
                .containsExactly(new DiscordMessage.MessagePart(false, null, msg));
    }

    @Test
    void parse_one_code_block() {
        var msg = """
                Hello, what's going on,
                now comes a code block:
                ```java
                I'm inside the code block
                hooray!```
                I come after the code block""";
        assertThat(new DiscordMessage(msg).getParts())
                .containsExactly(new DiscordMessage.MessagePart(false, null, """
                                Hello, what's going on,
                                now comes a code block:
                                """),
                        new DiscordMessage.MessagePart(true, "java", """
                                                                
                                I'm inside the code block
                                hooray!"""),
                        new DiscordMessage.MessagePart(false, null, """
                                                                
                                I come after the code block"""));
    }

    @Test
    void extract_content_from_code_block() {
        var raw = """
                ```java
                Hello, I'm a code block```""";
        assertThat(new DiscordMessage(raw).getParts())
                .containsExactly(new DiscordMessage.MessagePart(true, "java",
                        """
                                                                
                                Hello, I'm a code block"""));
    }
}