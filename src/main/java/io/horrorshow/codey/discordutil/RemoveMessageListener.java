package io.horrorshow.codey.discordutil;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class RemoveMessageListener extends ListenerAdapter {

    public static final String BASKET = "\uD83D\uDDD1️";

    private final MessageStore messageStore;

    public RemoveMessageListener(@Autowired JDA jda,
                                 @Autowired MessageStore messageStore) {
        this.messageStore = messageStore;

        jda.addEventListener(this);
    }

    @Override
    public void onGuildMessageReactionAdd(@NotNull GuildMessageReactionAddEvent event) {
        if (!event.getUser().isBot()) {
            event.getChannel()
                    .retrieveMessageById(event.getMessageId())
                    .queue(message -> handleMessageWithNewReaction(event, message));
        }
    }

    private void handleMessageWithNewReaction(@NotNull GuildMessageReactionAddEvent event,
                                              Message message) {
        final String emoji = event.getReactionEmote().getEmoji();
        if (BASKET.equals(emoji)
                && event.getJDA().getSelfUser().getId().equals(message.getAuthor().getId())) {
            removeMessage(message);
            log.info("removed formatted message by {}: {}",
                    event.getUser().getName(), message.getContentRaw());
        }
    }

    private void removeMessage(Message message) {
        message.delete().queue();
        messageStore.getFormattedCodeStore().values()
                .removeIf(storedMsg -> message.getId().equals(storedMsg.getId()));
    }
}
