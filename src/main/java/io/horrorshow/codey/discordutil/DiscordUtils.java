package io.horrorshow.codey.discordutil;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
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

    private final CodeyConfig config;


    public DiscordUtils(@Autowired JDA jda,
            @Autowired CodeyConfig config) {
        this.config = config;

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
            onReactionAdd(event);
        }
    }


    @Async
    public void onReactionAdd(@NotNull GuildMessageReactionAddEvent event) {
        var message = event.getChannel().retrieveMessageById(event.getMessageId()).complete();
        String emoji = event.getReactionEmote().getEmoji();
        if (BASKET.equals(emoji)) {
            if (event.getJDA().getSelfUser().getId().equals(message.getAuthor().getId())) {
                try {
                    message.delete().complete();
                } catch (ErrorResponseException e) {
                    log.debug("Unable to remove message");
                }
            }
        }
    }


    @Async
    public CompletableFuture<Message> sendRemovableMessageAsync(String text, TextChannel channel) {
        var message = channel.sendMessage(text).complete();
        message.addReaction(BASKET).complete();
        return CompletableFuture.completedFuture(message);
    }


    public void sendRemovableMessage(String text, TextChannel channel) {
        var message = channel.sendMessage(text).complete();
        message.addReaction(BASKET).complete();
    }


    public void drawRemovableImage(BufferedImage image, String title, TextChannel channel) {
        try {
            channel.sendFile(imgAsBytes(image), title)
                    .queue(message -> message.addReaction(BASKET).queue());
        } catch (IOException e) {
            log.error("Problem drawing removable image", e);
        }
    }


    public boolean isElevatedMember(Member member) {
        return member != null && member.getRoles().stream()
                .anyMatch(role -> config.getRoles().contains(role.getName()));
    }
}
