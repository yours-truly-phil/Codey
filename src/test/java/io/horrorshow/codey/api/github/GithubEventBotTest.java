package io.horrorshow.codey.api.github;

import org.junit.jupiter.api.Test;

import java.util.List;

import static io.horrorshow.codey.discordutil.DiscordUtils.truncateList;
import static org.assertj.core.api.Assertions.assertThat;


class GithubEventBotTest {

    @Test
    void truncateStringsTest() {
        var in = List.of("foo", "bar", "what's", "up?");
        var res = in.stream().filter(truncateList(6)).toList();
        assertThat(res).containsExactly("foo", "bar");
        var res2 = in.stream().filter(truncateList(7)).toList();
        assertThat(res2).containsExactly("foo", "bar");
        var res3 = in.stream().filter(truncateList(12)).toList();
        assertThat(res3).containsExactly("foo", "bar", "what's");
    }
}
