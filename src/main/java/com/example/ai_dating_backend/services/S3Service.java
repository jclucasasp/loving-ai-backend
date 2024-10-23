package com.example.ai_dating_backend.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;

import java.nio.file.Path;

@Slf4j
@RequiredArgsConstructor
@Service
public class S3Service {

    @Value("${spring.cloud.aws.bucket}")
    String bucketName;

    private final S3Client s3Client;

    public void uploadFile(Path path) {
        log.info("Attempting to upload file [{}]...", path.getFileName());
        try {
            s3Client.putObject(r ->
                            r.bucket(bucketName)
                                    .key(path.getFileName().toString())
                                    .build()
                    , path);
        } catch (Exception e) {
            log.error("Unable to upload file [{}]", path.getFileName());
            throw new RuntimeException(e);
        }
        log.info("File successfully uploaded...");
    }

}
