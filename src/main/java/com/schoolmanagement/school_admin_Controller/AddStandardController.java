package com.schoolmanagement.school_admin_Controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.schoolmanagement.school_admin_DTO.AddStandardDTO;
import com.schoolmanagement.school_admin_Repo.AcademicYearRepository;
import com.schoolmanagement.school_admin_Repo.AddStandardRepository;
import com.schoolmanagement.school_admin_Repo.AddTeacherRepository;
import com.schoolmanagement.school_admin_Repo.SchoolRepository;
import com.schoolmanagement.school_admin_Service.AddStandardService;
import com.schoolmanagement.school_admin_Entity.AddTeacher;

@RestController
@RequestMapping("/standard")
public class AddStandardController {

    @Autowired
    private AddStandardService service;

    @Autowired
    private SchoolRepository schoolRepository;

    @Autowired
    private AddStandardRepository standardRepository;

    @Autowired
    private AcademicYearRepository academicYearRepository;

    @Autowired
    private AddTeacherRepository teacherRepository;

    // ================= COMMON RESPONSE METHOD =================
    private Map<String, Object> buildResponse(int code, String status, String message, Object data) {
        Map<String, Object> response = new HashMap<>();
        response.put("code", code);
        response.put("status", status);
        response.put("message", message);
        response.put("data", data);
        return response;
    }

