package io.horrorshow.codey.api.piston;

import io.horrorshow.codey.discordutil.DiscordUtils;
import io.horrorshow.codey.discordutil.SlashCommands;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;


@Service
@Slf4j
public class PistonApiCommands extends ListenerAdapter {

    private final DiscordUtils discordUtils;
    private final PistonConfiguration config;
    private final PistonApi pistonApi;


    @Autowired
    public PistonApiCommands(JDA jda, DiscordUtils discordUtils, PistonConfiguration config, PistonApi pistonApi) {
        this.discordUtils = discordUtils;
        this.config = config;
        this.pistonApi = pistonApi;

        jda.addEventListener(this);
    }


    @Override
    public void onSlashCommand(@Nonnull SlashCommandEvent event) {
        var name = event.getName();
        if (SlashCommands.COMMAND.CHANGE_API.getName().equals(name)) {
            CompletableFuture.runAsync(() -> changeApi(event));
        } else if (SlashCommands.COMMAND.SHOW_APIS.getName().equals(name)) {
            if (discordUtils.isElevatedMember(event.getMember())) {
                event.reply("Apis:\n"
                            + config.getApis().keySet().stream()
                                    .map(key -> " - " + key + (key.equals(config.getCurrentApi()) ? " (*)" : ""))
                                    .collect(Collectors.joining("\n"))).queue();
            } else {
                event.reply("you don't have the permissions to do that").queue();
            }
        }
    }


    private void changeApi(SlashCommandEvent event) {
        if (discordUtils.isElevatedMember(event.getMember())) {
            var name = Objects.requireNonNull(event.getOption("name")).getAsString();
            if (config.getApis().containsKey(name)) {
                log.debug("using api " + name);
                config.setCurrentApi(name);
                pistonApi.updateCompilerInfo(name);
                event.reply("Changed api to %s".formatted(name)).complete();
                log.debug("updated compiler map");
            } else {
                event.reply("api %s not available".formatted(name)).queue();
            }
        } else {
            event.reply("you don't have permission to do that").queue();
        }
    }
}
