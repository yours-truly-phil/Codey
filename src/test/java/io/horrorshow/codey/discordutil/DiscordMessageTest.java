package io.horrorshow.codey.discordutil;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("SpellCheckingInspection")
class DiscordMessageTest {
    @Test
    void parse_no_code_blocks() {
        var msg = """
                Hello, what's going on?
                                
                Cheers :)
                                
                try `lol lol`""";
        assertThat(DiscordMessage.of(msg).getParts())
                .containsExactly(new MessagePart(false, null, msg));
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
        assertThat(DiscordMessage.of(msg).getParts())
                .containsExactly(new MessagePart(false, null, """
                                Hello, what's going on,
                                now comes a code block:
                                """),
                        new MessagePart(true, "java", """
                                                                
                                I'm inside the code block
                                hooray!"""),
                        new MessagePart(false, null, """
                                                                
                                I come after the code block"""));
    }

    @Test
    void extract_content_from_code_block() {
        var raw = """
                ```java
                Hello, I'm a code block```""";
        assertThat(DiscordMessage.of(raw).getParts())
                .containsExactly(new MessagePart(true, "java",
                        """
                                                                
                                Hello, I'm a code block"""));
    }

    @Test
    void multiline_code_starting_at_first_line() {
        var raw = """
                ```boolean contained = Arrays.binarySearch(sortedStringArray, "str", (o1, o2) -> {
                    if (o1.startsWith(o2))
                        return 0;
                    if (o2.startsWith(o1))
                        return 0;
                    return o1.compareTo(o2);
                }) >= 0;```""";
        assertThat(DiscordMessage.of(raw).getParts())
                .containsExactly(new MessagePart(true, null,
                        """
                                boolean contained = Arrays.binarySearch(sortedStringArray, "str", (o1, o2) -> {
                                    if (o1.startsWith(o2))
                                        return 0;
                                    if (o2.startsWith(o1))
                                        return 0;
                                    return o1.compareTo(o2);
                                }) >= 0;"""));
    }

    @Test
    void code_starting_in_first_line_with_language_set() {
        var raw = """
                ```cpp int i = 0;```""";
        assertThat(DiscordMessage.of(raw).getParts())
                .containsExactly(new MessagePart(true, "cpp",
                        " int i = 0;"));
    }

    @Test
    void extract_code_block_content_without_new_line_no_given_language() {
        assertThat(DiscordMessage.of("```test```").getParts())
                .containsExactly(new MessagePart(true, null, "test"));
    }

    @Test
    void recognize_code_block_without_language_content_in_first_line() {
        var raw = """
                ```@ToString
                @EqualsAndHashCode
                @AllArgsConstructor
                @Getter
                public static class MessagePart {
                    private final boolean isCode;
                    private final String lang;
                    private final String text;
                }```""";
        assertThat(DiscordMessage.of(raw).getParts())
                .containsExactly(new MessagePart(true, null,
                        """
                                @ToString
                                @EqualsAndHashCode
                                @AllArgsConstructor
                                @Getter
                                public static class MessagePart {
                                    private final boolean isCode;
                                    private final String lang;
                                    private final String text;
                                }"""));
    }

    @Test
    void starts_with_any_find_matches() {
        var testString = "xyz";
        var arr = new String[]{"a", "b", "c", "d", "e", "x", "y", "z"};
        assertThat(DiscordMessage.startsWithAnyOf(testString, arr))
                .isEqualTo(5);
    }

    @Test
    void starts_with_any_no_match() {
        var testString = "asdölfkjlakjdf";
        var arr = new String[]{"s", "v", "ö"};
        assertThat(DiscordMessage.startsWithAnyOf(testString, arr))
                .isEqualTo(-1);
    }

    @Test
    void starts_with_teststring_shorter_than_matches() {
        var testString = "d";
        var arr = new String[]{"a", "du", "ef"};
        assertThat(DiscordMessage.startsWithAnyOf(testString, arr))
                .isEqualTo(-1);
    }

    @Test
    void starts_with_finds_longest_match_when_multiple_matches() {
        var testString = "python this is python";
        var arr = new String[]{"a", "b", "c", "py", "python"};
        assertThat(DiscordMessage.startsWithAnyOf(testString, arr))
                .isEqualTo(4);
    }
}