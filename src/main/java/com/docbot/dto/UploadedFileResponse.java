package com.docbot.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UploadedFileResponse {

    private String id;
    private String userId;
    private String fileName;
    private String fileExtension;
    private Instant uploadedAt;
    private long fileSize;
}
