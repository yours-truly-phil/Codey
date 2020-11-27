package io.horrorshow.discordcodeformatter.formatter;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@AllArgsConstructor
@Getter
@EqualsAndHashCode
public class ParseResult {
    private final String text;
    private final String lang;
}
