package com.schoolmanagement.school_admin_Controller;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.schoolmanagement.school_admin_DTO.PromotionRequestDTO;
import com.schoolmanagement.school_admin_Service.PromotionService;

@RestController
@RequestMapping("/promotion")
public class PromotionController {

    private final PromotionService promotionService;

    public PromotionController(PromotionService promotionService) {
        this.promotionService = promotionService;
    }

    @PostMapping("/promoteStudents")
    public ResponseEntity<Map<String, Object>> promoteStudents(@RequestBody PromotionRequestDTO dto) {
        Map<String, Object> response = new LinkedHashMap<>();
        try {
            if (dto.getSchoolId() == null) {
                response.put("code", 400); response.put("status", "FAILED");
                response.put("message", "schoolId is required"); response.put("data", null);
                return ResponseEntity.badRequest().body(response);
            }
            if (dto.getFromAcademicYearId() == null || dto.getToAcademicYearId() == null) {
                response.put("code", 400); response.put("status", "FAILED");
                response.put("message", "Both fromAcademicYearId and toAcademicYearId are required"); response.put("data", null);
                return ResponseEntity.badRequest().body(response);
            }
            if (dto.getFromStandardId() == null || dto.getFromDivisionId() == null) {
                response.put("code", 400); response.put("status", "FAILED");
                response.put("message", "Source standardId and divisionId are required"); response.put("data", null);
                return ResponseEntity.badRequest().body(response);
            }
            if (dto.getToStandardId() == null || dto.getToDivisionId() == null) {
                response.put("code", 400); response.put("status", "FAILED");
                response.put("message", "Target standardId and divisionId are required"); response.put("data", null);
                return ResponseEntity.badRequest().body(response);
            }
            if (dto.getStudents() == null || dto.getStudents().isEmpty()) {
                response.put("code", 400); response.put("status", "FAILED");
                response.put("message", "students list is required and cannot be empty"); response.put("data", null);
                return ResponseEntity.badRequest().body(response);
            }

            promotionService.promoteStudents(dto);

            response.put("code", 200); response.put("status", "SUCCESS");
            response.put("message", "Students promoted successfully"); response.put("data", null);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            response.put("code", 400); response.put("status", "FAILED");
            response.put("message", e.getMessage()); response.put("data", null);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            response.put("code", 500); response.put("status", "FAILED");
            response.put("message", e.getMessage()); response.put("data", null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
