package io.horrorshow.codey.parser;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;


public class SourceProcessing {

    private static final Map<String, SourceProcessor> processors = Map.of(
            "java", new JavaSourceProcessor()
    );
    private static final SourceProcessor defaultProcessor = new DefaultSourceProcessor();


    public static ProcessResult processSource(@NotNull String source, @Nullable String lang) {
        var processor = lang != null ? processors.getOrDefault(lang, defaultProcessor) : defaultProcessor;
        return processor.process(source);
    }
}
