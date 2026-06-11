package com.example.demo1.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class SourceEmployeeClient {

    private final WebClient webClient;
    private final String sourceUrl;

    public SourceEmployeeClient(@Value("${employee.source.url:}") String sourceUrl) {
        this.webClient = WebClient.builder().build();
        this.sourceUrl = sourceUrl;
    }

    public String fetchEmployeeCsv() {
        if (sourceUrl == null || sourceUrl.isBlank()) {
            throw new IllegalStateException("Configure employee.source.url before importing from source.");
        }

        return webClient.get()
                .uri(sourceUrl)
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }
}
