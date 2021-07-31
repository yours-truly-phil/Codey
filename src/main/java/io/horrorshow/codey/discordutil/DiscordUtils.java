package io.horrorshow.codey.discordutil;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;


@Service
@Slf4j
public class DiscordUtils extends ListenerAdapter {

    public static final String BASKET = "\uD83D\uDDD1️";

    private final MessageStore messageStore;


    public DiscordUtils(@Autowired JDA jda,
            @Autowired MessageStore messageStore) {
        this.messageStore = messageStore;

        jda.addEventListener(this);
    }


    public static byte[] imgAsBytes(BufferedImage image) throws IOException {
        var baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos);
        baos.flush();
        return baos.toByteArray();
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
            log.info("{} removed formatted message by {}: {}",
                    event.getUser().getName(), message.getAuthor().getName(), message.getContentRaw());
        }
    }


    private void removeMessage(Message message) {
        message.delete().queue();
        messageStore.getFormattedCodeStore().values()
                .removeIf(storedMsg -> message.getId().equals(storedMsg.getId()));
    }


    @Async
    public CompletableFuture<Message> sendRemovableMessageAsync(String text, TextChannel channel) {
        var message = channel.sendMessage(text).complete();
        message.addReaction(BASKET).complete();
        return CompletableFuture.completedFuture(message);
    }


    public void sendRemovableMessage(String text, TextChannel channel) {
        channel.sendMessage(text).queue(message -> message.addReaction(BASKET).queue());
    }


    public void drawRemovableImage(BufferedImage image, String title, TextChannel channel) {
        try {
            channel.sendFile(imgAsBytes(image), title)
                    .queue(message -> message.addReaction(BASKET).queue());
        } catch (IOException e) {
            log.error("Problem drawing removable image", e);
        }
    }
}
