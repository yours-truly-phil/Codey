package io.horrorshow.codey.discordutil;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;


@Service
@Slf4j
public class CommonJDAListener extends ListenerAdapter {

    public static final String BASKET = "\uD83D\uDDD1️";


    public CommonJDAListener(JDA jda) {
        jda.addEventListener(this);
    }


    @Override
    public void onGuildMessageReactionAdd(@NotNull GuildMessageReactionAddEvent event) {
        if (!event.getUser().isBot()) {
            CompletableFuture.runAsync(() -> onReactionAdd(event));
        }
    }


    public void onReactionAdd(@NotNull GuildMessageReactionAddEvent event) {
        var message = event.getChannel().retrieveMessageById(event.getMessageId()).complete();
        if (DiscordUtils.hasEmoji(BASKET, event)) {
            if (event.getJDA().getSelfUser().getId().equals(message.getAuthor().getId())) {
                try {
                    message.delete().complete();
                } catch (ErrorResponseException e) {
                    log.warn("Unable to remove message");
                }
            }
        }
    }
}
