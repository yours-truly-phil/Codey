package io.horrorshow.codey.discordutil;

import io.horrorshow.codey.compiler.Output;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.react.GenericGuildMessageReactionEvent;

import javax.imageio.ImageIO;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static io.horrorshow.codey.discordutil.CommonJDAListener.BASKET;


@Slf4j
public class DiscordUtils {

    public static final int CHAR_LIMIT = 2000;
    public static final String CODE_BLOCK_TICKS = "```\n";


    public static byte[] imgAsBytes(BufferedImage image) throws IOException {
        var baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos);
        baos.flush();
        return baos.toByteArray();
    }


    public static CompletableFuture<Message> sendRemovableMessageAsync(String text, TextChannel channel) {
        var message = channel.sendMessage(text).complete();
        message.addReaction(BASKET).queue();
        return CompletableFuture.completedFuture(message);
    }


    public static CompletableFuture<Message> sendRemovableEmbed(MessageEmbed embed, TextChannel channel) {
        var message = channel.sendMessage(embed).complete();
        message.addReaction(BASKET).complete();
        return CompletableFuture.completedFuture(message);
    }


    public static CompletableFuture<Message> sendTextFile(String filename, String text, TextChannel channel) {
        var message = channel.sendFile(text.getBytes(StandardCharsets.UTF_8), filename).complete();
        message.addReaction(BASKET).queue();
        return CompletableFuture.completedFuture(message);
    }


    public static void sendRemovableMessage(String text, TextChannel channel) {
        var message = channel.sendMessage(text).complete();
        message.addReaction(BASKET).complete();
    }


    public static void drawRemovableImage(BufferedImage image, String title, TextChannel channel) {
        try {
            channel.sendFile(imgAsBytes(image), title)
                    .queue(message -> message.addReaction(BASKET).queue());
        } catch (IOException e) {
            log.error("Problem drawing removable image", e);
        }
    }


    public static String toCodeBlock(String msg, boolean truncate) {
        return "```\n%s```\n".formatted(
                truncate ? msg.substring(0, Math.min(msg.length(), CHAR_LIMIT - CODE_BLOCK_TICKS.length() * 2)) : msg);
    }


    public static List<String> compilerOutToDiscordMessage(Output output) {
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
        } else {
            discordMessages.add(output.sysOut());
        }

        return discordMessages;
    }


    public static boolean hasEmoji(String emoji, GenericGuildMessageReactionEvent event) {
        return event.getReactionEmote().isEmoji() && emoji.equals(event.getReactionEmote().getEmoji());
    }


    public static CompletableFuture<Message> sendRemovableMessageReply(Message origin, MessageEmbed content) {
        log.info("send removable reply embed");
        var message = origin.reply(content).complete();
        message.addReaction(BASKET).queue();
        return CompletableFuture.completedFuture(message);
    }

    public static String truncateMessage(String msg, int maxLength) {
        return msg.substring(0, Math.min(maxLength, msg.length()));
    }
}
