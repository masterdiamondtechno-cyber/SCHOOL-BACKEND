package com.schoolmanagement.school.service.impl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.schoolmanagement.school.entity.RecordedLecture;
import com.schoolmanagement.school.repository.RecordedLectureRepository;

import ws.schild.jave.MultimediaObject;
import ws.schild.jave.info.MultimediaInfo;

/**
 * Handles local video file storage asynchronously so the API can return
 * immediately with status "Processing". Once the file is moved to
 * uploads/lectures/, the lecture status is updated to "Published" (success)
 * or "Failed" (error).
 *
 * NOTE: Renamed from S3AsyncService — no longer uses AWS S3.
 */
@Service
public class S3AsyncService {

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    @Autowired
    private RecordedLectureRepository recordedLectureRepository;

    /**
     * Moves the video temp file to uploads/lectures/ in a background thread.
     * Automatically extracts video duration using JAVE2.
     * Updates the RecordedLecture entity status to Published/Failed.
     *
     * @param lectureId   DB id of the RecordedLecture entity
     * @param tempFile    Temp file written from MultipartFile
     * @param fileKey     Relative path to store in DB, e.g. /uploads/lectures/RL001_video.mp4
     * @param contentType MIME type (unused for local storage, kept for API compat)
     * @param safeName    Sanitised filename
     */
    @Async
    public void uploadLectureVideoAsync(Long lectureId, File tempFile,
            String fileKey, String contentType, String safeName) {
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
                // Duration extraction failed — continue with file move anyway
            }

            // ── Move temp file to lectures folder ─────────────────────────
            Path lecturesDir = Paths.get(uploadDir, "lectures");
            Files.createDirectories(lecturesDir);
            Path destination = lecturesDir.resolve(safeName);
            Files.move(tempFile.toPath(), destination, StandardCopyOption.REPLACE_EXISTING);

            // fileKey is already the relative URL path (e.g. /uploads/lectures/RL001_video.mp4)
            String fileUrl = fileKey;

            // ── Update entity to Published ───────────────────────────────
            RecordedLecture lecture = recordedLectureRepository.findById(lectureId)
                    .orElse(null);
            if (lecture != null) {
                lecture.setS3Key(fileKey);   // reusing s3Key field to store local relative path
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
            // Clean up temp file if move failed or it still exists
            if (tempFile != null && tempFile.exists()) {
                tempFile.delete();
            }
        }
    }
}

