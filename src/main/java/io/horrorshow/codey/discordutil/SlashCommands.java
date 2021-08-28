package io.horrorshow.codey.discordutil;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.horrorshow.codey.compiler.WandboxDiscordUtils;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.Objects;


@Service
@Slf4j
public class SlashCommands extends ListenerAdapter {

    private final CodeyConfig codeyConfig;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;


    public SlashCommands(@Autowired JDA jda,
            @Autowired CodeyConfig codeyConfig,
            @Autowired RestTemplate restTemplate,
            @Autowired ObjectMapper objectMapper) {
        this.codeyConfig = codeyConfig;
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;

        jda.addEventListener(this);

        jda.updateCommands().addCommands(List.of(
                new CommandData("say", "Makes the bot say what you tell it to").addOptions(
                        new OptionData(OptionType.STRING, "content", "What the bot should say").setRequired(true)),
                new CommandData("get", "Get request").addOptions(
                        new OptionData(OptionType.STRING, "url", "The URL to run the request against").setRequired(true))
        )).queue();
    }


    @Override
    public void onSlashCommand(SlashCommandEvent event) {
        switch (event.getName()) {
            case "say" -> say(event);
            case "get" -> get(event);
            default -> event.reply("Invalid command").queue();
        }
    }


    private void get(SlashCommandEvent event) {
        try {
            var url = Objects.requireNonNull(event.getOption("url")).getAsString();
            var res = restTemplate.getForObject(url, Map.class);
            var out = objectMapper.writerWithDefaultPrettyPrinter()
                            .writeValueAsString(res);
            var formatted = WandboxDiscordUtils.toCodeBlock(out);
            event.reply(formatted).queue();
        } catch (Exception e) {
            event.reply("Something went wrong: " + e.getMessage()).queue();
        }
    }


    private void say(SlashCommandEvent event) {
        if (Objects.requireNonNull(event.getMember()).getRoles().stream()
                .anyMatch(role -> codeyConfig.getRoles().contains(role.getName()))) {
            event.reply(Objects.requireNonNull(event.getOption("content")).getAsString())
                    .queue();
        }
    }
}
