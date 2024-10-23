package com.example.ai_dating_backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
public class AwsConfig {

    @Value("${spring.cloud.aws.region}")
    String region;

    @Value("${spring.cloud.aws.access.key.id}")
    String accessKeyId;

    @Value("${spring.cloud.aws.secret.access.key}")
    String secretAccessKey;

    @Bean
    public Region awsRegion() {
        return Region.of(region);
    }

    @Bean
    public S3Client s3Client(Region region) {
        AwsCredentials credentials = AwsBasicCredentials.create(accessKeyId, secretAccessKey);
        return S3Client.builder()
                .region(region)
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .build();
    }
}
