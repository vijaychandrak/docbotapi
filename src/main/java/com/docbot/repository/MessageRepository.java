package com.docbot.repository;

import com.docbot.model.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * JPA Repository for Message entity.
 * Provides database access for message management with pagination support.
 */
@Repository
public interface MessageRepository extends JpaRepository<Message, UUID> {

    Page<Message> findByConversationId(UUID conversationId, Pageable pageable);

    Page<Message> findByConversationIdAndSender(UUID conversationId, Message.MessageSender sender, Pageable pageable);

    long countByConversationId(UUID conversationId);

    long countByConversationIdAndSender(UUID conversationId, Message.MessageSender sender);

    void deleteByConversationId(UUID conversationId);

    List<Message> findByConversationIdOrderByCreatedAtAsc(UUID conversationId);
}
