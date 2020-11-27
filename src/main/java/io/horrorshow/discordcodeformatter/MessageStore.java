package io.horrorshow.discordcodeformatter;

import lombok.Getter;
import net.dv8tion.jda.api.entities.Message;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class MessageStore {
    @Getter
    private final Map<String, Message> formattedCodeStore = new HashMap<>();
}
