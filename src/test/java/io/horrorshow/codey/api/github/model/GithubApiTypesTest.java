package io.horrorshow.codey.api.github.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;


class GithubApiTypesTest {

    ObjectMapper objectMapper = new ObjectMapper();
    private static final String WEBHOOK_SAMPLES_DIR = "/github-webhook-samples";


    private String getGitHubSampleFile(String file) throws IOException {
        var path = Paths.get("src", "test", "resources", "%s/%s".formatted(WEBHOOK_SAMPLES_DIR, file));
        return Files.readString(path);
    }


    private <T> void deserialize(String json, Class<T> clazz) throws IOException {
        var payload = getGitHubSampleFile(json);
        objectMapper.readValue(payload, clazz);
    }


    @Test
    void deserialize_sample_push_payload() throws IOException {
        deserialize("push.json", GithubApiTypes.Push.class);
    }


    @Test
    void deserialize_new_sample_push_payload_format() throws IOException {
        deserialize("push_v2.json", GithubApiTypes.Push.class);
    }


    @Test
    void deserialize_sample_check_run_payload() throws IOException {
        deserialize("check_run.json", GithubApiTypes.CheckRunPayload.class);
    }


    @Test
    void deserialize_workflow_run_payload() throws IOException {
        deserialize("workflow_run.json", GithubApiTypes.WorkflowRunPayload.class);
    }


    @Test
    void deserialize_check_suite_payload() throws IOException {
        deserialize("check_suite.json", GithubApiTypes.CheckSuitePayload.class);
    }
}
