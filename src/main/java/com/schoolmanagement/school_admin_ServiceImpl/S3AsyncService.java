package com.schoolmanagement.school_admin_ServiceImpl;

import java.io.File;
import java.time.Duration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.schoolmanagement.school_admin_Entity.RecordedLecture;
import com.schoolmanagement.school_admin_Repo.RecordedLectureRepository;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import ws.schild.jave.MultimediaObject;
import ws.schild.jave.info.MultimediaInfo;

/**
 * Handles heavy S3 uploads asynchronously so the API can return immediately
 * with status "Processing". Once the upload completes, the lecture status
 * is updated to "Published" (success) or "Failed" (error).
 */
@Service
public class S3AsyncService {

    @Value("${aws.heavy.s3.bucket}")
    private String bucketName;

    private static final Duration PRESIGN_DURATION = Duration.ofHours(2);

    @Autowired
    @Qualifier("heavyS3Client")
    private S3Client s3Client;

    @Autowired
    @Qualifier("heavyS3Presigner")
    private S3Presigner s3Presigner;

    @Autowired
    private RecordedLectureRepository recordedLectureRepository;

    /**
     * Uploads the video file to S3 in a background thread.
     * Automatically extracts video duration using JAVE2.
     * Updates the RecordedLecture entity status to Published/Failed.
     */
    @Async
    public void uploadLectureVideoAsync(Long lectureId, File tempFile,
            String s3Key, String contentType, String safeName) {
        try {
            // ── Extract video duration automatically ──────────────────────
            Integer durationMinutes = null;
            try {
                MultimediaObject multimediaObject = new MultimediaObject(tempFile);
                MultimediaInfo info = multimediaObject.getInfo();
                long durationMs = info.getDuration();
                durationMinutes = (int) (durationMs / 60000);
                if (durationMinutes == 0 && durationMs > 0) {
                    durationMinutes = 1; // At least 1 minute for short videos
                }
            } catch (Exception ignored) {
                // Duration extraction failed — continue with upload anyway
            }

            // ── Upload to S3 ─────────────────────────────────────────────
            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .contentType(contentType)
                    .contentDisposition("inline; filename=\"" + safeName + "\"")
                    .build();

            s3Client.putObject(putRequest, RequestBody.fromFile(tempFile));

            // ── Generate presigned URL ───────────────────────────────────
            String fileUrl = generatePresignedUrl(s3Key);

            // ── Update entity to Published ───────────────────────────────
            RecordedLecture lecture = recordedLectureRepository.findById(lectureId)
                    .orElse(null);
            if (lecture != null) {
                lecture.setS3Key(s3Key);
                lecture.setFileUrl(fileUrl);
                lecture.setStatus("Published");
                if (durationMinutes != null) {
                    lecture.setDuration(durationMinutes);
                }
                recordedLectureRepository.save(lecture);
            }

        } catch (Exception e) {
            // Update entity to Failed
            RecordedLecture lecture = recordedLectureRepository.findById(lectureId)
                    .orElse(null);
            if (lecture != null) {
                lecture.setStatus("Failed");
                recordedLectureRepository.save(lecture);
            }

        } finally {
            // Clean up temp file
            if (tempFile != null && tempFile.exists()) {
                tempFile.delete();
            }
        }
    }

    private String generatePresignedUrl(String s3Key) {
        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(PRESIGN_DURATION)
                .getObjectRequest(r -> r.bucket(bucketName).key(s3Key))
                .build();
        PresignedGetObjectRequest presigned = s3Presigner.presignGetObject(presignRequest);
        return presigned.url().toString();
    }
}
