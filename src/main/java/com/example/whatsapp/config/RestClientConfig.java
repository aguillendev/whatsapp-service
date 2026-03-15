package com.example.whatsapp.config;

import org.springframework.beans.factory.annotation.Value;
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
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);

        return RestClient.builder()
                .requestFactory(requestFactory)
                .baseUrl("https://graph.facebook.com/" + properties.apiVersion())
                .defaultHeader("Authorization", "Bearer " + properties.accessToken())
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    @Bean
    public RestClient mcpRestClient(@Value("${mcp-client.url}") String mcpClientUrl) {
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30)) // el LLM puede tardar
                .build();
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);

        return RestClient.builder()
                .requestFactory(requestFactory)
                .baseUrl(mcpClientUrl)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }
}