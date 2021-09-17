package io.horrorshow.codey.api.github;

import io.horrorshow.codey.data.ChannelEntity;
import io.horrorshow.codey.data.GithubChannelRepository;
import io.horrorshow.codey.discordutil.DataStore;
import io.horrorshow.codey.discordutil.DiscordUtils;
import io.horrorshow.codey.discordutil.SlashCommands.COMMAND;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;


@Service
@Slf4j
public class GithubDiscordActions extends ListenerAdapter {

    private final DiscordUtils utils;
    private final GithubChannelRepository githubChannelRepository;
    private final DataStore.GithubEventChannels githubEventChannels;


    @Autowired
    public GithubDiscordActions(JDA jda, DiscordUtils utils, GithubChannelRepository githubChannelRepository, DataStore dataStore) {
        this.utils = utils;
        this.githubChannelRepository = githubChannelRepository;
        this.githubEventChannels = dataStore.getGithubEventChannels();

        try {
            jda.awaitReady();
            for (var channelEntity : githubChannelRepository.findAll()) {
                var channel = jda.getGuildChannelById(channelEntity.getChannelId());
                if (channel != null) {
                    githubEventChannels.put(channel.getId(), new ChannelInfo(channel, channelEntity));
                }
            }
        } catch (InterruptedException e) {
            log.error("Unable to wait for jda", e);
        }

        jda.addEventListener(this);
    }


    @Override
    public void onSlashCommand(@NotNull SlashCommandEvent event) {
        if (utils.isElevatedMember(event.getMember())) {
            if (COMMAND.SET_GITHUB_CHANNEL.getName().equals(event.getName())) {
                CompletableFuture.runAsync(() -> onSetGithubChannel(event));
            } else if (COMMAND.SHOW_GITHUB_CHANNELS.getName().equals(event.getName())) {
                CompletableFuture.runAsync(() -> onShowGithubChannels(event));
            }
        }
    }


    private void onShowGithubChannels(@NotNull SlashCommandEvent event) {
        event.reply("Channels:\n" + githubEventChannels.values().stream()
                .map(channelInfo -> " - Id=" + channelInfo.channel().getId() + " Name=" + channelInfo.channel().getName())
                .collect(Collectors.joining("\n"))).complete();
    }


    private void onSetGithubChannel(@NotNull SlashCommandEvent event) {
        var channel = Objects.requireNonNull(event.getOption("channel")).getAsGuildChannel();
        var containsChannel = githubEventChannels.containsKey(channel.getId());
        var remove = event.getOption("remove");
        if (remove != null && remove.getAsBoolean()) {
            if (containsChannel) {
                var removedChannelInfo = githubEventChannels.remove(channel.getId());
                githubChannelRepository.delete(removedChannelInfo.entity());
                event.reply("Removed channel " + channel.getName() + ".").complete();
            } else {
                event.reply("Channel not used to post github info").complete();
            }
        } else {
            if (!containsChannel) {
                var githubChannel = new ChannelEntity();
                githubChannel.setChannelId(channel.getId());
                var entity = githubChannelRepository.save(githubChannel);
                githubEventChannels.put(channel.getId(), new ChannelInfo(channel, entity));
                if (log.isDebugEnabled()) {
                    log.debug("new channel added and saved {}", githubChannel);
                }
                event.reply("Channel " + channel.getName() + " added").complete();
            } else {
                event.reply("Channel " + channel.getName() + " already active").complete();
            }
        }
    }
}
