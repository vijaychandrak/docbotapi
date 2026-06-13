package com.docbot.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Message entity representing individual messages in a conversation.
 * Stores each user and bot message separately for scalability.
 * Indexed by conversation_id and created_at for efficient queries.
 */
@Entity
@Table(
    name = "messages",
    indexes = {
        @Index(name = "idx_messages_conversation_id", columnList = "conversation_id"),
        @Index(name = "idx_messages_sender", columnList = "sender"),
        @Index(name = "idx_messages_created_at", columnList = "created_at"),
        @Index(name = "idx_messages_conv_created", columnList = "conversation_id, created_at")
    }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Message extends BaseEntity {

    @NotBlank(message = "Content is required")
    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "sender", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private MessageSender sender = MessageSender.USER;

    @Column(name = "token_count")
    private Integer tokenCount;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "conversation_id", nullable = false)
    private Conversation conversation;

    /**
     * Enum for message sender type
     */
    public enum MessageSender {
        USER,
        BOT
    }
}
