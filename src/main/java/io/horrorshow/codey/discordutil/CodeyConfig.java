package io.horrorshow.codey.discordutil;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import javax.validation.constraints.NotBlank;

import java.util.ArrayList;
import java.util.List;


@Data
@Configuration
@ConfigurationProperties(prefix = "codey")
public class CodeyConfig {

    @NotBlank
    private String token;
    private String githubWebhookSecret;
    private String embedColor;
    private List<String> roles = new ArrayList<>();
}
