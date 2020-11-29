package io.horrorshow.codey.formatter;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@AllArgsConstructor
@Getter
@EqualsAndHashCode
public class CodeSnippet {
    private final String text;
    private final String lang;
}
