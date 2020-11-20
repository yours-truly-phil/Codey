package io.horrorshow.discordcodeformatter;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class DiscordMessage {
    private static final Pattern matchCodeblocks = Pattern.compile("```\\w*\\n[\\s\\S]*?(```)");

    @Getter
    private final List<MessagePart> parts = new ArrayList<>();

    public DiscordMessage(String raw) {
        var m = matchCodeblocks.matcher(raw);
        int lastEnd = 0;
        while (m.find()) {
            var start = m.start();
            var end = m.end();
            if (start > lastEnd) {
                parts.add(new MessagePart(false, null, raw.substring(lastEnd, start)));
            }
            var codeContent = raw.substring(start, end);
            var lang = codeContent.substring("```".length(), codeContent.indexOf("\n"));
            codeContent = codeContent.substring(codeContent.indexOf("\n"), codeContent.length() - 3);
            parts.add(new MessagePart(true, lang, codeContent));
            lastEnd = end;
        }
        if (lastEnd < raw.length()) {
            parts.add(new MessagePart(false, null, raw.substring(lastEnd)));
        }
    }

    @ToString
    @EqualsAndHashCode
    @AllArgsConstructor
    @Getter
    public static class MessagePart {
        private final boolean isCode;
        private final String lang;
        private final String text;
    }
}
