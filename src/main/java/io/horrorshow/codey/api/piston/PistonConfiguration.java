package io.horrorshow.codey.api.piston;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;


@Data
@Configuration
@ConfigurationProperties(prefix = "piston")
public class PistonConfiguration {

    private boolean useLocal = false;

    private String runtimesUrl = "https://emkc.org/api/v2/piston/runtimes";
    private String executeUrl = "https://emkc.org/api/v2/piston/execute";

    private String localRuntimesUrl = "http://localhost:2000/api/v2/runtimes";
    private String localExecuteUrl = "http://localhost:2000/api/v2/execute";
}
