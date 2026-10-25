package com.schoolmanagement.admin.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.schoolmanagement.Config.PasswordGenerator;
import com.schoolmanagement.admin.dto.ApprovalRequestDTO;
import com.schoolmanagement.admin.dto.SchoolStatusUpdateDTO;
import com.schoolmanagement.admin.entity.SchoolRegistrationApproval;
import com.schoolmanagement.admin.repository.SchoolRegistrationApprovalRepository;
import com.schoolmanagement.admin.service.EmailService;
import com.schoolmanagement.admin.service.SchoolApprovalService;
import com.schoolmanagement.owner.entity.Owner;
import com.schoolmanagement.owner.repository.OwnerRepository;
import com.schoolmanagement.school.entity.School;
import com.schoolmanagement.school.repository.SchoolRepository;
import com.schoolmanagement.school.repository.RefreshTokenRepository;

@Service
public class SchoolApprovalServiceImpl implements SchoolApprovalService {

    @Autowired
    private SchoolRepository schoolRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private SchoolRegistrationApprovalRepository approvalRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private OwnerRepository ownerRepository;

    @Autowired
    private com.schoolmanagement.subscription.service.SubscriptionService subscriptionService;

    @Override
    public Map<String, Object> approveOrReject(ApprovalRequestDTO request) {
        Map<String, Object> response = new HashMap<>();

        try {
            // 1. Find the school
            Optional<School> schoolOpt = schoolRepository.findBySchoolId(request.getSchoolId());
            if (schoolOpt.isEmpty()) {
                response.put("status", "FAILED");
                response.put("message", "School not found with ID: " + request.getSchoolId());
                return response;
            }

            School school = schoolOpt.get();

            // 2. Find the approval record
            Optional<SchoolRegistrationApproval> approvalOpt =
                    approvalRepository.findBySchool_SchoolId(request.getSchoolId());
            if (approvalOpt.isEmpty()) {
                response.put("status", "FAILED");
                response.put("message", "Approval record not found for school ID: " + request.getSchoolId());
                return response;
            }

            SchoolRegistrationApproval approval = approvalOpt.get();
            String status = request.getApprovalRejectionStatus();

            if ("APPROVED".equalsIgnoreCase(status)) {
                // 3a. Generate and encode a new password
                String rawPassword = PasswordGenerator.generatePassword(10);
                String encodedPassword = passwordEncoder.encode(rawPassword);

                // 3b. Update school
                school.setPassword(encodedPassword);
                school.setRegistrationStatus("APPROVED");
                school.setSchoolStatus("ACTIVE");

                // Assign owner if provided
                if (request.getOwnerId() != null) {
                    Optional<Owner> ownerOpt = ownerRepository.findById(request.getOwnerId());
                    if (ownerOpt.isEmpty()) {
                        response.put("status", "FAILED");
                        response.put("message", "Owner not found with ID: " + request.getOwnerId());
                        return response;
                    }
                    school.setOwner(ownerOpt.get());
                }

                schoolRepository.save(school);

                // 3c. Update approval record
                approval.setApprovalRejectionStatus("APPROVED");
                approval.setRejectedReason(null);
                approvalRepository.save(approval);

                // Auto-start free trial if not already active
                try {
                    if (!subscriptionService.isSubscriptionActive(school.getSchoolId())) {
                        subscriptionService.startFreeTrial(school.getSchoolId());
                    }
                } catch (Exception e) {
                    System.err.println("Could not auto-start trial on approval: " + e.getMessage());
                }

                // 3d. Send approval email with raw password
                emailService.sendPasswordEmail(school.getEmail(), rawPassword);

                response.put("status", "SUCCESS");
                response.put("message", "School approved successfully. Login credentials sent to " + school.getEmail());

            } else if ("REJECTED".equalsIgnoreCase(status)) {
                // 4a. Update school
                school.setRegistrationStatus("REJECTED");
                school.setSchoolStatus("INACTIVE");
                schoolRepository.save(school);

                // 4b. Update approval record
                approval.setApprovalRejectionStatus("REJECTED");
                approval.setRejectedReason(request.getRejectedReason());
                approvalRepository.save(approval);

                // 4c. Send rejection email
                emailService.sendRejectionEmail(school.getEmail(), request.getRejectedReason());

                response.put("status", "SUCCESS");
                response.put("message", "School rejected successfully. Rejection email sent to " + school.getEmail());

            } else {
                response.put("status", "FAILED");
                response.put("message", "Invalid status. Expected APPROVED or REJECTED.");
            }

        } catch (Exception e) {
            response.put("status", "FAILED");
            response.put("message", "An error occurred: " + e.getMessage());
        }

        return response;
    }

    @Override
    public List<School> getActiveSchools() {
        return schoolRepository.findByschoolStatusIgnoreCase("ACTIVE");
    }

    @Override
    public List<School> getInactiveSchools() {
        return schoolRepository.findByRegistrationStatusIgnoreCase("PENDING");
    }

    @Override
    public long countActiveSchools() {
        return schoolRepository.countBySchoolStatusIgnoreCase("ACTIVE");
    }

    @Override
    public long countInactiveSchools() {
        return schoolRepository.countByRegistrationStatusIgnoreCase("PENDING");
    }

    @Override
    @jakarta.transaction.Transactional
    public Map<String, Object> updateSchoolStatus(SchoolStatusUpdateDTO request) {
        Map<String, Object> response = new HashMap<>();

        try {
            if (request.getSchoolId() == null) {
                response.put("status", "FAILED");
                response.put("message", "School ID is required");
                return response;
            }

            if (request.getStatus() == null || (!"ACTIVE".equalsIgnoreCase(request.getStatus()) && !"INACTIVE".equalsIgnoreCase(request.getStatus()))) {
                response.put("status", "FAILED");
                response.put("message", "Invalid status. Expected ACTIVE or INACTIVE.");
                return response;
            }

            Optional<School> schoolOpt = schoolRepository.findBySchoolId(request.getSchoolId());
            if (schoolOpt.isEmpty()) {
                response.put("status", "FAILED");
                response.put("message", "School not found with ID: " + request.getSchoolId());
                return response;
            }

            School school = schoolOpt.get();
            String status = request.getStatus().toUpperCase();
            school.setSchoolStatus(status);

            if ("INACTIVE".equals(status)) {
                // Clear active school token
                school.setToken(null);
                // Force logout: remove all refresh sessions for this school's users (admins, teachers, students, parents)
                refreshTokenRepository.deleteBySchoolId(school.getSchoolId());
            }

            schoolRepository.save(school);

            response.put("status", "SUCCESS");
            response.put("message", "School status updated to " + school.getSchoolStatus() + " successfully.");
        } catch (Exception e) {
            response.put("status", "FAILED");
            response.put("message", "An error occurred: " + e.getMessage());
        }

        return response;
    }
}