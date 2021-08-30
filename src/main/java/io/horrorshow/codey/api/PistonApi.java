package io.horrorshow.codey.api;

import io.horrorshow.codey.compiler.Output;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.concurrent.CompletableFuture;


@Service
@Qualifier("piston")
@Slf4j
public class PistonApi implements CompilerApi {

    private final RestTemplate restTemplate;
    private final PistonConfiguration config;
    private final CompilerInfo compiler;


    public PistonApi(@Autowired RestTemplate restTemplate,
            @Autowired PistonConfiguration config,
            @Autowired CompilerInfo compiler) {
        this.restTemplate = restTemplate;
        this.config = config;
        this.compiler = compiler;
    }


    public CompletableFuture<Output> compile(String content, String lang, String stdin, String args) {
        var runtime = compiler.compilerMap().get(lang);
        var file = new PistonFile("main", content);
        var request = new PistonRequest(lang, runtime.version, new PistonFile[] {file}, stdin, args,
                10000L, 20000L, -1L, -1L);

        var response = restTemplate.postForObject(config.getExecuteUrl(), request, PistonResponse.class);
        return response == null ? CompletableFuture.completedFuture(new Output(null, -1, "no response"))
                : CompletableFuture.completedFuture(new Output(response.run.output, response.run.code, response.run.stderr));
    }


    record PistonRequest(String language, String version, PistonFile[] files, String stdin, String args,
                         Long compile_timeout, Long run_timeout, Long compile_memory_limit, Long run_memory_limit) {

    }

    record PistonFile(String name, String content) {

    }

    public record PistonResponse(String language, String version, CompileResult run, CompileResult compile) {

    }

    public record CompileResult(String stdout, String stderr, String output, Integer code, String signal) {

    }

    public record PistonRuntime(String language, String version, String[] aliases, String runtime) {

    }

    public record CompilerInfo(Map<String, PistonRuntime> compilerMap) {

    }
}
