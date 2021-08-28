package io.horrorshow.codey.challenge;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;


@Data
@Configuration
@ConfigurationProperties(prefix = "challenge")
public class ChallengeConfiguration {

    private List<String> paths = new ArrayList<>();
}
