package com.schoolmanagement.school_admin_Controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.schoolmanagement.school_admin_DTO.AddSubjectDTO;
import com.schoolmanagement.school_admin_Entity.AddDivision;
import com.schoolmanagement.school_admin_Entity.AddStandard;
import com.schoolmanagement.school_admin_Repo.AcademicYearRepository;
import com.schoolmanagement.school_admin_Repo.AddDivisionRepository;
import com.schoolmanagement.school_admin_Repo.AddStandardRepository;
import com.schoolmanagement.school_admin_Repo.AddSubjectRepository;
import com.schoolmanagement.school_admin_Repo.SchoolRepository;
import com.schoolmanagement.school_admin_Service.AddSubjectService;

@RestController
@RequestMapping("/subject")
public class AddSubjectController {

    @Autowired
    private AddSubjectService service;

    @Autowired
    private SchoolRepository schoolRepository;

    @Autowired
    private AddStandardRepository standardRepository;

    @Autowired
    private AddDivisionRepository divisionRepository;

    @Autowired
    private AddSubjectRepository subjectRepository;

    @Autowired
    private AcademicYearRepository academicYearRepository;

    // Common Response Builder
    private Map<String, Object> buildResponse(int code, String status, String message, Object data) {
        Map<String, Object> res = new HashMap<>();
        res.put("code", code);
        res.put("status", status);
        res.put("message", message);
        res.put("data", data);
        return res;
    }

