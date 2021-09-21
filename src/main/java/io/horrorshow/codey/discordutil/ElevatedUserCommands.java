package io.horrorshow.codey.discordutil;

import io.horrorshow.codey.data.entity.ElevatedUser;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static io.horrorshow.codey.discordutil.SlashCommands.COMMAND;


@Service
@Slf4j
public class ElevatedUserCommands extends ListenerAdapter {

    private final ElevatedUsersState elevatedUsersState;
    private final AuthService authService;


    public ElevatedUserCommands(JDA jda, ApplicationState applicationState, AuthService authService) {
        this.elevatedUsersState = applicationState.getElevatedUsersState();
        this.authService = authService;

        jda.addEventListener(this);
    }


    @Override
    public void onSlashCommand(@NotNull SlashCommandEvent event) {
        if (authService.isElevatedMember(event.getMember())) {
            if (COMMAND.SET_ELEVATED_USER.getName().equals(event.getName())) {
                var userOption = event.getOption("user");
                if (userOption == null) {
                    event.reply("Missing user option").complete();
                    return;
                }
                var user = userOption.getAsUser();
                var removeOption = event.getOption("remove");
                if (removeOption == null || !removeOption.getAsBoolean()) {
                    CompletableFuture.runAsync(() -> onSetElevatedUser(event, user));
                } else {
                    CompletableFuture.runAsync(() -> onRemoveElevatedUser(event, user));
                }
            } else if (COMMAND.SHOW_ELEVATED_USERS.getName().equals(event.getName())) {
                event.reply("**Elevated users**\n"
                            + elevatedUsersState.getElevatedUsers().values().stream()
                                    .map(ElevatedUser::getName)
                                    .collect(Collectors.joining("\n"))).queue();
            }
        }
    }


    private void onSetElevatedUser(@NotNull SlashCommandEvent event, @NotNull User user) {
        elevatedUsersState.addElevatedUser(user.getId(), user.getName())
                .whenComplete((elevatedUser, e) -> {
                    if (e != null) {
                        event.reply(e.getMessage()).complete();
                    } else {
                        event.reply("Added " + elevatedUser.getName() + " to elevated users").complete();
                    }
                });
    }


    private void onRemoveElevatedUser(@NotNull SlashCommandEvent event, @NotNull User user) {
        elevatedUsersState.removeElevatedUser(user.getId())
                .whenComplete((removedUser, e) -> {
                    if (e != null) {
                        event.reply(e.getMessage()).complete();
                    } else {
                        event.reply("Removed " + removedUser.getName() + " from elevated users").complete();
                    }
                });
    }

}
