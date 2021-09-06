package io.horrorshow.codey.discordutil;

import io.horrorshow.codey.compiler.Output;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.react.GenericGuildMessageReactionEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;


@Service
@Slf4j
public class DiscordUtils extends ListenerAdapter {

    public static final String BASKET = "\uD83D\uDDD1️";

    public static final int CHAR_LIMIT = 2000;
    public static final String CODE_BLOCK_TICKS = "```\n";

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
        if (hasEmoji(BASKET, event)) {
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
        message.addReaction(BASKET).queue();
        return CompletableFuture.completedFuture(message);
    }


    @Async
    public CompletableFuture<Message> sendTextFile(String filename, String text, TextChannel channel) {
        var message = channel.sendFile(text.getBytes(StandardCharsets.UTF_8), filename).complete();
        message.addReaction(BASKET).queue();
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


    public static boolean hasEmoji(String emoji, GenericGuildMessageReactionEvent event) {
        return event.getReactionEmote().isEmoji() && Objects.equals(emoji, event.getReactionEmote().getEmoji());
    }


    public static String toCodeBlock(String msg, boolean concat) {
        return "```\n%s```\n".formatted(
                concat ? msg.substring(0, Math.min(msg.length(), CHAR_LIMIT - CODE_BLOCK_TICKS.length() * 2)) : msg);
    }


    public static List<String> toDiscordMessages(Output output) {
        List<String> discordMessages = new ArrayList<>();

        List<String> errors = new ArrayList<>();
        if (output.signal() != null) {
            errors.add("Signal: " + output.signal());
        }
        if (output.errMsg() != null && !output.errMsg().isBlank()) {
            errors.add("Error: " + output.errMsg());
        }
        if (output.status() != null && output.status() != 0) {
            errors.add("Status: " + output.status());
        }

        if (errors.size() > 0) {
            discordMessages.add("Problems during compilation\n" + String.join("\n", errors));
        }

        discordMessages.add(output.sysOut());

        return discordMessages;
    }

    public Color getColor() {
        return Color.decode(config.getEmbedColor());
    }
}
