package com.ravi.lifeassistant.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import jakarta.annotation.PostConstruct;
import org.springframework.web.client.RestTemplate;

@Configuration
public class OpenAIConfig {

    @Value("${openai.api.url}")
    private String apiUrl;

    @Value("${openai.api.key}")
    private String apiKey; 
    
//this is required for real api, server api
   /* @Bean
    public WebClient webClient() {
        return WebClient.builder()
                .baseUrl(apiUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }*/
    
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
    public String getApiUrl() {
        return apiUrl;
    }
    /* this is also required only for server api
    @PostConstruct
    public void testKey() {
        System.out.println("OpenAI key loaded: " + (apiKey != null));
    }*/
}
