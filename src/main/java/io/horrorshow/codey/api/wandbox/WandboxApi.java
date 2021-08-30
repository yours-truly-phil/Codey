package io.horrorshow.codey.api.wandbox;

import io.horrorshow.codey.api.CompilerApi;
import io.horrorshow.codey.compiler.Output;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;


@Service
@Qualifier("wandbox")
@Slf4j
public class WandboxApi implements CompilerApi {

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
    public CompletableFuture<Output> compile(String content, String lang, String stdin, String args) {
        var codeBlock = applyLanguageSpecificFixes(content, lang);
        var compiler = config.getCompiler().getOrDefault(lang, lang);
        var request = new WandboxRequest(codeBlock, compiler, stdin, args);
        var response = restTemplate.postForObject(config.getUrl(), request, WandboxResponse.class);
        return CompletableFuture.completedFuture(response == null
                ? new Output(null, -1, "no response")
                : new Output(response.getProgram_output(), response.getStatus(), response.getCompiler_error()));
    }


    private String applyLanguageSpecificFixes(String codeBlock, String lang) {
        if (langSpecificFunctions.containsKey(lang.toLowerCase())) {
            codeBlock = langSpecificFunctions.get(lang.toLowerCase()).apply(codeBlock);
        }
        return codeBlock;
    }
}
