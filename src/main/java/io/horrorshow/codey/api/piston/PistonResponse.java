package io.horrorshow.codey.api.piston;

public record PistonResponse(String language, String version, CompileResult run, CompileResult compile) {

}
