package io.horrorshow.codey;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.horrorshow.codey.discordutil.CodeyConfig;
import io.horrorshow.codey.discordutil.DataStore;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.listener.RetryListenerSupport;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.client.RestTemplate;

import javax.security.auth.login.LoginException;

import java.time.Duration;
import java.util.concurrent.Executor;


@SpringBootApplication
@EnableAsync(proxyTargetClass = true)
@EnableRetry
@Slf4j
public class CodeyApplication {

    public static void main(String[] args) {
        SpringApplication.run(CodeyApplication.class, args);
    }


    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder.setReadTimeout(Duration.ofSeconds(20)).build();
    }


    @Bean
    public Executor asyncExecutor() {
        var executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(50);
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
    public DataStore messageStore() {
        return new DataStore();
    }


    @Bean
    public RetryTemplate retryTemplate() {
        var retryTemplate = new RetryTemplate();

        var fixedBackOffPolicy = new FixedBackOffPolicy();
        fixedBackOffPolicy.setBackOffPeriod(2000L);
        retryTemplate.setBackOffPolicy(fixedBackOffPolicy);

        var retryPolicy = new SimpleRetryPolicy();
        retryPolicy.setMaxAttempts(3);
        retryTemplate.setRetryPolicy(retryPolicy);

        retryTemplate.registerListener(new RetryListenerSupport() {
            @Override
            public <T, E extends Throwable> void onError(RetryContext context, RetryCallback<T, E> callback, Throwable throwable) {
                if (log.isDebugEnabled()) {
                    log.debug("Retry Count {}", context.getRetryCount(), throwable);
                }
                super.onError(context, callback, throwable);
            }
        });

        return retryTemplate;
    }
}
