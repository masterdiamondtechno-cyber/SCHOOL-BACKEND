package com.schoolmanagement.school_admin_Controller;

import java.security.Principal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.schoolmanagement.school_admin_DTO.LectureViewDTO;
import com.schoolmanagement.school_admin_DTO.RecordedLectureDTO;
import com.schoolmanagement.school_admin_Entity.LectureStatus;
import com.schoolmanagement.school_admin_Entity.School;
import com.schoolmanagement.school_admin_Repo.SchoolRepository;
import com.schoolmanagement.school_admin_Repo.AddStudentRepository;
import com.schoolmanagement.school_admin_Repo.AddTeacherRepository;
import com.schoolmanagement.school_admin_Service.RecordedLectureService;

@RestController
@RequestMapping("/recorded-lecture")
public class RecordedLectureController {

    @Autowired
    private RecordedLectureService recordedLectureService;
    @Autowired
    private AddTeacherRepository teacherRepository;
    @Autowired
    private AddStudentRepository studentRepository;
    @Autowired
    private SchoolRepository schoolRepository;

    private void checkSubscription(Long schoolId) {
        School school = schoolRepository.findById(schoolId)
                .orElseThrow(() -> new RuntimeException("School not found"));
        
        // Block if subscription or trial expired
        if (school.isSubscriptionExpired()) {
            throw new org.springframework.security.access.AccessDeniedException(
                "Access Blocked: Your subscription or free trial has expired. Please contact your school administrator to renew subscription."
            );
        }

        String plan = school.getSubscriptionStatus();
        if (plan == null || (!plan.toLowerCase().contains("premium") && 
                             !plan.toLowerCase().contains("custom") && 
                             !plan.toLowerCase().contains("enterprise") && 
                             !plan.toLowerCase().contains("ultimate"))) {
            throw new org.springframework.security.access.AccessDeniedException(
                "Upgrade Required: Recorded Lectures feature is not available in your current subscription plan. Please upgrade to a Premium plan."
            );
        }
    }

    private void resolveAndCheckSubscription(Long schoolId, Principal principal) {
        if (schoolId != null) {
            checkSubscription(schoolId);
            return;
        }
        if (principal == null) return;
        String name = principal.getName();
        String role = getCurrentRole();
        
        if ("ROLE_STUDENT".equals(role)) {
            studentRepository.findByEmailIdOrMobileNumber(name, name)
                .ifPresent(s -> {
                    if (s.getSchool() != null) {
                        checkSubscription(s.getSchool().getSchoolId());
                    }
                });
        } else if ("ROLE_TEACHER".equals(role)) {
            teacherRepository.findByEmailIdOrMobileNumber(name, name)
                .ifPresent(t -> {
                    if (t.getSchool() != null) {
                        checkSubscription(t.getSchool().getSchoolId());
                    }
                });
        }
    }


    private static final List<String> ALLOWED_EXTENSIONS = List.of(
            "mp4", "avi", "mov", "mkv", "webm", "flv", "wmv", "m4v");

    private static final long MAX_FILE_SIZE = 500L * 1024 * 1024; // 500 MB

