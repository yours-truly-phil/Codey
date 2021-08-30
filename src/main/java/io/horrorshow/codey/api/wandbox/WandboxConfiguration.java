package io.horrorshow.codey.api.wandbox;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Data
@Configuration
@ConfigurationProperties(prefix = "wandbox")
public class WandboxConfiguration {
    private String url = "https://wandbox.org/api/compile.json";
    private Map<String, String> compiler = new HashMap<>();
}
