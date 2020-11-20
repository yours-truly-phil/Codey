package io.horrorshow.discordcodeformatter;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.printer.PrettyPrinterConfiguration;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class JavaFormatterTest {

    static final String UNCRUSTIFY_EXE = "uncrustify-0.72.0_f-win32\\uncrustify.exe";
    static final String UNCRUSTIFY_CFG = "uncrustify-0.72.0_f-win32\\cfg\\sun.cfg";

    static final PrettyPrinterConfiguration ppConf = new PrettyPrinterConfiguration();

    static {
        ppConf.setIndentSize(2);
        ppConf.setIndentType(PrettyPrinterConfiguration.IndentType.SPACES);
    }

    @Test
    void java_parser_hello_world() {
        var compUnit = StaticJavaParser.parse("""
                interface A{static void main(String[] a){System.out.println("Hello, World!");}}""");
        assertThat(compUnit.toString(ppConf))
                .isEqualTo("""
                        interface A {
                         
                           static void main(String[] a) {
                             System.out.println("Hello, World!");
                           }
                         }
                         """);
    }

    @Test
    void java_parser_single_method() {
        var compUnit = StaticJavaParser.parse("""
                public static void main(String[] args){System.out.println("Hello, World!");}""");
        assertThat(compUnit.toString(ppConf))
                .isEqualTo("""
                        interface A {
                         
                           static void main(String[] a) {
                             System.out.println("Hello, World!");
                           }
                         }
                         """);
    }

    @Test
    void uncrustify_hello_world() throws Exception {
        assertThat(uncrustify(createUncrustifyInputString("""
                interface A{static void main(String[] a){System.out.println("Hello, World!");}}""")))
                .isEqualTo("""
                        interface A {
                          static void main(String[] a) {
                            System.out.println("Hello, World!");
                          }
                        }""");
    }

    @Test
    void uncrustify_loop() throws Exception {
        assertThat(uncrustify(createUncrustifyInputString("""
                for(int i = 0; i < 10; i++) { System.out.println(i); }""")))
                .isEqualTo("""
                        for (int i = 0; i < 10; i++)
                          System.out.println(i);""");
    }

    @Test
    void uncrustify_single_method() throws Exception {
        assertThat(uncrustify(createUncrustifyInputString(
                """
                        public static void main(String[] args){System.out.println("Hello, World!");}""")))
                .isEqualTo("""
                        public static void main(String[] args) {
                          System.out.println("Hello, World!");
                        }""");

    }

    @Test
    void surroundUncrustifyString() {
        assertThat(createUncrustifyInputString("a\"b\"c"))
                .isEqualTo("'a\\\"b\\\"c'");
    }

    private String createUncrustifyInputString(String s) {
        return "'" +
                s.replaceAll("\"", "\\\\\"") +
                "'";
    }

    private String uncrustify(String testString) throws IOException, InterruptedException {
        StringBuilder sb = new StringBuilder();
        ProcessBuilder processBuilder = new ProcessBuilder("powershell.exe", "/c",
                String.format("%s | %s -c %s -l JAVA", testString, UNCRUSTIFY_EXE, UNCRUSTIFY_CFG));

        Process process = processBuilder.start();
        Executors.newSingleThreadExecutor()
                .submit(() -> sb.append(new BufferedReader(new InputStreamReader(process.getInputStream()))
                        .lines()
                        .collect(Collectors.joining("\n"))));

        int exitCode = process.waitFor();
        assertThat(exitCode).isEqualTo(0);
        return sb.toString().replace('\uFEFF', ' ').trim();
    }
}