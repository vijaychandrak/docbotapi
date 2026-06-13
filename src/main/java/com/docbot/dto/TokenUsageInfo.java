package com.docbot.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO to track token consumption for a chat request/response.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenUsageInfo {
    
    private int userMessageTokens;
    private int documentTokens;
    private int systemPromptTokens;
    private int responseTokens;
    private int totalTokens;

    /**
     * Calculate total tokens from individual components.
     */
    public void calculateTotal() {
        this.totalTokens = userMessageTokens + documentTokens + systemPromptTokens + responseTokens;
    }

    @Override
    public String toString() {
        return String.format(
            "TokenUsage[userMsg=%d, doc=%d, system=%d, response=%d, total=%d]",
            userMessageTokens, documentTokens, systemPromptTokens, responseTokens, totalTokens
        );
    }
}
