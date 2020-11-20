package io.horrorshow.discordcodeformatter;

import com.google.googlejavaformat.java.Formatter;
import com.google.googlejavaformat.java.FormatterException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
public class JavaFormatter {
    private final Formatter googleFormatter = new Formatter();

    public Optional<String> googleFormat(String source) {
        try {
            return Optional.ofNullable(googleFormatter.formatSource(source));
        } catch (FormatterException e) {
            return Optional.empty();
        }
    }
}
