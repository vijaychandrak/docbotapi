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
                    You are a document processing agent. When given the content of an uploaded file,
                    you extract key information, summarize it, and prepare it for further processing.
                    Respond with a structured summary of the document content.
                """)
                .description("Processes uploaded document content using Google Gemini")
                .build();
    }
}
