package com.docbot.service;

import com.docbot.dto.S3UploadResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

/**
 * Placeholder S3 storage service. In future this will call AWS SDK to upload files.
 * For now it returns a sample S3 response that can be persisted to DB.
 */
@Service
public class S3StorageService {

    private static final Logger log = LoggerFactory.getLogger(S3StorageService.class);

    /**
     * Pretend to upload file to S3 and return a sample response.
     * This method is synchronous for now; can be changed to async or queued later.
     */
    public S3UploadResult uploadFile(MultipartFile file, String userId) throws IOException {
        // Simulate building an S3 key
        String filename = file.getOriginalFilename();
        if (filename == null || filename.isBlank()) filename = "unknown";
        String key = String.format("uploads/%s/%s/%s", userId, UUID.randomUUID(), filename);

        // Example bucket and URL (placeholder)
        String bucket = "docbot-dev-bucket";
        String url = String.format("https://%s.s3.amazonaws.com/%s", bucket, key);

        log.info("[S3 Placeholder] Uploaded file to s3://{}/{} (url={})", bucket, key, url);

        return S3UploadResult.builder()
                .bucket(bucket)
                .key(key)
                .url(url)
                .build();
    }
}
