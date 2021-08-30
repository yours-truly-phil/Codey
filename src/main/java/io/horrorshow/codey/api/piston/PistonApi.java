package io.horrorshow.codey.api.piston;

import io.horrorshow.codey.api.CompilerApi;
import io.horrorshow.codey.compiler.Output;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.CompletableFuture;


@Service
@Qualifier("piston")
@Slf4j
public class PistonApi implements CompilerApi {

    private final RestTemplate restTemplate;
    private final PistonConfiguration config;
    private final PistonTypes.CompilerInfo compiler;


    public PistonApi(@Autowired RestTemplate restTemplate,
            @Autowired PistonConfiguration config,
            @Autowired PistonTypes.CompilerInfo compiler) {
        this.restTemplate = restTemplate;
        this.config = config;
        this.compiler = compiler;
    }


    public CompletableFuture<Output> compile(String content, String lang, String stdin, String args) {
        var runtime = compiler.compilerMap().get(lang);
        var file = new PistonTypes.PistonFile("main", content);
        var request = new PistonTypes.PistonRequest(lang, runtime.version(), new PistonTypes.PistonFile[] {file}, stdin, args,
                10000L, 20000L, -1L, -1L);

        var response = restTemplate.postForObject(config.getExecuteUrl(), request, PistonTypes.PistonResponse.class);
        return response == null ? CompletableFuture.completedFuture(new Output(null, -1, "no response"))
                : CompletableFuture.completedFuture(new Output(response.run().output(), response.run().code(), response.run().stderr()));
    }
}