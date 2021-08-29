package io.horrorshow.codey;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.horrorshow.codey.discordutil.CodeyConfig;
import io.horrorshow.codey.discordutil.MessageStore;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.client.RestTemplate;

import javax.security.auth.login.LoginException;

import java.util.concurrent.Executor;


@SpringBootApplication
@EnableAsync(proxyTargetClass = true)
public class CodeyApplication {

    public static void main(String[] args) {
        SpringApplication.run(CodeyApplication.class, args);
    }


    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder.build();
    }


    @Bean
    public Executor asyncExecutor() {
        var executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("Thread-");
        executor.initialize();
        return executor;
    }


    @Bean
    public JDA jda(@Autowired CodeyConfig configuration) throws LoginException {
        JDA jda = JDABuilder.createDefault(configuration.getToken()).build();
        jda.setAutoReconnect(true);
        return jda;
    }


    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }


    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
    public MessageStore messageStore() {
        return new MessageStore();
    }
}
