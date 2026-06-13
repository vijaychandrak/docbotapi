package com.docbot.service;

import com.docbot.dto.UploadedFileResponse;
import com.docbot.model.DocbotFile;
import com.docbot.model.UploadedFile;
import com.docbot.model.User;
import com.docbot.repository.DocbotFileRepository;
import com.docbot.repository.UserRepository;
import com.docbot.dto.S3UploadResult;
import com.docbot.exception.UserNotFoundException;
import com.docbot.exception.BadRequestException;
import com.docbot.service.S3StorageService;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;

/**
 * Service responsible for processing uploaded files:
 * - extracts text using Apache Tika
 * - uploads the binary to S3 (placeholder implementation)
 * - persists file metadata and extracted text as {@link com.docbot.model.DocbotFile}
 *
 * Error mapping notes (handled by global exception handlers):
 * - Throws {@link com.docbot.exception.BadRequestException} when provided IDs are not valid UUIDs (results in HTTP 400)
 * - Throws {@link com.docbot.exception.UserNotFoundException} when a user UUID is valid but no user exists (results in HTTP 404)
 * - Other unexpected errors will be handled by the generic handler (HTTP 500)
 */
@Service
public class FileStorageService {

    private static final Logger log = LoggerFactory.getLogger(FileStorageService.class);

    private final Tika tika = new Tika();
    private final DocbotFileRepository docbotFileRepository;
    private final UserRepository userRepository;
    private final S3StorageService s3StorageService;

    public FileStorageService(DocbotFileRepository docbotFileRepository,
                              UserRepository userRepository,
                              S3StorageService s3StorageService) {
        this.docbotFileRepository = docbotFileRepository;
        this.userRepository = userRepository;
        this.s3StorageService = s3StorageService;
    }

