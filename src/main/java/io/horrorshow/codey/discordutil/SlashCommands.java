package io.horrorshow.codey.discordutil;

import io.horrorshow.codey.challenge.ChallengeConfiguration;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;


@Service
@Slf4j
public class SlashCommands extends ListenerAdapter {

    private final ChallengeConfiguration config;


    public SlashCommands(@Autowired JDA jda, @Autowired ChallengeConfiguration config) {
        this.config = config;
        jda.addEventListener(this);

        jda.updateCommands().addCommands(
                new CommandData("say", "Makes the bot say what you tell it to")
                        .addOptions(new OptionData(OptionType.STRING, "content", "What the bot should say")
                                .setRequired(true))
        ).queue();
    }


    @Override
    public void onSlashCommand(SlashCommandEvent event) {
        if ("say".equals(event.getName())
            && Objects.requireNonNull(event.getMember()).getRoles().stream()
                    .anyMatch(role -> config.getRoles().contains(role.getName()))
        ) {
            event.reply(Objects.requireNonNull(event.getOption("content")).getAsString())
                    .queue();
        } else {
            event.reply("err").queue();
        }
    }
}
