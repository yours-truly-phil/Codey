package io.horrorshow.codey.api.github;

import io.horrorshow.codey.discordutil.DataStore;
import io.horrorshow.codey.discordutil.DiscordUtils;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;


@Service
@Slf4j
public class GithubEventBot extends ListenerAdapter {

    private final DiscordUtils discordUtils;
    private final DataStore.GithubEventChannels githubEventChannels;


    @Autowired
    public GithubEventBot(JDA jda, DiscordUtils discordUtils, DataStore dataStore) {
        this.discordUtils = discordUtils;
        this.githubEventChannels = dataStore.getGithubEventChannels();

        jda.addEventListener(this);
    }

    @Override
    public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent event) {
        if (!event.getAuthor().isBot()) {
            if ("!test-push".equals(event.getMessage().getContentDisplay())) {

            }
        }
    }


    @Async
    public void onPush(GithubWebhookEndpoint.GithubPush event) {
        log.info("onPush:\n{}", event);
        var embed = new EmbedBuilder()
                .setTimestamp(Instant.ofEpochSecond(event.repository.pushed_at))
                .setThumbnail(event.sender.avatar_url)
                .setTitle("Push by " + event.pusher.name + " into '" + event.repository.name + "'")
//                .setDescription("Commits:\n"
//                                + event.commits.stream()
//                                        .map(this::formatCommit)
//                                        .collect(Collectors.joining("\n")))
                .addField("Commits", event.commits.stream()
                        .map(this::formatCommit)
                        .collect(Collectors.joining("\n")), false)
                .setFooter(event.repository.html_url)
                .build();

        CompletableFuture.allOf(
                githubEventChannels.values().stream()
                        .map(channelInfo -> {
                            var channel = channelInfo.channel();
                            var textChannel = channel.getJDA().getTextChannelById(channel.getId());
                            if (textChannel != null) {
                                return discordUtils.sendRemovableEmbed(embed, textChannel);
                            }
                            return null;
                        }).toArray(CompletableFuture[]::new)
        ).exceptionally(e -> {
            log.error("error sending github update to text channel", e);
            return null;
        });
    }


    private String formatCommit(GithubWebhookEndpoint.GithubCommit commit) {
        return String.format("[%s](%s) [test](https://some.url/test)", commit.message, commit.url);
    }

}
