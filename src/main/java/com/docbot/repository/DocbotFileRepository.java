package com.docbot.repository;

import com.docbot.model.DocbotFile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * JPA Repository for DocbotFile entity.
 * Provides database access for file management.
 */
@Repository
public interface DocbotFileRepository extends JpaRepository<DocbotFile, UUID> {

    Page<DocbotFile> findByUserId(UUID userId, Pageable pageable);

    List<DocbotFile> findByUserIdAndUploadStatus(UUID userId, String uploadStatus);

    Optional<DocbotFile> findByIdAndUserId(UUID fileId, UUID userId);

    List<DocbotFile> findByUploadStatus(String uploadStatus);

    long countByUserId(UUID userId);

    void deleteByIdAndUserId(UUID fileId, UUID userId);
}
