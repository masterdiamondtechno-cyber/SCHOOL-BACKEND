package com.schoolmanagement.school.controller;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.schoolmanagement.school.dto.LoginRequestDTO;
import com.schoolmanagement.school.dto.SchoolDTO;
import com.schoolmanagement.school.dto.SchoolIdRequestDTO;
import com.schoolmanagement.school.dto.SendOtpRequestDTO;
import com.schoolmanagement.school.dto.UpdatePasswordRequestDTO;
import com.schoolmanagement.school.dto.VerifyOtpRequestDTO;
import com.schoolmanagement.school.entity.School;
import com.schoolmanagement.school.repository.SchoolRepository;
import com.schoolmanagement.school.service.SchoolService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/AddSchool")
public class SchoolController {

    @Autowired
	private  SchoolService schoolService;
	
	@Autowired
	private SchoolRepository schoolRepository;

 
    @PostMapping(value = "/create", consumes = {"multipart/form-data"})
    public ResponseEntity<Map<String, Object>> createSchool(
            @ModelAttribute SchoolDTO request,
            @RequestParam(value = "schoolLogo", required = false) MultipartFile logoFile) {

        Map<String, Object> response = new LinkedHashMap<>();

        try {

            // 1️⃣ Request body missing
            if (request == null) {
                response.put("code", 400);
                response.put("status", "FAILED");
                response.put("message", "Request body is missing");
                response.put("data", null);
                return ResponseEntity.badRequest().body(response);
            }

            // 2️⃣ Empty JSON {}
            if (request.getSchoolName() == null &&
                request.getEmail() == null &&
                request.getPhoneNo() == null &&
                request.getUdiseNo() == null) {

                response.put("code", 400);
                response.put("status", "FAILED");
                response.put("message", "No data provided in request body");
                response.put("data", null);
                return ResponseEntity.badRequest().body(response);
            }

            // 3️⃣ Required Fields Validation

            if (request.getSchoolName() == null || request.getSchoolName().trim().isEmpty()) {
                response.put("code", 400);
                response.put("status", "FAILED");
                response.put("message", "School Name is required");
                response.put("data", null);
                return ResponseEntity.badRequest().body(response);
            }

            if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
                response.put("code", 400);
                response.put("status", "FAILED");
                response.put("message", "Email is required");
                response.put("data", null);
                return ResponseEntity.badRequest().body(response);
            }

            if (request.getPhoneNo() == null || request.getPhoneNo().trim().isEmpty()) {
                response.put("code", 400);
                response.put("status", "FAILED");
                response.put("message", "Phone Number is required");
                response.put("data", null);
                return ResponseEntity.badRequest().body(response);
            }

            // 4️⃣ Email Format Validation
            if (!request.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                response.put("code", 400);
                response.put("status", "FAILED");
                response.put("message", "Invalid email format");
                response.put("data", null);
                return ResponseEntity.badRequest().body(response);
            }

            // 5️⃣ Mobile Number Validation (10 digits)
            if (!request.getPhoneNo().matches("\\d{10}")) {
                response.put("code", 400);
                response.put("status", "FAILED");
                response.put("message", "Phone number must be 10 digits");
                response.put("data", null);
                return ResponseEntity.badRequest().body(response);
            }

            // 6️⃣ Length Validations
            if (request.getSchoolName().length() > 150) {
                response.put("code", 400);
                response.put("status", "FAILED");
                response.put("message", "School name should not exceed 150 characters");
                response.put("data", null);
                return ResponseEntity.badRequest().body(response);
            }

            // === DEBUG LOGS ===
            long totalSchools = schoolRepository.count();
            System.out.println("\n\n====== CREATE SCHOOL DEBUG ======");
            System.out.println("Total schools currently in DB: " + totalSchools);
            System.out.println("Checking Email: [" + request.getEmail() + "]");
            
            boolean emailExists = schoolRepository.existsByEmail(request.getEmail());
            System.out.println("Result of existsByEmail: " + emailExists);
            System.out.println("=================================\n\n");

            // 7️⃣ Duplicate Email Check
            if (emailExists) {
                response.put("code", 400);
                response.put("status", "FAILED");
                response.put("message", "Email already exists");
                response.put("data", null);
                return ResponseEntity.badRequest().body(response);
            }
            
            // 7️⃣ Duplicate udice Check
            if (schoolRepository.existsByUdiseNo(request.getUdiseNo())) {
                response.put("code", 400);
                response.put("status", "FAILED");
                response.put("message", "UDISE NO already exists");
                response.put("data", null);
                return ResponseEntity.badRequest().body(response);
            }
            

            // 8️⃣ Duplicate Phone Check
            if (schoolRepository.existsByPhoneNo(request.getPhoneNo())) {
                response.put("code", 400);
                response.put("status", "FAILED");
                response.put("message", "Phone number already exists");
                response.put("data", null);
                return ResponseEntity.badRequest().body(response);
            }

            // 9️⃣ Save School
            School savedSchool = schoolService.createSchool(request, logoFile);

            response.put("code", 200);
            response.put("status", "SUCCESS");
            response.put("message", "School created successfully");
            
            // Return schoolId and optionally ownerId in response
            Map<String, Object> responseData = new LinkedHashMap<>();
            responseData.put("schoolId", savedSchool.getSchoolId());
            if (savedSchool.getOwner() != null) {
                responseData.put("ownerId", savedSchool.getOwner().getOwnerId());
            }
            response.put("data", responseData);

            return ResponseEntity.ok(response);

        } catch (Exception e) {

            response.put("code", 500);
            response.put("status", "FAILED");
            response.put("message", "Internal Server Error");
            response.put("data", null);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/getBySchoolId")
    public ResponseEntity<Map<String, Object>> getBySchoolId(
            @RequestBody(required = false) SchoolIdRequestDTO  request) {

        Map<String, Object> response = new LinkedHashMap<>();

        try {

            // 1️⃣ Request body missing
            if (request == null) {
                response.put("code", 400);
                response.put("status", "FAILED");
                response.put("message", "Request body is missing");
                response.put("data", null);
                return ResponseEntity.badRequest().body(response);
            }

            // 2️⃣ schoolId missing
            if (request.getSchoolId() == null) {
                response.put("code", 400);
                response.put("status", "FAILED");
                response.put("message", "School ID is required");
                response.put("data", null);
                return ResponseEntity.badRequest().body(response);
            }

            // 3️⃣ Check if school exists
            School school = schoolService.getBySchoolId(request.getSchoolId());

            if (school == null) {
                response.put("code", 404);
                response.put("status", "FAILED");
                response.put("message", "School not found");
                response.put("data", null);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            // 4️⃣ Success
            response.put("code", 200);
            response.put("status", "SUCCESS");
            response.put("message", "School retrieved successfully");
            response.put("data", school);

            return ResponseEntity.ok(response);

        } catch (Exception e) {

            response.put("code", 500);
            response.put("status", "FAILED");
            response.put("message", "Internal Server Error");
            response.put("data", null);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/resend-registration-email")
    public ResponseEntity<Map<String, Object>> resendRegistrationEmail(
            @RequestBody(required = false) SchoolIdRequestDTO request) {

        Map<String, Object> response = new LinkedHashMap<>();

        try {
            // 1️⃣ Request body missing
            if (request == null) {
                response.put("code", 400);
                response.put("status", "FAILED");
                response.put("message", "Request body is missing");
                response.put("data", null);
                return ResponseEntity.badRequest().body(response);
            }

            // 2️⃣ School ID missing
            if (request.getSchoolId() == null) {
                response.put("code", 400);
                response.put("status", "FAILED");
                response.put("message", "School ID is required");
                response.put("data", null);
                return ResponseEntity.badRequest().body(response);
            }

            // 3️⃣ Call service
            schoolService.resendRegistrationEmail(request.getSchoolId());

            response.put("code", 200);
            response.put("status", "SUCCESS");
            response.put("message", "Registration email sent successfully");
            response.put("data", null);

            return ResponseEntity.ok(response);

        } catch (RuntimeException ex) {
            response.put("code", 404);
            response.put("status", "FAILED");
            response.put("message", ex.getMessage());
            response.put("data", null);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);

        } catch (Exception e) {
            response.put("code", 500);
            response.put("status", "FAILED");
            response.put("message", "Internal Server Error");
            response.put("data", null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    @PostMapping("/getAll")
    public ResponseEntity<Map<String, Object>> getAllSchools() {

        Map<String, Object> response = new LinkedHashMap<>();

        try {

            List<School> schools = schoolService.getAllSchools();

            // 1️⃣ No Data Found
            if (schools == null || schools.isEmpty()) {
                response.put("code", 404);
                response.put("status", "FAILED");
                response.put("message", "No schools found");
                response.put("data", null);

                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            // 2️⃣ Success
            response.put("code", 200);
            response.put("status", "SUCCESS");
            response.put("message", "Schools retrieved successfully");
            response.put("data", schools);

            return ResponseEntity.ok(response);

        } catch (Exception e) {

            response.put("code", 500);
            response.put("status", "FAILED");
            response.put("message", "Internal Server Error");
            response.put("data", null);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    
    @PostMapping("/delete")
    public ResponseEntity<Map<String, Object>> deleteSchool(
            @RequestBody(required = false) SchoolIdRequestDTO request) {

        Map<String, Object> response = new LinkedHashMap<>();

        try {

            // 1️⃣ Request body missing
            if (request == null) {
                response.put("code", 400);
                response.put("status", "FAILED");
                response.put("message", "Request body is missing");
                response.put("data", null);
                return ResponseEntity.badRequest().body(response);
            }

            // 2️⃣ School ID missing
            if (request.getSchoolId() == null) {
                response.put("code", 400);
                response.put("status", "FAILED");
                response.put("message", "School ID is required");
                response.put("data", null);
                return ResponseEntity.badRequest().body(response);
            }

            // 3️⃣ Check if School exists
            School school = schoolService.getBySchoolId(request.getSchoolId());

            if (school == null) {
                response.put("code", 404);
                response.put("status", "FAILED");
                response.put("message", "School not found");
                response.put("data", null);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            // 4️⃣ Delete School
            schoolService.deleteSchool(request.getSchoolId());

            response.put("code", 200);
            response.put("status", "SUCCESS");
            response.put("message", "School deleted successfully");
            response.put("data", null);

            return ResponseEntity.ok(response);

        } catch (RuntimeException ex) {

            response.put("code", 404);
            response.put("status", "FAILED");
            response.put("message", ex.getMessage());
            response.put("data", null);

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);

        } catch (Exception e) {

            response.put("code", 500);
            response.put("status", "FAILED");
            response.put("message", "Internal Server Error");
            response.put("data", null);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    @PostMapping(value = "/update", consumes = {"multipart/form-data"})
    public ResponseEntity<Map<String, Object>> updateSchool(
            @ModelAttribute SchoolDTO request,
            @RequestParam(value = "schoolLogo", required = false) MultipartFile logoFile) {

        Map<String, Object> response = new LinkedHashMap<>();

        try {

            // 1️⃣ Request body missing
            if (request == null) {
                response.put("code", 400);
                response.put("status", "FAILED");
                response.put("message", "Request body is missing");
                response.put("data", null);
                return ResponseEntity.badRequest().body(response);
            }

            // 2️⃣ School ID required
            if (request.getSchoolId() == null) {
                response.put("code", 400);
                response.put("status", "FAILED");
                response.put("message", "School ID is required");
                response.put("data", null);
                return ResponseEntity.badRequest().body(response);
            }

            // 3️⃣ Check School Exists
            School school = schoolRepository.findById(request.getSchoolId())
                    .orElse(null);

            if (school == null) {
                response.put("code", 404);
                response.put("status", "FAILED");
                response.put("message", "School not found");
                response.put("data", null);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            // 4️⃣ School Name validation
            if (request.getSchoolName() == null || request.getSchoolName().trim().isEmpty()) {
                response.put("code", 400);
                response.put("status", "FAILED");
                response.put("message", "School Name is required");
                response.put("data", null);
                return ResponseEntity.badRequest().body(response);
            }

            if (request.getSchoolName().length() > 150) {
                response.put("code", 400);
                response.put("status", "FAILED");
                response.put("message", "School name should not exceed 150 characters");
                response.put("data", null);
                return ResponseEntity.badRequest().body(response);
            }

            // 5️⃣ Email validation
            if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
                response.put("code", 400);
                response.put("status", "FAILED");
                response.put("message", "Email is required");
                response.put("data", null);
                return ResponseEntity.badRequest().body(response);
            }

            if (!request.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                response.put("code", 400);
                response.put("status", "FAILED");
                response.put("message", "Invalid email format");
                response.put("data", null);
                return ResponseEntity.badRequest().body(response);
            }

            // 6️⃣ Phone validation
            if (request.getPhoneNo() == null || request.getPhoneNo().trim().isEmpty()) {
                response.put("code", 400);
                response.put("status", "FAILED");
                response.put("message", "Phone Number is required");
                response.put("data", null);
                return ResponseEntity.badRequest().body(response);
            }

            if (!request.getPhoneNo().matches("\\d{10}")) {
                response.put("code", 400);
                response.put("status", "FAILED");
                response.put("message", "Phone number must be 10 digits");
                response.put("data", null);
                return ResponseEntity.badRequest().body(response);
            }

            // 7️⃣ Duplicate email check (excluding same record)
            if (schoolRepository.existsByEmailAndSchoolIdNot(
                    request.getEmail(), request.getSchoolId())) {

                response.put("code", 400);
                response.put("status", "FAILED");
                response.put("message", "Email already exists");
                response.put("data", null);
                return ResponseEntity.badRequest().body(response);
            }

            // 8️⃣ Duplicate phone check
            if (schoolRepository.existsByPhoneNoAndSchoolIdNot(
                    request.getPhoneNo(), request.getSchoolId())) {

                response.put("code", 400);
                response.put("status", "FAILED");
                response.put("message", "Phone number already exists");
                response.put("data", null);
                return ResponseEntity.badRequest().body(response);
            }

            // 9️⃣ Update Fields
            school.setSchoolName(request.getSchoolName());
//            school.setUdiseNo(request.getUdiseNo());
            school.setState(request.getState());
            school.setDistrict(request.getDistrict());
            school.setTaluka(request.getTaluka());
            school.setPincode(request.getPincode());
            school.setSchoolAddress(request.getSchoolAddress());
            // schoolLogo is handled by the service via S3 upload
//            school.setEmail(request.getEmail());
//            school.setPhoneNo(request.getPhoneNo());
//            school.setRoleId(request.getRoleId());
            school.setSubscriptionStatus(request.getSubscriptionStatus());

            schoolService.updateSchool(request.getSchoolId(), request, logoFile);
            
            response.put("code", 200);
            response.put("status", "SUCCESS");
            response.put("message", "School updated successfully");
            response.put("data", null);

            return ResponseEntity.ok(response);

        } catch (Exception e) {

            response.put("code", 500);
            response.put("status", "FAILED");
            response.put("message", "Internal Server Error");
            response.put("data", null);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    @PostMapping("/sendOtp")
    public ResponseEntity<Map<String, Object>> sendOtp(@RequestBody SendOtpRequestDTO request) {
        Map<String, Object> response = new LinkedHashMap<>();
        try {
            String msg = schoolService.sendOtp(request);
            response.put("code", 200);
            response.put("status", "SUCCESS");
            response.put("message", msg);
            response.put("data", null);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("code", 400);
            response.put("status", "FAILED");
            response.put("message", e.getMessage());
            response.put("data", null);
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/verifyOtp")
    public ResponseEntity<Map<String, Object>> verifyOtp(@RequestBody VerifyOtpRequestDTO request) {
        Map<String, Object> response = new LinkedHashMap<>();
        try {
            String msg = schoolService.verifyOtp(request);
            response.put("code", 200);
            response.put("status", "SUCCESS");
            response.put("message", msg);
            response.put("data", null);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("code", 400);
            response.put("status", "FAILED");
            response.put("message", e.getMessage());
            response.put("data", null);
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/updatePassword")
    public ResponseEntity<Map<String, Object>> updatePassword(@RequestBody UpdatePasswordRequestDTO request) {
        Map<String, Object> response = new LinkedHashMap<>();
        try {
            String msg = schoolService.updatePassword(request);
            response.put("code", 200);
            response.put("status", "SUCCESS");
            response.put("message", msg);
            response.put("data", null);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("code", 400);
            response.put("status", "FAILED");
            response.put("message", e.getMessage());
            response.put("data", null);
            return ResponseEntity.badRequest().body(response);
        }
    }

}
