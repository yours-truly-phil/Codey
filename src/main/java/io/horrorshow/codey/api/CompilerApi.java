package io.horrorshow.codey.api;

import io.horrorshow.codey.compiler.Output;

import java.util.concurrent.CompletableFuture;


public interface CompilerApi {

    CompletableFuture<Output> compile(String content, String lang, String stdin, String args);
}
