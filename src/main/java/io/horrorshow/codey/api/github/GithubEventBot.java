package io.horrorshow.codey.api.github;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.horrorshow.codey.discordutil.DataStore;
import io.horrorshow.codey.discordutil.DiscordUtils;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;


@Service
@Slf4j
public class GithubEventBot extends ListenerAdapter {

    private final DiscordUtils discordUtils;
    private final DataStore.GithubEventChannels githubEventChannels;


    @Autowired
    public GithubEventBot(JDA jda, DiscordUtils discordUtils, DataStore dataStore) {
        this.discordUtils = discordUtils;
        this.githubEventChannels = dataStore.getGithubEventChannels();

        jda.addEventListener(this);
    }


    @Override
    public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent event) {
        if (!event.getAuthor().isBot()) {
            if (discordUtils.isElevatedMember(event.getMember()) && "!test-push".equals(event.getMessage().getContentDisplay())) {
                var objectMapper = new ObjectMapper();
                try {
                    var pushEvent = objectMapper.readValue(samplePush, GithubWebhookEndpoint.GithubPush.class);
                    onPush(pushEvent);
                } catch (JsonProcessingException e) {
                    log.error("problem deserializing sample push", e);
                }
            }
        }
    }


    @Async
    public void onPush(GithubWebhookEndpoint.GithubPush event) {
        log.info("onPush:\n{}", event);
        var embed = new EmbedBuilder()
                .setTimestamp(Instant.ofEpochSecond(Long.parseLong(event.repository.pushed_at)))
                .setThumbnail(event.sender.avatar_url)
                .setTitle("Github push by " + event.pusher.name)
                .addField("Repo", String.format("[%s](%s)", event.repository.name, event.repository.url), true)
                .addField("Pusher", event.pusher.name, true)
                .addField("Commits", event.commits.stream()
                        .map(this::formatCommit)
                        .collect(Collectors.joining("\n")), false)
                .addField("Repository language", event.repository.language, true)
                .addField("Repository Owner", event.repository.owner.name, true)
                .setFooter(String.format("git clone %s", event.repository.clone_url))
                .build();

        CompletableFuture.allOf(
                githubEventChannels.values().stream()
                        .map(channelInfo -> {
                            var channel = channelInfo.channel();
                            var textChannel = channel.getJDA().getTextChannelById(channel.getId());
                            if (textChannel != null) {
                                return discordUtils.sendRemovableEmbed(embed, textChannel);
                            }
                            return null;
                        }).toArray(CompletableFuture[]::new)
        ).exceptionally(e -> {
            log.error("error sending github update to text channel", e);
            return null;
        });
    }


    private String formatCommit(GithubWebhookEndpoint.GithubCommit commit) {
        return String.format("[%s](%s) (%d files)", commit.message, commit.url, commit.modified.size());
    }


    private static final String samplePush = """
            {
              "ref" : "refs/heads/master",
              "before" : "8a1c6e3fb6710ed0705c3aff49b69f50b17e6d65",
              "after" : "5dda454e709a6c2be5e06e124643ce39a28d7316",
              "repository" : {
                "id" : 314687386,
                "node_id" : "MDEwOlJlcG9zaXRvcnkzMTQ2ODczODY=",
                "name" : "Codey",
                "full_name" : "yours-truly-phil/Codey",
                "private" : false,
                "owner" : {
                  "name" : "yours-truly-phil",
                  "email" : "philippbseeger@googlemail.com",
                  "login" : "yours-truly-phil",
                  "id" : 26274860,
                  "node_id" : "MDQ6VXNlcjI2Mjc0ODYw",
                  "avatar_url" : "https://avatars.githubusercontent.com/u/26274860?v=4",
                  "gravatar_id" : "",
                  "url" : "https://api.github.com/users/yours-truly-phil",
                  "html_url" : "https://github.com/yours-truly-phil",
                  "followers_url" : "https://api.github.com/users/yours-truly-phil/followers",
                  "following_url" : "https://api.github.com/users/yours-truly-phil/following{/other_user}",
                  "gists_url" : "https://api.github.com/users/yours-truly-phil/gists{/gist_id}",
                  "starred_url" : "https://api.github.com/users/yours-truly-phil/starred{/owner}{/repo}",
                  "subscriptions_url" : "https://api.github.com/users/yours-truly-phil/subscriptions",
                  "organizations_url" : "https://api.github.com/users/yours-truly-phil/orgs",
                  "repos_url" : "https://api.github.com/users/yours-truly-phil/repos",
                  "events_url" : "https://api.github.com/users/yours-truly-phil/events{/privacy}",
                  "received_events_url" : "https://api.github.com/users/yours-truly-phil/received_events",
                  "type" : "User",
                  "site_admin" : false
                },
                "html_url" : "https://github.com/yours-truly-phil/Codey",
                "description" : "Discord bot to run competitive programming challenges, apply code formatting fixes, compile and run code, all without leaving discord and just a single click away.",
                "fork" : false,
                "url" : "https://github.com/yours-truly-phil/Codey",
                "forks_url" : "https://api.github.com/repos/yours-truly-phil/Codey/forks",
                "keys_url" : "https://api.github.com/repos/yours-truly-phil/Codey/keys{/key_id}",
                "collaborators_url" : "https://api.github.com/repos/yours-truly-phil/Codey/collaborators{/collaborator}",
                "teams_url" : "https://api.github.com/repos/yours-truly-phil/Codey/teams",
                "hooks_url" : "https://api.github.com/repos/yours-truly-phil/Codey/hooks",
                "issue_events_url" : "https://api.github.com/repos/yours-truly-phil/Codey/issues/events{/number}",
                "events_url" : "https://api.github.com/repos/yours-truly-phil/Codey/events",
                "assignees_url" : "https://api.github.com/repos/yours-truly-phil/Codey/assignees{/user}",
                "branches_url" : "https://api.github.com/repos/yours-truly-phil/Codey/branches{/branch}",
                "tags_url" : "https://api.github.com/repos/yours-truly-phil/Codey/tags",
                "blobs_url" : "https://api.github.com/repos/yours-truly-phil/Codey/git/blobs{/sha}",
                "git_tags_url" : "https://api.github.com/repos/yours-truly-phil/Codey/git/tags{/sha}",
                "git_refs_url" : "https://api.github.com/repos/yours-truly-phil/Codey/git/refs{/sha}",
                "trees_url" : "https://api.github.com/repos/yours-truly-phil/Codey/git/trees{/sha}",
                "statuses_url" : "https://api.github.com/repos/yours-truly-phil/Codey/statuses/{sha}",
                "languages_url" : "https://api.github.com/repos/yours-truly-phil/Codey/languages",
                "stargazers_url" : "https://api.github.com/repos/yours-truly-phil/Codey/stargazers",
                "contributors_url" : "https://api.github.com/repos/yours-truly-phil/Codey/contributors",
                "subscribers_url" : "https://api.github.com/repos/yours-truly-phil/Codey/subscribers",
                "subscription_url" : "https://api.github.com/repos/yours-truly-phil/Codey/subscription",
                "commits_url" : "https://api.github.com/repos/yours-truly-phil/Codey/commits{/sha}",
                "git_commits_url" : "https://api.github.com/repos/yours-truly-phil/Codey/git/commits{/sha}",
                "comments_url" : "https://api.github.com/repos/yours-truly-phil/Codey/comments{/number}",
                "issue_comment_url" : "https://api.github.com/repos/yours-truly-phil/Codey/issues/comments{/number}",
                "contents_url" : "https://api.github.com/repos/yours-truly-phil/Codey/contents/{+path}",
                "compare_url" : "https://api.github.com/repos/yours-truly-phil/Codey/compare/{base}...{head}",
                "merges_url" : "https://api.github.com/repos/yours-truly-phil/Codey/merges",
                "archive_url" : "https://api.github.com/repos/yours-truly-phil/Codey/{archive_format}{/ref}",
                "downloads_url" : "https://api.github.com/repos/yours-truly-phil/Codey/downloads",
                "issues_url" : "https://api.github.com/repos/yours-truly-phil/Codey/issues{/number}",
                "pulls_url" : "https://api.github.com/repos/yours-truly-phil/Codey/pulls{/number}",
                "milestones_url" : "https://api.github.com/repos/yours-truly-phil/Codey/milestones{/number}",
                "notifications_url" : "https://api.github.com/repos/yours-truly-phil/Codey/notifications{?since,all,participating}",
                "labels_url" : "https://api.github.com/repos/yours-truly-phil/Codey/labels{/name}",
                "releases_url" : "https://api.github.com/repos/yours-truly-phil/Codey/releases{/id}",
                "deployments_url" : "https://api.github.com/repos/yours-truly-phil/Codey/deployments",
                "created_at" : 1605912418,
                "updated_at" : "2021-09-17T14:12:17Z",
                "pushed_at" : 1631888184,
                "git_url" : "git://github.com/yours-truly-phil/Codey.git",
                "ssh_url" : "git@github.com:yours-truly-phil/Codey.git",
                "clone_url" : "https://github.com/yours-truly-phil/Codey.git",
                "svn_url" : "https://github.com/yours-truly-phil/Codey",
                "homepage" : "",
                "size" : 16981,
                "stargazers_count" : 6,
                "watchers_count" : 6,
                "language" : "Java",
                "has_issues" : true,
                "has_projects" : true,
                "has_downloads" : true,
                "has_wiki" : true,
                "has_pages" : true,
                "forks_count" : 2,
                "mirror_url" : null,
                "archived" : false,
                "disabled" : false,
                "open_issues_count" : 0,
                "license" : {
                  "key" : "gpl-3.0",
                  "name" : "GNU General Public License v3.0",
                  "spdx_id" : "GPL-3.0",
                  "url" : "https://api.github.com/licenses/gpl-3.0",
                  "node_id" : "MDc6TGljZW5zZTk="
                },
                "allow_forking" : true,
                "forks" : 2,
                "open_issues" : 0,
                "watchers" : 6,
                "default_branch" : "master",
                "stargazers" : 6,
                "master_branch" : "master"
              },
              "pusher" : {
                "name" : "yours-truly-phil",
                "email" : "philippbseeger@googlemail.com"
              },
              "sender" : {
                "login" : "yours-truly-phil",
                "id" : 26274860,
                "node_id" : "MDQ6VXNlcjI2Mjc0ODYw",
                "avatar_url" : "https://avatars.githubusercontent.com/u/26274860?v=4",
                "gravatar_id" : "",
                "url" : "https://api.github.com/users/yours-truly-phil",
                "html_url" : "https://github.com/yours-truly-phil",
                "followers_url" : "https://api.github.com/users/yours-truly-phil/followers",
                "following_url" : "https://api.github.com/users/yours-truly-phil/following{/other_user}",
                "gists_url" : "https://api.github.com/users/yours-truly-phil/gists{/gist_id}",
                "starred_url" : "https://api.github.com/users/yours-truly-phil/starred{/owner}{/repo}",
                "subscriptions_url" : "https://api.github.com/users/yours-truly-phil/subscriptions",
                "organizations_url" : "https://api.github.com/users/yours-truly-phil/orgs",
                "repos_url" : "https://api.github.com/users/yours-truly-phil/repos",
                "events_url" : "https://api.github.com/users/yours-truly-phil/events{/privacy}",
                "received_events_url" : "https://api.github.com/users/yours-truly-phil/received_events",
                "type" : "User",
                "site_admin" : false
              },
              "created" : false,
              "deleted" : false,
              "forced" : false,
              "base_ref" : null,
              "compare" : "https://github.com/yours-truly-phil/Codey/compare/8a1c6e3fb671...5dda454e709a",
              "commits" : [ {
                "id" : "5dda454e709a6c2be5e06e124643ce39a28d7316",
                "tree_id" : "185f2f10014ba523a14592b4fd927279b1a002b6",
                "distinct" : true,
                "message" : "Just gnerating another push",
                "timestamp" : "2021-09-17T16:16:21+02:00",
                "url" : "https://github.com/yours-truly-phil/Codey/commit/5dda454e709a6c2be5e06e124643ce39a28d7316",
                "author" : {
                  "name" : "Philipp Seeger",
                  "email" : "pseeger@k15t.com",
                  "username" : "yours-truly-phil"
                },
                "committer" : {
                  "name" : "Philipp Seeger",
                  "email" : "pseeger@k15t.com",
                  "username" : "yours-truly-phil"
                },
                "added" : [ ],
                "removed" : [ ],
                "modified" : [ "src/main/java/io/horrorshow/codey/api/github/GithubDiscordActions.java", "src/main/java/io/horrorshow/codey/api/github/GithubEventBot.java" ]
              } ],
              "head_commit" : {
                "id" : "5dda454e709a6c2be5e06e124643ce39a28d7316",
                "tree_id" : "185f2f10014ba523a14592b4fd927279b1a002b6",
                "distinct" : true,
                "message" : "Just gnerating another push",
                "timestamp" : "2021-09-17T16:16:21+02:00",
                "url" : "https://github.com/yours-truly-phil/Codey/commit/5dda454e709a6c2be5e06e124643ce39a28d7316",
                "author" : {
                  "name" : "Philipp Seeger",
                  "email" : "pseeger@k15t.com",
                  "username" : "yours-truly-phil"
                },
                "committer" : {
                  "name" : "Philipp Seeger",
                  "email" : "pseeger@k15t.com",
                  "username" : "yours-truly-phil"
                },
                "added" : [ ],
                "removed" : [ ],
                "modified" : [ "src/main/java/io/horrorshow/codey/api/github/GithubDiscordActions.java", "src/main/java/io/horrorshow/codey/api/github/GithubEventBot.java" ]
              }
            }""";

}
