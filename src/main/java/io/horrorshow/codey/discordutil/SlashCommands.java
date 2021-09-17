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
                        new OptionData(OptionType.STRING, "m", "what should it say?", true),
                        new OptionData(OptionType.BOOLEAN, "ping", "ping when done?"))),
        SHOW_REMINDERS("show-reminders", new CommandData("show-reminders", "Show your running reminders")
                .addOptions(new OptionData(OptionType.BOOLEAN, "all", "Show reminders of all users"))),
        STOP_REMINDER("stop-reminder", new CommandData("stop-reminder", "Stop a running reminder")
                .addOptions(new OptionData(OptionType.INTEGER, "id", "Id of the reminder to stop", true))),
        CHANGE_API("change-api", new CommandData("change-api", "set compiler api")
                .addOptions(new OptionData(OptionType.STRING, "name", "Name of the endpoint", true))),
        SHOW_APIS("show-apis", new CommandData("show-apis", "Show available apis")),
        SET_GITHUB_CHANNEL("set-github-channel", new CommandData("set-github-channel", "Post github updates in this channel")
                .addOptions(new OptionData(OptionType.CHANNEL, "channel", "The channel codey posts discord updates in", true),
                        new OptionData(OptionType.BOOLEAN, "remove", "No longer post into this channel"))),
        SHOW_GITHUB_CHANNELS("show-github-channels", new CommandData("show-github-channels", "Shows current github channels"));

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
    private final DataStore dataStore;
    private final DiscordUtils discordUtils;


    public SlashCommands(@Autowired JDA jda,
            @Autowired Api api,
            @Autowired DataStore dataStore,
            @Autowired DiscordUtils discordUtils) {
        this.api = api;
        this.dataStore = dataStore;
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

                    dataStore.getCompilationCache().clearByGuild(guild);

                    lines.add("Removed %d compilation results".formatted(
                            dataStore.getCompilationCache().countByGuild(guild)));
                }
            });

            lines.add("%d compilation results".formatted(
                    dataStore.getCompilationCache().countByGuild(guild)));

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
                    "%s\n\n%s".formatted(url, api.prettyPrintJson(api.getRequest(url))), true);
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
