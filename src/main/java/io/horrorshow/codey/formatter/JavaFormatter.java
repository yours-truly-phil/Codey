package io.horrorshow.codey.formatter;

import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.Node;
import com.github.javaparser.printer.DefaultPrettyPrinter;
import com.github.javaparser.printer.configuration.DefaultConfigurationOption;
import com.github.javaparser.printer.configuration.DefaultPrinterConfiguration;
import com.github.javaparser.printer.configuration.Indentation;
import com.github.javaparser.printer.configuration.PrinterConfiguration;
import com.google.googlejavaformat.java.Formatter;
import com.google.googlejavaformat.java.FormatterException;
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

    private final PrinterConfiguration ppConf = new DefaultPrettyPrinter().getConfiguration();

    private final List<Function<String, Optional<String>>> parsers = new ArrayList<>();

    private final Formatter googleFormatter = new Formatter();

    record CodeSnippet(String text, String lang) {

    }


    public JavaFormatter() {
        var configOption = new DefaultConfigurationOption(DefaultPrinterConfiguration.ConfigOption.INDENTATION);
        ppConf.addOption(configOption.value(new Indentation(Indentation.IndentType.SPACES, 2)));

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


    public Optional<CodeSnippet> format(String source) {
        for (var parser : parsers) {
            var res = parser.apply(source);
            if (res.isPresent()) {
                return Optional.of(new CodeSnippet(res.get(), "java"));
            }
        }
        return Optional.empty();
    }


    private Optional<String> javaParse(String source,
            Function<String, Node> javaParserFun,
            PrinterConfiguration ppCfg) {
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
}
