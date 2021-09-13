package io.horrorshow.codey.api.piston;

import io.horrorshow.codey.api.CompilerApi;
import io.horrorshow.codey.compiler.Output;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;


@Service
@Qualifier("piston")
@Slf4j
public class PistonApi implements CompilerApi {

    private final RestTemplate restTemplate;
    private final RetryTemplate retryTemplate;
    private final PistonConfiguration config;
    private final PistonTypes.CompilerInfo compilerInfo = new PistonTypes.CompilerInfo();


    public PistonApi(@Autowired RestTemplate restTemplate,
            @Autowired PistonConfiguration config,
            @Autowired RetryTemplate retryTemplate) {
        this.restTemplate = restTemplate;
        this.retryTemplate = retryTemplate;
        this.config = config;

        updateCompilerInfo(config.getCurrentApi());
    }


    public CompletableFuture<Output> compile(String content, String lang, String stdin, String args) {
        var runtime = compilerInfo.get(lang);
        var file = new PistonTypes.PistonFile("main", content);
        var request = new PistonTypes.PistonRequest(lang, runtime.version(),
                new PistonTypes.PistonFile[] {file}, stdin, args,
                20000L, 20000L, -1L, -1L);
        var executeUrl = config.getApis().get(config.getCurrentApi()).execute();
        var response = retryTemplate.execute(callback ->
                restTemplate.postForObject(executeUrl, request, PistonTypes.PistonResponse.class));

        return response == null
                ? CompletableFuture.completedFuture(new Output(null, -1, null, "no response"))
                : CompletableFuture.completedFuture(
                        new Output(response.run().output(), response.run().code(), response.run().signal(), response.run().stderr()));
    }


    public void updateCompilerInfo(String name) {
        var runtimesUrl = config.getApis().get(name).runtimes();
        var runtimes = restTemplate.getForObject(runtimesUrl, PistonTypes.PistonRuntime[].class);

        compilerInfo.clear();
        fillCompilerMap(runtimes);
    }


    private void fillCompilerMap(PistonTypes.PistonRuntime[] runtimes) {
        Arrays.stream(Objects.requireNonNull(runtimes, "Can't run without piston runtimes and compiler versions"))
                .forEach(runtime -> {
                    compilerInfo.put(runtime.language(), runtime);
                    if (runtime.aliases() != null) {
                        Arrays.stream(runtime.aliases()).forEach(alias -> compilerInfo.put(alias, runtime));
                    }
                });
    }
}
