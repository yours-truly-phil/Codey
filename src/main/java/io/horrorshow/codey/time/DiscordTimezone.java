package io.horrorshow.codey.time;

import io.horrorshow.codey.discordutil.DiscordUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Arrays;

@Service
public class DiscordTimezone extends ListenerAdapter {
    private final DiscordUtils utils;

    public DiscordTimezone(@Autowired JDA jda, @Autowired DiscordUtils utils) {
        this.utils = utils;

        jda.addEventListener(this);
    }

    private final String emoji = "⏰";
    private final String deleteEmoji = "\uD83D\uDDD1";

    @Override
    public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent event) {
        if (!event.getAuthor().isBot()) {
            onMessage(event.getMessage());
        }
    }

    private void onMessage(Message message) {
        String rawContent = message.getContentRaw();
        String[] rawContentParts = rawContent.split("\\s+");

        if(rawContentParts.length == 0)
            return;

        Object[] result = time(rawContentParts);

        if(!((boolean) result[0])) {
            if(result[1] != null && (boolean) result[1])
            {
                message.addReaction(emoji).complete();
                message.reply(new EmbedBuilder()
                        .setColor(utils.getColor())
                        .setTitle("Did you know that I can provide time conversions automatically?")
                        .addField("Here is a tip:",
                                "You can use regions from the IANA Time Zone Database (TZDB). " +
                                "This has region IDs of the form `{area}/{city}`, such as `Europe/Paris` or `America/New_York`", false)
                        .build()).complete().addReaction(deleteEmoji).complete();
            }
            return;
        }

        OffsetDateTime timeStamp = (OffsetDateTime) result[3];

        message.reply(new EmbedBuilder()
                .setFooter("Time in your timezone: ")
                .setTimestamp(timeStamp)
                .setColor(utils.getColor())
                .build()).mentionRepliedUser(false).complete();
    }

    private Object[] time(String[] rawContentParts) {
        boolean returnValue = false;
        Object[] array = new Object[4];

        int index = -1;

        for(int i = 0; i < rawContentParts.length; i++)
        {
            String s = rawContentParts[i];
            if(!s.contains(":"))
            {
                returnValue = false;
            } else
            {
                String[] parts = s.split(":");
                if(parts.length < 2)
                {
                    returnValue = false;
                    continue;
                }

                try {
                    int firstPart = Integer.parseInt(parts[0]);
                    int secondPart = Integer.parseInt(parts[1]);

                    array[0] = firstPart;
                    array[1] = secondPart;

                    returnValue = !(firstPart < 1 || firstPart > 23 || secondPart < 0 || secondPart > 59);

                    index = i;

                    if(returnValue)
                        break;
                } catch (NumberFormatException e)
                {
                    returnValue = false;
                }
            }
        }

        if(index != -1)
        {
            if(index >= rawContentParts.length - 1) {
                returnValue = false;
                array[1] = true;
            }
            else
            {
                array[2] = rawContentParts[index + 1];

                if(!ZoneId.getAvailableZoneIds().contains((String) array[2]))
                {
                    array[0] = false;
                    array[1] = true;
                    return array;
                }

                array[3] = OffsetDateTime.now(ZoneId.of((String) array[2])).withHour((Integer) array[0]).withMinute((Integer) array[1]);
            }
        }

        array[0] = returnValue;
        return array;
    }
}
