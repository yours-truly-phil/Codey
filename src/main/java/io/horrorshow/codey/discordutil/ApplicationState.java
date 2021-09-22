package io.horrorshow.codey.discordutil;

import io.horrorshow.codey.api.github.GithubEventState;
import io.horrorshow.codey.data.repository.Repositories;
import io.horrorshow.codey.time.ReminderTask;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


@Service
@Slf4j
public class ApplicationState {

    @Getter
    private final CompilationCache compilationCache = new CompilationCache();

    @Getter
    private final Map<Long, ReminderTask> timerMap = new ConcurrentHashMap<>();

    @Getter
    private final GithubEventState githubEventState;

    @Getter
    private final ElevatedUsersState elevatedUsersState;

    @Getter
    /***
     * A hashmap that is not persisted in any way that is used to look up if the message should be deletable.
     *
     * It should not be a big issue, if a user reacts, and the message is not deleted.
     * However, vice-versa is not true. If a malicious user reacts, and the message is deleted - that is an issue.
     *
     * The fact that the hashmap is empty on restart does not matter too much.
     */
    private final ConcurrentHashMap<String,Boolean> deleteableMessageIds;


    @Autowired
    public ApplicationState(JDA jda, Repositories repositories) {
        this.githubEventState = new GithubEventState(jda, repositories.getGithubChannelRepository());
        this.elevatedUsersState = new ElevatedUsersState(repositories.getElevatedUserRepository());
        this.deleteableMessageIds = new ConcurrentHashMap<>();
    }


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
