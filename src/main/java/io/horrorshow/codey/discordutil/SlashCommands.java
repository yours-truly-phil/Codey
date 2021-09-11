package io.horrorshow.codey.discordutil;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.horrorshow.codey.api.Api;
import lombok.Getter;
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
import java.util.Arrays;
import java.util.Objects;


@Service
@Slf4j
public class SlashCommands extends ListenerAdapter {

    public enum COMMAND {
        SAY("say", new CommandData("say", "Makes the bot say what you tell it to")
                .addOptions(new OptionData(OptionType.STRING, "content",
                        "What the bot should say", true))),
        GET("get", new CommandData("get", "Get request")
                .addOptions(new OptionData(OptionType.STRING, "url",
                        "The URL to run the request against", true))),
        CACHE("cache", new CommandData("cache", "Manage formatted code store")
                .addOptions(new OptionData(OptionType.BOOLEAN, "clear", "Clear the cache"))),
        REMIND_ME("remind-me", new CommandData("remind-me", "Set a reminder")
                .addOptions(new OptionData(OptionType.INTEGER, "in", "how many minutes from now?", true),
                        new OptionData(OptionType.STRING, "m", "what should it say?", true))),
        SHOW_REMINDERS("show-reminders", new CommandData("show-reminders", "Show your running reminders")
                .addOptions(new OptionData(OptionType.BOOLEAN, "all", "Show reminders of all users"))),
        STOP_REMINDER("stop-reminder", new CommandData("stop-reminder", "Stop a running reminder")
                .addOptions(new OptionData(OptionType.STRING, "id", "Id of the reminder to stop", true)));

        @Getter
        public final String name;
        @Getter
        private final CommandData data;


        COMMAND(String name, CommandData data) {
            this.name = name;
            this.data = data;
        }
    }

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

        jda.updateCommands().addCommands(Arrays.stream(COMMAND.values()).map(COMMAND::getData).toList()).queue();

        jda.addEventListener(this);
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
