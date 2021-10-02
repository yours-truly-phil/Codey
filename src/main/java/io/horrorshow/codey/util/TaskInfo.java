package io.horrorshow.codey.util;

import net.dv8tion.jda.api.entities.AbstractChannel;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageUpdateEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import org.jetbrains.annotations.Nullable;

import java.nio.ByteBuffer;
import java.util.Base64;
import java.util.UUID;


public class TaskInfo {

    private static final Base64.Encoder BASE64_URL_ENCODER = Base64.getUrlEncoder().withoutPadding();

    public final String id;
    public final User user;
    public final AbstractChannel channel;
    public final Guild guild;


    public TaskInfo(SlashCommandEvent event) {
        this(event.getUser(), event.getChannel(), event.getGuild());
    }


    public TaskInfo(GuildMessageReactionAddEvent event) {
        this(event.getUser(), event.getChannel(), event.getGuild());
    }


    public TaskInfo(GuildMessageUpdateEvent event) {
        this(event.getAuthor(), event.getChannel(), event.getGuild());
    }


    public TaskInfo(GuildMessageReceivedEvent event) {
        this(event.getAuthor(), event.getChannel(), event.getGuild());
    }


    public TaskInfo(@Nullable User user, @Nullable AbstractChannel channel, @Nullable Guild guild) {
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
