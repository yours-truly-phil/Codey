package io.horrorshow.codey.parser;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.printer.DefaultPrettyPrinter;
import com.github.javaparser.printer.configuration.DefaultConfigurationOption;
import com.github.javaparser.printer.configuration.DefaultPrinterConfiguration;
import com.github.javaparser.printer.configuration.Indentation;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


public class JavaParserTest {

    @Test
    void testJavaParser() {
        var config = new DefaultPrettyPrinter().getConfiguration();
        var indent = new DefaultConfigurationOption(DefaultPrinterConfiguration.ConfigOption.INDENTATION);
        var indentation = new Indentation(Indentation.IndentType.SPACES, 2);
        indent.value(indentation);
        config.addOption(indent);
        var source = "public class A{public static void main(String[] args){System.out.println(\"Hello, World\");}}";

        var compilationUnit = StaticJavaParser.parse(source);
        assertThat(compilationUnit.toString(config)).isEqualTo("""
                public class A {
                                        
                  public static void main(String[] args) {
                    System.out.println("Hello, World");
                  }
                }
                """);
    }

}
