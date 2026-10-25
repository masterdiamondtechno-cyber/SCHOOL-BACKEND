package com.schoolmanagement.school.serviceImpl;

import java.io.File;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.schoolmanagement.school.dto.LectureViewDTO;
import com.schoolmanagement.school.dto.RecordedLectureDTO;
import com.schoolmanagement.school.entity.AcademicYear;
import com.schoolmanagement.school.entity.AddDivision;
import com.schoolmanagement.school.entity.AddStandard;
import com.schoolmanagement.school.entity.AddStudent;
import com.schoolmanagement.school.entity.AddSubject;
import com.schoolmanagement.school.entity.AddTeacher;
import com.schoolmanagement.school.entity.LectureView;
import com.schoolmanagement.school.entity.RecordedLecture;
import com.schoolmanagement.school.entity.School;
import com.schoolmanagement.school.repository.AcademicYearRepository;
import com.schoolmanagement.school.repository.AddDivisionRepository;
import com.schoolmanagement.school.repository.AddStandardRepository;
import com.schoolmanagement.school.repository.AddStudentRepository;
import com.schoolmanagement.school.repository.AddSubjectRepository;
import com.schoolmanagement.school.repository.AddTeacherRepository;
import com.schoolmanagement.school.repository.LectureViewRepository;
import com.schoolmanagement.school.repository.RecordedLectureRepository;
import com.schoolmanagement.school.repository.SchoolRepository;
import com.schoolmanagement.school.service.RecordedLectureService;

@Service
public class RecordedLectureServiceImpl implements RecordedLectureService {

    // Local folder prefix for all lecture videos — served via /uploads/**
    private static final String LECTURE_PREFIX = "/uploads/lectures/";

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd MMM yyyy");

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    @Autowired
    private RecordedLectureRepository recordedLectureRepository;
    @Autowired
    private SchoolRepository schoolRepository;
    @Autowired
    private AcademicYearRepository academicYearRepository;
    @Autowired
    private AddSubjectRepository subjectRepository;
    @Autowired
    private AddStandardRepository standardRepository;
    @Autowired
    private AddDivisionRepository divisionRepository;
    @Autowired
    private AddTeacherRepository teacherRepository;
    @Autowired
    private AddStudentRepository studentRepository;
    @Autowired
    private LectureViewRepository lectureViewRepository;
    @Autowired
    private S3AsyncService s3AsyncService;   // local async file mover (reused class name)

    // ─────────────────────────────────────────────────────────────────────────
    // ENTITY → DTO CONVERTER
    // ─────────────────────────────────────────────────────────────────────────

