package io.horrorshow.codey.api.piston;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;


@Data
@Configuration
@ConfigurationProperties(prefix = "piston")
public class PistonConfiguration {

    private String currentApi = "emkc";
    private final Map<String, PistonUrl> apis = new HashMap<>();

    @ConstructorBinding
    public record PistonUrl(String runtimes, String execute) {

    }
}
