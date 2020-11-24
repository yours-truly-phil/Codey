package io.horrorshow.discordcodeformatter;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.function.Consumer;

@Service
public class WandboxApi {
    @Async
    public void compile(String codeBlock, String lang, Consumer<WandboxOutput> callback) {
        callback.accept(new WandboxOutput("Not yet implemented"));
    }
}
