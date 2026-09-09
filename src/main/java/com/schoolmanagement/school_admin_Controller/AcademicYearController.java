package com.schoolmanagement.school_admin_Controller;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.schoolmanagement.school_admin_DTO.AcademicYearDTO;
import com.schoolmanagement.school_admin_Service.AcademicYearService;

@RestController
@RequestMapping("/academicYear")
public class AcademicYearController {

    private final AcademicYearService academicYearService;

    public AcademicYearController(AcademicYearService academicYearService) {
        this.academicYearService = academicYearService;
    }

    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> create(@RequestBody AcademicYearDTO dto) {
        Map<String, Object> response = new LinkedHashMap<>();
        try {
            if (dto.getSchoolId() == null) {
                response.put("code", 400); response.put("status", "FAILED");
                response.put("message", "schoolId is required"); response.put("data", null);
                return ResponseEntity.badRequest().body(response);
            }
            AcademicYearDTO created = academicYearService.create(dto);
            response.put("code", 200); response.put("status", "SUCCESS");
            response.put("message", "Academic Year created successfully"); response.put("data", created);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("code", 500); response.put("status", "FAILED");
            response.put("message", e.getMessage()); response.put("data", null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/getAll")
    public ResponseEntity<Map<String, Object>> getAll(@RequestBody AcademicYearDTO dto) {
        Map<String, Object> response = new LinkedHashMap<>();
        try {
            if (dto.getSchoolId() == null) {
                response.put("code", 400); response.put("status", "FAILED");
                response.put("message", "schoolId is required"); response.put("data", null);
                return ResponseEntity.badRequest().body(response);
            }
            List<AcademicYearDTO> list = academicYearService.getAllBySchoolId(dto.getSchoolId());
            if (list.isEmpty()) {
                response.put("code", 404); response.put("status", "FAILED");
                response.put("message", "No academic years found"); response.put("data", null);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            response.put("code", 200); response.put("status", "SUCCESS");
            response.put("message", "Academic years fetched successfully");
            response.put("data", list);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("code", 500); response.put("status", "FAILED");
            response.put("message", e.getMessage()); response.put("data", null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/getCurrent")
    public ResponseEntity<Map<String, Object>> getCurrent(@RequestBody AcademicYearDTO dto) {
        Map<String, Object> response = new LinkedHashMap<>();
        try {
            if (dto.getSchoolId() == null) {
                response.put("code", 400); response.put("status", "FAILED");
                response.put("message", "schoolId is required"); response.put("data", null);
                return ResponseEntity.badRequest().body(response);
            }
            AcademicYearDTO ay = academicYearService.getCurrent(dto.getSchoolId());
            if (ay == null) {
                response.put("code", 404); response.put("status", "FAILED");
                response.put("message", "No current academic year found"); response.put("data", null);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            response.put("code", 200); response.put("status", "SUCCESS");
            response.put("message", "Current academic year fetched successfully"); response.put("data", ay);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("code", 500); response.put("status", "FAILED");
            response.put("message", e.getMessage()); response.put("data", null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/update")
    public ResponseEntity<Map<String, Object>> update(@RequestBody AcademicYearDTO dto) {
        Map<String, Object> response = new LinkedHashMap<>();
        try {
            if (dto.getAcademicYearId() == null) {
                response.put("code", 400); response.put("status", "FAILED");
                response.put("message", "academicYearId is required"); response.put("data", null);
                return ResponseEntity.badRequest().body(response);
            }
            AcademicYearDTO updated = academicYearService.update(dto);
            response.put("code", 200); response.put("status", "SUCCESS");
            response.put("message", "Academic Year updated successfully"); response.put("data", updated);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            response.put("code", 404); response.put("status", "FAILED");
            response.put("message", e.getMessage()); response.put("data", null);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (Exception e) {
            response.put("code", 500); response.put("status", "FAILED");
            response.put("message", e.getMessage()); response.put("data", null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/delete")
    public ResponseEntity<Map<String, Object>> delete(@RequestBody AcademicYearDTO dto) {
        Map<String, Object> response = new LinkedHashMap<>();
        try {
            if (dto.getAcademicYearId() == null) {
                response.put("code", 400); response.put("status", "FAILED");
                response.put("message", "academicYearId is required"); response.put("data", null);
                return ResponseEntity.badRequest().body(response);
            }
            if (dto.getSchoolId() == null) {
                response.put("code", 400); response.put("status", "FAILED");
                response.put("message", "schoolId is required"); response.put("data", null);
                return ResponseEntity.badRequest().body(response);
            }
            academicYearService.delete(dto.getAcademicYearId(), dto.getSchoolId());
            response.put("code", 200); response.put("status", "SUCCESS");
            response.put("message", "Academic Year deleted successfully"); response.put("data", null);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            response.put("code", 404); response.put("status", "FAILED");
            response.put("message", e.getMessage()); response.put("data", null);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (Exception e) {
            response.put("code", 500); response.put("status", "FAILED");
            response.put("message", e.getMessage()); response.put("data", null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/activate")
    public ResponseEntity<Map<String, Object>> activate(@RequestBody AcademicYearDTO dto) {
        Map<String, Object> response = new LinkedHashMap<>();
        try {
            if (dto.getSchoolId() == null) {
                response.put("code", 400); response.put("status", "FAILED");
                response.put("message", "schoolId is required in the body");
                return ResponseEntity.badRequest().body(response);
            }
            if (dto.getAcademicYearId() == null) {
                response.put("code", 400); response.put("status", "FAILED");
                response.put("message", "academicYearId is required");
                return ResponseEntity.badRequest().body(response);
            }
            academicYearService.activate(dto.getAcademicYearId(), dto.getSchoolId());
            response.put("code", 200); response.put("status", "SUCCESS");
            response.put("message", "Academic Year activated. All others deactivated.");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            response.put("code", 404); response.put("status", "FAILED");
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (Exception e) {
            response.put("code", 500); response.put("status", "FAILED");
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/archive")
    public ResponseEntity<Map<String, Object>> archive(@RequestBody AcademicYearDTO dto) {
        Map<String, Object> response = new LinkedHashMap<>();
        try {
            if (dto.getSchoolId() == null) {
                response.put("code", 400); response.put("status", "FAILED");
                response.put("message", "schoolId is required in the body");
                return ResponseEntity.badRequest().body(response);
            }
            if (dto.getAcademicYearId() == null) {
                response.put("code", 400); response.put("status", "FAILED");
                response.put("message", "academicYearId is required");
                return ResponseEntity.badRequest().body(response);
            }
            academicYearService.archive(dto.getAcademicYearId(), dto.getSchoolId());
            response.put("code", 200); response.put("status", "SUCCESS");
            response.put("message", "Academic Year archived successfully.");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            response.put("code", 404); response.put("status", "FAILED");
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (Exception e) {
            response.put("code", 500); response.put("status", "FAILED");
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
