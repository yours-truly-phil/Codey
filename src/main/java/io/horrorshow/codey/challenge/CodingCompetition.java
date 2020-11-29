package io.horrorshow.codey.challenge;

import io.horrorshow.codey.api.WandboxApi;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CodingCompetition extends ListenerAdapter {

    private static final String SHOW_CUR_PROBLEM_CMD = "$challenge";

    private final WandboxApi wandboxApi;

    public CodingCompetition(@Autowired JDA jda,
                             @Autowired WandboxApi wandboxApi) {
        this.wandboxApi = wandboxApi;

        jda.addEventListener(this);
    }

    @Override
    public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;

        if (SHOW_CUR_PROBLEM_CMD.equals(event.getMessage().getContentRaw())) {
            event.getChannel().sendMessage("Coding challenges coming soon")
                    .queue();
        }
    }
}
