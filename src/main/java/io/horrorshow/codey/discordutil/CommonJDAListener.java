package io.horrorshow.codey.discordutil;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;


@Service
@Slf4j
public class CommonJDAListener extends ListenerAdapter {

    public static final String BASKET = "\uD83D\uDDD1️";


    public CommonJDAListener(JDA jda) {
        jda.addEventListener(this);
    }

    //Being static avoids the need to inject this map to DiscordUtils
    public static Map<String,Boolean> deletableMessageMap = new HashMap<>();

    @Override
    public void onGuildMessageReactionAdd(@NotNull GuildMessageReactionAddEvent event) {
        if (!event.getUser().isBot()) {
            CompletableFuture.runAsync(() -> onReactionAdd(event));
        }
    }


    public void onReactionAdd(@NotNull GuildMessageReactionAddEvent event) {
        var message = event.getChannel().retrieveMessageById(event.getMessageId()).complete();
        /***
         * Use a hashmap that is not persisted in any way to look up if the message should be deletable.
         *
         * It should not be a big issue, if a user reacts, and the message is not deleted.
         * However, vice-versa is not true. If a malicious user reacts, and the message is deleted - that is an issue.
         *
         * The fact that the hashmap is empty on restart does not matter too much.
         */

        if (DiscordUtils.hasEmoji(BASKET, event)) {
            if (deletableMessageMap.containsKey(event.getMessageId()) && event.getJDA().getSelfUser().getId().equals(message.getAuthor().getId())) {
                try {
                    deletableMessageMap.remove(message.getId());
                    message.delete().complete();
                } catch (ErrorResponseException e) {
                    log.warn("Unable to remove message");
                }
            }
        }
    }
}
