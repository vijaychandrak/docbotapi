package com.docbot.service;

import com.google.adk.agents.LlmAgent;
import com.google.adk.runner.Runner;
import com.google.adk.sessions.InMemorySessionService;
import com.google.adk.sessions.Session;
import com.google.adk.artifacts.InMemoryArtifactService;
import com.google.genai.types.Content;
import com.google.genai.types.Part;
import io.reactivex.rxjava3.core.Flowable;
import com.google.adk.events.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class GoogleAdkService {

    private static final Logger log = LoggerFactory.getLogger(GoogleAdkService.class);

    private final LlmAgent documentProcessingAgent;
    private final InMemorySessionService sessionService;
    private final InMemoryArtifactService artifactService;
    private final Runner runner;

    public GoogleAdkService(LlmAgent documentProcessingAgent) {
        this.documentProcessingAgent = documentProcessingAgent;
        this.sessionService = new InMemorySessionService();
        this.artifactService = new InMemoryArtifactService();
        this.runner = new Runner(documentProcessingAgent, "docbot-app", artifactService, sessionService);
    }

    /**
     * Process the extracted file content through Google ADK agent.
     * Returns the agent's structured response/summary.
     */
    public String processDocumentContent(String fileId, String extractedText) {
        try {
            // Create a session for this file processing
            Session session = sessionService
                    .createSession("docbot-app", fileId)
                    .blockingGet();

            // Build user message with the extracted text
            Content userMessage = Content.fromParts(
                    Part.fromText(extractedText)
            );

            // Run the agent and collect response
            Flowable<Event> events = runner.runAsync(
                    session.userId(),
                    session.id(),
                    userMessage
            );

            StringBuilder response = new StringBuilder();
            events.blockingForEach(event -> {
                if (event.finalResponse()) {
                    event.content().ifPresent(content -> {
                        if (content.parts().isPresent()) {
                            for (Part part : content.parts().get()) {
                                if (part.text().isPresent()) {
                                    response.append(part.text().get());
                                }
                            }
                        }
                    });
                }
            });

            log.info("ADK processed document for fileId={}, responseLength={}", fileId, response.length());
            return response.toString();

        } catch (Exception e) {
            log.error("Google ADK processing failed for fileId={}: {}", fileId, e.getMessage(), e);
            return "ADK processing unavailable: " + e.getMessage();
        }
    }
}
