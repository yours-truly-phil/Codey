package io.horrorshow.codey.api.github;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.horrorshow.codey.discordutil.CodeyConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.HmacAlgorithms;
import org.apache.commons.codec.digest.HmacUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;
import java.util.Map;


@Controller
@EnableAutoConfiguration
@Slf4j
public class GithubWebhookEndpoint {

    private static final String EOL = "\n";
    private static final int SIGNATURE_LENGTH = 71;

    private final HmacUtils hmacUtils;
    private final ObjectMapper objectMapper;


    @Autowired
    public GithubWebhookEndpoint(CodeyConfig codeyConfig, ObjectMapper objectMapper) {
        this.hmacUtils = new HmacUtils(HmacAlgorithms.HMAC_SHA_256, codeyConfig.getGithubWebhookSecret());
        this.objectMapper = objectMapper;
    }


    @RequestMapping("/")
    @ResponseBody
    public String home() {
        return "up and running!\n";
    }


    @RequestMapping(value = "/github-webhook", method = RequestMethod.POST)
    public ResponseEntity<String> handle(@RequestHeader Map<String, String> header, @RequestBody String payload) {
        if (log.isDebugEnabled()) {
            try {
                log.debug("header:\n{}\npayload:\n{}", header,
                        objectMapper.writerWithDefaultPrettyPrinter()
                                .writeValueAsString(objectMapper.readValue(payload, Map.class)));
            } catch (JsonProcessingException e) {
                log.error("error object mapper pretty print", e);
            }
        }
        var responseHeaders = new HttpHeaders();

        var signature = header.get("x-hub-signature-256");
        if (signature == null) {
            return new ResponseEntity<>("No signature given." + EOL, responseHeaders, HttpStatus.BAD_REQUEST);
        }

        var hash = String.format("sha256=%s", hmacUtils.hmacHex(payload));
        boolean invalidLength = signature.length() != SIGNATURE_LENGTH;

        if (invalidLength || !MessageDigest.isEqual(signature.getBytes(StandardCharsets.UTF_8), hash.getBytes(StandardCharsets.UTF_8))) {
            return new ResponseEntity<>("Invalid signature." + EOL, responseHeaders, HttpStatus.UNAUTHORIZED);
        }

        try {
            var event = header.get("x-github-event");
            if (event.equals("push")) {
                var push = objectMapper.readValue(payload, GithubPush.class);
                log.info("push:\n{}", push);
            } else if (event.equals("ping")) {
                var ping = objectMapper.readValue(payload, GithubPing.class);
                log.info("ping:\n{}", ping);
            } else {
                log.warn("unknown github event '{}'", event);
                var map = objectMapper.readValue(payload, Map.class);
                log.info("payload:\n{}", objectMapper.writerWithDefaultPrettyPrinter()
                        .writeValueAsString(map));
            }
        } catch (JsonProcessingException e) {
            log.error("unable to parse json from github-webhook", e);
        }

        int bytes = payload.getBytes(StandardCharsets.UTF_8).length;
        String message = "Signature OK." + EOL + String.format("Received %d bytes.", bytes) + EOL;
        return new ResponseEntity<>(message, responseHeaders, HttpStatus.OK);
    }


    static class GithubPing {
        @JsonProperty public String zen;
        @JsonProperty public String hook_id;
        @JsonProperty public Map<String, Object> hook;
        @JsonProperty public GithubRepository repository;
        @JsonProperty public GithubSender sender;
    }


    static class GithubCommit {

        @JsonProperty public String id;
        @JsonProperty public String timestamp;
        @JsonProperty public String message;
        @JsonProperty public GithubAuthor author;
        @JsonProperty public String url;
        @JsonProperty public boolean distinct;
        @JsonProperty public List<String> added;
        @JsonProperty public List<String> modified;
        @JsonProperty public List<String> removed;

    }

    static class GithubAuthor {

        @JsonProperty public String name;
        @JsonProperty public String email;
    }


    static class GithubPush {

        @JsonProperty public String ref;
        @JsonProperty public String before;
        @JsonProperty public String after;
        @JsonProperty public boolean created;
        @JsonProperty public boolean deleted;
        @JsonProperty public boolean forced;
        @JsonProperty public Object base_ref;
        @JsonProperty public String compare;
        @JsonProperty public List<GithubCommit> commits;
        @JsonProperty public String head_commit;
        @JsonProperty public GithubRepository repository;
        @JsonProperty public GithubPusher pusher;
        @JsonProperty public GithubSender sender;
    }

    static class GithubSender {

