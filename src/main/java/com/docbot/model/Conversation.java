package com.docbot.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

/**
 * Conversation entity representing a Q&A session about a specific document.
 * One file can have multiple conversations (different users or different sessions).
 * Messages are stored in a separate table for scalability.
 */
@Entity
@Table(
    name = "conversations",
    indexes = {
        @Index(name = "idx_conversations_user_id", columnList = "user_id"),
        @Index(name = "idx_conversations_file_id", columnList = "file_id"),
        @Index(name = "idx_conversations_created_at", columnList = "created_at")
    }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Conversation extends BaseEntity {

    @NotBlank(message = "Title is required")
    @Column(name = "title", nullable = false, length = 500)
    private String title;

    @Column(name = "is_archived", nullable = false)
    @Builder.Default
    private Boolean isArchived = false;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "file_id", nullable = false)
    private DocbotFile file;

    @OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private Set<Message> messages = new HashSet<>();
}
