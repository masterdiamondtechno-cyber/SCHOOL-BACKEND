package com.schoolmanagement.school.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.schoolmanagement.school.dto.AddDivisionDTO;
import com.schoolmanagement.school.entity.AddStandard;
import com.schoolmanagement.school.entity.AddTeacher;
import com.schoolmanagement.school.repository.AcademicYearRepository;
import com.schoolmanagement.school.repository.AddDivisionRepository;
import com.schoolmanagement.school.repository.AddStandardRepository;
import com.schoolmanagement.school.repository.AddTeacherRepository;
import com.schoolmanagement.school.repository.SchoolRepository;
import com.schoolmanagement.school.service.AddDivisionService;

@RestController
@RequestMapping("/division")
public class AddDivisionController {

    @Autowired
    private AddDivisionService service;

    @Autowired
    private SchoolRepository schoolRepository;

    @Autowired
    private AddStandardRepository standardRepository;

    @Autowired
    private AddDivisionRepository divisionRepository;

    @Autowired
    private AcademicYearRepository academicYearRepository;

    @Autowired
    private AddTeacherRepository teacherRepository;

    // Common Response Builder
    private Map<String, Object> buildResponse(int code, String status, String message, Object data) {
        Map<String, Object> res = new HashMap<>();
        res.put("code", code);
        res.put("status", status);
        res.put("message", message);
        res.put("data", data);
        return res;
    }

