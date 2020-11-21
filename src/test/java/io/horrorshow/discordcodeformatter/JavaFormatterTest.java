package io.horrorshow.discordcodeformatter;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JavaFormatterTest {

    JavaFormatter javaFormatter = new JavaFormatter();

    @Test
    void java_parser_hello_world() {
        var res = javaFormatter.format("""
                interface A{static void main(String[] a){System.out.println("Hello, World!");}}""");
        assertThat(res).isPresent();
        assertThat(res.get().getText()).isEqualTo("""
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
        assertThat(res).isPresent();
        assertThat(res.get().getText()).isEqualTo("""
                public static void main(String[] args) {
                  System.out.println("Hello, World!");
                }""");
    }

    @Test
    void doesnt_change_non_code() {
        var text = """
                This isn't code ;)""";
        var res = javaFormatter.format(text);
        assertThat(res).isEmpty();
    }
}