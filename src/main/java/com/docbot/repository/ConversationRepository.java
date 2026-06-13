package com.docbot.repository;

import com.docbot.model.Conversation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * JPA Repository for Conversation entity.
 * Provides database access for conversation management.
 */
@Repository
public interface ConversationRepository extends JpaRepository<Conversation, UUID> {

    Page<Conversation> findByUserId(UUID userId, Pageable pageable);

    Page<Conversation> findByUserIdAndIsArchived(UUID userId, Boolean isArchived, Pageable pageable);

    Optional<Conversation> findByIdAndUserId(UUID conversationId, UUID userId);

    List<Conversation> findByFileIdAndUserId(UUID fileId, UUID userId);

    long countByUserId(UUID userId);

    long countByUserIdAndIsArchived(UUID userId, Boolean isArchived);

    void deleteByIdAndUserId(UUID conversationId, UUID userId);
}
