package com.docbot.controller;

import com.docbot.model.UploadedFile;
import com.docbot.service.FileStorageService;
import com.docbot.service.GoogleAdkService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private static final Logger log = LoggerFactory.getLogger(ChatController.class);

    private final FileStorageService fileStorageService;
    private final GoogleAdkService googleAdkService;

    // Simple in-memory chat history: fileId -> list of messages
    private final Map<String, List<Map<String, Object>>> chatHistory = new ConcurrentHashMap<>();

    public ChatController(FileStorageService fileStorageService, GoogleAdkService googleAdkService) {
        this.fileStorageService = fileStorageService;
        this.googleAdkService = googleAdkService;
    }

    @PostMapping
    public ResponseEntity<?> chat(@RequestBody Map<String, String> request) {
        String fileId = request.get("fileId");
        String message = request.get("message");

        if (fileId == null || fileId.isBlank() || message == null || message.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "fileId and message are required"));
        }

        UploadedFile file = fileStorageService.getFileById(fileId);
        if (file == null) {
            return ResponseEntity.notFound().build();
        }

        // Build concise prompt to reduce token usage
        String contextPrompt = "Document:\n" + file.getExtractedText() + "\n\nQ: " + message;
        log.info("Context prompt: {}", contextPrompt);

        String adkResponse = googleAdkService.processDocumentContent(fileId, contextPrompt);
        log.info("Chat fileId={}, responseLength={}", fileId, adkResponse.length());

        // Store in chat history
        chatHistory.computeIfAbsent(fileId, k -> new ArrayList<>());
        chatHistory.get(fileId).add(Map.of(
                "sender", "user",
                "message", message,
                "timestamp", Instant.now().toString()
        ));
        chatHistory.get(fileId).add(Map.of(
                "sender", "bot",
                "message", adkResponse,
                "timestamp", Instant.now().toString()
        ));

        log.info("Chat for fileId={}: userMsg length={}, response length={}",
                fileId, message.length(), adkResponse.length());

        return ResponseEntity.ok(Map.of(
                "response", adkResponse,
                "timestamp", Instant.now().toString()
        ));
    }

    @GetMapping("/history/{fileId}")
    public ResponseEntity<?> getChatHistory(@PathVariable String fileId) {
        List<Map<String, Object>> history = chatHistory.getOrDefault(fileId, List.of());
        return ResponseEntity.ok(history);
    }
}
