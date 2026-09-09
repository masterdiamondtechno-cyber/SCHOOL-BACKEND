package com.schoolmanagement.school_admin_Controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/status")
public class SystemStatusController {

    @GetMapping
    public Map<String, Object> getSystemStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("application", "School Management System");
        status.put("status", "UP");
        status.put("environment", "Local Development");
        status.put("version", "0.0.1-SNAPSHOT");
        status.put("timestamp", System.currentTimeMillis());
        return status;
    }
}
