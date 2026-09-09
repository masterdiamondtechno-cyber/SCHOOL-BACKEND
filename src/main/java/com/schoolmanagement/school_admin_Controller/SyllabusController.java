package com.schoolmanagement.school_admin_Controller;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.schoolmanagement.school_admin_DTO.SyllabusDTO;
import com.schoolmanagement.school_admin_Service.SyllabusService;

@RestController
@RequestMapping("/syllabus")
public class SyllabusController {

    @Autowired
    private SyllabusService syllabusService;

    private static final List<String> ALLOWED_EXTENSIONS = List.of("pdf", "doc", "docx");
    private static final long MAX_FILE_SIZE = 100L * 1024 * 1024; // 100 MB

    // ==================== Helper ====================
    private Map<String, Object> build(int code, String status, String message, Object data) {
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("code", code);
        r.put("status", status);
        r.put("message", message);
        r.put("data", data);
        return r;
    }

    private String validateFile(MultipartFile file) {
        if (file == null || file.isEmpty())
            return "File is required";
        if (file.getSize() > MAX_FILE_SIZE)
            return "File size exceeds 100MB";

        String name = file.getOriginalFilename();
        if (name == null || !name.contains("."))
            return "Invalid file name";

        String ext = name.substring(name.lastIndexOf(".") + 1).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(ext))
            return "Invalid file type. Allowed: PDF, DOC, DOCX";

