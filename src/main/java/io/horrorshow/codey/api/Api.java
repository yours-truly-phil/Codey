package io.horrorshow.codey.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;


@Service
@Slf4j
public class Api {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;


    public Api(@Autowired RestTemplate restTemplate, @Autowired ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }


    public Object getRequest(String url) {
        return restTemplate.getForObject(url, Object.class);
    }


    public String prettyPrintJson(Object json) throws JsonProcessingException {
        return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(json);
    }
}
