package com.docbot.service;

import com.docbot.dto.UploadedFileResponse;
import com.docbot.model.UploadedFile;
import com.docbot.repository.FileRepository;
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
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class FileStorageService {

    private static final Logger log = LoggerFactory.getLogger(FileStorageService.class);

    private final S3Service s3Service;
    private final FileRepository fileRepository;
    private final Tika tika = new Tika();

    // In-memory store (used when DB is commented out)
    private final Map<String, UploadedFile> inMemoryStore = new ConcurrentHashMap<>();

    public FileStorageService(S3Service s3Service, FileRepository fileRepository) {
        this.s3Service = s3Service;
        this.fileRepository = fileRepository;
    }

    /**
     * Upload file and save metadata.
     */
    public UploadedFile storeFile(MultipartFile file, String userId) throws IOException {
        String id = UUID.randomUUID().toString();
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isBlank()) {
            originalFilename = "unknown";
        }

        String sanitizedFilename = originalFilename.substring(originalFilename.lastIndexOf('/') + 1);
        String extension = getFileExtension(sanitizedFilename);
        String s3Key = "uploads/" + id + "/" + sanitizedFilename;

        // --- S3 Upload (comment out to disable S3) ---
        s3Service.uploadFile(s3Key, file.getInputStream(), file.getSize(), file.getContentType());
        // --- End S3 Upload ---

        UploadedFile uploadedFile = UploadedFile.builder()
                .id(id)
                .userId(userId)
                .fileName(sanitizedFilename)
                .fileExtension(extension)
                .uploadedAt(Instant.now())
                .fileSize(file.getSize())
                .s3Key(s3Key)
                .build();

        // --- DB Save (comment out to use in-memory store) ---
        fileRepository.save(uploadedFile);
        // --- End DB Save ---

        // --- In-Memory Save (uncomment when DB is commented out) ---
        // inMemoryStore.put(id, uploadedFile);
        // --- End In-Memory Save ---

        log.info("File uploaded: id={}, name={}, s3Key={}", id, sanitizedFilename, s3Key);
        return uploadedFile;
    }

    /**
     * Extract text from S3 file on demand.
     */
    public String extractTextFromS3(String s3Key) {
        // --- S3 Download + Extract (comment out to disable S3) ---
        try (InputStream inputStream = s3Service.downloadFile(s3Key)) {
            String text = tika.parseToString(inputStream);
            log.info("Text extracted from S3: s3Key={}, length={}", s3Key, text.length());
            return text;
        } catch (IOException | TikaException e) {
            log.error("Failed to extract content from S3 key={}: {}", s3Key, e.getMessage());
            return "[Content extraction failed: " + e.getMessage() + "]";
        }
        // --- End S3 Download + Extract ---

        // --- Mock response (uncomment when S3 is commented out) ---
        // return "[Mock extracted text for testing]";
        // --- End Mock response ---
    }

    public List<UploadedFile> getAllFiles() {
        // --- DB (comment out to use in-memory) ---
        return fileRepository.findAll();
        // --- End DB ---

        // --- In-Memory (uncomment when DB is commented out) ---
        // return List.copyOf(inMemoryStore.values());
        // --- End In-Memory ---
    }

    public List<UploadedFile> getFilesByUserId(String userId) {
        // --- DB ---
        return fileRepository.findByUserId(userId);
        // --- End DB ---

        // --- In-Memory ---
        // return inMemoryStore.values().stream()
        //         .filter(f -> userId.equals(f.getUserId()))
        //         .toList();
        // --- End In-Memory ---
    }

    public UploadedFile getFileById(String id) {
        // --- DB ---
        return fileRepository.findById(id).orElse(null);
        // --- End DB ---

        // --- In-Memory ---
        // return inMemoryStore.get(id);
        // --- End In-Memory ---
    }

    public boolean deleteFile(String id) {
        // --- DB ---
        Optional<UploadedFile> file = fileRepository.findById(id);
        if (file.isEmpty()) {
            return false;
        }
        // --- End DB ---

        // --- In-Memory ---
        // UploadedFile removed = inMemoryStore.remove(id);
        // if (removed == null) return false;
        // --- End In-Memory ---

        // --- S3 Delete ---
        s3Service.deleteFile(file.get().getS3Key());
        // --- End S3 Delete ---

        // --- DB Delete ---
        fileRepository.deleteById(id);
        // --- End DB Delete ---

        return true;
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

    private String getFileExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        return (dotIndex > 0) ? filename.substring(dotIndex + 1).toLowerCase() : "";
    }
}