package io.horrorshow.discordcodeformatter;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.printer.PrettyPrinterConfiguration;
import com.google.googlejavaformat.java.Formatter;
import com.google.googlejavaformat.java.FormatterException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
public class JavaFormatter {

    static final PrettyPrinterConfiguration ppConf = new PrettyPrinterConfiguration();

    static {
        ppConf.setIndentSize(2);
        ppConf.setIndentType(PrettyPrinterConfiguration.IndentType.SPACES);
    }

    private final Formatter googleFormatter = new Formatter();

    public Optional<String> googleFormat(String source) {
        try {
            return Optional.ofNullable(googleFormatter.formatSource(source));
        } catch (FormatterException e) {
            return Optional.empty();
        }
    }

    public Optional<String> javaParserFormat(String source) {
        try {
            var code = StaticJavaParser.parse(source);
            return Optional.of(code.toString(ppConf).replaceAll("\\r\\n", "\n"));
        } catch (Exception e) {
            log.error("javaparser error", e);
        }
        try {
            var code = StaticJavaParser.parseBlock(source);
            return Optional.of(code.toString(ppConf).replaceAll("\\r\\n", "\n"));
        } catch (Exception e) {
            log.error("block parsing error", e);
        }
        try {
            var code = StaticJavaParser.parseMethodDeclaration(source);
            return Optional.of(code.toString(ppConf).replaceAll("\\r\\n", "\n"));
        } catch (Exception e) {
            log.error("method declaration error", e);
        }
        return Optional.empty();
    }
}
