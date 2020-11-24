package io.horrorshow.discordcodeformatter;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.function.Consumer;

@Service
public class WandboxApi {

    private final RestTemplate restTemplate;

    public WandboxApi(@Autowired RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Async
    public void compile(String codeBlock, String lang, Consumer<WandboxOutput> callback) {
        var compiler = ("java".equalsIgnoreCase(lang)) ? "openjdk-head" : lang;
        var request = new WandboxRequest(codeBlock, compiler);
        var response = restTemplate
                .postForObject("https://wandbox.org/api/compile.json", request, String.class);
        callback.accept(new WandboxOutput(response));
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Data
    static class WandboxRequest {
        private final String code;
        private final String compiler;
    }
}
