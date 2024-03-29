package io.horrorshow.codey.api.piston;

import io.horrorshow.codey.discordutil.AuthService;
import io.horrorshow.codey.discordutil.SlashCommands;
import io.horrorshow.codey.util.CodeyTask;
import io.horrorshow.codey.util.TaskInfo;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;

import java.util.Objects;
import java.util.stream.Collectors;


@Service
@Slf4j
public class PistonApiCommands extends ListenerAdapter {

    private final AuthService authService;
    private final PistonConfiguration config;
    private final PistonApi pistonApi;


    @Autowired
    public PistonApiCommands(JDA jda, AuthService authService, PistonConfiguration config, PistonApi pistonApi) {
        this.authService = authService;
        this.config = config;
        this.pistonApi = pistonApi;

        jda.addEventListener(this);
    }


    @Override
    public void onSlashCommandInteraction(@Nonnull SlashCommandInteractionEvent event) {
        var name = event.getName();
        if (SlashCommands.COMMAND.CHANGE_API.getName().equals(name)) {
            CodeyTask.runAsync(() -> changeApi(event), new TaskInfo(event));
        } else if (SlashCommands.COMMAND.SHOW_APIS.getName().equals(name)) {
            if (authService.isElevatedMember(event.getMember())) {
                event.reply("Apis:" + System.lineSeparator()
                            + config.getApis().keySet().stream()
                                    .map(key -> " - " + key + (key.equals(config.getCurrentApi()) ? " (*)" : ""))
                                    .collect(Collectors.joining(System.lineSeparator()))).queue();
            } else {
                event.reply("you don't have the permissions to do that").queue();
            }
        }
    }


    private void changeApi(SlashCommandInteractionEvent event) {
        if (authService.isElevatedMember(event.getMember())) {
            var name = Objects.requireNonNull(event.getOption("name")).getAsString();
            if (config.getApis().containsKey(name)) {
                config.setCurrentApi(name);
                pistonApi.updateCompilerInfo(name);
                event.reply("Changed api to %s".formatted(name)).complete();
            } else {
                event.reply("api %s not available".formatted(name)).queue();
            }
        } else {
            event.reply("you don't have permission to do that").queue();
        }
    }
}