    private String getCurrentRole() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null)
            return null;
        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElse(null);
    }

    private Long resolveTeacherId(Principal principal) {
        if (principal == null)
            return null;
        return teacherRepository
                .findByEmailIdOrMobileNumber(principal.getName(), principal.getName())
                .map(t -> t.getId())
                .orElse(null);
    }

    private Long resolveStudentDivisionId(Principal principal) {
        if (principal == null)
            return null;
        String name = principal.getName();
        return studentRepository
                .findByEmailIdOrMobileNumber(name, name)
                .filter(s -> s.getAddDivision() != null)
                .map(s -> s.getAddDivision().getDivisionId())
                .orElse(null);
    }

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
            return "Video file is required";
        if (file.getSize() > MAX_FILE_SIZE)
            return "File size exceeds 500MB";

        String name = file.getOriginalFilename();
        if (name == null || !name.contains("."))
            return "Invalid file name";

        String ext = name.substring(name.lastIndexOf(".") + 1).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(ext))
            return "Invalid file type. Allowed: " + String.join(", ", ALLOWED_EXTENSIONS);

        return null;
    }

    // ================= CREATE =================

    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> create(
            @RequestParam String title,
            @RequestParam(required = false) String status,
            @RequestParam Long schoolId,
            @RequestParam Long academicYearId,
            @RequestParam Long subjectId,
            @RequestParam Long standardId,
            @RequestParam(required = false) Long divisionId,
            @RequestParam(required = false) Long teacherId,
            @RequestParam(required = false) Integer duration,
            @RequestParam(required = false) String recordedDate,
            @RequestParam(required = false) MultipartFile file,
            Principal principal) {

        try {
            checkSubscription(schoolId);

            if (title == null || title.isBlank())
                return ResponseEntity.badRequest()
                        .body(build(400, "FAILED", "title is required", null));

            // Validate status if provided
            if (status != null && !status.isBlank()) {
                LectureStatus.fromValue(status);
            }

            // Only validate file if it's present
            if (file != null && !file.isEmpty()) {
                String fileError = validateFile(file);
                if (fileError != null)
                    return ResponseEntity.badRequest()
                            .body(build(400, "FAILED", fileError, null));
            }

            String role = getCurrentRole();
            Long resolvedTeacherId = teacherId;

            if ("ROLE_TEACHER".equals(role)) {
                Long myId = resolveTeacherId(principal);
                if (myId == null)
                    return ResponseEntity.status(401)
                            .body(build(401, "FAILED", "Unauthorized", null));
                resolvedTeacherId = myId;
            }

            RecordedLectureDTO dto = new RecordedLectureDTO();
            dto.setTitle(title);
            dto.setStatus(status);
            dto.setSchoolId(schoolId);
            dto.setAcademicYearId(academicYearId);
            dto.setSubjectId(subjectId);
            dto.setStandardId(standardId);
            dto.setDivisionId(divisionId);
            dto.setTeacherId(resolvedTeacherId);
            dto.setDuration(duration);
            dto.setRecordedDate(recordedDate);

            recordedLectureService.createLecture(dto, file,
                    role.equals("ROLE_SCHOOL") ? "SCHOOL" : "TEACHER");

            return ResponseEntity.ok(build(200, "SUCCESS", "Recorded lecture created successfully", null));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(build(400, "FAILED", e.getMessage(), null));
        }
    }

    // ================= UPDATE =================

    @PostMapping(value = "/update", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> update(
            @RequestParam("id") Long id,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "duration", required = false) Integer duration,
            @RequestParam(value = "recordedDate", required = false) String recordedDate,
            @RequestParam(value = "subjectId", required = false) Long subjectId,
            @RequestParam(value = "standardId", required = false) Long standardId,
            @RequestParam(value = "divisionId", required = false) Long divisionId,
            @RequestParam(value = "teacherId", required = false) Long teacherId,
            @RequestParam(value = "file", required = false) MultipartFile file,
            Principal principal) {

        try {
            checkSubscription(recordedLectureService.getLectureById(id).getSchoolId());
            // Validate status if provided
            if (status != null && !status.isBlank()) {
                LectureStatus.fromValue(status);
            }

            // Only validate file if it's present
            if (file != null && !file.isEmpty()) {
                String fileError = validateFile(file);
                if (fileError != null) {
                    return ResponseEntity.badRequest()
                            .body(build(400, "FAILED", fileError, null));
                }
            }

            String role = getCurrentRole();
            Long myTeacherId = "ROLE_TEACHER".equals(role)
                    ? resolveTeacherId(principal)
                    : null;

            RecordedLectureDTO dto = new RecordedLectureDTO();
            dto.setId(id);
            dto.setTitle(title);
            dto.setStatus(status);
            dto.setDuration(duration);
            dto.setRecordedDate(recordedDate);
            dto.setSubjectId(subjectId);
            dto.setStandardId(standardId);
            dto.setDivisionId(divisionId);
            dto.setTeacherId(teacherId);

            recordedLectureService.updateLecture(dto, file, myTeacherId, role);

            return ResponseEntity.ok(
                    build(200, "SUCCESS", "Recorded lecture updated successfully", null));

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(build(400, "FAILED", e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(build(500, "FAILED", "Internal Server Error: " + e.getMessage(), null));
        }
    }

    // ================= GET ALL =================

    @PostMapping("/getall")
    public ResponseEntity<Map<String, Object>> getAll(
            @RequestBody Map<String, Object> req,
            Principal principal) {

        try {
            Long schoolId = req.get("schoolId") != null ? Long.valueOf(req.get("schoolId").toString()) : null;
            resolveAndCheckSubscription(schoolId, principal);
            Long academicYearId = req.get("academicYearId") != null ? Long.valueOf(req.get("academicYearId").toString())
                    : null;
            String status = req.get("status") != null ? req.get("status").toString() : null;
            Long subjectId = req.get("subjectId") != null ? Long.valueOf(req.get("subjectId").toString()) : null;

            if (academicYearId == null)
                return ResponseEntity.badRequest()
                        .body(build(400, "FAILED", "'academicYearId' is required", null));

            String role = getCurrentRole();
            List<RecordedLectureDTO> list;

            if ("ROLE_STUDENT".equals(role)) {
                Long divisionId = resolveStudentDivisionId(principal);
                if (divisionId == null)
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body(build(403, "FAILED", "Student division not found", null));
                list = recordedLectureService.getByDivision(divisionId, academicYearId);
            } else {
                // For TEACHER and SCHOOL roles
                Long lectureId = req.get("lectureId") != null ? Long.valueOf(req.get("lectureId").toString()) : null;
                Long myTeacherId = null;

                if ("ROLE_TEACHER".equals(role)) {
                    myTeacherId = resolveTeacherId(principal);
                }

                list = recordedLectureService.getAllBySchool(schoolId, academicYearId, status, subjectId, lectureId,
                        myTeacherId);
            }

            return ResponseEntity.ok(
                    build(200, "SUCCESS", "Recorded lectures fetched successfully", list));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(build(500, "FAILED", "Internal Server Error: " + e.getMessage(), null));
        }
    }

    // ================= DELETE =================

    @PostMapping("/delete")
    public ResponseEntity<Map<String, Object>> delete(
            @RequestParam Long id,
            Principal principal) {

        try {
            checkSubscription(recordedLectureService.getLectureById(id).getSchoolId());
            String role = getCurrentRole();
            Long teacherId = "ROLE_TEACHER".equals(role)
                    ? resolveTeacherId(principal)
                    : null;

            recordedLectureService.deleteLecture(id, teacherId, role);

            return ResponseEntity.ok(build(200, "SUCCESS", "Deleted", null));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(build(400, "FAILED", e.getMessage(), null));
        }
    }

    // ================= GET BY ID =================

    @PostMapping("/getbyid")
    public ResponseEntity<Map<String, Object>> getById(
            @RequestBody Map<String, Object> req) {

        try {
            Long id = req.get("id") != null ? Long.valueOf(req.get("id").toString()) : null;
            if (id == null) {
                return ResponseEntity.badRequest()
                        .body(build(400, "FAILED", "'id' is required", null));
            }
            checkSubscription(recordedLectureService.getLectureById(id).getSchoolId());

            RecordedLectureDTO result = recordedLectureService.getLectureById(id);

            return ResponseEntity.ok(
                    build(200, "SUCCESS", "Data fetched successfully", result));

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(build(404, "FAILED", e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(build(500, "FAILED", "Internal Server Error: " + e.getMessage(), null));
        }
    }

    // ================= STREAM =================

    @PostMapping("/stream")
    public ResponseEntity<Map<String, Object>> stream(
            @RequestBody Map<String, Object> req,
            Principal principal) {

        try {
            Long id = req.get("id") != null ? Long.valueOf(req.get("id").toString()) : null;
            if (id == null) {
                return ResponseEntity.badRequest()
                        .body(build(400, "FAILED", "'id' is required", null));
            }
            checkSubscription(recordedLectureService.getLectureById(id).getSchoolId());

            String role = getCurrentRole();

            Long myTeacherId = null;
            Long myDivisionId = null;

            if ("ROLE_TEACHER".equals(role)) {
                myTeacherId = resolveTeacherId(principal);
            }

            if ("ROLE_STUDENT".equals(role)) {
                myDivisionId = resolveStudentDivisionId(principal);
            }

            Long schoolId = req.get("schoolId") != null ? Long.valueOf(req.get("schoolId").toString()) : null;
            Long academicYearId = req.get("academicYearId") != null ? Long.valueOf(req.get("academicYearId").toString()) : null;

            String presignedUrl = recordedLectureService.streamLecture(
                    id,
                    schoolId,
                    academicYearId,
                    myTeacherId,
                    role,
                    myDivisionId,
                    principal.getName());

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("streamUrl", presignedUrl);

            return ResponseEntity.ok(
                    build(200, "SUCCESS", "Stream URL generated successfully", data));

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(build(403, "FAILED", e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(build(500, "FAILED", "Internal Server Error: " + e.getMessage(), null));
        }
    }

    @PostMapping("/viewers")
    public ResponseEntity<Map<String, Object>> getViewers(
            @RequestBody Map<String, Object> req,
            Principal principal) {
        try {
            String role = getCurrentRole();
            if (!"ROLE_SCHOOL".equals(role) && !"ROLE_TEACHER".equals(role)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(build(403, "FAILED", "Access denied: Only School or Teachers can view student engagement",
                                null));
            }

            Long schoolId = req.get("schoolId") != null ? Long.valueOf(req.get("schoolId").toString()) : null;
            Long academicYearId = req.get("academicYearId") != null ? Long.valueOf(req.get("academicYearId").toString())
                    : null;
            Long lectureId = req.get("lectureId") != null ? Long.valueOf(req.get("lectureId").toString()) : null;

            Long resolvedSchoolId = schoolId;
            if (resolvedSchoolId == null && lectureId != null) {
                resolvedSchoolId = recordedLectureService.getLectureById(lectureId).getSchoolId();
            }
            resolveAndCheckSubscription(resolvedSchoolId, principal);

            if (lectureId == null && (schoolId == null || academicYearId == null)) {
                return ResponseEntity.badRequest()
                        .body(build(400, "FAILED",
                                "Either 'lectureId' or both 'schoolId' and 'academicYearId' are required", null));
            }

            // Security: If teacher, they should only see viewers for their own lectures
            if ("ROLE_TEACHER".equals(role) && lectureId != null) {
                Long teacherId = resolveTeacherId(principal);
                RecordedLectureDTO lecture = recordedLectureService.getLectureById(lectureId);
                if (lecture == null || !lecture.getTeacherId().equals(teacherId)) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body(build(403, "FAILED",
                                    "Access denied: You can only view engagements for your own lectures", null));
                }
            }

            List<LectureViewDTO> viewers = recordedLectureService.getLectureViewers(schoolId, academicYearId,
                    lectureId);
            return ResponseEntity.ok(
                    build(200, "SUCCESS", "Viewers fetched successfully", viewers));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(build(500, "FAILED", "Internal Server Error: " + e.getMessage(), null));
        }
    }

    // ================= STATS =================

    @PostMapping("/stats")
    public ResponseEntity<Map<String, Object>> stats(
            @RequestBody Map<String, Object> req) {

        try {
            Long schoolId = req.get("schoolId") != null ? Long.valueOf(req.get("schoolId").toString()) : null;
            Long academicYearId = req.get("academicYearId") != null ? Long.valueOf(req.get("academicYearId").toString()) : null;

            if (schoolId == null) {
                return ResponseEntity.badRequest()
                        .body(build(400, "FAILED", "'schoolId' is required", null));
            }
            checkSubscription(schoolId);

            Map<String, Object> statsData = recordedLectureService
                    .getStats(schoolId, academicYearId);

            return ResponseEntity.ok(
                    build(200, "SUCCESS", "Stats fetched successfully", statsData));

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(build(400, "FAILED", e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(build(500, "FAILED", "Internal Server Error: " + e.getMessage(), null));
        }
    }
}
