package com.docbot.controller;

import com.docbot.dto.UploadedFileResponse;
import com.docbot.model.UploadedFile;
import com.docbot.service.FileStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/files")
public class FileController {

    private static final Logger log = LoggerFactory.getLogger(FileController.class);

    private final FileStorageService fileStorageService;

    public FileController(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    /**
     * Upload a file — extracts content and stores it.
     * Matches: POST /api/files/upload  (multipart form with field "file")
     */
    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file,
                                        @RequestParam("userId") String userId) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "File is empty"));
        }
        if (userId == null || userId.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "userId is required"));
        }

        try {
            // Store file and extract content
            UploadedFile uploaded = fileStorageService.storeFile(file, userId);

            log.info("File uploaded: id={}, name={}, userId={}", uploaded.getId(), uploaded.getFileName(), uploaded.getUserId());

            // Return response matching Angular UploadedFile model
            UploadedFileResponse response = fileStorageService.toResponse(uploaded);
            return ResponseEntity.ok(response);

        } catch (IOException e) {
            log.error("File upload failed: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "File upload failed: " + e.getMessage()));
        }
    }

    /**
     * List all uploaded files.
     * Matches: GET /api/files
     */
    @GetMapping
    public ResponseEntity<List<UploadedFileResponse>> getAllFiles() {
        List<UploadedFileResponse> files = fileStorageService.getAllFiles().stream()
                .map(fileStorageService::toResponse)
                .toList();
        return ResponseEntity.ok(files);
    }

    /**
     * List files for a specific user.
     * Matches: GET /api/files/user/{userId}
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<UploadedFileResponse>> getFilesByUser(@PathVariable String userId) {
        List<UploadedFileResponse> files = fileStorageService.getFilesByUserId(userId).stream()
                .map(fileStorageService::toResponse)
                .toList();
        return ResponseEntity.ok(files);
    }

    /**
     * Get a specific file by ID.
     * Matches: GET /api/files/{fileId}
     */
    @GetMapping("/{fileId}")
    public ResponseEntity<?> getFileById(@PathVariable String fileId) {
        UploadedFile file = fileStorageService.getFileById(fileId);
        if (file == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(fileStorageService.toResponse(file));
    }

    /**
     * Delete a file by ID.
     * Matches: DELETE /api/files/{fileId}
     */
    @DeleteMapping("/{fileId}")
    public ResponseEntity<?> deleteFile(@PathVariable String fileId) {
        boolean deleted = fileStorageService.deleteFile(fileId);
        if (!deleted) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(Map.of("message", "File deleted successfully"));
    }
}