    // ================= ADD DIVISION =================
    @PostMapping("/addDivision")
    public ResponseEntity<Map<String, Object>> add(@RequestBody AddDivisionDTO dto) {
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

            if (dto.getStandardId() == null)
                return ResponseEntity.badRequest()
                        .body(buildResponse(400, "FAILED", "StandardId is required", null));

            AddStandard standard = standardRepository.findById(dto.getStandardId()).orElse(null);

            if (standard == null)
                return ResponseEntity.status(404)
                        .body(buildResponse(404, "FAILED", "Standard not found", null));

            if (!standard.getSchool().getSchoolId().equals(dto.getSchoolId()))
                return ResponseEntity.badRequest()
                        .body(buildResponse(400, "FAILED", "Standard does not belong to given School", null));

            if (dto.getDivisionName() == null || dto.getDivisionName().isBlank())
                return ResponseEntity.badRequest()
                        .body(buildResponse(400, "FAILED", "Division name is required", null));

            if (divisionRepository.existsByAddStandard_StandardIdAndDivisionName(dto.getStandardId(), dto.getDivisionName()))
                return ResponseEntity.status(409)
                        .body(buildResponse(409, "FAILED", "Division already exists for this standard", null));

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

            service.addDivision(dto);

            return ResponseEntity.ok(
                    buildResponse(200, "SUCCESS", "Division added successfully", null));

        } catch (RuntimeException e) {
            return ResponseEntity.status(400)
                    .body(buildResponse(400, "FAILED", e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(buildResponse(500, "FAILED", "Something went wrong", null));
        }
    }

    // ================= GET ALL BY SCHOOL =================
    @PostMapping("/getAllBySchoolId")
    public ResponseEntity<Map<String, Object>> getAllBySchool(@RequestBody Map<String, Long> req) {
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

            List<AddDivisionDTO> list = service.getAllBySchoolId(schoolId, academicYearId);

            if (list.isEmpty())
                return ResponseEntity.status(404)
                        .body(buildResponse(404, "FAILED", "No divisions found", null));

            return ResponseEntity.ok(
                    buildResponse(200, "SUCCESS", "Divisions fetched successfully", list));

        } catch (RuntimeException e) {
            return ResponseEntity.status(400)
                    .body(buildResponse(400, "FAILED", e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(buildResponse(500, "FAILED", "Something went wrong", null));
        }
    }

    // ================= GET ALL DIVISIONS BY STANDARD =================
    @PostMapping("/getAllDivisionsByStandardId")
    public ResponseEntity<Map<String, Object>> getAllDivisionsByStandard(@RequestBody Map<String, Long> req) {
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

            AddStandard standard = standardRepository.findById(standardId).orElse(null);

            if (standard == null)
                return ResponseEntity.status(404)
                        .body(buildResponse(404, "FAILED", "Standard not found", null));

            if (!standard.getSchool().getSchoolId().equals(schoolId))
                return ResponseEntity.badRequest()
                        .body(buildResponse(400, "FAILED", "Standard does not belong to given School", null));

            List<AddDivisionDTO> list = service.getAllByStandardId(standardId, academicYearId);

            if (list.isEmpty())
                return ResponseEntity.status(404)
                        .body(buildResponse(404, "FAILED", "No divisions found", null));

            return ResponseEntity.ok(
                    buildResponse(200, "SUCCESS", "Divisions fetched successfully", list));

        } catch (RuntimeException e) {
            return ResponseEntity.status(400)
                    .body(buildResponse(400, "FAILED", e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(buildResponse(500, "FAILED", "Something went wrong", null));
        }
    }

    // ================= GET BY ID =================
    @PostMapping("/getByDivisionId")
    public ResponseEntity<Map<String, Object>> getById(@RequestBody Map<String, Long> req) {
        try {

            Long schoolId = req.get("schoolId");
            Long divisionId = req.get("divisionId");
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

            if (divisionId == null)
                return ResponseEntity.badRequest()
                        .body(buildResponse(400, "FAILED", "DivisionId is required", null));

            if (!divisionRepository.existsById(divisionId))
                return ResponseEntity.status(404)
                        .body(buildResponse(404, "FAILED", "Division not found", null));

            AddDivisionDTO dto = service.getByDivisionId(divisionId, academicYearId);

            if (!dto.getSchoolId().equals(schoolId))
                return ResponseEntity.badRequest()
                        .body(buildResponse(400, "FAILED", "Division does not belong to given School", null));

            return ResponseEntity.ok(
                    buildResponse(200, "SUCCESS", "Division fetched successfully", dto));

        } catch (RuntimeException e) {
            return ResponseEntity.status(400)
                    .body(buildResponse(400, "FAILED", e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(buildResponse(500, "FAILED", "Something went wrong", null));
        }
    }

    // ================= UPDATE =================
    @PostMapping("/updateDivision")
    public ResponseEntity<Map<String, Object>> update(@RequestBody AddDivisionDTO dto) {
        try {

            if (dto.getSchoolId() == null)
                return ResponseEntity.badRequest()
                        .body(buildResponse(400, "FAILED", "SchoolId is required", null));

            if (!schoolRepository.existsById(dto.getSchoolId()))
                return ResponseEntity.status(404)
                        .body(buildResponse(404, "FAILED", "School not found", null));

            if (dto.getDivisionId() == null)
                return ResponseEntity.badRequest()
                        .body(buildResponse(400, "FAILED", "DivisionId is required", null));

            if (!divisionRepository.existsById(dto.getDivisionId()))
                return ResponseEntity.status(404)
                        .body(buildResponse(404, "FAILED", "Division not found", null));

            AddDivisionDTO existing = service.getByDivisionId(dto.getDivisionId(), null);

            if (!existing.getSchoolId().equals(dto.getSchoolId()))
                return ResponseEntity.badRequest()
                        .body(buildResponse(400, "FAILED", "Division does not belong to given School", null));

            // Check if another division with the same name already exists under the same standard
            if (dto.getDivisionName() != null && !dto.getDivisionName().isBlank()) {
                if (divisionRepository.existsByAddStandard_StandardIdAndDivisionName(existing.getStandardId(), dto.getDivisionName())
                        && !existing.getDivisionName().equalsIgnoreCase(dto.getDivisionName())) {
                    return ResponseEntity.status(409)
                            .body(buildResponse(409, "FAILED", "Division name already exists for this standard", null));
                }
            }

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

            service.updateDivision(dto);

            return ResponseEntity.ok(
                    buildResponse(200, "SUCCESS", "Division updated successfully", null));

        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(buildResponse(500, "FAILED", "Something went wrong", null));
        }
    }

    // ================= DELETE =================
    @PostMapping("/deleteDivision")
    public ResponseEntity<Map<String, Object>> delete(@RequestBody Map<String, Long> req) {
        try {

            Long schoolId = req.get("schoolId");
            Long divisionId = req.get("divisionId");

            if (schoolId == null)
                return ResponseEntity.badRequest()
                        .body(buildResponse(400, "FAILED", "SchoolId is required", null));

            if (!schoolRepository.existsById(schoolId))
                return ResponseEntity.status(404)
                        .body(buildResponse(404, "FAILED", "School not found", null));

            if (divisionId == null)
                return ResponseEntity.badRequest()
                        .body(buildResponse(400, "FAILED", "DivisionId is required", null));

            if (!divisionRepository.existsById(divisionId))
                return ResponseEntity.status(404)
                        .body(buildResponse(404, "FAILED", "Division not found", null));

            AddDivisionDTO dto = service.getByDivisionId(divisionId, null);

            if (!dto.getSchoolId().equals(schoolId))
                return ResponseEntity.badRequest()
                        .body(buildResponse(400, "FAILED", "Division does not belong to given School", null));

            service.deleteDivision(divisionId);

            return ResponseEntity.ok(
                    buildResponse(200, "SUCCESS", "Division deleted successfully", null));

        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(buildResponse(500, "FAILED", "Something went wrong", null));
        }
    }
}