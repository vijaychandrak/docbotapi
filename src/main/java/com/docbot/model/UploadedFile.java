package com.docbot.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "uploaded_files")
public class UploadedFile {

    @Id
    private String id;

    private String userId;
    private String fileName;
    private String fileExtension;
    private Instant uploadedAt;
    private long fileSize;

    // S3 key for retrieving the file later
    private String s3Key;
}