package com.docbot.config;

import com.google.adk.agents.LlmAgent;
import com.google.adk.models.Gemini;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GoogleAdkConfig {

    @Value("${google.adk.model}")
    private String modelName;

    @Value("${google.adk.api-key}")
    private String apiKey;

    @Bean
    public LlmAgent documentProcessingAgent() {
        return LlmAgent.builder()
                .name("document_processor")
                .model(Gemini.builder().modelName(modelName).apiKey(apiKey).build())
                .instruction("""
                    You are a helpful document assistant. You are given the content of an uploaded document
                    and a user's question about it. Read through the document content carefully and answer
                    the user's question in plain, natural language. Do not return JSON or structured data
                    unless the user specifically asks for it.
                """)
                .description("Processes uploaded document content using Google Gemini")
                .build();
    }
}