        return null;
    }

    // ==================== 1. CREATE ====================

    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> create(
            @RequestParam String title,
            @RequestParam Long subjectId,
            @RequestParam Long standardId,
            @RequestParam Long teacherId,
            @RequestParam Integer totalChapters,
            @RequestParam Long schoolId,
            @RequestParam Long academicYearId,
            @RequestParam(required = false) String status,
            @RequestParam MultipartFile file) {

        try {
            if (title == null || title.isBlank())
                return ResponseEntity.badRequest()
                        .body(build(400, "FAILED", "'title' is required", null));

            if (totalChapters == null || totalChapters <= 0)
                return ResponseEntity.badRequest()
                        .body(build(400, "FAILED", "'totalChapters' must be a positive number", null));

            String fileError = validateFile(file);
            if (fileError != null)
                return ResponseEntity.badRequest()
                        .body(build(400, "FAILED", fileError, null));

            SyllabusDTO dto = new SyllabusDTO();
            dto.setTitle(title);
            dto.setSubjectId(subjectId);
            dto.setStandardId(standardId);
            dto.setTeacherId(teacherId);
            dto.setTotalChapters(totalChapters);
            dto.setSchoolId(schoolId);
            dto.setAcademicYearId(academicYearId);
            dto.setStatus(status);

            SyllabusDTO result = syllabusService.createSyllabus(dto, file);

            return ResponseEntity.ok(build(200, "SUCCESS", "Syllabus created successfully", null));

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(build(400, "FAILED", e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(build(500, "FAILED", "Internal Server Error: " + e.getMessage(), null));
        }
    }

    // ==================== 2. UPDATE ====================

    @PostMapping(value = "/update", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> update(
            @RequestParam Long id,
            @RequestParam Long schoolId,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) Long subjectId,
            @RequestParam(required = false) Long standardId,
            @RequestParam(required = false) Long teacherId,
            @RequestParam(required = false) Integer totalChapters,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) MultipartFile file) {

        try {
            // Validate file if provided
            if (file != null && !file.isEmpty()) {
                String fileError = validateFile(file);
                if (fileError != null)
                    return ResponseEntity.badRequest()
                            .body(build(400, "FAILED", fileError, null));
            }

            if (status != null && !status.isBlank()
                    && !"ACTIVE".equals(status) && !"COMPLETED".equals(status) && !"DRAFT".equals(status)) {
                return ResponseEntity.badRequest()
                        .body(build(400, "FAILED", "Invalid status. Must be ACTIVE, COMPLETED, or DRAFT", null));
            }

            SyllabusDTO dto = new SyllabusDTO();
            dto.setId(id);
            dto.setSchoolId(schoolId);
            dto.setTitle(title);
            dto.setSubjectId(subjectId);
            dto.setStandardId(standardId);
            dto.setTeacherId(teacherId);
            dto.setTotalChapters(totalChapters);
            dto.setStatus(status);

            syllabusService.updateSyllabus(dto, file);

            return ResponseEntity.ok(build(200, "SUCCESS", "Syllabus updated successfully", null));

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(build(400, "FAILED", e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(build(500, "FAILED", "Internal Server Error: " + e.getMessage(), null));
        }
    }

    // ==================== 3. DELETE ====================

    @PostMapping("/delete")
    public ResponseEntity<Map<String, Object>> delete(
            @RequestBody SyllabusDTO dto) {

        try {
            if (dto.getId() == null || dto.getSchoolId() == null)
                return ResponseEntity.badRequest()
                        .body(build(400, "FAILED", "'id' and 'schoolId' are required", null));

            syllabusService.deleteSyllabus(dto.getId(), dto.getSchoolId());

            return ResponseEntity.ok(build(200, "SUCCESS", "Syllabus deleted successfully", null));

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(build(404, "FAILED", e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(build(500, "FAILED", "Internal Server Error: " + e.getMessage(), null));
        }
    }

    // ==================== 4. GET BY ID ====================

    @PostMapping("/getById")
    public ResponseEntity<Map<String, Object>> getById(
            @RequestBody SyllabusDTO dto) {

        try {
            if (dto.getId() == null || dto.getSchoolId() == null)
                return ResponseEntity.badRequest()
                        .body(build(400, "FAILED", "'id' and 'schoolId' are required", null));

            SyllabusDTO result = syllabusService.getSyllabusById(dto.getId(), dto.getSchoolId());

            return ResponseEntity.ok(build(200, "SUCCESS", "Syllabus fetched successfully", result));

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(build(404, "FAILED", e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(build(500, "FAILED", "Internal Server Error: " + e.getMessage(), null));
        }
    }

    // ==================== 5. GET ALL ====================

    @PostMapping("/getAll")
    public ResponseEntity<Map<String, Object>> getAll(
            @RequestBody SyllabusDTO dto) {

        try {
            if (dto.getSchoolId() == null || dto.getAcademicYearId() == null)
                return ResponseEntity.badRequest()
                        .body(build(400, "FAILED", "'schoolId' and 'academicYearId' are required", null));

            List<SyllabusDTO> list = syllabusService.getAllSyllabus(
                    dto.getSchoolId(), dto.getAcademicYearId(), dto.getStatus());

            if (list.isEmpty())
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(build(404, "FAILED", "No syllabus found", null));

            return ResponseEntity.ok(build(200, "SUCCESS", "Syllabi fetched successfully", list));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(build(500, "FAILED", "Internal Server Error: " + e.getMessage(), null));
        }
    }

    // ==================== 6. UPDATE PROGRESS ====================

    @PostMapping("/updateProgress")
    public ResponseEntity<Map<String, Object>> updateProgress(
            @RequestBody SyllabusDTO dto) {

        try {
            if (dto.getId() == null || dto.getSchoolId() == null || dto.getCompletedChapters() == null)
                return ResponseEntity.badRequest()
                        .body(build(400, "FAILED", "'id', 'schoolId', and 'completedChapters' are required", null));

            SyllabusDTO result = syllabusService.updateProgress(
                    dto.getId(), dto.getSchoolId(), dto.getCompletedChapters());

            return ResponseEntity.ok(build(200, "SUCCESS", "Progress updated successfully", result));

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(build(400, "FAILED", e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(build(500, "FAILED", "Internal Server Error: " + e.getMessage(), null));
        }
    }

    // ==================== 7. STATS ====================

    @PostMapping("/stats")
    public ResponseEntity<Map<String, Object>> stats(
            @RequestBody SyllabusDTO dto) {

        try {
            if (dto.getSchoolId() == null || dto.getAcademicYearId() == null)
                return ResponseEntity.badRequest()
                        .body(build(400, "FAILED", "'schoolId' and 'academicYearId' are required", null));

            Map<String, Object> statsData = syllabusService.getStats(
                    dto.getSchoolId(), dto.getAcademicYearId());

            return ResponseEntity.ok(build(200, "SUCCESS", "Stats fetched successfully", statsData));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(build(500, "FAILED", "Internal Server Error: " + e.getMessage(), null));
        }
    }

    // ==================== 8. DOWNLOAD ====================

    @PostMapping("/download")
    public ResponseEntity<Map<String, Object>> download(
            @RequestBody SyllabusDTO dto) {

        try {
            if (dto.getId() == null || dto.getSchoolId() == null)
                return ResponseEntity.badRequest()
                        .body(build(400, "FAILED", "'id' and 'schoolId' are required", null));

            String presignedUrl = syllabusService.downloadSyllabus(dto.getId(), dto.getSchoolId());

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("downloadUrl", presignedUrl);

            return ResponseEntity.ok(build(200, "SUCCESS", "Download URL generated successfully", data));

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(build(404, "FAILED", e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(build(500, "FAILED", "Internal Server Error: " + e.getMessage(), null));
        }
    }

    // ==================== 9. UPDATE STATUS ====================

    @PostMapping("/updateStatus")
    public ResponseEntity<Map<String, Object>> updateStatus(
            @RequestBody SyllabusDTO dto) {

        try {
            if (dto.getId() == null || dto.getSchoolId() == null || dto.getStatus() == null)
                return ResponseEntity.badRequest()
                        .body(build(400, "FAILED", "'id', 'schoolId', and 'status' are required", null));

            syllabusService.updateStatus(dto.getId(), dto.getSchoolId(), dto.getStatus());

            return ResponseEntity.ok(build(200, "SUCCESS", "Syllabus status updated successfully", null));

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(build(400, "FAILED", e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(build(500, "FAILED", "Internal Server Error: " + e.getMessage(), null));
        }
    }
}
