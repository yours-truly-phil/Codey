package io.horrorshow.codey.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class WandboxResponse {
    private String program_message;
    private String program_output;
    private String status;
    private String compiler_error;
    private String compiler_message;
}
