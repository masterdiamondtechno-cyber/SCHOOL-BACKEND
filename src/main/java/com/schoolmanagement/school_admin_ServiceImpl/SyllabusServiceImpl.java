package com.schoolmanagement.school_admin_ServiceImpl;

import java.io.IOException;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.schoolmanagement.school_admin_DTO.SyllabusDTO;
import com.schoolmanagement.school_admin_Entity.AcademicYear;
import com.schoolmanagement.school_admin_Entity.AddStandard;
import com.schoolmanagement.school_admin_Entity.AddSubject;
import com.schoolmanagement.school_admin_Entity.AddTeacher;
import com.schoolmanagement.school_admin_Entity.School;
import com.schoolmanagement.school_admin_Entity.Syllabus;
import com.schoolmanagement.school_admin_Repo.AcademicYearRepository;
import com.schoolmanagement.school_admin_Repo.AddStandardRepository;
import com.schoolmanagement.school_admin_Repo.AddSubjectRepository;
import com.schoolmanagement.school_admin_Repo.AddTeacherRepository;
import com.schoolmanagement.school_admin_Repo.SchoolRepository;
import com.schoolmanagement.school_admin_Repo.SyllabusRepository;
import com.schoolmanagement.school_admin_Service.SyllabusService;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

@Service
public class SyllabusServiceImpl implements SyllabusService {

    @Value("${aws.s3.bucket}")
    private String bucketName;

    private static final String S3_PREFIX = "syllabus/";
    private static final Duration PRESIGN_DURATION = Duration.ofHours(1);

    @Autowired private SyllabusRepository     syllabusRepository;
    @Autowired private SchoolRepository       schoolRepository;
    @Autowired private AcademicYearRepository academicYearRepository;
    @Autowired private AddSubjectRepository   subjectRepository;
    @Autowired private AddStandardRepository  standardRepository;
    @Autowired private AddTeacherRepository   teacherRepository;
    @Autowired private S3Client               s3Client;
    @Autowired private S3Presigner            s3Presigner;

    // ─────────────────────────────────────────────────────────────────────────
    // ENTITY → DTO CONVERTER
    // ─────────────────────────────────────────────────────────────────────────

    private SyllabusDTO toDTO(Syllabus s) {
        SyllabusDTO dto = new SyllabusDTO();

        dto.setId(s.getId());
        dto.setSyllabusId(s.getSyllabusId());
        dto.setTitle(s.getTitle());
        dto.setTotalChapters(s.getTotalChapters());
        dto.setCompletedChapters(s.getCompletedChapters());
        dto.setStatus(s.getStatus());
        dto.setFileType(s.getFileType());
        dto.setFileSize(s.getFileSize());
        dto.setCreatedAt(s.getCreatedAt());
        dto.setUpdatedAt(s.getUpdatedAt());

        // Calculate progress percentage
        if (s.getTotalChapters() != null && s.getTotalChapters() > 0) {
            int completed = s.getCompletedChapters() != null ? s.getCompletedChapters() : 0;
            dto.setProgress((int) Math.round((completed * 100.0) / s.getTotalChapters()));
        } else {
            dto.setProgress(0);
        }

        // FK display names
        if (s.getSchool() != null)
            dto.setSchoolId(s.getSchool().getSchoolId());

        if (s.getAcademicYear() != null)
            dto.setAcademicYearId(s.getAcademicYear().getAcademicYearId());

        if (s.getSubject() != null) {
            dto.setSubjectId(s.getSubject().getSubjectId());
            dto.setSubjectName(s.getSubject().getSubjectName());
        }

        if (s.getStandard() != null) {
            dto.setStandardId(s.getStandard().getStandardId());
            dto.setStandardName(s.getStandard().getStandard());
        }

        if (s.getTeacher() != null) {
            dto.setTeacherId(s.getTeacher().getId());
            dto.setTeacherName(s.getTeacher().getTeacherName());
        }

        // Generate presigned URL for file access
        if (s.getS3Key() != null && !s.getS3Key().isBlank())
            dto.setFileUrl(generatePresignedUrl(s.getS3Key()));

        return dto;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // S3 HELPERS
    // ─────────────────────────────────────────────────────────────────────────

    private String uploadToS3(MultipartFile file, String syllabusId) {
        String originalName = file.getOriginalFilename();
        String extension = "";
        if (originalName != null && originalName.contains("."))
            extension = originalName.substring(originalName.lastIndexOf("."));

        String safeName = (originalName != null)
                ? originalName.replaceAll("[^a-zA-Z0-9._-]", "-")
                : "file" + extension;

        String s3Key = S3_PREFIX + syllabusId.replace("#", "").replace(".", "") + "_" + safeName;

        try {
            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .contentType(file.getContentType())
                    .contentDisposition("attachment; filename=\"" + safeName + "\"")
                    .build();

            s3Client.putObject(putRequest, RequestBody.fromBytes(file.getBytes()));
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload file to S3: " + e.getMessage());
        }

        return s3Key;
    }

    private void deleteFromS3(String s3Key) {
        if (s3Key == null || s3Key.isBlank()) return;
        try {
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .build());
        } catch (Exception ignored) {}
    }