    // ================= ADD STANDARD =================
    @PostMapping("/addStandard")
    public ResponseEntity<Map<String, Object>> add(@RequestBody AddStandardDTO dto) {
        try {

            if (dto.getSchoolId() == null)
                return ResponseEntity.badRequest()
                        .body(buildResponse(400, "FAILED", "SchoolId is required", null));

            if (!schoolRepository.existsById(dto.getSchoolId()))
                return ResponseEntity.status(404)
                        .body(buildResponse(404, "FAILED", "School not found", null));

            if (dto.getAcademicYearId() == null)
                return ResponseEntity.badRequest()
                        .body(buildResponse(400, "FAILED", "academicYearId is required", null));

            if (!academicYearRepository.existsById(dto.getAcademicYearId()))
                return ResponseEntity.status(404)
                        .body(buildResponse(404, "FAILED", "Academic Year not found", null));

            if (dto.getStandard() == null || dto.getStandard().isBlank())
                return ResponseEntity.badRequest()
                        .body(buildResponse(400, "FAILED", "Standard name is required", null));

            if (standardRepository.existsBySchool_SchoolIdAndStandard(dto.getSchoolId(), dto.getStandard()))
                return ResponseEntity.status(409)
                        .body(buildResponse(409, "FAILED", "Standard already exists for this school", null));

            // Validate classTeacherId if provided
            if (dto.getClassTeacherId() != null) {
                AddTeacher teacher = teacherRepository.findById(dto.getClassTeacherId()).orElse(null);
                if (teacher == null)
                    return ResponseEntity.status(404)
                            .body(buildResponse(404, "FAILED", "Teacher not found", null));
                if (!teacher.getSchool().getSchoolId().equals(dto.getSchoolId()))
                    return ResponseEntity.badRequest()
                            .body(buildResponse(400, "FAILED", "Teacher does not belong to the given School", null));
            }

            service.addStandard(dto);

            return ResponseEntity.ok(
                    buildResponse(200, "SUCCESS", "Standard added successfully", null));

        } catch (RuntimeException e) {
            return ResponseEntity.status(400)
                    .body(buildResponse(400, "FAILED", e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(buildResponse(500, "FAILED", "Something went wrong", null));
        }
    }

    // ================= GET ALL =================
    @PostMapping("/getAllBySchoolId")
    public ResponseEntity<Map<String, Object>> getAll(@RequestBody Map<String, Long> req) {
        try {

            Long schoolId = req.get("schoolId");
            Long academicYearId = req.get("academicYearId");

            if (schoolId == null)
                return ResponseEntity.badRequest()
                        .body(buildResponse(400, "FAILED", "SchoolId is required", null));

            if (academicYearId == null)
                return ResponseEntity.badRequest()
                        .body(buildResponse(400, "FAILED", "academicYearId is required", null));

            if (!schoolRepository.existsById(schoolId))
                return ResponseEntity.status(404)
                        .body(buildResponse(404, "FAILED", "School not found", null));

            if (!academicYearRepository.existsById(academicYearId))
                return ResponseEntity.status(404)
                        .body(buildResponse(404, "FAILED", "Academic Year not found", null));

            List<AddStandardDTO> list = service.getAllBySchoolId(schoolId, academicYearId);

            if (list.isEmpty())
                return ResponseEntity.status(404)
                        .body(buildResponse(404, "FAILED", "No standards found", null));

            return ResponseEntity.ok(
                    buildResponse(200, "SUCCESS", "Standards fetched successfully", list));

        } catch (RuntimeException e) {
            return ResponseEntity.status(400)
                    .body(buildResponse(400, "FAILED", e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(buildResponse(500, "FAILED", "Something went wrong", null));
        }
    }

    // ================= GET BY ID =================
    @PostMapping("/getByStandardId")
    public ResponseEntity<Map<String, Object>> getById(@RequestBody Map<String, Long> req) {
        try {

            Long schoolId = req.get("schoolId");
            Long standardId = req.get("standardId");
            Long academicYearId = req.get("academicYearId");

            if (schoolId == null)
                return ResponseEntity.badRequest()
                        .body(buildResponse(400, "FAILED", "SchoolId is required", null));

            if (academicYearId == null)
                return ResponseEntity.badRequest()
                        .body(buildResponse(400, "FAILED", "academicYearId is required", null));

            if (!schoolRepository.existsById(schoolId))
                return ResponseEntity.status(404)
                        .body(buildResponse(404, "FAILED", "School not found", null));

            if (!academicYearRepository.existsById(academicYearId))
                return ResponseEntity.status(404)
                        .body(buildResponse(404, "FAILED", "Academic Year not found", null));

            if (standardId == null)
                return ResponseEntity.badRequest()
                        .body(buildResponse(400, "FAILED", "StandardId is required", null));

            if (!standardRepository.existsById(standardId))
                return ResponseEntity.status(404)
                        .body(buildResponse(404, "FAILED", "Standard not found", null));

            AddStandardDTO dto = service.getByStandardId(standardId, academicYearId);

            if (!dto.getSchoolId().equals(schoolId))
                return ResponseEntity.badRequest()
                        .body(buildResponse(400, "FAILED", "Standard does not belong to given School", null));

            return ResponseEntity.ok(
                    buildResponse(200, "SUCCESS", "Standard fetched successfully", dto));

        } catch (RuntimeException e) {
            return ResponseEntity.status(400)
                    .body(buildResponse(400, "FAILED", e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(buildResponse(500, "FAILED", "Something went wrong", null));
        }
    }

    // ================= UPDATE =================
    @PostMapping("/updateStandard")
    public ResponseEntity<Map<String, Object>> update(@RequestBody AddStandardDTO dto) {
        try {

            if (dto.getStandardId() == null)
                return ResponseEntity.badRequest()
                        .body(buildResponse(400, "FAILED", "StandardId is required", null));

            if (!standardRepository.existsById(dto.getStandardId()))
                return ResponseEntity.status(404)
                        .body(buildResponse(404, "FAILED", "Standard not found", null));

            if (dto.getSchoolId() == null)
                return ResponseEntity.badRequest()
                        .body(buildResponse(400, "FAILED", "SchoolId is required", null));

            if (!schoolRepository.existsById(dto.getSchoolId()))
                return ResponseEntity.status(404)
                        .body(buildResponse(404, "FAILED", "School not found", null));

            // Validate classTeacherId if provided
            if (dto.getClassTeacherId() != null) {
                AddTeacher teacher = teacherRepository.findById(dto.getClassTeacherId()).orElse(null);
                if (teacher == null)
                    return ResponseEntity.status(404)
                            .body(buildResponse(404, "FAILED", "Teacher not found", null));
                if (!teacher.getSchool().getSchoolId().equals(dto.getSchoolId()))
                    return ResponseEntity.badRequest()
                            .body(buildResponse(400, "FAILED", "Teacher does not belong to the given School", null));
            }

            service.updateStandard(dto);

            return ResponseEntity.ok(
                    buildResponse(200, "SUCCESS", "Standard updated successfully", null));

        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(buildResponse(500, "FAILED", "Something went wrong", null));
        }
    }

    // ================= DELETE =================
    @PostMapping("/deleteStandard")
    public ResponseEntity<Map<String, Object>> delete(@RequestBody Map<String, Long> req) {
        try {

            Long schoolId = req.get("schoolId");
            Long standardId = req.get("standardId");

            if (schoolId == null)
                return ResponseEntity.badRequest()
                        .body(buildResponse(400, "FAILED", "SchoolId is required", null));

            if (!schoolRepository.existsById(schoolId))
                return ResponseEntity.status(404)
                        .body(buildResponse(404, "FAILED", "School not found", null));

            if (standardId == null)
                return ResponseEntity.badRequest()
                        .body(buildResponse(400, "FAILED", "StandardId is required", null));

            if (!standardRepository.existsById(standardId))
                return ResponseEntity.status(404)
                        .body(buildResponse(404, "FAILED", "Standard not found", null));

            // Use a dummy academicYearId=0 for delete since we just need basic standard info
            AddStandardDTO dto = service.getByStandardId(standardId, null);

            if (!dto.getSchoolId().equals(schoolId))
                return ResponseEntity.badRequest()
                        .body(buildResponse(400, "FAILED", "Standard does not belong to given School", null));

            service.deleteStandard(standardId);

            return ResponseEntity.ok(
                    buildResponse(200, "SUCCESS", "Standard deleted successfully", null));

        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(buildResponse(500, "FAILED", "Something went wrong", null));
        }
    }
}