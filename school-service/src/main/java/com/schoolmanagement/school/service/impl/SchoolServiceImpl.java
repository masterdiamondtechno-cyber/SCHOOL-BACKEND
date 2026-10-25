package com.schoolmanagement.school.serviceImpl;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Value;

import com.schoolmanagement.school.service.LocalFileStorageService;

import com.schoolmanagement.owner.entity.Owner;
import com.schoolmanagement.owner.repository.OwnerRepository;
import com.schoolmanagement.Config.JwtUtil;
import com.schoolmanagement.admin.entity.SchoolRegistrationApproval;
import com.schoolmanagement.admin.repository.SchoolRegistrationApprovalRepository;
import com.schoolmanagement.school.dto.LoginRequestDTO;
import com.schoolmanagement.school.dto.SchoolDTO;
import com.schoolmanagement.school.dto.SendOtpRequestDTO;
import com.schoolmanagement.school.dto.UpdatePasswordRequestDTO;
import com.schoolmanagement.school.dto.VerifyOtpRequestDTO;
import com.schoolmanagement.school.entity.Role;
import com.schoolmanagement.school.entity.School;
import com.schoolmanagement.school.repository.AcademicYearRepository;
import com.schoolmanagement.school.repository.AddDivisionRepository;
import com.schoolmanagement.school.repository.AddStandardRepository;
import com.schoolmanagement.school.repository.AddStudentRepository;
import com.schoolmanagement.school.repository.AddSubjectRepository;
import com.schoolmanagement.school.repository.AddTeacherRepository;
import com.schoolmanagement.school.repository.AttendanceRepository;
import com.schoolmanagement.school.repository.ExamMarksRepository;
import com.schoolmanagement.school.repository.ExamRepository;
import com.schoolmanagement.school.repository.FeePaymentRepository;
import com.schoolmanagement.school.repository.FeeStructureRepository;
import com.schoolmanagement.school.repository.NotificationRepository;
import com.schoolmanagement.school.repository.RoleRepository;
import com.schoolmanagement.school.repository.SchoolRepository;
import com.schoolmanagement.school.repository.TimetableRepository;
import com.schoolmanagement.school.service.SchoolService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SchoolServiceImpl implements SchoolService {

    @Autowired private JavaMailSender mailSender;
    @Autowired private SchoolRepository repository;
    @Autowired private SchoolRegistrationApprovalRepository approvalRepository;
    @Autowired private JwtUtil jwtUtil;
    @Autowired private PasswordEncoder passwordEncoder;

    @Autowired private LocalFileStorageService fileStorageService;
    @Autowired private AddStandardRepository standardRepository;
    @Autowired private AddDivisionRepository divisionRepository;
    @Autowired private AddSubjectRepository subjectRepository;
    @Autowired private AddStudentRepository studentRepository;
    @Autowired private AddTeacherRepository teacherRepository;
    @Autowired private AcademicYearRepository academicYearRepository;
    @Autowired private TimetableRepository timetableRepository;
    @Autowired private AttendanceRepository attendanceRepository;
    @Autowired private ExamRepository examRepository;
    @Autowired private ExamMarksRepository examMarksRepository;
    @Autowired private FeeStructureRepository feeStructureRepository;
    @Autowired private FeePaymentRepository feePaymentRepository;
    @Autowired private NotificationRepository notificationRepository;
    @Autowired private RoleRepository roleRepository;
    @Autowired private OwnerRepository ownerRepository;
    @Autowired private com.schoolmanagement.subscription.service.SubscriptionService subscriptionService;

    @Override
    @Transactional
    public School createSchool(SchoolDTO dto, MultipartFile logoFile) {
        if (repository.existsByEmail(dto.getEmail()))
            throw new RuntimeException("Email already exists");
        if (repository.existsByPhoneNo(dto.getPhoneNo()))
            throw new RuntimeException("Phone number already exists");

        School school = new School();
        mapDtoToEntity(dto, school);

        // Auto-set roleId to 2 (School role)
        Role role = roleRepository.findById(2L)
                .orElseThrow(() -> new RuntimeException("Default School role (id=2) not found"));
        school.setRole(role);

        // Generate temporary password
        String tempPassword = "School@" + (int)(Math.random() * 90000 + 10000);
        school.setPassword(passwordEncoder.encode(tempPassword));

        School savedSchool = repository.save(school);

        // Upload Logo to Local Storage
        if (logoFile != null && !logoFile.isEmpty()) {
            String logoUrl = fileStorageService.storeFile(logoFile, LocalFileStorageService.MODULE_LOGOS, "school_" + savedSchool.getSchoolId());
            savedSchool.setS3Key(logoUrl);
            savedSchool.setSchoolLogo(logoUrl);
            savedSchool = repository.save(savedSchool);
        }

        SchoolRegistrationApproval approval = new SchoolRegistrationApproval();
        approval.setSchool(savedSchool);
        approval.setSchoolName(savedSchool.getSchoolName());
        approval.setUdiseNo(savedSchool.getUdiseNo());
        approval.setEmail(savedSchool.getEmail());
        approval.setPhone(savedSchool.getPhoneNo());
        approvalRepository.save(approval);

        // Auto-assign 14-day free trial for SaaS
        try {
            subscriptionService.startFreeTrial(savedSchool.getSchoolId());
        } catch (Exception e) {
            System.err.println("Could not auto-start free trial: " + e.getMessage());
        }

        // Send Email notifying registration is under review
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(savedSchool.getEmail());
            message.setFrom("your-email@gmail.com"); // Can be configured via properties
            message.setSubject("School Registration Under Review");
            message.setText("Dear " + savedSchool.getSchoolName() + ",\n\n"
                    + "Your school registration has been submitted successfully and is currently under review.\n"
                    + "Once our administrator approves your account, you will receive a confirmation email containing your official login credentials.\n\n"
                    + "Registered Email: " + savedSchool.getEmail() + "\n\n"
                    + "Thank you for your patience.");
            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("Failed to send email: " + e.getMessage());
        }

        return savedSchool;
    }

    @Override
    public List<School> getAllSchools() {
        return repository.findAll();
    }

    @Override
    public School getBySchoolId(Long schoolId) {
        return repository.findById(schoolId)
                .orElseThrow(() -> new RuntimeException("School not found"));
    }

    @Override
    public School updateSchool(Long schoolId, SchoolDTO dto, MultipartFile logoFile) {
        School school = repository.findById(schoolId)
                .orElseThrow(() -> new RuntimeException("School not found"));
        mapDtoToEntity(dto, school);

        if (logoFile != null && !logoFile.isEmpty()) {
            // Delete old logo from local storage before uploading new one
            fileStorageService.deleteFile(school.getS3Key());
            String logoUrl = fileStorageService.storeFile(logoFile, LocalFileStorageService.MODULE_LOGOS, "school_" + school.getSchoolId());
            school.setS3Key(logoUrl);
            school.setSchoolLogo(logoUrl);
        }

        return repository.save(school);
    }

    @Override
    @Transactional
    public void resendRegistrationEmail(Long schoolId) {
        School school = repository.findById(schoolId)
                .orElseThrow(() -> new RuntimeException("School not found"));

        // Generate temporary password
        String tempPassword = "School@" + (int)(Math.random() * 90000 + 10000);
        school.setPassword(passwordEncoder.encode(tempPassword));
        repository.save(school);

        // Send Email notifying registration is under review
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(school.getEmail());
            message.setFrom("your-email@gmail.com"); // Can be configured via properties
            message.setSubject("School Registration Under Review");
            message.setText("Dear " + school.getSchoolName() + ",\n\n"
                    + "Your school registration is currently under review by our administration team.\n"
                    + "Once approved, you will receive a confirmation email containing your official login credentials.\n\n"
                    + "Registered Email: " + school.getEmail() + "\n\n"
                    + "Thank you for your patience.");
            mailSender.send(message);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send email: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public void deleteSchool(Long schoolId) {
        School school = repository.findById(schoolId)
                .orElseThrow(() -> new RuntimeException("School not found"));

        examMarksRepository.deleteBySchool_SchoolId(schoolId);
        feePaymentRepository.deleteBySchool_SchoolId(schoolId);
        attendanceRepository.deleteBySchool_SchoolId(schoolId);
        timetableRepository.deleteBySchool_SchoolId(schoolId);
        examRepository.deleteBySchool_SchoolId(schoolId);
        feeStructureRepository.deleteBySchool_SchoolId(schoolId);
        notificationRepository.deleteBySchool_SchoolId(schoolId);
        academicYearRepository.deleteBySchool_SchoolId(schoolId);
        studentRepository.deleteBySchool_SchoolId(schoolId);
        teacherRepository.deleteBySchool_SchoolId(schoolId);
        subjectRepository.deleteBySchool_SchoolId(schoolId);
        divisionRepository.deleteBySchool_SchoolId(schoolId);
        standardRepository.deleteBySchool_SchoolId(schoolId);
        approvalRepository.deleteBySchool_SchoolId(schoolId);
        repository.delete(school);
    }

    private void mapDtoToEntity(SchoolDTO dto, School school) {
        school.setSchoolName(dto.getSchoolName());
        school.setUdiseNo(dto.getUdiseNo());
        school.setState(dto.getState());
        school.setDistrict(dto.getDistrict());
        school.setTaluka(dto.getTaluka());
        school.setPincode(dto.getPincode());
        school.setSchoolAddress(dto.getSchoolAddress());
        // schoolLogo is now a MultipartFile — handled separately via S3 upload
        school.setEmail(dto.getEmail());
        school.setPhoneNo(dto.getPhoneNo());
        school.setPrincipalName(dto.getPrincipalName());
        if (dto.getRoleId() != null) {
            Role role = roleRepository.findById(dto.getRoleId())
                    .orElseThrow(() -> new RuntimeException("Role not found"));
            school.setRole(role);
        }
        
        if (dto.getOwnerId() != null) {
            Owner owner = ownerRepository.findById(dto.getOwnerId())
                    .orElseThrow(() -> new RuntimeException("Owner not found"));
            school.setOwner(owner);
        }
        
        if (school.getSchoolId() == null) {
            if (dto.getSubscriptionStatus() == null || "ACTIVE".equalsIgnoreCase(dto.getSubscriptionStatus()) || "PENDING".equalsIgnoreCase(dto.getSubscriptionStatus())) {
                school.setSubscriptionStatus("Standard Plan (Monthly)");
                school.setTrialEndDate(LocalDateTime.now().plusDays(7));
                school.setSubscriptionSource("TRIAL");
            } else {
                school.setSubscriptionStatus(dto.getSubscriptionStatus());
                school.setTrialEndDate(null);
                school.setSubscriptionSource("PURCHASED");
                school.setSubscriptionStartDate(LocalDateTime.now());
                school.setSubscriptionEndDate(LocalDateTime.now().plusDays(30));
            }
        } else {
            if (dto.getSubscriptionStatus() != null && !dto.getSubscriptionStatus().equals(school.getSubscriptionStatus())) {
                school.setSubscriptionStatus(dto.getSubscriptionStatus());
                school.setTrialEndDate(null);
                school.setSubscriptionSource("PURCHASED");
                school.setSubscriptionStartDate(LocalDateTime.now());
                school.setSubscriptionEndDate(LocalDateTime.now().plusDays(30));
            }
        }
        school.setSchoolStatus(dto.getSchoolStatus());

    }



    // ── Helper ───────────────────────────────────────────────
    private Map<String, Object> buildClaims(School school) {
        Map<String, Object> claims = new LinkedHashMap<>();
        claims.put("uid",  school.getSchoolId());
        claims.put("sid",  school.getSchoolId());
        claims.put("role", school.getRole() != null ? school.getRole().getName() : "SCHOOL");
        return claims;
    }

    @Override
    public Map<String, Object> login(LoginRequestDTO request) {
        Map<String, Object> response = new HashMap<>();

        School school = repository
                .findByEmailOrPhoneNo(request.getUsername(), request.getUsername())
                .orElseThrow(() -> new RuntimeException("Invalid Credentials"));

        if (!"ACTIVE".equalsIgnoreCase(school.getSchoolStatus()))
            throw new RuntimeException("School is Inactive");

        if (!passwordEncoder.matches(request.getPassword(), school.getPassword()))
            throw new RuntimeException("Invalid Password");

        String token = jwtUtil.generateToken(school.getEmail(), buildClaims(school));

        school.setToken(token);
        school.setLoginAt(LocalDateTime.now());
        repository.save(school);

        response.put("code", 200);
        response.put("status", "SUCCESS");
        response.put("message", "Logged In");
        response.put("data", token);
        return response;
    }

    @Override
    public Map<String, Object> logout(String token, Long schoolId, String phoneNo, String email) {
        Map<String, Object> response = new HashMap<>();

        School school = repository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid Token"));

        if (!school.getSchoolId().equals(schoolId) ||
            !school.getPhoneNo().equals(phoneNo) ||
            !school.getEmail().equals(email))
            throw new RuntimeException("School details do not match");

        school.setToken(null);
        school.setLogoutAt(LocalDateTime.now());
        repository.save(school);

        response.put("code", 200);
        response.put("status", "SUCCESS");
        response.put("message", "Logged Out Successfully");
        response.put("data", null);
        return response;
    }

    @Override
    public String sendOtp(SendOtpRequestDTO request) {
        String emailOrPhone = request.getEmailOrPhone();
        Optional<School> userOpt = emailOrPhone.contains("@")
                ? repository.findByEmail(emailOrPhone)
                : repository.findByPhoneNo(emailOrPhone);

        if (userOpt.isEmpty()) throw new RuntimeException("User not found");

        School user = userOpt.get();
        String otp = String.valueOf((int)(Math.random() * 900000 + 100000));
        user.setOtp(otp);
        user.setOtpVerify("1");
        user.setOtpGeneratedAt(LocalDateTime.now());
        repository.save(user);

        if (user.getEmail() != null) {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(user.getEmail());
            message.setSubject("Your OTP Code");
            message.setText("Your OTP is: " + otp);
            mailSender.send(message);
        }
        return "OTP sent successfully";
    }

    @Override
    public String verifyOtp(VerifyOtpRequestDTO request) {
        String emailOrPhone = request.getEmailOrPhone();
        Optional<School> userOpt = emailOrPhone.contains("@")
                ? repository.findByEmail(emailOrPhone)
                : repository.findByPhoneNo(emailOrPhone);

        if (userOpt.isEmpty()) throw new RuntimeException("User not found");

        School user = userOpt.get();
        if (!"1".equals(user.getOtpVerify()) || user.getOtp() == null || !user.getOtp().equals(request.getOtp()))
            throw new RuntimeException("Invalid OTP");

        user.setOtp(null);
        user.setOtpVerify("null");
        repository.save(user);
        return "OTP verified successfully";
    }

    @Override
    public String updatePassword(UpdatePasswordRequestDTO request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword()))
            throw new RuntimeException("Passwords do not match");

        String emailOrPhone = request.getEmailOrPhone();
        Optional<School> userOpt = emailOrPhone.contains("@")
                ? repository.findByEmail(emailOrPhone)
                : repository.findByPhoneNo(emailOrPhone);

        if (userOpt.isEmpty()) throw new RuntimeException("User not found");

        School user = userOpt.get();
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        repository.save(user);

        if (user.getEmail() != null) {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(user.getEmail());
            message.setFrom("your-email@gmail.com");
            message.setSubject("Password Updated Successfully");
            message.setText("Your password has been updated successfully.\n\nNew Password: " + request.getNewPassword());
            mailSender.send(message);
        }
        return "Password updated successfully and sent to email";
    }
}