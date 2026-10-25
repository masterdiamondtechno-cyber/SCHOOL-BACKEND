package com.schoolmanagement.school.serviceImpl;

import java.io.IOException;
import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.schoolmanagement.school.dto.StudyMaterialDTO;
import com.schoolmanagement.school.entity.AcademicYear;
import com.schoolmanagement.school.entity.AddDivision;
import com.schoolmanagement.school.entity.AddStandard;
import com.schoolmanagement.school.entity.AddSubject;
import com.schoolmanagement.school.entity.AddTeacher;
import com.schoolmanagement.school.entity.MaterialType;
import com.schoolmanagement.school.entity.School;
import com.schoolmanagement.school.entity.StudyMaterial;
import com.schoolmanagement.school.repository.AcademicYearRepository;
import com.schoolmanagement.school.repository.AddDivisionRepository;
import com.schoolmanagement.school.repository.AddStandardRepository;
import com.schoolmanagement.school.repository.AddSubjectRepository;
import com.schoolmanagement.school.repository.AddTeacherRepository;
import com.schoolmanagement.school.repository.SchoolRepository;
import com.schoolmanagement.school.repository.StudyMaterialRepository;
import com.schoolmanagement.school.service.StudyMaterialService;

import com.schoolmanagement.school.service.LocalFileStorageService;

@Service
public class StudyMaterialServiceImpl implements StudyMaterialService {

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd MMM yyyy");

    @Autowired private StudyMaterialRepository studyMaterialRepository;
    @Autowired private SchoolRepository        schoolRepository;
    @Autowired private AcademicYearRepository  academicYearRepository;
    @Autowired private AddSubjectRepository    subjectRepository;
    @Autowired private AddStandardRepository   standardRepository;
    @Autowired private AddDivisionRepository   divisionRepository;
    @Autowired private AddTeacherRepository    teacherRepository;
    @Autowired private LocalFileStorageService  fileStorageService;

    // ─────────────────────────────────────────────────────────────────────────
    // ENTITY → DTO CONVERTER
    // ─────────────────────────────────────────────────────────────────────────

