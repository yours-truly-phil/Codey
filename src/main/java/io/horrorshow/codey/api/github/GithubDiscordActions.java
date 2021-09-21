package io.horrorshow.codey.api.github;

import io.horrorshow.codey.discordutil.ApplicationState;
import io.horrorshow.codey.discordutil.AuthService;
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

    private final GithubEventState githubEventState;
    private final AuthService authService;


    @Autowired
    public GithubDiscordActions(JDA jda, ApplicationState applicationState, AuthService authService) {
        this.githubEventState = applicationState.getGithubEventState();
        this.authService = authService;

        jda.addEventListener(this);
    }


    @Override
    public void onSlashCommand(@NotNull SlashCommandEvent event) {
        if (authService.isElevatedMember(event.getMember())) {
            if (COMMAND.SET_GITHUB_CHANNEL.getName().equals(event.getName())) {
                CompletableFuture.runAsync(() -> onSetGithubChannel(event));
            } else if (COMMAND.SHOW_GITHUB_CHANNELS.getName().equals(event.getName())) {
                CompletableFuture.runAsync(() -> onShowGithubChannels(event));
            }
        }
    }


    private void onShowGithubChannels(@NotNull SlashCommandEvent event) {
        var channelInfos = githubEventState.getAll();
        event.reply("Channels:\n" + channelInfos.stream()
                .map(channelInfo -> " - Id=" + channelInfo.channel().getId() + " Name=" + channelInfo.channel().getName())
                .collect(Collectors.joining("\n"))).complete();
    }


    private void onSetGithubChannel(@NotNull SlashCommandEvent event) {
        var channel = Objects.requireNonNull(event.getOption("channel")).getAsGuildChannel();

        githubEventState.contains(channel.getId())
                .thenAccept(containsChannel -> {
                    var remove = event.getOption("remove");
                    if (remove != null && remove.getAsBoolean()) {
                        if (containsChannel) {
                            githubEventState.remove(channel.getId())
                                    .whenComplete((channelInfo, e) -> {
                                        if (e != null) {
                                            event.reply("Unable to remove channel " + channel.getName()).queue();
                                        } else {
                                            event.reply("Removed channel " + channelInfo.channel().getName() + ".").queue();
                                        }
                                    });
                        } else {
                            event.reply("Channel not used to post github info").queue();
                        }
                    } else {
                        if (!containsChannel) {
                            githubEventState.add(channel)
                                    .whenComplete((channelInfo, e) -> {
                                        if (e != null) {
                                            event.reply("Unable to add channel " + channel.getName()).queue();
                                        } else {
                                            event.reply("Channel " + channelInfo.channel().getName() + " added").queue();
                                        }
                                    });
                        } else {
                            event.reply("Channel already used for this").queue();
                        }
                    }
                });
    }
}
