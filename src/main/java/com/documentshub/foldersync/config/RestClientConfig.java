package com.documentshub.foldersync.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Bean
    RestClient.Builder restClientBuilder() {
        return RestClient.builder()
            .requestInterceptor((request, body, execution) -> {
                return execution.execute(request, body);
            });
    }
}