        @JsonProperty public String login;
        @JsonProperty public Long id;
        @JsonProperty public String node_id;
        @JsonProperty public String avatar_url;
        @JsonProperty public String gravatar_id;
        @JsonProperty public String url;
        @JsonProperty public String html_url;
        @JsonProperty public String followers_url;
        @JsonProperty public String following_url;
        @JsonProperty public String gists_url;
        @JsonProperty public String starred_url;
        @JsonProperty public String subscriptions_url;
        @JsonProperty public String organizations_url;
        @JsonProperty public String repos_url;
        @JsonProperty public String events_url;
        @JsonProperty public String received_events_url;
        @JsonProperty public String type;
        @JsonProperty public boolean site_admin;
    }

    static class GithubPusher {

        @JsonProperty public String name;
        @JsonProperty public String email;
    }

    static class GithubRepository {

        @JsonProperty public Long id;
        @JsonProperty public String node_id;
        @JsonProperty public String name;
        @JsonProperty public String full_name;
        @JsonProperty("private") public boolean _private;
        @JsonProperty public GithubOwner owner;
        @JsonProperty public String html_url;
        @JsonProperty public String description;
        @JsonProperty public boolean fork;
        @JsonProperty public String url;
        @JsonProperty public String forks_url;
        @JsonProperty public String keys_url;
        @JsonProperty public String collaborators_url;
        @JsonProperty public String teams_url;
        @JsonProperty public String hooks_url;
        @JsonProperty public String issue_events_url;
        @JsonProperty public String events_url;
        @JsonProperty public String assignees_url;
        @JsonProperty public String branches_url;
        @JsonProperty public String tags_url;
        @JsonProperty public String blobs_url;
        @JsonProperty public String git_tags_url;
        @JsonProperty public String git_refs_url;
        @JsonProperty public String trees_url;
        @JsonProperty public String statuses_url;
        @JsonProperty public String languages_url;
        @JsonProperty public String stargazers_url;
        @JsonProperty public String contributors_url;
        @JsonProperty public String subscribers_url;
        @JsonProperty public String subscription_url;
        @JsonProperty public String commits_url;
        @JsonProperty public String git_commits_url;
        @JsonProperty public String comments_url;
        @JsonProperty public String issue_comment_url;
        @JsonProperty public String contents_url;
        @JsonProperty public String compare_url;
        @JsonProperty public String merges_url;
        @JsonProperty public String archive_url;
        @JsonProperty public String downloads_url;
        @JsonProperty public String issues_url;
        @JsonProperty public String pulls_url;
        @JsonProperty public String milestones_url;
        @JsonProperty public String notifications_url;
        @JsonProperty public String labels_url;
        @JsonProperty public String releases_url;
        @JsonProperty public String deployments_url;
        @JsonProperty public String created_at;
        @JsonProperty public String updated_at;
        @JsonProperty public String pushed_at;
        @JsonProperty public String git_url;
        @JsonProperty public String ssh_url;
        @JsonProperty public String clone_url;
        @JsonProperty public String svn_url;
        @JsonProperty public String homepage;
        @JsonProperty public Integer size;
        @JsonProperty public Integer stargazers_count;
        @JsonProperty public Integer watchers_count;
        @JsonProperty public String language;
        @JsonProperty public boolean has_issues;
        @JsonProperty public boolean has_projects;
        @JsonProperty public boolean has_downloads;
        @JsonProperty public boolean has_wiki;
        @JsonProperty public boolean has_pages;
        @JsonProperty public Integer forks_count;
        @JsonProperty public String mirror_url;
        @JsonProperty public boolean archived;
        @JsonProperty public boolean disabled;
        @JsonProperty public Integer open_issues_count;
        @JsonProperty public Map<String, String> license;
        @JsonProperty public boolean allow_forking;
        @JsonProperty public Integer forks;
        @JsonProperty public Integer open_issues;
        @JsonProperty public Integer watchers;
        @JsonProperty public String default_branch;
        @JsonProperty public Integer stargazers;
        @JsonProperty public String master_branch;
    }

    static class GithubOwner {

        @JsonProperty public String name;
        @JsonProperty public String email;
        @JsonProperty public String login;
        @JsonProperty public Long id;
        @JsonProperty public String node_id;
        @JsonProperty public String avatar_url;
        @JsonProperty public String gravatar_id;
        @JsonProperty public String url;
        @JsonProperty public String html_url;
        @JsonProperty public String followers_url;
        @JsonProperty public String following_url;
        @JsonProperty public String gists_url;
        @JsonProperty public String starred_url;
        @JsonProperty public String subscriptions_url;
        @JsonProperty public String organizations_url;
        @JsonProperty public String repos_url;
        @JsonProperty public String events_url;
        @JsonProperty public String received_events_url;
        @JsonProperty public String type;
        @JsonProperty public boolean site_admin;
    }
}
