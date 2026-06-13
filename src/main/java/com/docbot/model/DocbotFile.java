package com.docbot.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DocbotFile entity representing uploaded documents.
 * Stores file metadata and extracted text content.
 * Linked to User (many files per user).
 */
@Entity
@Table(
    name = "files",
    indexes = {
        @Index(name = "idx_files_user_id", columnList = "user_id"),
        @Index(name = "idx_files_created_at", columnList = "created_at")
    }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocbotFile extends BaseEntity {

    @NotBlank(message = "File name is required")
    @Column(name = "file_name", nullable = false, length = 500)
    private String fileName;

    @Column(name = "file_path", nullable = false, length = 1000)
    private String filePath;

    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    @Column(name = "mime_type", length = 100)
    private String mimeType;

    @Column(name = "extracted_text", columnDefinition = "TEXT")
    private String extractedText;

    @Column(name = "upload_status", nullable = false, length = 50)
    @Builder.Default
    private String uploadStatus = "PENDING"; // PENDING, EXTRACTED, READY, FAILED

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "file", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private java.util.Set<Conversation> conversations = new java.util.HashSet<>();
}
