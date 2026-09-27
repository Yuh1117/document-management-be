package com.vpgh.dms.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.core.checksums.RequestChecksumCalculation;
import software.amazon.awssdk.core.checksums.ResponseChecksumValidation;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.net.URI;

@Configuration
public class S3Configuration {
    @Value("${r2.access-key}")
    private String accessKey;

    @Value("${r2.secret-key}")
    private String secretKey;

    @Value("${r2.region}")
    private String region;

    @Value("${r2.endpoint}")
    private String endpoint;

    @Bean
    public S3Client s3Client() {
        AwsBasicCredentials awsBasicCredentials = AwsBasicCredentials.create(accessKey, secretKey);

        return S3Client.builder()
                .region(Region.of(region))
                .endpointOverride(URI.create(endpoint))
                .credentialsProvider(StaticCredentialsProvider.create(awsBasicCredentials))
                .serviceConfiguration(r2ServiceConfiguration())
                .requestChecksumCalculation(RequestChecksumCalculation.WHEN_REQUIRED)
                .responseChecksumValidation(ResponseChecksumValidation.WHEN_REQUIRED)
                .build();
    }

    @Bean
    public S3Presigner s3Presigner() {
        AwsBasicCredentials awsBasicCredentials = AwsBasicCredentials.create(accessKey, secretKey);

        return S3Presigner.builder()
                .region(Region.of(region))
                .endpointOverride(URI.create(endpoint))
                .credentialsProvider(StaticCredentialsProvider.create(awsBasicCredentials))
                .serviceConfiguration(r2ServiceConfiguration())
                .build();
    }

    private software.amazon.awssdk.services.s3.S3Configuration r2ServiceConfiguration() {
        return software.amazon.awssdk.services.s3.S3Configuration.builder()
                .pathStyleAccessEnabled(true)
                .build();
    }
}
