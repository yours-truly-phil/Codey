package io.horrorshow.discordcodeformatter;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

@Service
@Slf4j
public class WandboxApi {

    private static final Map<String, String> COMPILER = new HashMap<>();

    static {
        COMPILER.put("java", "openjdk-head");
        COMPILER.put("c", "gcc-head-c");
        COMPILER.put("cpp", "gcc-head");
        COMPILER.put("cs", "mono-head");
        COMPILER.put("erlang", "erlang-head");
        COMPILER.put("elixir", "elixir-head");
        COMPILER.put("haskell", "ghc-head");
        COMPILER.put("d", "dmd-head");
        COMPILER.put("rust", "rust-head");
        COMPILER.put("py", "pypy-head");
        COMPILER.put("python", "pypy-head");
        COMPILER.put("ruby", "ruby-head");
        COMPILER.put("scala", "scala-2.13.x");
        COMPILER.put("groovy", "groovy-head");
        COMPILER.put("javascript", "nodejs-head");
        COMPILER.put("js", "nodejs-head");
        COMPILER.put("coffee", "coffeescript-head");
        COMPILER.put("swift", "swift-head");
        COMPILER.put("perl", "perl-head");
        COMPILER.put("php", "php-head");
        COMPILER.put("lua", "luajit-head");
        COMPILER.put("sqlite", "sqlite-head");
        COMPILER.put("pascal", "fpc-head");
        COMPILER.put("lisp", "sbcl-head");
        COMPILER.put("vim", "vim-head");
        COMPILER.put("ocaml", "ocaml-head");
        COMPILER.put("go", "go-head");
        COMPILER.put("bash", "bash");
        COMPILER.put("pony", "pony-head");
        COMPILER.put("crystal", "crystal-head");
        COMPILER.put("nim", "nim-head");
        COMPILER.put("openssl", "openssl-head");
        COMPILER.put("fs", "fsharp-head");
        COMPILER.put("cmake", "cmake-head");
        COMPILER.put("r", "r-head");
        COMPILER.put("ts", "typescript-3.9.5");
    }

    private final RestTemplate restTemplate;

    public WandboxApi(@Autowired RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Async
    public void compile(String codeBlock, String lang, Consumer<WandboxResponse> callback) {
        try {
            var compiler = COMPILER.getOrDefault(lang, lang);
            var request = new WandboxRequest(codeBlock, compiler);
            var response = restTemplate
                    .postForObject("https://wandbox.org/api/compile.json", request, WandboxResponse.class);
            callback.accept(response);
        } catch (RestClientException e) {
            log.debug("compilation unsuccessful {}", e.getMessage());
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Data
    static class WandboxRequest {
        private final String code;
        private final String compiler;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Data
    static class WandboxResponse {
        private String program_message;
        private String program_output;
        private String status;
        private String compiler_error;
        private String compiler_message;
    }
}
