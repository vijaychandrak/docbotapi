package com.docbot.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Simple DTO representing a (mock) response from S3 after uploading a file.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class S3UploadResult {
    private String bucket;
    private String key;
    private String url;
}
