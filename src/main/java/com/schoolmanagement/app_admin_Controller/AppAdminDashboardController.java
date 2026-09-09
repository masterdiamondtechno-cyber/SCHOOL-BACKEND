package com.schoolmanagement.app_admin_Controller;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.schoolmanagement.app_admin_Entity.AdminDashboardItem;
import com.schoolmanagement.app_admin_Repo.AdminDashboardItemRepository;
import com.schoolmanagement.school_admin_Entity.School;
import com.schoolmanagement.school_admin_Repo.AddStudentRepository;
import com.schoolmanagement.school_admin_Repo.SchoolRepository;
import com.schoolmanagement.school_admin_Service.DashboardService;

@RestController
@RequestMapping("/app-admin/dashboard")
public class AppAdminDashboardController {

    private final AdminDashboardItemRepository adminDashboardItemRepository;
    private final DashboardService dashboardService;
    private final SchoolRepository schoolRepository;
    private final AddStudentRepository studentRepository;

    public AppAdminDashboardController(AdminDashboardItemRepository adminDashboardItemRepository,
            DashboardService dashboardService,
            SchoolRepository schoolRepository,
            AddStudentRepository studentRepository) {
        this.adminDashboardItemRepository = adminDashboardItemRepository;
        this.dashboardService = dashboardService;
        this.schoolRepository = schoolRepository;
        this.studentRepository = studentRepository;
    }

    private Map<String, Object> build(int code, String status, String message, Object data) {
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("code", code);
        r.put("status", status);
        r.put("message", message);
        r.put("data", data);
        return r;
    }

    // ===== Save Admin Dashboard Item =====
    @PostMapping("/item/save")
    public ResponseEntity<Map<String, Object>> saveItem(@RequestBody Map<String, String> request) {
        try {
            String dashboardItemName = request.get("dashboardItemName");
            if (dashboardItemName == null || dashboardItemName.isBlank())
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'dashboardItemName' is required", null));

            if (adminDashboardItemRepository.existsByDashboardItemNameAndRole(dashboardItemName, "admin")) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(build(409, "FAILED",
                                "Dashboard item '" + dashboardItemName + "' already exists for role 'admin'", null));
            }

            AdminDashboardItem item = new AdminDashboardItem();
            item.setDashboardItemName(dashboardItemName);
            item.setRole("admin");
            adminDashboardItemRepository.save(item);

            return ResponseEntity.ok(build(200, "SUCCESS", "Admin dashboard item saved successfully", null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(build(500, "FAILED", e.getMessage(), null));
        }
    }

    // ===== Get Admin Dashboard Data =====
    @PostMapping("/data")
    public ResponseEntity<Map<String, Object>> getDashboardData() {
        try {
            List<AdminDashboardItem> items = adminDashboardItemRepository.findByRole("admin");
            if (items.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(build(404, "FAILED", "No dashboard items found for role 'admin'", null));
            }

            List<Map<String, Object>> result = new ArrayList<>();
            for (AdminDashboardItem item : items) {
                Map<String, Object> widget = new LinkedHashMap<>();
                widget.put("dashboardItemName", item.getDashboardItemName());
                widget.put("data", dashboardService.resolveAdminData(item.getDashboardItemName()));
                result.add(widget);
            }

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("items", result);

            return ResponseEntity.ok(build(200, "SUCCESS", "Admin dashboard data fetched successfully", data));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(build(500, "FAILED", e.getMessage(), null));
        }
    }

    // ===== Get All Branches (Schools) with Student Count =====
    @PostMapping("/branches")
    public ResponseEntity<Map<String, Object>> getAllBranches() {
        try {
            List<School> schools = schoolRepository.findAll();
            if (schools.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(build(404, "FAILED", "No branches found", null));
            }

            List<Map<String, Object>> branches = new ArrayList<>();
            for (School school : schools) {
                Map<String, Object> branch = new LinkedHashMap<>();
                branch.put("schoolId", school.getSchoolId());
                branch.put("schoolName", school.getSchoolName());
                branch.put("address", school.getSchoolAddress());
                branch.put("district", school.getDistrict());
                branch.put("state", school.getState());
                branch.put("status", school.getSubscriptionStatus());
                branch.put("principalName", school.getPrincipalName());
                branch.put("email", school.getEmail());
                branch.put("phoneNo", school.getPhoneNo());
                branch.put("totalStudents", studentRepository.countBySchool_SchoolId(school.getSchoolId()));
                branches.add(branch);
            }

            return ResponseEntity.ok(build(200, "SUCCESS", "Branches fetched successfully", branches));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(build(500, "FAILED", e.getMessage(), null));
        }
    }
}
