package io.horrorshow.codey.parser;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.printer.DefaultPrettyPrinter;
import com.github.javaparser.printer.configuration.DefaultConfigurationOption;
import com.github.javaparser.printer.configuration.DefaultPrinterConfiguration;
import com.github.javaparser.printer.configuration.Indentation;
import org.junit.jupiter.api.Test;

import static io.horrorshow.codey.parser.SourceProcessing.processSource;
import static org.assertj.core.api.Assertions.assertThat;


public class JavaSourceProcessorTest {

    @Test
    void pretty_printer_code_sample() {
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


    @Test
    void process_statement() {
        var statementSrc = "System.out.println(\"Lonely println statement\");";
        var source = processSource(statementSrc, "java");
        assertThat(source).isEqualTo(new ProcessResult("""
                import java.util.*;
                                
                public class CodeyClass {
                                
                    static public void main(String[] args) {
                        {
                            System.out.println("Lonely println statement");
                        }
                    }
                }
                """, true, null));
    }


    @Test
    void process_source_with_null_uses_default_processor() {
        var statementSrc = "System.out.println(\"foo\");";
        var source = processSource(statementSrc, null);
        assertThat(source).isEqualTo(new ProcessResult("System.out.println(\"foo\");", true, null));
    }


    @Test
    void process_expression() {
        var expressionSrc = "for (int i = 0; i < 100; i++) { System.out.println(\"test\"); }";
        var source = processSource(expressionSrc, "java");
        assertThat(source).isEqualTo(new ProcessResult("""
                import java.util.*;
                                
                public class CodeyClass {
                                
                    static public void main(String[] args) {
                        {
                            for (int i = 0; i < 100; i++) {
                                System.out.println("test");
                            }
                        }
                    }
                }
                """, true, null));
    }


    @Test
    void process_proper_class() {
        var source = """
                import java.util.List;
                public class A {
                    public static void main(String[] args) {
                        List<String> list;
                    }
                }
                """;
        assertThat(processSource(source, "java"))
                .isEqualTo(new ProcessResult(source, true, null));
    }


    @Test
    void not_java_source_as_java_returns_error() {
        var unknown = "int main() { println(); }";
        var source = processSource(unknown, "java");
        assertThat(source.isOk()).isEqualTo(false);
    }
}
