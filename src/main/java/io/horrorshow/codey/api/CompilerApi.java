package io.horrorshow.codey.api;

import io.horrorshow.codey.compiler.Output;
import org.springframework.scheduling.annotation.Async;

import java.util.concurrent.CompletableFuture;


public interface CompilerApi {

    @Async
    CompletableFuture<Output> compile(String content, String lang, String stdin, String args);
}