    private StudyMaterialDTO toDTO(StudyMaterial m) {
        StudyMaterialDTO dto = new StudyMaterialDTO();

        dto.setId(m.getId());
        dto.setMaterialId(m.getMaterialId());
        dto.setTitle(m.getTitle());
        // DTO carries the human-readable label (e.g. "Practice Sheet") for JSON responses
        dto.setMaterialType(m.getMaterialType() != null ? m.getMaterialType().getLabel() : null);
        dto.setStatus(m.getStatus());
        dto.setFileType(m.getFileType());
        dto.setFileSize(m.getFileSize());
        dto.setDownloadCount(m.getDownloadCount());
        dto.setUploadedByRole(m.getUploadedByRole());

        if (m.getUploadDate() != null)
            dto.setUploadDate(m.getUploadDate().format(DATE_FORMATTER));

        if (m.getSchool() != null)
            dto.setSchoolId(m.getSchool().getSchoolId());

        if (m.getAcademicYear() != null)
            dto.setAcademicYearId(m.getAcademicYear().getAcademicYearId());

        if (m.getSubject() != null) {
            dto.setSubjectId(m.getSubject().getSubjectId());
            dto.setSubjectName(m.getSubject().getSubjectName());
        }

        if (m.getStandard() != null) {
            dto.setStandardId(m.getStandard().getStandardId());
            dto.setStandardName(m.getStandard().getStandard());
        }

        if (m.getDivision() != null) {
            dto.setDivisionId(m.getDivision().getDivisionId());
            dto.setDivisionName(m.getDivision().getDivisionName());
        }

        if (m.getTeacher() != null) {
            dto.setTeacherId(m.getTeacher().getId());
            dto.setTeacherName(m.getTeacher().getTeacherName());
        }

        // Build "10-A" class display
        if (m.getStandard() != null && m.getDivision() != null)
            dto.setClassDisplay(m.getStandard().getStandard() + "-" + m.getDivision().getDivisionName());

        // Generate a fresh presigned URL for access (valid 1 hour)
        if (m.getS3Key() != null && !m.getS3Key().isBlank())
            dto.setFileUrl(m.getS3Key());
        else if (m.getFileUrl() != null && !m.getFileUrl().isBlank())
            dto.setFileUrl(m.getFileUrl());

        return dto;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // AUTO-GENERATE MATERIAL ID  →  SM001, SM002, SM003 ...
    // ─────────────────────────────────────────────────────────────────────────

    private String generateMaterialId() {
        Optional<StudyMaterial> lastOpt = studyMaterialRepository.findTopByOrderByIdDesc();
        int nextNumber = 1;
        if (lastOpt.isPresent() && lastOpt.get().getMaterialId() != null) {
            String lastId = lastOpt.get().getMaterialId(); // e.g. "SM007"
            try {
                nextNumber = Integer.parseInt(lastId.substring(2)) + 1;
            } catch (NumberFormatException ignored) {}
        }
        return "SM" + String.format("%03d", nextNumber); // SM001 ... SM999
    }

    // ─────────────────────────────────────────────────────────────────────────
    // FILE TYPE & SIZE HELPERS
    // ─────────────────────────────────────────────────────────────────────────

    private String resolveFileType(MultipartFile file) {
        String name = file.getOriginalFilename();
        if (name == null || !name.contains(".")) return "UNKNOWN";
        String ext = name.substring(name.lastIndexOf(".") + 1).toUpperCase();
        return switch (ext) {
            case "PDF"               -> "PDF";
            case "DOC", "DOCX"       -> "DOC";
            case "PPT", "PPTX"       -> "PPT";
            case "ZIP", "RAR", "7Z"  -> "ZIP";
            case "XLS", "XLSX"       -> "XLS";
            default                   -> ext;
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

    private AddDivision resolveDivision(Long divisionId, Long schoolId) {
        AddDivision division = divisionRepository.findByDivisionId(divisionId)
                .orElseThrow(() -> new RuntimeException("Division not found"));
        if (!division.getSchool().getSchoolId().equals(schoolId))
            throw new RuntimeException("Division does not belong to this school");
        return division;
    }

    private AddTeacher resolveTeacher(Long teacherId, Long schoolId) {
        return teacherRepository.findByIdAndSchool_SchoolId(teacherId, schoolId)
                .orElseThrow(() -> new RuntimeException("Teacher not found for this school"));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 1. CREATE
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public void createStudyMaterial(StudyMaterialDTO dto, MultipartFile file,
                                    String uploadedByRole) {

        // ── 1. Auto-generate business ID ──────────────────────────────────────
        String materialId = generateMaterialId();

        // ── 2. Resolve all FK entities ────────────────────────────────────────
        School       school      = resolveSchool(dto.getSchoolId());
        AcademicYear academicYear = resolveAcademicYear(dto.getAcademicYearId(), dto.getSchoolId());
        AddSubject   subject     = resolveSubject(dto.getSubjectId(),  dto.getSchoolId());
        AddStandard  standard    = resolveStandard(dto.getStandardId(), dto.getSchoolId());
        AddDivision  division    = resolveDivision(dto.getDivisionId(), dto.getSchoolId());
        AddTeacher   teacher     = resolveTeacher(dto.getTeacherId(),  dto.getSchoolId());

        // ── 3. Upload file to Local Storage ───────────────────────────────────
        String fileUrl = fileStorageService.storeFile(file, LocalFileStorageService.MODULE_STUDY_MATERIALS, materialId);

        // ── 4. Build and save entity ──────────────────────────────────────────
        StudyMaterial material = new StudyMaterial();
        material.setMaterialId(materialId);
        material.setTitle(dto.getTitle().trim());
        // Parse the incoming String to the enum (accepts both "NOTES" and "Notes")
        material.setMaterialType(MaterialType.fromValue(dto.getMaterialType()));
        material.setStatus(dto.getStatus());
        material.setS3Key(fileUrl);
        material.setFileUrl(fileUrl);
        material.setFileType(resolveFileType(file));
        material.setFileSize(formatFileSize(file.getSize()));
        material.setDownloadCount(0L);
        material.setUploadedByRole(uploadedByRole);
        material.setSchool(school);
        material.setAcademicYear(academicYear);
        material.setSubject(subject);
        material.setStandard(standard);
        material.setDivision(division);
        material.setTeacher(teacher);

        studyMaterialRepository.save(material);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 2. GET BY ID
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public StudyMaterialDTO getStudyMaterialById(Long id) {
        StudyMaterial material = studyMaterialRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Study material not found"));
        return toDTO(material);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 3. GET ALL (School admin — full list with optional filters)
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public List<StudyMaterialDTO> getAllBySchool(Long schoolId, Long academicYearId,
                                                 String materialType) {
        List<StudyMaterial> list;

        boolean hasYear = academicYearId != null;

        // Convert String filter to enum — null / blank / "All" means no filter
        MaterialType typeFilter = null;
        if (materialType != null && !materialType.isBlank() && !materialType.equalsIgnoreCase("All")) {
            typeFilter = MaterialType.fromValue(materialType);
        }
        boolean hasType = typeFilter != null;

        if (hasYear && hasType) {
            list = studyMaterialRepository
                    .findBySchool_SchoolIdAndAcademicYear_AcademicYearIdAndMaterialType(
                            schoolId, academicYearId, typeFilter);
        } else if (hasYear) {
            list = studyMaterialRepository
                    .findBySchool_SchoolIdAndAcademicYear_AcademicYearId(schoolId, academicYearId);
        } else if (hasType) {
            list = studyMaterialRepository
                    .findBySchool_SchoolIdAndMaterialType(schoolId, typeFilter);
        } else {
            list = studyMaterialRepository.findBySchool_SchoolId(schoolId);
        }

        return list.stream().map(this::toDTO).collect(Collectors.toList());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 4. GET BY DIVISION (Student access — only Published materials)
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public List<StudyMaterialDTO> getByDivision(Long divisionId, Long academicYearId) {
        List<StudyMaterial> list;

        if (academicYearId != null) {
            list = studyMaterialRepository
                    .findByDivision_DivisionIdAndAcademicYear_AcademicYearIdAndStatus(
                            divisionId, academicYearId, "Published");
        } else {
            list = studyMaterialRepository
                    .findByDivision_DivisionIdAndStatus(divisionId, "Published");
        }

        return list.stream().map(this::toDTO).collect(Collectors.toList());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 5. GET BY TEACHER (Teacher's own materials)
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public List<StudyMaterialDTO> getByTeacher(Long teacherId, Long schoolId) {
        List<StudyMaterial> list = studyMaterialRepository
                .findByTeacher_IdAndSchool_SchoolId(teacherId, schoolId);
        return list.stream().map(this::toDTO).collect(Collectors.toList());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 6. UPDATE
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public void updateStudyMaterial(StudyMaterialDTO dto, MultipartFile file,
                                    Long requestingTeacherId, String requestingRole) {

        StudyMaterial material = studyMaterialRepository.findById(dto.getId())
                .orElseThrow(() -> new RuntimeException("Study material not found"));

        // ── Ownership check for TEACHER role ─────────────────────────────────
        if ("ROLE_TEACHER".equals(requestingRole)) {
            if (material.getTeacher() == null ||
                    !material.getTeacher().getId().equals(requestingTeacherId)) {
                throw new RuntimeException("Access denied: You can only edit your own study materials");
            }
        }

        // ── Update simple fields if provided ──────────────────────────────────
        if (dto.getTitle() != null && !dto.getTitle().isBlank())
            material.setTitle(dto.getTitle().trim());

        if (dto.getMaterialType() != null && !dto.getMaterialType().isBlank())
            // Parse the incoming String to the enum (accepts both "NOTES" and "Notes")
            material.setMaterialType(MaterialType.fromValue(dto.getMaterialType()));

        if (dto.getStatus() != null && !dto.getStatus().isBlank())
            material.setStatus(dto.getStatus());

        // ── Update FK references if provided ──────────────────────────────────
        Long schoolId = material.getSchool().getSchoolId();

        if (dto.getSubjectId() != null)
            material.setSubject(resolveSubject(dto.getSubjectId(), schoolId));

        if (dto.getStandardId() != null)
            material.setStandard(resolveStandard(dto.getStandardId(), schoolId));

        if (dto.getDivisionId() != null)
            material.setDivision(resolveDivision(dto.getDivisionId(), schoolId));

        if (dto.getTeacherId() != null && "ROLE_SCHOOL".equals(requestingRole))
            material.setTeacher(resolveTeacher(dto.getTeacherId(), schoolId));

        // ── Replace file if a new one is uploaded (delete old from local storage first) ──
        if (file != null && !file.isEmpty()) {
            fileStorageService.deleteFile(material.getS3Key());
            String newFileUrl = fileStorageService.storeFile(file, LocalFileStorageService.MODULE_STUDY_MATERIALS, material.getMaterialId());
            material.setS3Key(newFileUrl);
            material.setFileUrl(newFileUrl);
            material.setFileType(resolveFileType(file));
            material.setFileSize(formatFileSize(file.getSize()));
        }

        studyMaterialRepository.save(material);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 7. DELETE
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public void deleteStudyMaterial(Long id, Long requestingTeacherId, String requestingRole) {
        StudyMaterial material = studyMaterialRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Study material not found"));

        // ── Ownership check for TEACHER role ─────────────────────────────────
        if ("ROLE_TEACHER".equals(requestingRole)) {
            if (material.getTeacher() == null ||
                    !material.getTeacher().getId().equals(requestingTeacherId)) {
                throw new RuntimeException("Access denied: You can only delete your own study materials");
            }
        }

        fileStorageService.deleteFile(material.getS3Key());
        studyMaterialRepository.delete(material);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 8. DOWNLOAD (role-aware access + increment count + return file URL)
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public String downloadStudyMaterial(Long id, Long requestingTeacherId,
                                        String requestingRole, Long requestingDivisionId) {

        StudyMaterial material = studyMaterialRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Study material not found"));

        // ── Access control ────────────────────────────────────────────────────
        if ("ROLE_STUDENT".equals(requestingRole)) {
            // Students: only if the material belongs to their division AND is Published
            if (material.getDivision() == null ||
                    !material.getDivision().getDivisionId().equals(requestingDivisionId)) {
                throw new RuntimeException("Access denied: This material is not available for your division");
            }
            if (!"Published".equals(material.getStatus())) {
                throw new RuntimeException("Access denied: This material is not published yet");
            }
        } else if ("ROLE_TEACHER".equals(requestingRole)) {
            // Teachers: only if they are the one who uploaded it
            if (material.getTeacher() == null ||
                    !material.getTeacher().getId().equals(requestingTeacherId)) {
                throw new RuntimeException("Access denied: You can only download your own study materials");
            }
        }
        // ROLE_SCHOOL: full access — no restriction

        if (material.getS3Key() == null || material.getS3Key().isBlank())
            throw new RuntimeException("File not found in storage");

        // ── Increment download count ───────────────────────────────────────────
        material.setDownloadCount(material.getDownloadCount() + 1);
        studyMaterialRepository.save(material);

        // ── Return direct URL to file ─────────────────────────────────────────
        return material.getS3Key();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 9. STATS
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public Map<String, Object> getStats(Long schoolId, Long academicYearId) {
        Map<String, Object> stats = new HashMap<>();

        boolean hasYear = academicYearId != null;

        long total = hasYear
                ? studyMaterialRepository
                        .countBySchool_SchoolIdAndAcademicYear_AcademicYearId(schoolId, academicYearId)
                : studyMaterialRepository
                        .countBySchool_SchoolId(schoolId);

        long published = hasYear
                ? studyMaterialRepository
                        .countBySchool_SchoolIdAndAcademicYear_AcademicYearIdAndStatus(
                                schoolId, academicYearId, "Published")
                : studyMaterialRepository
                        .countBySchool_SchoolIdAndStatus(schoolId, "Published");

        Long totalDownloads = hasYear
                ? studyMaterialRepository
                        .sumDownloadsBySchoolIdAndAcademicYearId(schoolId, academicYearId)
                : studyMaterialRepository
                        .sumDownloadsBySchoolId(schoolId);

        stats.put("totalMaterials", total);
        stats.put("published",      published);
        stats.put("totalDownloads", totalDownloads != null ? totalDownloads : 0L);

        return stats;
    }
}
