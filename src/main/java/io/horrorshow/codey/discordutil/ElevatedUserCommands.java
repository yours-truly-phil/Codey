package io.horrorshow.codey.discordutil;

import io.horrorshow.codey.data.entity.ElevatedUser;
import io.horrorshow.codey.util.CodeyTask;
import io.horrorshow.codey.util.TaskInfo;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;


@Service
@Slf4j
public class ElevatedUserCommands extends ListenerAdapter {

    private final ElevatedUsersState elevatedUsersState;
    private final AuthService authService;


    public ElevatedUserCommands(JDA jda, ApplicationState applicationState, AuthService authService) {
        elevatedUsersState = applicationState.getElevatedUsersState();
        this.authService = authService;

        jda.addEventListener(this);
    }


    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (authService.isElevatedMember(event.getMember())) {
            if (SlashCommands.COMMAND.SET_ELEVATED_USER.getName().equals(event.getName())) {
                var taskInfo = new TaskInfo(event.getUser(), event.getChannel(), event.getGuild());
                var userOption = event.getOption("user");
                if (userOption == null) {
                    event.reply("Missing user option").complete();
                    return;
                }
                var user = userOption.getAsUser();
                var removeOption = event.getOption("remove");
                if (removeOption == null || !removeOption.getAsBoolean()) {
                    CodeyTask.runAsync(() -> onSetElevatedUser(event, user), taskInfo);
                } else {
                    CodeyTask.runAsync(() -> onRemoveElevatedUser(event, user), taskInfo);
                }
            } else if (SlashCommands.COMMAND.SHOW_ELEVATED_USERS.getName().equals(event.getName())) {
                event.reply("**Elevated users**" + System.lineSeparator()
                        + elevatedUsersState.getElevatedUsers().values().stream()
                        .map(ElevatedUser::getName)
                        .collect(Collectors.joining(System.lineSeparator()))).queue();
            }
        }
    }


    private void onSetElevatedUser(@NotNull IReplyCallback event, @NotNull User user) {
        log.info("try to set to elevated user {}", user.getName());
        elevatedUsersState.addElevatedUser(user.getId(), user.getName())
                .whenComplete((elevatedUser, e) -> {
                    if (e != null) {
                        log.info(e.getMessage());
                        event.reply(e.getMessage()).complete();
                    } else {
                        var msg = "Added " + elevatedUser.getName() + " to elevated users";
                        log.info(msg);
                        event.reply(msg).complete();
                    }
                });
    }


    private void onRemoveElevatedUser(@NotNull IReplyCallback event, @NotNull User user) {
        log.info("try to remove from elevated users {}", user.getName());
        elevatedUsersState.removeElevatedUser(user.getId())
                .whenComplete((removedUser, e) -> {
                    if (e != null) {
                        log.info(e.getMessage());
                        event.reply(e.getMessage()).complete();
                    } else {
                        var msg = "Removed " + removedUser.getName() + " from elevated users";
                        log.info(msg);
                        event.reply(msg).complete();
                    }
                });
    }

}
