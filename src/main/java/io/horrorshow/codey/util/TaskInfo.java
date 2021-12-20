package io.horrorshow.codey.util;

import net.dv8tion.jda.api.entities.Channel;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.MessageUpdateEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import org.jetbrains.annotations.Nullable;

import java.nio.ByteBuffer;
import java.util.Base64;
import java.util.UUID;


public class TaskInfo {

    private static final Base64.Encoder BASE64_URL_ENCODER = Base64.getUrlEncoder().withoutPadding();

    public final String id;
    public final User user;
    public final Channel channel;
    public final Guild guild;


    public TaskInfo(SlashCommandEvent event) {
        this(event.getUser(), event.getChannel(), event.getGuild());
    }


    public TaskInfo(MessageReactionAddEvent event) {
        this(event.getUser(), event.getChannel(), event.getGuild());
    }


    public TaskInfo(MessageUpdateEvent event) {
        this(event.getAuthor(), event.getChannel(), event.getGuild());
    }


    public TaskInfo(MessageReceivedEvent event) {
        this(event.getAuthor(), event.getChannel(), event.getGuild());
    }


    public TaskInfo(@Nullable User user, @Nullable Channel channel, @Nullable Guild guild) {
        this.id = BASE64_URL_ENCODER.encodeToString(uuidToBytes(UUID.randomUUID()));
        this.user = user;
        this.channel = channel;
        this.guild = guild;
    }


    public static TaskInfo empty() {
        return new TaskInfo(null, null, null);
    }


    private static byte[] uuidToBytes(UUID uuid) {
        ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
        bb.putLong(uuid.getMostSignificantBits());
        bb.putLong(uuid.getLeastSignificantBits());
        return bb.array();
    }

}
