package io.horrorshow.codey.api.github;

import io.horrorshow.codey.data.ChannelEntity;
import io.horrorshow.codey.data.GithubChannelRepository;
import javassist.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.GuildChannel;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;


@Slf4j
public class GithubEventState {

    private final GithubChannelRepository githubChannelRepository;

    private final Map<String, ChannelInfo> channelIdChannelMap = new ConcurrentHashMap<>();


    public GithubEventState(JDA jda, GithubChannelRepository githubChannelRepository) {
        this.githubChannelRepository = githubChannelRepository;

        try {
            jda.awaitReady();
            for (var channelEntity : githubChannelRepository.findAll()) {
                var guildChannel = jda.getGuildChannelById(channelEntity.getChannelId());
                if (guildChannel != null) {
                    channelIdChannelMap.put(guildChannel.getId(), new ChannelInfo(guildChannel, channelEntity));
                }
            }
        } catch (InterruptedException e) {
            log.error("Unable to await jda ready state", e);
        }
    }


    public Collection<ChannelInfo> getAll() {
        return channelIdChannelMap.values();
    }


    public CompletableFuture<ChannelInfo> remove(String channelId) {
        if (channelIdChannelMap.containsKey(channelId)) {
            var channelInfo = channelIdChannelMap.remove(channelId);
            githubChannelRepository.delete(channelInfo.entity());
            return CompletableFuture.completedFuture(channelInfo);
        } else {
            return CompletableFuture.failedFuture(new NotFoundException("Channel " + channelId + " unused"));
        }
    }


    public CompletableFuture<ChannelInfo> add(GuildChannel guildChannel) {
        var containsChannel = channelIdChannelMap.containsKey(guildChannel.getId());
        if (!containsChannel) {
            var channelEntity = new ChannelEntity();
            channelEntity.setChannelId(guildChannel.getId());
            var savedEntity = githubChannelRepository.save(channelEntity);

            var channelInfo = new ChannelInfo(guildChannel, savedEntity);
            channelIdChannelMap.put(guildChannel.getId(), channelInfo);
            return CompletableFuture.completedFuture(channelInfo);
        } else {
            return CompletableFuture.failedFuture(new NotFoundException("Channel " + guildChannel.getId() + " unused"));
        }
    }


    public CompletableFuture<Boolean> contains(String channelId) {
        if (channelIdChannelMap.containsKey(channelId)) {
            var channelEntity = channelIdChannelMap.get(channelId);
            var exists = githubChannelRepository.existsById(channelEntity.entity().getId());
            return CompletableFuture.completedFuture(exists);
        } else {
            return CompletableFuture.completedFuture(false);
        }
    }
}
