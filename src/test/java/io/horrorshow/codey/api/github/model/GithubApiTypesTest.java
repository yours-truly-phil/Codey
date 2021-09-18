package io.horrorshow.codey.api.github.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.util.IOUtils;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;


class GithubApiTypesTest {

    ObjectMapper objectMapper = new ObjectMapper();
    private static final String WEBHOOK_SAMPLES_DIR = "/github-webhook-samples";


    private String getGitHubSampleFile(String file) {
        return IOUtils.toString(this.getClass()
                .getResourceAsStream("%s/%s".formatted(WEBHOOK_SAMPLES_DIR, file)), StandardCharsets.UTF_8);
    }


    private <T> void deserialize(String json, Class<T> clazz) throws JsonProcessingException {
        var payload = getGitHubSampleFile(json);
        objectMapper.readValue(payload, clazz);
    }


    @Test
    void deserialize_sample_push_payload() throws JsonProcessingException {
        deserialize("push.json", GithubApiTypes.Push.class);
    }


    @Test
    void deserialize_sample_check_run_payload() throws JsonProcessingException {
        deserialize("check_run.json", GithubApiTypes.CheckRunPayload.class);
    }

    @Test
    void deserialize_workflow_run_payload() throws JsonProcessingException {
        deserialize("workflow_run.json", GithubApiTypes.WorkflowRunPayload.class);
    }

    @Test
    void deserialize_check_suite_payload() throws JsonProcessingException {
        deserialize("check_suite.json", GithubApiTypes.CheckSuitePayload.class);
    }
}
