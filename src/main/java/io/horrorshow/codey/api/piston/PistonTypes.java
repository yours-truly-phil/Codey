package io.horrorshow.codey.api.piston;

import java.util.Map;


public class PistonTypes {

    public static record CompileResult(String stdout, String stderr, String output, Integer code, String signal) {

    }

    public static record CompilerInfo(Map<String, PistonRuntime> compilerMap) {

    }

    public static record PistonRequest(String language, String version, PistonFile[] files, String stdin, String args,
                                       Long compile_timeout, Long run_timeout, Long compile_memory_limit, Long run_memory_limit) {

    }

    public static record PistonResponse(String language, String version, CompileResult run, CompileResult compile) {

    }

    public static record PistonRuntime(String language, String version, String[] aliases, String runtime) {

    }

    public record PistonFile(String name, String content) {

    }
}
