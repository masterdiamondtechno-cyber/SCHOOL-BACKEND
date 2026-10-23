package com.schoolmanagement.school.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.schoolmanagement.school.service.DataMigrationService;

@RestController
@RequestMapping("/api/system")
public class DataMigrationController {
    
    private final DataMigrationService migrationService;

    public DataMigrationController(DataMigrationService migrationService) {
        this.migrationService = migrationService;
    }

    @PostMapping("/migrate-student-history")
    public ResponseEntity<Map<String, String>> runMigration() {
        migrationService.migrateStudentAcademicHistory();
        Map<String, String> response = new HashMap<>();
        response.put("status", "SUCCESS");
        response.put("message", "All existing student mappings successfully migrated to the new StudentAcademicYear history table.");
        return ResponseEntity.ok(response);
    }
}
