package com.docbot.service;

import com.docbot.dto.UploadedFileResponse;
import com.docbot.model.UploadedFile;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class FileStorageService {

    private static final Logger log = LoggerFactory.getLogger(FileStorageService.class);

    private final Tika tika = new Tika();
    private final Map<String, UploadedFile> fileStore = new ConcurrentHashMap<>();

    public UploadedFile storeFile(MultipartFile file, String userId) throws IOException {
        String id = UUID.randomUUID().toString();
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isBlank()) {
            originalFilename = "unknown";
        }

        String sanitizedFilename = originalFilename.substring(originalFilename.lastIndexOf('/') + 1);
        String extension = getFileExtension(sanitizedFilename);

        // Extract text directly from the upload stream
        String extractedText = extractTextFromFile(file);

        UploadedFile uploadedFile = UploadedFile.builder()
                .id(id)
                .userId(userId)
                .fileName(sanitizedFilename)
                .fileExtension(extension)
                .uploadedAt(Instant.now())
                .fileSize(file.getSize())
                .extractedText(extractedText)
                .build();

        fileStore.put(id, uploadedFile);
        log.info("File processed: id={}, name={}, size={}, extractedLength={}",
                id, sanitizedFilename, file.getSize(), extractedText.length());

        return uploadedFile;
    }

    public List<UploadedFile> getAllFiles() {
        return List.copyOf(fileStore.values());
    }

    public List<UploadedFile> getFilesByUserId(String userId) {
        return fileStore.values().stream()
                .filter(f -> userId.equals(f.getUserId()))
                .toList();
    }

    public UploadedFile getFileById(String id) {
        return fileStore.get(id);
    }

    public boolean deleteFile(String id) {
        return fileStore.remove(id) != null;
    }

    public UploadedFileResponse toResponse(UploadedFile file) {
        return UploadedFileResponse.builder()
                .id(file.getId())
                .userId(file.getUserId())
                .fileName(file.getFileName())
                .fileExtension(file.getFileExtension())
                .uploadedAt(file.getUploadedAt())
                .fileSize(file.getFileSize())
                .build();
    }

    private String extractTextFromFile(MultipartFile file) {
        try (InputStream inputStream = file.getInputStream()) {
            return tika.parseToString(inputStream);
        } catch (IOException | TikaException e) {
            log.error("Failed to extract content from {}: {}", file.getOriginalFilename(), e.getMessage());
            return "[Content extraction failed: " + e.getMessage() + "]";
        }
    }

    private String getFileExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        return (dotIndex > 0) ? filename.substring(dotIndex + 1).toLowerCase() : "";
    }
}