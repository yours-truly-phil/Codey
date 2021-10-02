package io.horrorshow.codey.api.github;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.horrorshow.codey.api.github.model.GithubApiTypes;
import io.horrorshow.codey.discordutil.CodeyConfig;
import io.horrorshow.codey.util.CodeyTask;
import io.horrorshow.codey.util.TaskInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.HmacAlgorithms;
import org.apache.commons.codec.digest.HmacUtils;
import org.jetbrains.annotations.Nullable;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@Controller
@EnableAutoConfiguration
@Slf4j
public class GithubWebhookEndpoint {

    private static final String EOL = "\n";
    private static final int SIGNATURE_LENGTH = 71;

    private final List<HmacUtils> hmacUtils = new ArrayList<>();
    private final ObjectMapper objectMapper;
    private final GithubEventBot githubEventBot;
    private final boolean devMode;


    @Autowired
    public GithubWebhookEndpoint(CodeyConfig codeyConfig, ObjectMapper objectMapper, GithubEventBot githubEventBot) {
        for (var secret : codeyConfig.getGithubWebhookSecrets()) {
            hmacUtils.add(new HmacUtils(HmacAlgorithms.HMAC_SHA_256, secret));
        }
        if (log.isDebugEnabled()) {
            log.debug("loaded " + hmacUtils.size() + " secrets");
        }
        this.objectMapper = objectMapper;
        this.githubEventBot = githubEventBot;
        this.devMode = codeyConfig.isDevMode();
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
        if (!devMode) {
            ResponseEntity<String> err = validateSignature(header, payload, responseHeaders);
            if (err != null) {
                return err;
            }
        }

        try {
            var event = header.get("x-github-event");
            switch (event) {
                case "push" -> {
                    var push = objectMapper.readValue(payload, GithubApiTypes.Push.class);
                    CodeyTask.runAsync(() -> githubEventBot.onPush(push), TaskInfo.empty());
                }
                case "ping" -> {
                    var ping = objectMapper.readValue(payload, GithubApiTypes.Ping.class);
                    if (log.isTraceEnabled()) {
                        log.trace("ping:\n{}", ping);
                    }
                }
                case "page_build" -> {
                    var pageBuild = objectMapper.readValue(payload, GithubApiTypes.PageBuild.class);
                    if (log.isTraceEnabled()) {
                        log.trace("page_build:\n{}", pageBuild);
                    }
                }
                case "workflow_job" -> {
                    var workflowJob = objectMapper.readValue(payload, GithubApiTypes.WorkflowPayload.class);
                    if (log.isTraceEnabled()) {
                        log.trace("workflow_job:\n{}", workflowJob);
                    }
                }
                case "workflow_run" -> printDefault("workflow_run", payload);
                case "check_run" -> printDefault("check_run", payload);
                case "status" -> printDefault("status", payload);
                case "check_suite" -> printDefault("check_suite", payload);
                case "deployment" -> printDefault("deployment", payload);
                case "deployment_status" -> printDefault("deployment_status", payload);
                default -> {
                    log.warn("unknown github event '{}'", event);
                    var map = objectMapper.readValue(payload, Map.class);
                    if (log.isTraceEnabled()) {
                        log.trace("payload:\n{}", objectMapper.writerWithDefaultPrettyPrinter()
                                .writeValueAsString(map));
                    }
                }
            }
        } catch (JsonProcessingException e) {
            log.error("unable to parse json from github-webhook", e);
        }

        int bytes = payload.getBytes(StandardCharsets.UTF_8).length;
        String message = "Signature OK." + EOL + String.format("Received %d bytes.", bytes) + EOL;
        return new ResponseEntity<>(message, responseHeaders, HttpStatus.OK);
    }


    @Nullable
    private ResponseEntity<String> validateSignature(Map<String, String> header, String payload, HttpHeaders responseHeaders) {

        var signature = header.get("x-hub-signature-256");
        if (signature == null) {
            log.warn("no signature, headers={}", header);
            return new ResponseEntity<>("No signature given." + EOL, responseHeaders, HttpStatus.BAD_REQUEST);
        }

        boolean invalidLength = signature.length() != SIGNATURE_LENGTH;
        if (invalidLength) {
            log.warn("invalid signature length, signature={}", signature);
            return new ResponseEntity<>("Invalid signature." + EOL, responseHeaders, HttpStatus.UNAUTHORIZED);
        }

        boolean hashOk = hmacUtils.stream().anyMatch(hmac -> {
            var hash = String.format("sha256=%s", hmac.hmacHex(payload));
            return MessageDigest.isEqual(signature.getBytes(StandardCharsets.UTF_8), hash.getBytes(StandardCharsets.UTF_8));
        });
        if (!hashOk) {
            log.warn("wrong secret signature={}", signature);
            return new ResponseEntity<>("Invalid signature." + EOL, responseHeaders, HttpStatus.UNAUTHORIZED);
        }
        return null;
    }


    private void printDefault(String job, String payload) throws JsonProcessingException {
        var obj = objectMapper.readValue(payload, Map.class);
        log.info("{}}:\n{}", job, objectMapper.writerWithDefaultPrettyPrinter()
                .writeValueAsString(obj));
    }
}
