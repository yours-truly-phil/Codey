package io.horrorshow.codey.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;


@Service
@Slf4j
public class WandboxApi {

    private final RestTemplate restTemplate;
    private final WandboxConfiguration config;

    private final Map<String, Function<String, String>> langSpecificFunctions =
            Map.of("java", this::fixJavaCode);


    public WandboxApi(@Autowired RestTemplate restTemplate,
            @Autowired WandboxConfiguration config) {
        this.restTemplate = restTemplate;
        this.config = config;
    }


    private String fixJavaCode(String s) {
        return s.replaceAll("public class ", "class ");
    }


    @Async
    public CompletableFuture<WandboxResponse> compileAsync(String codeBlock, String lang) {
        return compileAsync(codeBlock, lang, "", "");
    }


    @Async
    public CompletableFuture<WandboxResponse> compileAsync(String codeBlock, String lang,
            String stdin, String runtimeOptions) {
        return CompletableFuture.completedFuture(compile(codeBlock, lang, stdin, runtimeOptions));
    }


    public WandboxResponse compile(String codeBlock, String lang, String stdin, String runtimeOptions)
            throws RestClientException {
        codeBlock = applyLanguageSpecificFixes(codeBlock, lang);
        var compiler = config.getCompiler().getOrDefault(lang, lang);
        var request = new WandboxRequest(codeBlock, compiler, stdin, runtimeOptions);
        return restTemplate
                .postForObject(config.getUrl(), request, WandboxResponse.class);
    }


    private String applyLanguageSpecificFixes(String codeBlock, String lang) {
        if (langSpecificFunctions.containsKey(lang.toLowerCase())) {
            codeBlock = langSpecificFunctions.get(lang.toLowerCase()).apply(codeBlock);
        }
        return codeBlock;
    }
}
