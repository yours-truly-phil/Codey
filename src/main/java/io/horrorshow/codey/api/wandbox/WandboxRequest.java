package io.horrorshow.codey.api.wandbox;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class WandboxRequest {
    private final String code;
    private final String compiler;
    private final String stdin;
    private final String runtime_option_raw;
}
