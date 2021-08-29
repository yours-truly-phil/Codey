package io.horrorshow.codey.discordutil;

import lombok.Getter;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class MessageStore {

    @Getter
    private final CompilationCache compilationCache = new CompilationCache();

    public static class CompilationCache {

        private final Map<String, Map<String, List<String>>> compilationResult = new ConcurrentHashMap<>();


        public boolean hasResult(@NotNull Message message) {
            return compilationResult.containsKey(message.getGuild().getId())
                   && compilationResult.get(message.getGuild().getId()).containsKey(message.getId());
        }


        public void cache(@NotNull Message message, @NotNull List<String> content) {
            compilationResult.computeIfAbsent(message.getGuild().getId(), guild -> new ConcurrentHashMap<>())
                    .put(message.getId(), content);
        }


        public List<String> get(@NotNull Message message) {
            return compilationResult.get(message.getGuild().getId()).get(message.getId());
        }


        public int countByGuild(@NotNull Guild guild) {
            if (!compilationResult.containsKey(guild.getId())) {
                return 0;
            } else {
                return compilationResult.get(guild.getId()).size();
            }
        }


        public void clearByGuild(@NotNull Guild guild) {
            if (compilationResult.containsKey(guild.getId())) {
                compilationResult.get(guild.getId()).clear();
            }
        }
    }
}