    /**
     * Process an uploaded file and persist metadata.
     *
     * Exceptions and HTTP mapping (via GlobalExceptionHandler):
     * - BadRequestException if userId is not a valid UUID (HTTP 400)
     * - UserNotFoundException if user UUID is valid but user does not exist (HTTP 404)
     * - IOException for I/O problems during file handling (bubbled to caller)
     */
    public UploadedFile storeFile(MultipartFile file, String userId) throws IOException {
        String originalFilename = getOriginalFilename(file);
        String sanitizedFilename = sanitizeFilename(originalFilename);
        String extension = getFileExtension(sanitizedFilename);

        // Extract text content from the uploaded file using Apache Tika
        // This enables AI/LLM processing and search capabilities on document content
        String extractedText = extractTextFromFile(file);

        // Parse and validate userId as UUID to provide a clear 400 if the client sent a malformed id
        log.info("Received file upload: filename={}, userId={}", originalFilename, userId);
        UUID userUuid = parseUuidOrThrow(userId, "userId must be a valid UUID");

        // Fetch the user by userId (UUID)
        // If user is missing, throw a dedicated exception that maps to HTTP 404 so callers get a clear response
        User user = userRepository.findById(userUuid)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));

        // Upload to S3 (placeholder) and persist DB record with S3 key
        S3UploadResult s3Result = s3StorageService.uploadFile(file, userId);

        // Create and save DocbotFile to database with S3 key as filePath
        // extractedText is persisted for AI/LLM processing during chat interactions
        DocbotFile docbotFile = DocbotFile.builder()
                .fileName(sanitizedFilename)
                .filePath(s3Result.getKey())
                .fileSize(file.getSize())
                .mimeType(file.getContentType())
                .extractedText(extractedText)
                .uploadStatus("READY")
                .user(user)
                .build();

        DocbotFile savedFile = docbotFileRepository.save(docbotFile);

        // Convert to UploadedFile for response compatibility
        // Include extractedText (now persisted to DB)
        UploadedFile uploadedFile = UploadedFile.builder()
                .id(savedFile.getId().toString())
                .userId(userId)
                .fileName(sanitizedFilename)
                .fileExtension(extension)
                .uploadedAt(savedFile.getCreatedAt().atZone(java.time.ZoneId.systemDefault()).toInstant())
                .fileSize(file.getSize())
                .extractedText(extractedText)
                .build();

        log.info("File processed and saved to DB: id={}, name={}, size={}, extractedLength={}",
                savedFile.getId(), sanitizedFilename, file.getSize(), extractedText.length());

        return uploadedFile;
    }

        /**
         * Store uploaded file for the given username (authenticated principal).
         * Looks up the User by username and persists the file linked to that user.
         */
        public UploadedFile storeFileForUsername(MultipartFile file, String username) throws IOException {
        if (username == null || username.isBlank()) {
            throw new BadRequestException("username is required");
        }

        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UserNotFoundException("User not found with username: " + username));

        String originalFilename = getOriginalFilename(file);
        String sanitizedFilename = sanitizeFilename(originalFilename);
        String extension = getFileExtension(sanitizedFilename);

        // Extract text content from the uploaded file using Apache Tika
        // This enables AI/LLM processing and search capabilities on document content
        String extractedText = extractTextFromFile(file);

        S3UploadResult s3Result = s3StorageService.uploadFile(file, user.getId().toString());

        // Create DocbotFile with extracted text to persist for AI/LLM processing
        DocbotFile docbotFile = DocbotFile.builder()
            .fileName(sanitizedFilename)
            .filePath(s3Result.getKey())
            .fileSize(file.getSize())
            .mimeType(file.getContentType())
            .extractedText(extractedText)
            .uploadStatus("READY")
            .user(user)
            .build();

        DocbotFile savedFile = docbotFileRepository.save(docbotFile);

        // Build response with extracted text (now persisted to DB)
        UploadedFile uploadedFile = UploadedFile.builder()
            .id(savedFile.getId().toString())
            .userId(user.getId().toString())
            .fileName(sanitizedFilename)
            .fileExtension(extension)
            .uploadedAt(savedFile.getCreatedAt().atZone(java.time.ZoneId.systemDefault()).toInstant())
            .fileSize(file.getSize())
            .extractedText(extractedText)
            .build();

        log.info("File processed and saved to DB: id={}, name={}, user={}, extractedLength={}",
            savedFile.getId(), sanitizedFilename, username, extractedText.length());

        return uploadedFile;
        }

    public List<UploadedFile> getAllFiles() {
        return docbotFileRepository.findAll().stream()
                .map(this::convertToUploadedFile)
                .toList();
    }

    public List<UploadedFile> getFilesByUserId(String userId) {
        // Accept either a UUID string or a username; try UUID first
        try {
            UUID userUuid = UUID.fromString(userId);
            return docbotFileRepository.findByUserId(userUuid, org.springframework.data.domain.Pageable.unpaged())
                    .getContent().stream()
                    .map(this::convertToUploadedFile)
                    .toList();
        } catch (IllegalArgumentException ex) {
            // treat as username
            return userRepository.findByUsername(userId)
                    .map(user -> docbotFileRepository.findByUserId(user.getId(), org.springframework.data.domain.Pageable.unpaged())
                            .getContent().stream().map(this::convertToUploadedFile).toList())
                    .orElseThrow(() -> new UserNotFoundException("User not found: " + userId));
        }
    }

    public UploadedFile getFileById(String id) {
        UUID uuid = parseUuidOrThrow(id, "file id must be a valid UUID");
        return docbotFileRepository.findById(uuid)
                .map(this::convertToUploadedFile)
                .orElse(null);
    }

    public boolean deleteFile(String id) {
        // Validate id first and return 400 for malformed UUIDs
        UUID uuid = parseUuidOrThrow(id, "file id must be a valid UUID");
        try {
            docbotFileRepository.deleteById(uuid);
            return true;
        } catch (Exception e) {
            log.error("Failed to delete file: {}", e.getMessage());
            return false;
        }
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

    private UploadedFile convertToUploadedFile(DocbotFile docbotFile) {
        // Convert DB entity to DTO including extractedText for AI/chat processing
        return UploadedFile.builder()
                .id(docbotFile.getId().toString())
                .userId(docbotFile.getUser().getId().toString())
                .fileName(docbotFile.getFileName())
                .fileExtension(getFileExtension(docbotFile.getFileName()))
                .uploadedAt(docbotFile.getCreatedAt().atZone(java.time.ZoneId.systemDefault()).toInstant())
                .fileSize(docbotFile.getFileSize())
                .extractedText(docbotFile.getExtractedText())
                .build();
    }

    /**
     * Extract text content from uploaded file using Apache Tika.
     * The extracted text is persisted to the database for AI/LLM chat interactions and document understanding.
     * If extraction fails, returns an error message placeholder instead of null.
     *
     * @param file the uploaded file to extract text from
     * @return extracted text content, or error message if extraction fails
     */
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

    /**
     * Parse a string as UUID and throw BadRequestException if invalid.
     */
    private UUID parseUuidOrThrow(String id, String errorMessage) {
        try {
            return UUID.fromString(id);
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException(errorMessage + ": " + id);
        }
    }

    /**
     * Extract filename from MultipartFile, defaulting to "unknown" if blank.
     */
    private String getOriginalFilename(MultipartFile file) {
        String filename = file.getOriginalFilename();
        return (filename == null || filename.isBlank()) ? "unknown" : filename;
    }

    /**
     * Sanitize filename by removing path components.
     */
    private String sanitizeFilename(String filename) {
        return filename.substring(filename.lastIndexOf('/') + 1);
    }
}