    // ================= ADD SUBJECT =================
    @PostMapping("/addSubject")
    public ResponseEntity<Map<String, Object>> add(@RequestBody AddSubjectDTO dto) {
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

            if (dto.getDivisionId() == null)
                return ResponseEntity.badRequest()
                        .body(buildResponse(400, "FAILED", "DivisionId is required", null));

            AddDivision division = divisionRepository.findById(dto.getDivisionId()).orElse(null);

            if (division == null)
                return ResponseEntity.status(404)
                        .body(buildResponse(404, "FAILED", "Division not found", null));

            if (!division.getAddStandard().getStandardId().equals(dto.getStandardId()))
                return ResponseEntity.badRequest()
                        .body(buildResponse(400, "FAILED", "Division does not belong to given Standard", null));

            if (dto.getSubjectName() == null || dto.getSubjectName().isBlank())
                return ResponseEntity.badRequest()
                        .body(buildResponse(400, "FAILED", "Subject name is required", null));

            if (subjectRepository
                    .existsBySchool_SchoolIdAndAddStandard_StandardIdAndAddDivision_DivisionIdAndSubjectName(
                            dto.getSchoolId(),
                            dto.getStandardId(),
                            dto.getDivisionId(),
                            dto.getSubjectName()))
                return ResponseEntity.status(409)
                        .body(buildResponse(409, "FAILED", "Subject already exists for this division", null));

            service.addSubject(dto);

            return ResponseEntity.ok(
                    buildResponse(200, "SUCCESS", "Subject added successfully", null));

        } catch (RuntimeException e) {
            return ResponseEntity.status(400)
                    .body(buildResponse(400, "FAILED", e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(buildResponse(500, "FAILED", "Something went wrong", null));
        }
    }

    // ================= GET ALL =================
    @PostMapping("/getAll")
    public ResponseEntity<Map<String, Object>> getAll(@RequestBody Map<String, Long> req) {
        try {

            Long schoolId = req.get("schoolId");
            Long standardId = req.get("standardId");
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

            if (standardId == null)
                return ResponseEntity.badRequest()
                        .body(buildResponse(400, "FAILED", "StandardId is required", null));

            if (!standardRepository.existsById(standardId))
                return ResponseEntity.status(404)
                        .body(buildResponse(404, "FAILED", "Standard not found", null));

            if (divisionId == null)
                return ResponseEntity.badRequest()
                        .body(buildResponse(400, "FAILED", "DivisionId is required", null));

            if (!divisionRepository.existsById(divisionId))
                return ResponseEntity.status(404)
                        .body(buildResponse(404, "FAILED", "Division not found", null));

            List<AddSubjectDTO> list = service.getAllBySchoolStandardDivision(schoolId, standardId, divisionId,
                    academicYearId);

            return ResponseEntity.ok(
                    buildResponse(200, "SUCCESS",
                            list.isEmpty() ? "No subjects found" : "Subjects fetched successfully",
                            list));

        } catch (RuntimeException e) {
            return ResponseEntity.status(400)
                    .body(buildResponse(400, "FAILED", e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(buildResponse(500, "FAILED", "Something went wrong", null));
        }
    }

    // ================= GET ALL BY SCHOOL ID =================
    @PostMapping("/getAllBySchoolId")
    public ResponseEntity<Map<String, Object>> getAllBySchoolId(@RequestBody Map<String, Long> req) {
        try {
            Long schoolId = req.get("schoolId");
            Long academicYearId = req.get("academicYearId");

            if (schoolId == null)
                return ResponseEntity.badRequest().body(buildResponse(400, "FAILED", "SchoolId is required", null));
            if (academicYearId == null)
                return ResponseEntity.badRequest()
                        .body(buildResponse(400, "FAILED", "academicYearId is required", null));

            if (!schoolRepository.existsById(schoolId))
                return ResponseEntity.status(404).body(buildResponse(404, "FAILED", "School not found", null));
            if (!academicYearRepository.existsById(academicYearId))
                return ResponseEntity.status(404).body(buildResponse(404, "FAILED", "Academic Year not found", null));

            List<AddSubjectDTO> list = service.getAllBySchoolId(schoolId, academicYearId);

            return ResponseEntity.ok(buildResponse(200, "SUCCESS",
                    list.isEmpty() ? "No subjects found" : "Subjects fetched successfully", list));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(buildResponse(500, "FAILED", "Something went wrong", null));
        }
    }

    // ================= GET BY ID =================
    @PostMapping("/getBySubjectId")
    public ResponseEntity<Map<String, Object>> getById(@RequestBody Map<String, Long> req) {
        try {

            Long schoolId = req.get("schoolId");
            Long subjectId = req.get("subjectId");
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

            if (subjectId == null)
                return ResponseEntity.badRequest()
                        .body(buildResponse(400, "FAILED", "SubjectId is required", null));

            if (!subjectRepository.existsById(subjectId))
                return ResponseEntity.status(404)
                        .body(buildResponse(404, "FAILED", "Subject not found", null));

            AddSubjectDTO dto = service.getBySubjectId(subjectId, academicYearId);

            if (!dto.getSchoolId().equals(schoolId))
                return ResponseEntity.badRequest()
                        .body(buildResponse(400, "FAILED", "Subject does not belong to given School", null));

            return ResponseEntity.ok(
                    buildResponse(200, "SUCCESS", "Subject fetched successfully", dto));

        } catch (RuntimeException e) {
            return ResponseEntity.status(400)
                    .body(buildResponse(400, "FAILED", e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(buildResponse(500, "FAILED", "Something went wrong", null));
        }
    }

    // ================= UPDATE =================
    @PostMapping("/updateSubject")
    public ResponseEntity<Map<String, Object>> update(@RequestBody AddSubjectDTO dto) {
        try {

            if (dto.getSchoolId() == null)
                return ResponseEntity.badRequest()
                        .body(buildResponse(400, "FAILED", "SchoolId is required", null));

            if (!schoolRepository.existsById(dto.getSchoolId()))
                return ResponseEntity.status(404)
                        .body(buildResponse(404, "FAILED", "School not found", null));

            if (dto.getSubjectId() == null)
                return ResponseEntity.badRequest()
                        .body(buildResponse(400, "FAILED", "SubjectId is required", null));

            if (!subjectRepository.existsById(dto.getSubjectId()))
                return ResponseEntity.status(404)
                        .body(buildResponse(404, "FAILED", "Subject not found", null));

            AddSubjectDTO existing = service.getBySubjectId(dto.getSubjectId(), null);

            if (!existing.getSchoolId().equals(dto.getSchoolId()))
                return ResponseEntity.badRequest()
                        .body(buildResponse(400, "FAILED", "Subject does not belong to given School", null));

            // Check if another subject with the same name already exists under the same
            // school/standard/division
            if (dto.getSubjectName() != null && !dto.getSubjectName().isBlank()) {
                if (subjectRepository
                        .existsBySchool_SchoolIdAndAddStandard_StandardIdAndAddDivision_DivisionIdAndSubjectName(
                                existing.getSchoolId(), existing.getStandardId(), existing.getDivisionId(),
                                dto.getSubjectName())
                        && !existing.getSubjectName().equalsIgnoreCase(dto.getSubjectName())) {
                    return ResponseEntity.status(409)
                            .body(buildResponse(409, "FAILED", "Subject name already exists for this division", null));
                }
            }

            service.updateSubject(dto);

            return ResponseEntity.ok(
                    buildResponse(200, "SUCCESS", "Subject updated successfully", null));

        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(buildResponse(500, "FAILED", "Something went wrong", null));
        }
    }

    // ================= DELETE =================
    @PostMapping("/deleteSubject")
    public ResponseEntity<Map<String, Object>> delete(@RequestBody Map<String, Long> req) {
        try {

            Long schoolId = req.get("schoolId");
            Long subjectId = req.get("subjectId");

            if (schoolId == null)
                return ResponseEntity.badRequest()
                        .body(buildResponse(400, "FAILED", "SchoolId is required", null));

            if (!schoolRepository.existsById(schoolId))
                return ResponseEntity.status(404)
                        .body(buildResponse(404, "FAILED", "School not found", null));

            if (subjectId == null)
                return ResponseEntity.badRequest()
                        .body(buildResponse(400, "FAILED", "SubjectId is required", null));

            if (!subjectRepository.existsById(subjectId))
                return ResponseEntity.status(404)
                        .body(buildResponse(404, "FAILED", "Subject not found", null));

            AddSubjectDTO dto = service.getBySubjectId(subjectId, null);

            if (!dto.getSchoolId().equals(schoolId))
                return ResponseEntity.badRequest()
                        .body(buildResponse(400, "FAILED", "Subject does not belong to given School", null));

            service.deleteSubject(subjectId);

            return ResponseEntity.ok(
                    buildResponse(200, "SUCCESS", "Subject deleted successfully", null));

        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(buildResponse(500, "FAILED", "Something went wrong", null));
        }
    }

}