    private String generatePresignedUrl(String s3Key) {
        try {
            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(PRESIGN_DURATION)
                    .getObjectRequest(r -> r.bucket(bucketName).key(s3Key))
                    .build();

            PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(presignRequest);
            return presignedRequest.url().toString();
        } catch (Exception e) {
            return null;
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // AUTO-GENERATE SYLLABUS ID → #SYL.001, #SYL.002 ...
    // ─────────────────────────────────────────────────────────────────────────

    private String generateSyllabusId() {
        Optional<Syllabus> lastOpt = syllabusRepository.findTopByOrderByIdDesc();
        int nextNumber = 1;
        if (lastOpt.isPresent() && lastOpt.get().getSyllabusId() != null) {
            String lastId = lastOpt.get().getSyllabusId(); // e.g. "#SYL.007"
            try {
                String numPart = lastId.replace("#SYL.", "");
                nextNumber = Integer.parseInt(numPart) + 1;
            } catch (NumberFormatException ignored) {}
        }
        return "#SYL." + String.format("%03d", nextNumber);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // FILE TYPE & SIZE HELPERS
    // ─────────────────────────────────────────────────────────────────────────

    private String resolveFileType(MultipartFile file) {
        String name = file.getOriginalFilename();
        if (name == null || !name.contains(".")) return "UNKNOWN";
        String ext = name.substring(name.lastIndexOf(".") + 1).toUpperCase();
        return switch (ext) {
            case "PDF"          -> "PDF";
            case "DOC", "DOCX"  -> "DOC";
            default             -> ext;
        };
    }

    private String formatFileSize(long bytes) {
        if (bytes < 1024)                return bytes + " B";
        if (bytes < 1024 * 1024)         return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1024L * 1024 * 1024) return String.format("%.1f MB", bytes / (1024.0 * 1024));
        return String.format("%.2f GB", bytes / (1024.0 * 1024 * 1024));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // RESOLVE FK HELPERS
    // ─────────────────────────────────────────────────────────────────────────

    private School resolveSchool(Long schoolId) {
        return schoolRepository.findBySchoolId(schoolId)
                .orElseThrow(() -> new RuntimeException("School not found"));
    }

    private AcademicYear resolveAcademicYear(Long academicYearId, Long schoolId) {
        return academicYearRepository
                .findByAcademicYearIdAndSchool_SchoolId(academicYearId, schoolId)
                .orElseThrow(() -> new RuntimeException("Academic year not found for this school"));
    }

    private AddSubject resolveSubject(Long subjectId, Long schoolId) {
        AddSubject subject = subjectRepository.findBySubjectId(subjectId)
                .orElseThrow(() -> new RuntimeException("Subject not found"));
        if (!subject.getSchool().getSchoolId().equals(schoolId))
            throw new RuntimeException("Subject does not belong to this school");
        return subject;
    }

    private AddStandard resolveStandard(Long standardId, Long schoolId) {
        AddStandard standard = standardRepository.findByStandardId(standardId)
                .orElseThrow(() -> new RuntimeException("Standard not found"));
        if (!standard.getSchool().getSchoolId().equals(schoolId))
            throw new RuntimeException("Standard does not belong to this school");
        return standard;
    }

    private AddTeacher resolveTeacher(Long teacherId, Long schoolId) {
        return teacherRepository.findByIdAndSchool_SchoolId(teacherId, schoolId)
                .orElseThrow(() -> new RuntimeException("Teacher not found for this school"));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 1. CREATE
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public SyllabusDTO createSyllabus(SyllabusDTO dto, MultipartFile file) {

        String syllabusId = generateSyllabusId();

        // Resolve all FK entities
        School       school       = resolveSchool(dto.getSchoolId());
        AcademicYear academicYear = resolveAcademicYear(dto.getAcademicYearId(), dto.getSchoolId());
        AddSubject   subject      = resolveSubject(dto.getSubjectId(), dto.getSchoolId());
        AddStandard  standard     = resolveStandard(dto.getStandardId(), dto.getSchoolId());
        AddTeacher   teacher      = resolveTeacher(dto.getTeacherId(), dto.getSchoolId());

        // Upload file to S3
        String s3Key = uploadToS3(file, syllabusId);

        // Build entity
        Syllabus syllabus = new Syllabus();
        syllabus.setSyllabusId(syllabusId);
        syllabus.setTitle(dto.getTitle().trim());
        syllabus.setTotalChapters(dto.getTotalChapters());
        syllabus.setCompletedChapters(0);
        syllabus.setStatus(dto.getStatus() != null ? dto.getStatus() : "ACTIVE");
        syllabus.setS3Key(s3Key);
        syllabus.setFileUrl(generatePresignedUrl(s3Key));
        syllabus.setFileType(resolveFileType(file));
        syllabus.setFileSize(formatFileSize(file.getSize()));
        syllabus.setSchool(school);
        syllabus.setAcademicYear(academicYear);
        syllabus.setSubject(subject);
        syllabus.setStandard(standard);
        syllabus.setTeacher(teacher);

        syllabusRepository.save(syllabus);

        SyllabusDTO result = new SyllabusDTO();
        result.setSyllabusId(syllabusId);
        return result;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 2. GET BY ID
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public SyllabusDTO getSyllabusById(Long id, Long schoolId) {
        Syllabus syllabus = syllabusRepository.findByIdAndSchool_SchoolId(id, schoolId)
                .orElseThrow(() -> new RuntimeException("Syllabus not found"));
        return toDTO(syllabus);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 3. GET ALL (with optional status filter)
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public List<SyllabusDTO> getAllSyllabus(Long schoolId, Long academicYearId, String status) {

        List<Syllabus> list;

        if (status != null && !status.isBlank()) {
            list = syllabusRepository
                    .findBySchool_SchoolIdAndAcademicYear_AcademicYearIdAndStatus(
                            schoolId, academicYearId, status);
        } else {
            list = syllabusRepository
                    .findBySchool_SchoolIdAndAcademicYear_AcademicYearId(
                            schoolId, academicYearId);
        }

        return list.stream().map(this::toDTO).collect(Collectors.toList());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 4. UPDATE
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public void updateSyllabus(SyllabusDTO dto, MultipartFile file) {

        Syllabus syllabus = syllabusRepository.findByIdAndSchool_SchoolId(dto.getId(), dto.getSchoolId())
                .orElseThrow(() -> new RuntimeException("Syllabus not found"));

        Long schoolId = syllabus.getSchool().getSchoolId();

        // Update simple fields if provided
        if (dto.getTitle() != null && !dto.getTitle().isBlank())
            syllabus.setTitle(dto.getTitle().trim());

        if (dto.getTotalChapters() != null)
            syllabus.setTotalChapters(dto.getTotalChapters());

        if (dto.getStatus() != null && !dto.getStatus().isBlank())
            syllabus.setStatus(dto.getStatus());

        // Update FK references if provided
        if (dto.getSubjectId() != null)
            syllabus.setSubject(resolveSubject(dto.getSubjectId(), schoolId));

        if (dto.getStandardId() != null)
            syllabus.setStandard(resolveStandard(dto.getStandardId(), schoolId));

        if (dto.getTeacherId() != null)
            syllabus.setTeacher(resolveTeacher(dto.getTeacherId(), schoolId));

        // Replace file if a new one is uploaded
        if (file != null && !file.isEmpty()) {
            deleteFromS3(syllabus.getS3Key());
            String newS3Key = uploadToS3(file, syllabus.getSyllabusId());
            syllabus.setS3Key(newS3Key);
            syllabus.setFileUrl(generatePresignedUrl(newS3Key));
            syllabus.setFileType(resolveFileType(file));
            syllabus.setFileSize(formatFileSize(file.getSize()));
        }

        syllabusRepository.save(syllabus);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 5. DELETE
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public void deleteSyllabus(Long id, Long schoolId) {
        Syllabus syllabus = syllabusRepository.findByIdAndSchool_SchoolId(id, schoolId)
                .orElseThrow(() -> new RuntimeException("Syllabus not found"));

        deleteFromS3(syllabus.getS3Key());
        syllabusRepository.delete(syllabus);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 6. UPDATE PROGRESS
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public SyllabusDTO updateProgress(Long id, Long schoolId, Integer completedChapters) {

        Syllabus syllabus = syllabusRepository.findByIdAndSchool_SchoolId(id, schoolId)
                .orElseThrow(() -> new RuntimeException("Syllabus not found"));

        if (completedChapters < 0)
            throw new RuntimeException("completedChapters cannot be negative");

        if (completedChapters > syllabus.getTotalChapters())
            throw new RuntimeException("completedChapters (" + completedChapters
                    + ") cannot exceed totalChapters (" + syllabus.getTotalChapters() + ")");

        syllabus.setCompletedChapters(completedChapters);

        // Auto-complete when all chapters are done
        if (completedChapters.equals(syllabus.getTotalChapters())) {
            syllabus.setStatus("COMPLETED");
        }

        syllabusRepository.save(syllabus);

        return toDTO(syllabus);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 7. STATS
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public Map<String, Object> getStats(Long schoolId, Long academicYearId) {

        Map<String, Object> stats = new HashMap<>();

        long total     = syllabusRepository.countBySchool_SchoolIdAndAcademicYear_AcademicYearId(schoolId, academicYearId);
        long active    = syllabusRepository.countBySchool_SchoolIdAndAcademicYear_AcademicYearIdAndStatus(schoolId, academicYearId, "ACTIVE");
        long completed = syllabusRepository.countBySchool_SchoolIdAndAcademicYear_AcademicYearIdAndStatus(schoolId, academicYearId, "COMPLETED");
        long draft     = syllabusRepository.countBySchool_SchoolIdAndAcademicYear_AcademicYearIdAndStatus(schoolId, academicYearId, "DRAFT");

        // Calculate overall progress: average of all individual progress percentages
        int overallProgress = 0;
        if (total > 0) {
            List<Syllabus> all = syllabusRepository
                    .findBySchool_SchoolIdAndAcademicYear_AcademicYearId(schoolId, academicYearId);

            double sumPercent = 0;
            for (Syllabus s : all) {
                if (s.getTotalChapters() != null && s.getTotalChapters() > 0) {
                    int comp = s.getCompletedChapters() != null ? s.getCompletedChapters() : 0;
                    sumPercent += (comp * 100.0) / s.getTotalChapters();
                }
            }
            overallProgress = (int) Math.round(sumPercent / total);
        }

        stats.put("totalSyllabus", total);
        stats.put("active", active);
        stats.put("completed", completed);
        stats.put("draft", draft);
        stats.put("overallProgress", overallProgress);

        return stats;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 8. DOWNLOAD
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public String downloadSyllabus(Long id, Long schoolId) {
        Syllabus syllabus = syllabusRepository.findByIdAndSchool_SchoolId(id, schoolId)
                .orElseThrow(() -> new RuntimeException("Syllabus not found"));

        if (syllabus.getS3Key() == null || syllabus.getS3Key().isBlank())
            throw new RuntimeException("File not found in storage");

        return generatePresignedUrl(syllabus.getS3Key());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 9. UPDATE STATUS
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public void updateStatus(Long id, Long schoolId, String status) {
        Syllabus syllabus = syllabusRepository.findByIdAndSchool_SchoolId(id, schoolId)
                .orElseThrow(() -> new RuntimeException("Syllabus not found"));

        if (!"ACTIVE".equals(status) && !"COMPLETED".equals(status) && !"DRAFT".equals(status))
            throw new RuntimeException("Invalid status. Must be ACTIVE, COMPLETED, or DRAFT");

        syllabus.setStatus(status);
        syllabusRepository.save(syllabus);
    }
}
