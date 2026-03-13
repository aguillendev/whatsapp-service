package com.example.whatsapp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.time.Duration;

@Configuration
public class RestClientConfig {

    @Bean
    public RestClient whatsappRestClient(WhatsappProperties properties) {
        // Use Java 21's HttpClient which plays nicely with virtual threads
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();

        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        // By default, JdkClientHttpRequestFactory doesn't impose aggressive connection limits
        // making it a great fit for virtual threads.

        return RestClient.builder()
                .requestFactory(requestFactory)
                .baseUrl("https://graph.facebook.com/" + properties.apiVersion())
                .defaultHeader("Authorization", "Bearer " + properties.accessToken())
                .defaultHeader("Content-Type", "application/json")
                .build();
    }
}