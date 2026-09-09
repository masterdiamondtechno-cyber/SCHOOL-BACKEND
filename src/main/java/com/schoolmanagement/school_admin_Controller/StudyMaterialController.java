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

import com.schoolmanagement.school_admin_DTO.StudyMaterialDTO;
import com.schoolmanagement.school_admin_Entity.MaterialType;
import com.schoolmanagement.school_admin_Repo.AddStudentRepository;
import com.schoolmanagement.school_admin_Repo.AddTeacherRepository;
import com.schoolmanagement.school_admin_Service.StudyMaterialService;

@RestController
@RequestMapping("/study-material")
public class StudyMaterialController {

    @Autowired private StudyMaterialService studyMaterialService;
    @Autowired private AddTeacherRepository teacherRepository;
    @Autowired private AddStudentRepository studentRepository;

    private static final List<String> ALLOWED_EXTENSIONS = List.of(
            "pdf", "doc", "docx", "ppt", "pptx", "zip", "rar");

    private static final long MAX_FILE_SIZE = 50L * 1024 * 1024;

    private String getCurrentRole() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return null;
        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElse(null);
    }

    private Long resolveTeacherId(Principal principal) {
        if (principal == null) return null;
        return teacherRepository
                .findByEmailIdOrMobileNumber(principal.getName(), principal.getName())
                .map(t -> t.getId())
                .orElse(null);
    }

    private Long resolveStudentDivisionId(Principal principal) {
        if (principal == null) return null;
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
        if (file == null || file.isEmpty()) return "File is required";
        if (file.getSize() > MAX_FILE_SIZE) return "File size exceeds 50MB";

        String name = file.getOriginalFilename();
        if (name == null || !name.contains(".")) return "Invalid file name";

        String ext = name.substring(name.lastIndexOf(".") + 1).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(ext))
            return "Invalid file type";

        return null;
    }

    // 🔥 FIXED NORMALIZER
    private String normalizeMaterialType(String input) {
        return MaterialType.fromValue(input).name(); 
        // ALWAYS convert to ENUM NAME → NOTES, PRACTICE_SHEET
    }

    // ================= CREATE =================

    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> create(
            @RequestParam String title,
            @RequestParam String materialType,
            @RequestParam String status,
            @RequestParam Long schoolId,
            @RequestParam Long academicYearId,
            @RequestParam Long subjectId,
            @RequestParam Long standardId,
            @RequestParam Long divisionId,
            @RequestParam Long teacherId,
            @RequestParam MultipartFile file,
            Principal principal) {

        try {

            if (title == null || title.isBlank())
                return ResponseEntity.badRequest()
                        .body(build(400, "FAILED", "title required", null));

            // ✅ FIX HERE
            String normalizedType = normalizeMaterialType(materialType);

            if (!status.equals("Published") && !status.equals("Draft"))
                return ResponseEntity.badRequest()
                        .body(build(400, "FAILED", "Invalid status", null));

            String fileError = validateFile(file);
            if (fileError != null)
                return ResponseEntity.badRequest()
                        .body(build(400, "FAILED", fileError, null));

            String role = getCurrentRole();
            Long resolvedTeacherId = teacherId;

            if ("ROLE_TEACHER".equals(role)) {
                Long myId = resolveTeacherId(principal);
                if (myId == null)
                    return ResponseEntity.status(401)
                            .body(build(401, "FAILED", "Unauthorized", null));
                resolvedTeacherId = myId;
            }

            StudyMaterialDTO dto = new StudyMaterialDTO();
            dto.setTitle(title);
            dto.setMaterialType(normalizedType); // ✅ ALWAYS ENUM FORMAT
            dto.setStatus(status);
            dto.setSchoolId(schoolId);
            dto.setAcademicYearId(academicYearId);
            dto.setSubjectId(subjectId);
            dto.setStandardId(standardId);
            dto.setDivisionId(divisionId);
            dto.setTeacherId(resolvedTeacherId);

            studyMaterialService.createStudyMaterial(dto, file,
                    role.equals("ROLE_SCHOOL") ? "SCHOOL" : "TEACHER");

            return ResponseEntity.ok(build(200, "SUCCESS", "Created", null));

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
            @RequestParam(value = "materialType", required = false) String materialType,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "subjectId", required = false) Long subjectId,
            @RequestParam(value = "standardId", required = false) Long standardId,
            @RequestParam(value = "divisionId", required = false) Long divisionId,
            @RequestParam(value = "teacherId", required = false) Long teacherId,
            @RequestParam(value = "file", required = false) MultipartFile file,
            Principal principal) {

        try {
            // Validate materialType
            if (materialType != null && !materialType.isBlank()) {
                MaterialType.fromValue(materialType);
            }

            // Validate status
            if (status != null && !status.isBlank()
                    && !"Published".equals(status) && !"Draft".equals(status)) {
                return ResponseEntity.badRequest()
                        .body(build(400, "FAILED", "'status' must be Published or Draft", null));
            }

            String role = getCurrentRole();
            Long myTeacherId = "ROLE_TEACHER".equals(role)
                    ? resolveTeacherId(principal)
                    : null;

            StudyMaterialDTO dto = new StudyMaterialDTO();
            dto.setId(id);
            dto.setTitle(title);
            dto.setMaterialType(materialType);
            dto.setStatus(status);
            dto.setSubjectId(subjectId);
            dto.setStandardId(standardId);
            dto.setDivisionId(divisionId);
            dto.setTeacherId(teacherId);

            studyMaterialService.updateStudyMaterial(dto, file, myTeacherId, role);

            return ResponseEntity.ok(
                    build(200, "SUCCESS", "Study material updated successfully", null));

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
            @RequestBody StudyMaterialDTO dto,
            Principal principal) {

        try {
            String role = getCurrentRole();
            List<StudyMaterialDTO> list;

            if ("ROLE_STUDENT".equals(role)) {
                Long divisionId = resolveStudentDivisionId(principal);
                list = studyMaterialService.getByDivision(
                        divisionId, dto.getAcademicYearId());

            } else if ("ROLE_TEACHER".equals(role)) {
                Long teacherId = resolveTeacherId(principal);
                list = studyMaterialService.getByTeacher(
                        teacherId, dto.getSchoolId());

            } else {
                list = studyMaterialService.getAllBySchool(
                        dto.getSchoolId(),
                        dto.getAcademicYearId(),
                        dto.getMaterialType());
            }

            return ResponseEntity.ok(build(200, "SUCCESS", "Fetched", list));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(build(400, "FAILED", e.getMessage(), null));
        }
    }

    // ================= DELETE =================

    @PostMapping("/delete")
    public ResponseEntity<Map<String, Object>> delete(
            @RequestBody StudyMaterialDTO dto,
            Principal principal) {

        try {
            String role = getCurrentRole();
            Long teacherId = "ROLE_TEACHER".equals(role)
                    ? resolveTeacherId(principal) : null;

            studyMaterialService.deleteStudyMaterial(dto.getId(), teacherId, role);

            return ResponseEntity.ok(build(200, "SUCCESS", "Deleted", null));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(build(400, "FAILED", e.getMessage(), null));
        }
    }
    
    // ================= GET BY ID =================
    
    @PostMapping("/getbyid")
    public ResponseEntity<Map<String, Object>> getById(
            @RequestBody StudyMaterialDTO dto) {

        try {
            if (dto == null || dto.getId() == null) {
                return ResponseEntity.badRequest()
                        .body(build(400, "FAILED", "'id' is required", null));
            }

            StudyMaterialDTO result = studyMaterialService.getStudyMaterialById(dto.getId());

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
    
    
    // ================= DOWNLOAD =================

    @PostMapping("/download")
    public ResponseEntity<Map<String, Object>> download(
            @RequestBody StudyMaterialDTO dto,
            Principal principal) {

        try {
            if (dto == null || dto.getId() == null) {
                return ResponseEntity.badRequest()
                        .body(build(400, "FAILED", "'id' is required", null));
            }

            String role = getCurrentRole();

            Long myTeacherId = null;
            Long myDivisionId = null;

            if ("ROLE_TEACHER".equals(role)) {
                myTeacherId = resolveTeacherId(principal);
            }

            if ("ROLE_STUDENT".equals(role)) {
                myDivisionId = resolveStudentDivisionId(principal);
            }

            String presignedUrl = studyMaterialService.downloadStudyMaterial(
                    dto.getId(),
                    myTeacherId,
                    role,
                    myDivisionId
            );

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("downloadUrl", presignedUrl);

            return ResponseEntity.ok(
                    build(200, "SUCCESS", "Download URL generated successfully", data));

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(build(403, "FAILED", e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(build(500, "FAILED", "Internal Server Error: " + e.getMessage(), null));
        }
    }
    
    // ================= STATS =================
    
    @PostMapping("/stats")
    public ResponseEntity<Map<String, Object>> stats(
            @RequestBody StudyMaterialDTO dto) {

        try {
            if (dto == null || dto.getSchoolId() == null) {
                return ResponseEntity.badRequest()
                        .body(build(400, "FAILED", "'schoolId' is required", null));
            }

            Map<String, Object> statsData = studyMaterialService
                    .getStats(dto.getSchoolId(), dto.getAcademicYearId());

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