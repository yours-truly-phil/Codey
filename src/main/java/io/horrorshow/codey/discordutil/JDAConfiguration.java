package io.horrorshow.codey.discordutil;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import javax.validation.constraints.NotBlank;

@Data
@Configuration
@ConfigurationProperties(prefix = "jda")
public class JDAConfiguration {

    @NotBlank
    private String token;
}
