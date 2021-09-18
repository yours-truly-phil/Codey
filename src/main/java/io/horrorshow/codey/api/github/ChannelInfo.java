package io.horrorshow.codey.api.github;

import io.horrorshow.codey.data.entity.ChannelEntity;
import net.dv8tion.jda.api.entities.GuildChannel;


public record ChannelInfo(GuildChannel channel, ChannelEntity entity) {

}