    private RecordedLectureDTO toDTO(RecordedLecture r) {
        RecordedLectureDTO dto = new RecordedLectureDTO();

        dto.setId(r.getId());
        dto.setLectureId(r.getLectureId());
        dto.setTitle(r.getTitle());
        dto.setStatus(r.getStatus());
        dto.setFileType(r.getFileType());
        dto.setFileSize(r.getFileSize());
        dto.setDuration(r.getDuration());
        dto.setViewCount(r.getViewCount());

        if (r.getRecordedDate() != null)
            dto.setRecordedDate(r.getRecordedDate().format(DATE_FORMATTER));

        if (r.getSchool() != null)
            dto.setSchoolId(r.getSchool().getSchoolId());

        if (r.getAcademicYear() != null)
            dto.setAcademicYearId(r.getAcademicYear().getAcademicYearId());

        if (r.getSubject() != null) {
            dto.setSubjectId(r.getSubject().getSubjectId());
            dto.setSubjectName(r.getSubject().getSubjectName());
        }

        if (r.getStandard() != null) {
            dto.setStandardId(r.getStandard().getStandardId());
            dto.setStandardName(r.getStandard().getStandard());
        }

        if (r.getDivision() != null) {
            dto.setDivisionId(r.getDivision().getDivisionId());
            dto.setDivisionName(r.getDivision().getDivisionName());
        }

        if (r.getTeacher() != null) {
            dto.setTeacherId(r.getTeacher().getId());
            dto.setTeacherName(r.getTeacher().getTeacherName());
        }

        // Build "10-A" class display
        if (r.getStandard() != null && r.getDivision() != null)
            dto.setClassDisplay(r.getStandard().getStandard() + "-" + r.getDivision().getDivisionName());

        // File URL: s3Key now stores the local relative path /uploads/lectures/...
        if (r.getS3Key() != null && !r.getS3Key().isBlank())
            dto.setFileUrl(r.getS3Key());

        return dto;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // LOCAL FILE HELPERS
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Deletes a local lecture file (silent — does not throw if path is null/blank).
     * localPath looks like: /uploads/lectures/RL001_video.mp4
     */
    private void deleteLocalFile(String localPath) {
        if (localPath == null || localPath.isBlank()) return;
        try {
            // Strip leading slash and resolve against working directory
            String relative = localPath.startsWith("/") ? localPath.substring(1) : localPath;
            java.nio.file.Path path = java.nio.file.Paths.get(relative);
            java.nio.file.Files.deleteIfExists(path);
        } catch (Exception ignored) {
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // AUTO-GENERATE LECTURE ID → RL001, RL002, RL003 ...
    // ─────────────────────────────────────────────────────────────────────────

    private String generateLectureId() {
        Optional<RecordedLecture> lastOpt = recordedLectureRepository.findTopByOrderByIdDesc();
        int nextNumber = 1;
        if (lastOpt.isPresent() && lastOpt.get().getLectureId() != null) {
            String lastId = lastOpt.get().getLectureId(); // e.g. "RL007"
            try {
                nextNumber = Integer.parseInt(lastId.substring(2)) + 1;
            } catch (NumberFormatException ignored) {
            }
        }
        return "RL" + String.format("%03d", nextNumber); // RL001 ... RL999
    }

    // ─────────────────────────────────────────────────────────────────────────
    // FILE TYPE & SIZE HELPERS
    // ─────────────────────────────────────────────────────────────────────────

    private String resolveFileType(MultipartFile file) {
        String name = file.getOriginalFilename();
        if (name == null || !name.contains("."))
            return "UNKNOWN";
        return name.substring(name.lastIndexOf(".") + 1).toUpperCase();
    }

    private String formatFileSize(long bytes) {
        if (bytes < 1024)
            return bytes + " B";
        if (bytes < 1024 * 1024)
            return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1024L * 1024 * 1024)
            return String.format("%.1f MB", bytes / (1024.0 * 1024));
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
    @Transactional
    public void createLecture(RecordedLectureDTO dto, MultipartFile file,
            String uploadedByRole) {

        // ── 1. Resolve all FK entities ────────────────────────────────────────
        School school = resolveSchool(dto.getSchoolId());
        AcademicYear academicYear = resolveAcademicYear(dto.getAcademicYearId(), dto.getSchoolId());
        AddSubject subject = resolveSubject(dto.getSubjectId(), dto.getSchoolId());
        AddStandard standard = resolveStandard(dto.getStandardId(), dto.getSchoolId());
        AddDivision division = resolveDivision(dto.getDivisionId(), dto.getSchoolId());
        AddTeacher teacher = resolveTeacher(dto.getTeacherId(), dto.getSchoolId());

        // ── 2. Build initial entity with "Processing" or "Draft" status ──────
        String lectureId = generateLectureId();
        RecordedLecture lecture = new RecordedLecture();
        lecture.setLectureId(lectureId);
        lecture.setTitle(dto.getTitle().trim());
        lecture.setDuration(dto.getDuration());
        lecture.setViewCount(0L);
        lecture.setSchool(school);
        lecture.setAcademicYear(academicYear);
        lecture.setSubject(subject);
        lecture.setStandard(standard);
        lecture.setDivision(division);
        lecture.setTeacher(teacher);

        // Parse recordedDate
        if (dto.getRecordedDate() != null && !dto.getRecordedDate().isBlank()) {
            try {
                lecture.setRecordedDate(java.time.LocalDate.parse(dto.getRecordedDate(),
                        DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            } catch (Exception e) {
                try {
                    lecture.setRecordedDate(java.time.LocalDate.parse(dto.getRecordedDate(), DATE_FORMATTER));
                } catch (Exception ignored) {
                }
            }
        }

        // ── 3. Handle status based on file presence ──────────────────────────
        if (file == null || file.isEmpty()) {
            lecture.setStatus("Draft");
            recordedLectureRepository.save(lecture);
            return;
        }

        // If file is present, set to Processing first
        lecture.setStatus("Processing");
        lecture.setFileType(resolveFileType(file));
        lecture.setFileSize(formatFileSize(file.getSize()));

        // Save initial record so "Processing" is visible in DB immediately
        lecture = recordedLectureRepository.save(lecture);

        // ── 4. Prepare temp file and trigger async local file move ────────────
        try {
            String originalName = file.getOriginalFilename();
            String extension = "";
            if (originalName != null && originalName.contains("."))
                extension = originalName.substring(originalName.lastIndexOf("."));

            String safeName = (originalName != null)
                    ? originalName.replaceAll("[^a-zA-Z0-9._-]", "-")
                    : "file" + extension;

            // Unique filename: RL001_originalname.mp4
            String uniqueSafeName = lectureId + "_" + safeName;

            // fileKey is the relative URL path served by Spring static handler
            String fileKey = LECTURE_PREFIX + uniqueSafeName;

            // Write to a temp file (async method can't use MultipartFile after request ends)
            File tempFile = File.createTempFile("lecture_", extension);
            file.transferTo(tempFile);

            // Fire-and-forget: file move happens in background thread
            s3AsyncService.uploadLectureVideoAsync(
                    lecture.getId(), tempFile, fileKey,
                    file.getContentType(), uniqueSafeName);

        } catch (IOException e) {
            lecture.setStatus("Failed");
            recordedLectureRepository.save(lecture);
            throw new RuntimeException("Failed to prepare file for upload: " + e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 2. GET BY ID
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public RecordedLectureDTO getLectureById(Long id) {
        RecordedLecture lecture = recordedLectureRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Recorded lecture not found"));
        return toDTO(lecture);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 3. GET ALL (School admin — full list with optional filters)
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public List<RecordedLectureDTO> getAllBySchool(Long schoolId, Long academicYearId,
                                            String status, Long subjectId,
                                            Long lectureId, Long requestingTeacherId) {
        
        // ── Fetch candidates for the school and year ──────────────────────
        List<RecordedLecture> list = recordedLectureRepository.findAll().stream()
                .filter(l -> schoolId == null || (l.getSchool() != null && l.getSchool().getSchoolId().equals(schoolId)))
                .filter(l -> academicYearId == null || (l.getAcademicYear() != null && l.getAcademicYear().getAcademicYearId().equals(academicYearId)))
                .collect(Collectors.toList());

        return list.stream()
                // Filter by specific ID if provided
                .filter(l -> lectureId == null || l.getId().equals(lectureId))
                // Filter by teacher if provided (for ROLE_TEACHER)
                .filter(l -> requestingTeacherId == null || 
                        (l.getTeacher() != null && l.getTeacher().getId().equals(requestingTeacherId)))
                // Filter by status
                .filter(l -> status == null || status.equalsIgnoreCase("All") || l.getStatus().equalsIgnoreCase(status))
                // Filter by subject
                .filter(l -> subjectId == null || 
                        (l.getSubject() != null && l.getSubject().getSubjectId().equals(subjectId)))
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 4. GET BY DIVISION (Student access — only Published lectures)
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public List<RecordedLectureDTO> getByDivision(Long divisionId, Long academicYearId) {
        List<RecordedLecture> list;

        if (academicYearId != null) {
            list = recordedLectureRepository
                    .findByDivision_DivisionIdAndAcademicYear_AcademicYearIdAndStatus(
                            divisionId, academicYearId, "Published");
        } else {
            list = recordedLectureRepository
                    .findByDivision_DivisionIdAndStatus(divisionId, "Published");
        }

        return list.stream().map(this::toDTO).collect(Collectors.toList());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 5. GET BY TEACHER (Teacher's own lectures)
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public List<RecordedLectureDTO> getByTeacher(Long teacherId, Long schoolId) {
        List<RecordedLecture> list = recordedLectureRepository
                .findByTeacher_IdAndSchool_SchoolId(teacherId, schoolId);
        return list.stream().map(this::toDTO).collect(Collectors.toList());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 6. UPDATE
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public void updateLecture(RecordedLectureDTO dto, MultipartFile file,
            Long requestingTeacherId, String requestingRole) {

        RecordedLecture lecture = recordedLectureRepository.findById(dto.getId())
                .orElseThrow(() -> new RuntimeException("Recorded lecture not found"));

        // ── Ownership check for TEACHER role ─────────────────────────────────
        if ("ROLE_TEACHER".equals(requestingRole)) {
            if (lecture.getTeacher() == null ||
                    !lecture.getTeacher().getId().equals(requestingTeacherId)) {
                throw new RuntimeException("Access denied: You can only edit your own recorded lectures");
            }
        }

        // ── Update simple fields if provided ──────────────────────────────────
        if (dto.getTitle() != null && !dto.getTitle().isBlank())
            lecture.setTitle(dto.getTitle().trim());

        if (dto.getStatus() != null && !dto.getStatus().isBlank())
            lecture.setStatus(dto.getStatus());

        if (dto.getDuration() != null)
            lecture.setDuration(dto.getDuration());

        if (dto.getRecordedDate() != null && !dto.getRecordedDate().isBlank()) {
            try {
                lecture.setRecordedDate(java.time.LocalDate.parse(dto.getRecordedDate(),
                        DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            } catch (Exception e) {
                try {
                    lecture.setRecordedDate(java.time.LocalDate.parse(dto.getRecordedDate(), DATE_FORMATTER));
                } catch (Exception ignored) {
                }
            }
        }

        // ── Update FK references if provided ──────────────────────────────────
        Long schoolId = lecture.getSchool().getSchoolId();

        if (dto.getSubjectId() != null)
            lecture.setSubject(resolveSubject(dto.getSubjectId(), schoolId));

        if (dto.getStandardId() != null)
            lecture.setStandard(resolveStandard(dto.getStandardId(), schoolId));

        if (dto.getDivisionId() != null)
            lecture.setDivision(resolveDivision(dto.getDivisionId(), schoolId));

        if (dto.getTeacherId() != null && "ROLE_SCHOOL".equals(requestingRole))
            lecture.setTeacher(resolveTeacher(dto.getTeacherId(), schoolId));

        // ── Replace video file if a new one is uploaded ───────────────────────
        if (file != null && !file.isEmpty()) {
            lecture.setStatus("Processing");
            recordedLectureRepository.save(lecture);

            try {
                // Delete old local file if exists
                if (lecture.getS3Key() != null) {
                    deleteLocalFile(lecture.getS3Key());
                }

                String originalName = file.getOriginalFilename();
                String extension = "";
                if (originalName != null && originalName.contains("."))
                    extension = originalName.substring(originalName.lastIndexOf("."));

                String safeName = (originalName != null)
                        ? originalName.replaceAll("[^a-zA-Z0-9._-]", "-")
                        : "file" + extension;

                String uniqueSafeName = lecture.getLectureId() + "_" + safeName;
                String fileKey = LECTURE_PREFIX + uniqueSafeName;

                // Write to temp and trigger async move
                File tempFile = File.createTempFile("lecture_", extension);
                file.transferTo(tempFile);

                s3AsyncService.uploadLectureVideoAsync(
                        lecture.getId(), tempFile, fileKey,
                        file.getContentType(), uniqueSafeName);

                lecture.setFileType(resolveFileType(file));
                lecture.setFileSize(formatFileSize(file.getSize()));

            } catch (Exception e) {
                lecture.setStatus("Failed");
                recordedLectureRepository.save(lecture);
                throw new RuntimeException("Upload failed: " + e.getMessage());
            }
        }

        recordedLectureRepository.save(lecture);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 7. DELETE
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public void deleteLecture(Long id, Long requestingTeacherId, String requestingRole) {
        RecordedLecture lecture = recordedLectureRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Recorded lecture not found"));

        // ── Ownership check for TEACHER role ─────────────────────────────────
        if ("ROLE_TEACHER".equals(requestingRole)) {
            if (lecture.getTeacher() == null ||
                    !lecture.getTeacher().getId().equals(requestingTeacherId)) {
                throw new RuntimeException("Access denied: You can only delete your own recorded lectures");
            }
        }

        deleteLocalFile(lecture.getS3Key());
        recordedLectureRepository.delete(lecture);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 8. STREAM (role-aware access + increment view count + direct URL)
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public String streamLecture(Long id, Long schoolId, Long academicYearId,
                               Long requestingTeacherId, String requestingRole,
                               Long requestingDivisionId, String requestingUserEmail) {
        
        RecordedLecture lecture = recordedLectureRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Lecture not found"));

        // ── Association Logic: Verify lecture belongs to school and academic year ──────────────────
        if (schoolId != null && !lecture.getSchool().getSchoolId().equals(schoolId)) {
            throw new RuntimeException("Access Denied: Lecture does not belong to the specified school");
        }
        if (academicYearId != null && !lecture.getAcademicYear().getAcademicYearId().equals(academicYearId)) {
            throw new RuntimeException("Access Denied: Lecture does not belong to the specified academic year");
        }

        // ── Access control ────────────────────────────────────────────────────
        if ("ROLE_STUDENT".equals(requestingRole)) {
            // Students: only if the lecture belongs to their division AND is Published
            if (lecture.getDivision() == null ||
                    !lecture.getDivision().getDivisionId().equals(requestingDivisionId)) {
                throw new RuntimeException("Access denied: This lecture is not available for your division");
            }
            if (!"Published".equals(lecture.getStatus())) {
                throw new RuntimeException("Access denied: This lecture is not published yet");
            }
        } else if ("ROLE_TEACHER".equals(requestingRole)) {
            // Teachers: only if they are the one who uploaded it
            if (lecture.getTeacher() == null ||
                    !lecture.getTeacher().getId().equals(requestingTeacherId)) {
                throw new RuntimeException("Access denied: You can only stream your own recorded lectures");
            }
        }
        // ROLE_SCHOOL: full access — no restriction

        if (lecture.getS3Key() == null || lecture.getS3Key().isBlank())
            throw new RuntimeException("Video file not found in storage");

        // ── Increment view count ──────────────────────────────────────────────
        lecture.setViewCount(lecture.getViewCount() + 1);
        recordedLectureRepository.save(lecture);

        // ── Auto-record view for students ─────────────────────────────────────
        if ("ROLE_STUDENT".equals(requestingRole)) {
            studentRepository.findByEmailIdOrMobileNumber(requestingUserEmail, requestingUserEmail)
                .ifPresent(student -> recordView(id, student.getId()));
        }

        // ── Return direct local URL (served by Spring static handler) ─────────
        return lecture.getS3Key(); // e.g. /uploads/lectures/RL001_video.mp4
    }

    @Override
    @Transactional
    public void recordView(Long lectureId, Long studentId) {
        RecordedLecture lecture = recordedLectureRepository.findById(lectureId).orElse(null);
        AddStudent student = studentRepository.findById(studentId).orElse(null);
        
        if (lecture != null && student != null) {
            LectureView view = new LectureView();
            view.setLecture(lecture);
            view.setStudent(student);
            lectureViewRepository.save(view);
        }
    }

    @Override
    public List<LectureViewDTO> getLectureViewers(Long schoolId, Long academicYearId, Long lectureId) {
        List<LectureView> views;
        
        if (lectureId != null) {
            RecordedLecture lecture = recordedLectureRepository.findById(lectureId).orElse(null);
            // Association logic: Verify lecture belongs to school and academic year if provided
            if (lecture == null || 
                (schoolId != null && !lecture.getSchool().getSchoolId().equals(schoolId)) ||
                (academicYearId != null && !lecture.getAcademicYear().getAcademicYearId().equals(academicYearId))) {
                return List.of();
            }
            views = lectureViewRepository.findByLecture_Id(lectureId);
        } else if (schoolId != null && academicYearId != null) {
            // Get all viewers for all lectures in that school + academic year
            views = lectureViewRepository.findByLecture_School_SchoolIdAndLecture_AcademicYear_AcademicYearId(schoolId, academicYearId);
        } else {
            views = List.of();
        }

        return views.stream()
            .map(view -> {
                LectureViewDTO dto = new LectureViewDTO();
                AddStudent s = view.getStudent();
                dto.setStudentId(s.getId());
                dto.setStudentName(s.getFirstName() + " " + (s.getLastName() != null ? s.getLastName() : ""));
                dto.setRollNo(s.getRollNumber());
                dto.setStandardName(s.getAddStandard() != null ? s.getAddStandard().getStandard() : "N/A");
                dto.setDivisionName(s.getAddDivision() != null ? s.getAddDivision().getDivisionName() : "N/A");
                dto.setViewedAt(view.getViewedAt().format(DateTimeFormatter.ofPattern("dd MMM yyyy hh:mm a")));
                return dto;
            })
            .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 9. STATS
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public Map<String, Object> getStats(Long schoolId, Long academicYearId) {
        Map<String, Object> stats = new HashMap<>();

        boolean hasYear = academicYearId != null;

        long total = hasYear
                ? recordedLectureRepository
                        .countBySchool_SchoolIdAndAcademicYear_AcademicYearId(schoolId, academicYearId)
                : recordedLectureRepository
                        .countBySchool_SchoolId(schoolId);

        long published = hasYear
                ? recordedLectureRepository
                        .countBySchool_SchoolIdAndAcademicYear_AcademicYearIdAndStatus(
                                schoolId, academicYearId, "Published")
                : recordedLectureRepository
                        .countBySchool_SchoolIdAndStatus(schoolId, "Published");

        Long totalViews = hasYear
                ? recordedLectureRepository
                        .sumViewsBySchoolIdAndAcademicYearId(schoolId, academicYearId)
                : recordedLectureRepository
                        .sumViewsBySchoolId(schoolId);

        stats.put("totalLectures", total);
        stats.put("published", published);
        stats.put("totalViews", totalViews != null ? totalViews : 0L);

        return stats;
    }
}
