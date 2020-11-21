package io.horrorshow.discordcodeformatter;

import com.github.javaparser.printer.PrettyPrinterConfiguration;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JavaFormatterTest {

    static final PrettyPrinterConfiguration ppConf = new PrettyPrinterConfiguration();

    static {
        ppConf.setIndentSize(2);
        ppConf.setIndentType(PrettyPrinterConfiguration.IndentType.SPACES);
    }

    JavaFormatter javaFormatter = new JavaFormatter();

    @Test
    void java_parser_hello_world() {
        var res = javaFormatter.format("""
                interface A{static void main(String[] a){System.out.println("Hello, World!");}}""");
        assertThat(res.isChanged()).isTrue();
        assertThat(res.getText()).isEqualTo("""
                interface A {
                  static void main(String[] a) {
                    System.out.println("Hello, World!");
                  }
                }
                """);
    }

    @Test
    void java_parser_single_method() {
        var res = javaFormatter.format("""
                public static void main(String[] args){System.out.println("Hello, World!");}""");
        assertThat(res.isChanged()).isTrue();
        assertThat(res.getText()).isEqualTo("""
                public static void main(String[] args) {
                  System.out.println("Hello, World!");
                }""");
    }

    @Test
    void doesnt_change_non_code() {
        var text = """
                This isn't code ;)""";
        var res = javaFormatter.format(text);
        assertThat(res.isChanged()).isFalse();
        assertThat(res.getText()).isEqualTo(text);
    }
}