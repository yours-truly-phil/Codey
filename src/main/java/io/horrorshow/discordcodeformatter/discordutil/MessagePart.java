package io.horrorshow.discordcodeformatter.discordutil;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@ToString
@EqualsAndHashCode
@AllArgsConstructor
@Getter
public class MessagePart {
    private final boolean isCode;
    private final String lang;
    private final String text;
}