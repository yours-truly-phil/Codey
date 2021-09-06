package io.horrorshow.codey.parser;

public class DefaultSourceProcessor implements SourceProcessor {

    @Override
    public ProcessResult process(String source) {
        return new ProcessResult(source, true, null);
    }
}
