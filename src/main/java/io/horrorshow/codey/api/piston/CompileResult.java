package io.horrorshow.codey.api.piston;

public record CompileResult(String stdout, String stderr, String output, Integer code, String signal) {

}
