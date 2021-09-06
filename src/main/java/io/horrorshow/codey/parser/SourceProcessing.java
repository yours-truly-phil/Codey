package io.horrorshow.codey.parser;

import org.jetbrains.annotations.NotNull;

import java.util.Map;


public class SourceProcessing {

    private static final Map<String, SourceProcessor> processors = Map.of(
            "java", new JavaSourceProcessor()
    );
    private static final SourceProcessor defaultProcessor = new DefaultSourceProcessor();


    public static ProcessResult processSource(@NotNull String source, @NotNull String lang) {
        var processor = processors.getOrDefault(lang, defaultProcessor);
        return processor.process(source);
    }
}
