package io.horrorshow.discordcodeformatter;

import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.Node;
import com.github.javaparser.printer.PrettyPrinterConfiguration;
import com.google.googlejavaformat.java.Formatter;
import com.google.googlejavaformat.java.FormatterException;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@Service
@Slf4j
public class JavaFormatter {

    private final PrettyPrinterConfiguration ppConf = new PrettyPrinterConfiguration();

    private final List<Function<String, Optional<String>>> parsers = new ArrayList<>();

    private final Formatter googleFormatter = new Formatter();

    public JavaFormatter() {
        ppConf.setIndentSize(2);
        ppConf.setIndentType(PrettyPrinterConfiguration.IndentType.SPACES);

        ParserConfiguration jpConf = new ParserConfiguration();
        jpConf.setCharacterEncoding(StandardCharsets.UTF_8);
        jpConf.setTabSize(2);
        StaticJavaParser.setConfiguration(jpConf);

        parsers.add(this::googleFormat);
        parsers.add(s -> javaParse(s, StaticJavaParser::parse, ppConf));
        parsers.add(s -> javaParse(s, StaticJavaParser::parseBlock, ppConf));
        parsers.add(s -> javaParse(s, StaticJavaParser::parseMethodDeclaration, ppConf));
        parsers.add(s -> javaParse(s, StaticJavaParser::parseStatement, ppConf));
        parsers.add(s -> javaParse(s, StaticJavaParser::parseExpression, ppConf));
        parsers.add(s -> javaParse(s, StaticJavaParser::parseVariableDeclarationExpr, ppConf));
    }

    public ParseResult format(String source) {
        for (var parser : parsers) {
            var res = parser.apply(source);
            if (res.isPresent()) {
                return new ParseResult(res.get(), true);
            }
        }
        return new ParseResult(source, false);
    }

    private Optional<String> javaParse(String source,
                                       Function<String, Node> javaParserFun,
                                       PrettyPrinterConfiguration ppCfg) {
        try {
            var prettyResult = javaParserFun.apply(source).toString(ppCfg)
                    .replaceAll("\\r\\n", "\n")
                    .replaceAll("\\r", "\n");
            return Optional.of(prettyResult);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public Optional<String> googleFormat(String source) {
        try {
            return Optional.ofNullable(googleFormatter.formatSource(source));
        } catch (FormatterException e) {
            return Optional.empty();
        }
    }

    @AllArgsConstructor
    @Getter
    @EqualsAndHashCode
    static class ParseResult {
        private final String text;
        private final boolean isChanged;
    }
}
