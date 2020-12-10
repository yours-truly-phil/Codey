package io.horrorshow.codey.challenge;

import io.horrorshow.codey.api.WandboxApi;
import io.horrorshow.codey.api.WandboxResponse;
import io.horrorshow.codey.challenge.xml.Problem;
import io.horrorshow.codey.challenge.xml.TestCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class TestRunner {
    private final WandboxApi wandboxApi;

    public TestRunner(@Autowired WandboxApi wandboxApi) {
        this.wandboxApi = wandboxApi;
    }

    public Map<TestCase, Optional<WandboxResponse>> runTests(Problem problem, String code, String lang) {
        Map<TestCase, Optional<WandboxResponse>> res = new HashMap<>();
        var caseList = problem.getTestcases().getTestcase();
        for (var test : caseList) {
            try {
                var wandboxResult = wandboxApi.compile(code, lang, test.getInput(), "");
                res.put(test, Optional.of(wandboxResult));
            } catch (RestClientException e) {
                res.put(test, Optional.empty());
            }
        }
        return res;
    }
}
