package com.schoolmanagement.Config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

/**
 * Spring configuration that wires the AWS S3Client and S3Presigner beans
 * for both the LIGHT bucket (logos, study materials) and the HEAVY bucket
 * (recorded lectures).
 *
 * Light bucket properties:  aws.region, aws.accessKey, aws.secretKey, aws.s3.bucket
 * Heavy bucket properties:  aws.heavy.region, aws.heavy.accessKey, aws.heavy.secretKey, aws.heavy.s3.bucket
 */
@Configuration
public class S3Config {

    // ── Light bucket credentials (existing — logos, study materials) ───────
    @Value("${aws.region:ap-south-1}")
    private String lightRegion;

    @Value("${aws.accessKey:local-access-key}")
    private String lightAccessKey;

    @Value("${aws.secretKey:local-secret-key}")
    private String lightSecretKey;

    // ── Heavy bucket credentials (recorded lectures) ──────────────────────
    @Value("${aws.heavy.region:ap-south-1}")
    private String heavyRegion;

    @Value("${aws.heavy.accessKey:local-access-key}")
    private String heavyAccessKey;

    @Value("${aws.heavy.secretKey:local-secret-key}")
    private String heavySecretKey;

    // =====================================================================
    // LIGHT BUCKET beans  (default / @Primary)
    // =====================================================================

    /**
     * Light S3Client — used for logos and study-material uploads.
     * Marked @Primary so existing @Autowired injections continue to work.
     */
    @Bean("lightS3Client")
    @Primary
    public S3Client lightS3Client() {
        return S3Client.builder()
                .region(Region.of(lightRegion))
                .credentialsProvider(
                    StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(lightAccessKey, lightSecretKey)))
                .build();
    }

    /**
     * Light S3Presigner — presigned URLs for logos and study materials.
     */
    @Bean("lightS3Presigner")
    @Primary
    public S3Presigner lightS3Presigner() {
        return S3Presigner.builder()
                .region(Region.of(lightRegion))
                .credentialsProvider(
                    StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(lightAccessKey, lightSecretKey)))
                .build();
    }

    // =====================================================================
    // HEAVY BUCKET beans  (recorded lectures)
    // =====================================================================

    /**
     * Heavy S3Client — used for recorded-lecture video uploads.
     */
    @Bean("heavyS3Client")
    public S3Client heavyS3Client() {
        return S3Client.builder()
                .region(Region.of(heavyRegion))
                .credentialsProvider(
                    StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(heavyAccessKey, heavySecretKey)))
                .build();
    }

    /**
     * Heavy S3Presigner — presigned URLs for recorded-lecture streaming.
     */
    @Bean("heavyS3Presigner")
    public S3Presigner heavyS3Presigner() {
        return S3Presigner.builder()
                .region(Region.of(heavyRegion))
                .credentialsProvider(
                    StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(heavyAccessKey, heavySecretKey)))
                .build();
    }
}
