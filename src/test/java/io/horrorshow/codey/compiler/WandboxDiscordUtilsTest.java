package io.horrorshow.codey.compiler;

import io.horrorshow.codey.api.wandbox.WandboxResponse;
import org.junit.jupiter.api.Test;

import static io.horrorshow.codey.compiler.WandboxDiscordUtils.formatWandboxResponse;
import static io.horrorshow.codey.discordutil.DiscordUtils.CHAR_LIMIT;
import static org.assertj.core.api.Assertions.assertThat;


class WandboxDiscordUtilsTest {

    @Test
    void return_status_code_if_neq_0() {
        var response = new WandboxResponse();
        response.setStatus(1);
        assertThat(formatWandboxResponse(response))
                .contains("""
                        ```
                        status code: 1```
                        """);
    }


    @Test
    void hide_status_code_if_status_eq_0() {
        var response = new WandboxResponse();
        response.setStatus(1);
        assertThat(formatWandboxResponse(response))
                .doesNotContain("""
                        ```
                        status code: 0```
                        """);
    }


    @Test
    void return_no_output_returned_if_response_values_null() {
        var response = new WandboxResponse();
        assertThat(formatWandboxResponse(response))
                .containsExactly("no output returned");
    }


    @Test
    void return_compiler_error() {
        var response = new WandboxResponse();
        response.setCompiler_error("compiler error");
        assertThat(formatWandboxResponse(response))
                .contains("""
                        ```
                        compiler error```
                        """);
    }


    @Test
    void return_program_message() {
        var response = new WandboxResponse();
        response.setProgram_message("program message");
        assertThat(formatWandboxResponse(response))
                .contains("""
                        ```
                        program message```
                        """);
    }


    @Test
    void return_compiler_message_if_compiler_error_is_null() {
        var response = new WandboxResponse();
        response.setCompiler_error(null);
        response.setCompiler_message("compiler message");
        assertThat(formatWandboxResponse(response))
                .contains("""
                        ```
                        compiler message```
                        """);
    }


    @Test
    void return_program_output_if_program_message_is_null() {
        var response = new WandboxResponse();
        response.setProgram_message(null);
        response.setProgram_output("program output");
        assertThat(formatWandboxResponse(response))
                .contains("""
                        ```
                        program output```
                        """);
    }


    @Test
    void truncate_all_individual_outputs_to_char_limit() {
        var longString = "#".repeat(CHAR_LIMIT + 1);
        var response = new WandboxResponse();
        response.setStatus(1);
        response.setProgram_output(longString);
        response.setCompiler_message(longString);
        assertThat(formatWandboxResponse(response))
                .allMatch(s -> s.length() == CHAR_LIMIT);
    }
}
