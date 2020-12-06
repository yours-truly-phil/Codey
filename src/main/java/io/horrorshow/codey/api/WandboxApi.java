package io.horrorshow.codey.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.function.Consumer;
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
    public void compileAsync(String codeBlock, String lang, Consumer<WandboxResponse> callback) {
        compileAsync(codeBlock, lang, "", "", callback);
    }

    @Async
    public void compileAsync(String codeBlock, String lang,
                             String stdin, String runtimeOptions,
                             Consumer<WandboxResponse> callback) {
        try {
            callback.accept(compile(codeBlock, lang, stdin, runtimeOptions));
        } catch (RestClientException e) {
            log.error("exception compiling in lang '{}' stdin '{}' runtimeArgs '{}' code: {}",
                    lang, stdin, runtimeOptions, codeBlock, e);
        }
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
