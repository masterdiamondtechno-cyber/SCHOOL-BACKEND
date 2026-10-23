package com.schoolmanagement.school.serviceImpl;

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

import com.schoolmanagement.school.dto.SyllabusDTO;
import com.schoolmanagement.school.entity.AcademicYear;
import com.schoolmanagement.school.entity.AddStandard;
import com.schoolmanagement.school.entity.AddSubject;
import com.schoolmanagement.school.entity.AddTeacher;
import com.schoolmanagement.school.entity.School;
import com.schoolmanagement.school.entity.Syllabus;
import com.schoolmanagement.school.repository.AcademicYearRepository;
import com.schoolmanagement.school.repository.AddStandardRepository;
import com.schoolmanagement.school.repository.AddSubjectRepository;
import com.schoolmanagement.school.repository.AddTeacherRepository;
import com.schoolmanagement.school.repository.SchoolRepository;
import com.schoolmanagement.school.repository.SyllabusRepository;
import com.schoolmanagement.school.service.SyllabusService;

import com.schoolmanagement.school.service.LocalFileStorageService;

@Service
public class SyllabusServiceImpl implements SyllabusService {

    @Autowired private SyllabusRepository     syllabusRepository;
    @Autowired private SchoolRepository       schoolRepository;
    @Autowired private AcademicYearRepository academicYearRepository;
    @Autowired private AddSubjectRepository   subjectRepository;
    @Autowired private AddStandardRepository  standardRepository;
    @Autowired private AddTeacherRepository   teacherRepository;
    @Autowired private LocalFileStorageService fileStorageService;

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

        // File URL mapping
        if (s.getS3Key() != null && !s.getS3Key().isBlank())
            dto.setFileUrl(s.getS3Key());
        else if (s.getFileUrl() != null && !s.getFileUrl().isBlank())
            dto.setFileUrl(s.getFileUrl());

        return dto;
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

        // Upload file to Local Storage
        String fileUrl = fileStorageService.storeFile(file, LocalFileStorageService.MODULE_SYLLABUS, syllabusId.replace("#", "").replace(".", ""));

        // Build entity
        Syllabus syllabus = new Syllabus();
        syllabus.setSyllabusId(syllabusId);
        syllabus.setTitle(dto.getTitle().trim());
        syllabus.setTotalChapters(dto.getTotalChapters());
        syllabus.setCompletedChapters(0);
        syllabus.setStatus(dto.getStatus() != null ? dto.getStatus() : "ACTIVE");
        syllabus.setS3Key(fileUrl);
        syllabus.setFileUrl(fileUrl);
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
            fileStorageService.deleteFile(syllabus.getS3Key());
            String newFileUrl = fileStorageService.storeFile(file, LocalFileStorageService.MODULE_SYLLABUS, syllabus.getSyllabusId().replace("#", "").replace(".", ""));
            syllabus.setS3Key(newFileUrl);
            syllabus.setFileUrl(newFileUrl);
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

        fileStorageService.deleteFile(syllabus.getS3Key());
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

        return syllabus.getS3Key();
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
