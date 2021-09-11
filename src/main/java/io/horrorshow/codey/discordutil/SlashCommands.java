package io.horrorshow.codey.discordutil;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.horrorshow.codey.api.Api;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.EmbedType;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


@Service
@Slf4j
public class SlashCommands extends ListenerAdapter {

    private final Api api;
    private final MessageStore messageStore;
    private final DiscordUtils discordUtils;


    public SlashCommands(@Autowired JDA jda,
            @Autowired Api api,
            @Autowired MessageStore messageStore,
            @Autowired DiscordUtils discordUtils) {
        this.api = api;
        this.messageStore = messageStore;
        this.discordUtils = discordUtils;

        jda.addEventListener(this);

        jda.updateCommands().addCommands(List.of(
                new CommandData("say", "Makes the bot say what you tell it to")
                        .addOptions(new OptionData(OptionType.STRING, "content",
                                "What the bot should say", true)),

                new CommandData("get", "Get request")
                        .addOptions(new OptionData(OptionType.STRING, "url",
                                "The URL to run the request against", true)),

                new CommandData("cache", "Manage formatted code store")
                        .addOptions(new OptionData(OptionType.BOOLEAN, "clear", "Clear the cache"))
        )).queue();
    }


    @Override
    public void onSlashCommand(SlashCommandEvent event) {
        switch (event.getName()) {
            case "say" -> say(event);
            case "get" -> get(event);
            case "cache" -> cache(event);
        }
    }


    private void cache(SlashCommandEvent event) {
        var guild = event.getGuild();
        var lines = new ArrayList<String>();
        if (guild != null) {
            event.getOptions().forEach(option -> {
                if ("clear".equals(option.getName())
                    && option.getAsBoolean()
                    && discordUtils.isElevatedMember(event.getMember())) {

                    messageStore.getCompilationCache().clearByGuild(guild);

                    lines.add("Removed %d compilation results".formatted(
                            messageStore.getCompilationCache().countByGuild(guild)));
                }
            });

            lines.add("%d compilation results".formatted(
                    messageStore.getCompilationCache().countByGuild(guild)));

            event.replyEmbeds(new MessageEmbed(null,
                    "Cache", String.join("\n", lines),
                    EmbedType.RICH, null, 4711, null, null, null,
                    null, null, null, null
            )).queue();
        }
    }


    @Async
    public void get(SlashCommandEvent event) {
        try {
            var url = Objects.requireNonNull(event.getOption("url")).getAsString();
            var res = DiscordUtils.toCodeBlock(
                    "%s\n\n%s".formatted(url, api.prettyPrintJson(api.getRequest(url))), false);
            event.reply(res).queue();
        } catch (JsonProcessingException e) {
            event.reply("Error: %s".formatted(e.getMessage())).queue();
            log.debug("Error in get slash command: {}", e.getMessage());
        }
    }


    private void say(SlashCommandEvent event) {
        if (discordUtils.isElevatedMember(event.getMember())) {
            event.reply(Objects.requireNonNull(event.getOption("content")).getAsString())
                    .queue();
        }
    }
}
