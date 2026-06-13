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
     * <p>
     * Endpoint: POST /api/files/upload (multipart form with field "file" and "userId")
     * <p>
     * Possible responses:
     * - 200 OK — file processed successfully, returns {@link com.docbot.dto.UploadedFileResponse}
     * - 400 Bad Request — missing/empty file, missing userId, or malformed UUID for userId. Body is JSON:
     *   { timestamp, status:400, error:"Bad Request", message, path }
     * - 404 Not Found — user not found (valid UUID but no matching user). Body is JSON:
     *   { timestamp, status:404, error:"Not Found", message, path }
     * - 500 Internal Server Error — unexpected server error. Body is JSON:
     *   { timestamp, status:500, error:"Internal Server Error", message, path }
     *
     * Note: the service extracts text using Apache Tika and stores metadata in the database. S3 upload is
     * currently a placeholder and will return a generated S3 key.
     */
    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "File is empty"));
        }

        var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getName() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Unauthorized"));
        }

        String username = auth.getName();

        try {
            // Store file and extract content linked to authenticated user
            UploadedFile uploaded = fileStorageService.storeFileForUsername(file, username);

            log.info("File uploaded: id={}, name={}, username={}", uploaded.getId(), uploaded.getFileName(), uploaded.getUserId());

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
     * List files for the currently authenticated user.
     * Matches: GET /api/files/me
     */
    @GetMapping("/me")
    public ResponseEntity<List<UploadedFileResponse>> getFilesForCurrentUser() {
        var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getName() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String username = auth.getName();
        List<UploadedFileResponse> files = fileStorageService.getFilesByUserId(username).stream()
                .map(fileStorageService::toResponse)
                .toList();
        return ResponseEntity.ok(files);
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
