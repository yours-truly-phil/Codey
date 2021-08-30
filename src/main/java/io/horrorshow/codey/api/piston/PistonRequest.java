package io.horrorshow.codey.api.piston;

public record PistonRequest(String language, String version, PistonApi.PistonFile[] files, String stdin, String args,
                            Long compile_timeout, Long run_timeout, Long compile_memory_limit, Long run_memory_limit) {

}
