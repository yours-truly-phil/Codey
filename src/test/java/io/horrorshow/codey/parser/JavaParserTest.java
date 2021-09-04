package io.horrorshow.codey.parser;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.stmt.BlockStmt;
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


    @Test
    void wrap_expression_in_class_with_main() {

        var source = "System.out.println(\"Lonely println statement\")";

        var expression = StaticJavaParser.parseExpression(source);
        var body = new BlockStmt().addStatement(expression);
        var stringArgs = new Parameter().setType("String[]").setName("args");
        var myClass = new CompilationUnit().addClass("CodeyClass").setPublic(true);
        myClass.addMethod("main", Modifier.Keyword.STATIC)
                .setStatic(true).setPublic(true)
                .setParameters(NodeList.nodeList(stringArgs))
                .setBody(body);

        assertThat(myClass.toString()).isEqualTo("""
                public class CodeyClass {
                                
                    static public void main(String[] args) {
                        System.out.println("Lonely println statement");
                    }
                }""");
    }
}
