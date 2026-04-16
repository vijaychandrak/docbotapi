package com.docbot.config;

import org.springframework.context.annotation.Configuration;

/**
 * CORS is not needed in development (Angular proxy handles it)
 * or in production (reverse proxy like nginx serves both from same origin).
 */
@Configuration
public class CorsConfig {
}
