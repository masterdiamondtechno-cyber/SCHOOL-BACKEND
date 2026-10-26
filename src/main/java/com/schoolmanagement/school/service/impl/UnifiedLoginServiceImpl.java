package com.schoolmanagement.school.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.schoolmanagement.Config.JwtUtil;
import com.schoolmanagement.admin.entity.User;
import com.schoolmanagement.admin.repository.UserRepository;
import com.schoolmanagement.admin.service.UserService;
import com.schoolmanagement.owner.entity.Owner;
import com.schoolmanagement.owner.repository.OwnerRepository;
import com.schoolmanagement.school.dto.LoginRequestDTO;
import com.schoolmanagement.school.dto.OwnerOtpVerifyRequestDTO;
import com.schoolmanagement.school.entity.AddStudent;
import com.schoolmanagement.school.entity.AddTeacher;
import com.schoolmanagement.school.entity.RefreshToken;
import com.schoolmanagement.school.entity.School;
import com.schoolmanagement.school.entity.AcademicYear;
import com.schoolmanagement.school.repository.AcademicYearRepository;
import com.schoolmanagement.school.repository.AddStudentRepository;
import com.schoolmanagement.school.repository.AddTeacherRepository;
import com.schoolmanagement.school.repository.SchoolRepository;
import com.schoolmanagement.school.repository.StudentAcademicYearRepository;
import com.schoolmanagement.school.entity.StudentAcademicYear;
import com.schoolmanagement.school.service.RefreshTokenService;
import com.schoolmanagement.school.service.UnifiedLoginService;
import com.schoolmanagement.admin.service.EmailService;

@Service
public class UnifiedLoginServiceImpl implements UnifiedLoginService {

    @Autowired
    private SchoolRepository schoolRepository;
    @Autowired
    private AddTeacherRepository teacherRepository;
    @Autowired
    private AddStudentRepository studentRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private RefreshTokenService refreshTokenService;
    @Autowired
    private UserService userService;
    @Autowired
    private OwnerRepository ownerRepository;
    @Autowired
    private AcademicYearRepository academicYearRepository;
    @Autowired
    private StudentAcademicYearRepository studentAcademicYearRepository;
    @Autowired
    private EmailService emailService;

    @Override
    public Map<String, Object> login(LoginRequestDTO request) {

        String username = request.getUsername();
        String rawPassword = request.getPassword();

        // 1. Try School
        Optional<School> schoolOpt = schoolRepository.findByEmailOrPhoneNo(username, username);
        if (schoolOpt.isPresent() && passwordEncoder.matches(rawPassword, schoolOpt.get().getPassword())) {
            School school = schoolOpt.get();
            if (!"ACTIVE".equalsIgnoreCase(school.getSchoolStatus()))
                throw new RuntimeException("School is Inactive");

            // Detect first-ever login: loginAt is null before this session
            boolean isFirstLogin = (school.getLoginAt() == null);

            String accessToken = jwtUtil.generateToken(school.getEmail(), buildSchoolClaims(school));
            RefreshToken refreshToken = refreshTokenService.createRefreshToken(
                    school.getSchoolId(), "SCHOOL", school.getEmail(), school.getSchoolId(), null);

            school.setToken(accessToken);
            school.setLoginAt(LocalDateTime.now());
            schoolRepository.save(school);

            return buildSchoolResponse(school, accessToken, refreshToken.getToken(), isFirstLogin);
        }

        // 2. Try Teacher
        Optional<AddTeacher> teacherOpt = teacherRepository.findByEmailIdOrMobileNumber(username, username);
        if (teacherOpt.isPresent() && passwordEncoder.matches(rawPassword, teacherOpt.get().getPassword())) {
            AddTeacher teacher = teacherOpt.get();
            if (!"Active".equalsIgnoreCase(teacher.getStatus()))
                throw new RuntimeException("Teacher is Inactive");
            if (teacher.getSchool() != null && !"ACTIVE".equalsIgnoreCase(teacher.getSchool().getSchoolStatus()))
                throw new RuntimeException("School is Inactive");

            String accessToken = jwtUtil.generateToken(teacher.getEmailId(), buildTeacherClaims(teacher));
            RefreshToken refreshToken = refreshTokenService.createRefreshToken(
                    teacher.getId(), "TEACHER", teacher.getEmailId(),
                    teacher.getSchool() != null ? teacher.getSchool().getSchoolId() : null,
                    teacher.getAcademicYear() != null ? teacher.getAcademicYear().getAcademicYearId() : null);

            teacher.setToken(accessToken);
            teacher.setLoginAt(LocalDateTime.now());
            teacherRepository.save(teacher);

            return buildTeacherResponse(teacher, accessToken, refreshToken.getToken());
        }

        // 3. Try Student (aadhar first, then email/mobile fallback)
        Optional<AddStudent> studentOpt = studentRepository.findByAadharNumber(username);
        if (studentOpt.isEmpty()) {
            studentOpt = studentRepository.findByEmailIdOrMobileNumber(username, username);
        }
        if (studentOpt.isPresent() && passwordEncoder.matches(rawPassword, studentOpt.get().getPassword())) {
            AddStudent student = studentOpt.get();
            if (!"Active".equalsIgnoreCase(student.getStatus()))
                throw new RuntimeException("Student is Inactive");
            if (student.getSchool() != null && !"ACTIVE".equalsIgnoreCase(student.getSchool().getSchoolStatus()))
                throw new RuntimeException("School is Inactive");

            String accessToken = jwtUtil.generateToken(student.getEmailId(), buildStudentClaims(student));
            RefreshToken refreshToken = refreshTokenService.createRefreshToken(
                    student.getId(), "STUDENT", student.getEmailId(),
                    student.getSchool() != null ? student.getSchool().getSchoolId() : null,
                    student.getAcademicYear() != null ? student.getAcademicYear().getAcademicYearId() : null);

            student.setToken(accessToken);
            student.setLoginAt(LocalDateTime.now());
            studentRepository.save(student);

            return buildStudentResponse(student, accessToken, refreshToken.getToken());
        }

        // 4. Try Parent
        List<AddStudent> parentStudents = studentRepository
                .findByParentEmailIdOrParentMobileNumber(username, username);
        if (!parentStudents.isEmpty()) {

            AddStudent validChild = parentStudents.stream()
                    .filter(s -> s.getParentPassword() != null)
                    .findFirst()
                    .orElse(null);

            if (validChild != null && passwordEncoder.matches(rawPassword, validChild.getParentPassword())) {
                if (validChild.getSchool() != null && !"ACTIVE".equalsIgnoreCase(validChild.getSchool().getSchoolStatus()))
                    throw new RuntimeException("School is Inactive");

                Map<String, Object> claims = new LinkedHashMap<>();
                claims.put("uid", validChild.getId());
                claims.put("sid", validChild.getSchool() != null ? validChild.getSchool().getSchoolId() : null);
                claims.put("role", "PARENT");

                String email = validChild.getParentEmailId() != null
                        ? validChild.getParentEmailId()
                        : validChild.getParentMobileNumber();
                String accessToken = jwtUtil.generateToken(email, claims);
                RefreshToken refreshToken = refreshTokenService.createRefreshToken(
                        validChild.getId(), "PARENT", email, null, null);

                // Set token + loginAt on all children
                for (AddStudent child : parentStudents) {
                    child.setParentToken(accessToken);
                    child.setParentLoginAt(LocalDateTime.now());
                    studentRepository.save(child);
                }

                return buildParentResponse(parentStudents, accessToken, refreshToken.getToken());
            }
        }

        // 5. Owner (Franchise Manager)
        Optional<Owner> ownerOpt = ownerRepository.findByEmailOrPhoneNo(username, username);
        if (ownerOpt.isPresent() && passwordEncoder.matches(rawPassword, ownerOpt.get().getPassword())) {
            Owner owner = ownerOpt.get();
            if (!"ACTIVE".equalsIgnoreCase(owner.getStatus()))
                throw new RuntimeException("Owner account is Inactive");

            String otp = String.format("%06d", new java.util.Random().nextInt(999999));
            owner.setOtp(otp);
            owner.setOtpGeneratedAt(LocalDateTime.now());
            ownerRepository.save(owner);

            // Send OTP email to owner
            emailService.sendLoginOtpEmail(owner.getEmail(), otp);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "OTP sent successfully, please complete verification at /api/auth/otpverify");
            
            Map<String, Object> data = new HashMap<>();
            data.put("status", "otp_required");
            data.put("otpRequired", true);
            response.put("data", data);
            
            return response;
        }

        // 6. Admin Login (Direct Login - No OTP)
        Optional<User> adminOpt = userRepository.findByUsernameOrEmail(username, username);
        if (adminOpt.isPresent()) {
            User user = adminOpt.get();

            // Validate active status
            if (!"ACTIVE".equalsIgnoreCase(user.getStatus())) {
                throw new RuntimeException("Admin account is Inactive");
            }

            // Validate password matches BCrypt hash
            if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
                throw new RuntimeException("Invalid Password");
            }

            // Validate correct role
            if (user.getRole() == null || !"ADMIN".equalsIgnoreCase(user.getRole().getName())) {
                throw new RuntimeException("User does not possess the correct role");
            }

            // Generate token directly (no OTP required)
            Map<String, Object> claims = new LinkedHashMap<>();
            claims.put("uid", user.getUserId());
            claims.put("role", "APP_ADMIN");

            String token = jwtUtil.generateToken(user.getEmail(), claims);
            user.setToken(token);
            user.setLoginAt(LocalDateTime.now());
            userRepository.save(user);

            Map<String, Object> userInfo = new LinkedHashMap<>();
            userInfo.put("id", user.getUserId());
            userInfo.put("name", user.getFullName());
            userInfo.put("email", user.getEmail());
            userInfo.put("phone", user.getPhone());

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("token", token);
            data.put("user", userInfo);
            data.put("role", "superadmin");

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Login successful");
            response.put("data", data);
            return response;
        }

        throw new RuntimeException("Invalid Credentials");
    }

    @Override
    public Map<String, Object> logout(String token) {
        try {
            Long uid = Long.valueOf(jwtUtil.extractClaim(token, "uid").toString());
            String role = jwtUtil.extractRole(token).toUpperCase();

            // Clear all refresh sessions globally for this user (logs out all devices
            // securely)
            refreshTokenService.deleteAllTokensForUser(uid, role);

            // Safely clear the specific user's login timestamp and token without
            // multi-device overlap issues
            switch (role) {
                case "SCHOOL":
                    schoolRepository.findById(uid).ifPresent(s -> {
                        s.setToken(null);
                        s.setLogoutAt(LocalDateTime.now());
                        schoolRepository.save(s);
                    });
                    break;
                case "TEACHER":
                    teacherRepository.findById(uid).ifPresent(t -> {
                        t.setToken(null);
                        t.setLogoutAt(LocalDateTime.now());
                        teacherRepository.save(t);
                    });
                    break;
                case "STUDENT":
                    studentRepository.findById(uid).ifPresent(st -> {
                        st.setToken(null);
                        st.setLogoutAt(LocalDateTime.now());
                        studentRepository.save(st);
                    });
                    break;
                case "PARENT":
                    List<AddStudent> parentKids = studentRepository
                            .findByParentEmailIdOrParentMobileNumber(
                                    jwtUtil.extractUsername(token), jwtUtil.extractUsername(token));
                    for (AddStudent kid : parentKids) {
                        kid.setParentToken(null);
                        kid.setParentLogoutAt(LocalDateTime.now());
                        studentRepository.save(kid);
                    }
                    break;
                case "APP_ADMIN":
                    userRepository.findById(uid).ifPresent(a -> {
                        a.setToken(null);
                        a.setLogoutAt(LocalDateTime.now());
                        userRepository.save(a);
                    });
                    break;
                case "OWNER":
                    ownerRepository.findById(uid).ifPresent(o -> {
                        o.setToken(null);
                        o.setLogoutAt(LocalDateTime.now());
                        ownerRepository.save(o);
                    });
                    break;
            }

            return buildLogoutResponse();

        } catch (Exception e) {
            throw new RuntimeException("Invalid Token or Token Expired");
        }
    }

    @Override
    public Map<String, Object> verifyOwnerOtp(OwnerOtpVerifyRequestDTO request) {
        String username = request.getUsername();
        String otp = request.getOtp();

        Optional<Owner> ownerOpt = ownerRepository.findByEmailOrPhoneNo(username, username);
        if (ownerOpt.isEmpty()) {
            throw new RuntimeException("Invalid Username");
        }

        Owner owner = ownerOpt.get();
        if (owner.getOtp() == null || !owner.getOtp().equals(otp)) {
            throw new RuntimeException("Invalid OTP");
        }
        if (owner.getOtpGeneratedAt().plusMinutes(5).isBefore(LocalDateTime.now())) {
            throw new RuntimeException("OTP expired");
        }

        owner.setOtp(null);

        Map<String, Object> claims = new LinkedHashMap<>();
        claims.put("uid", owner.getOwnerId());
        claims.put("role", "OWNER");

        String accessToken = jwtUtil.generateToken(owner.getEmail(), claims);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(
                owner.getOwnerId(), "OWNER", owner.getEmail(), null, null);

        owner.setToken(accessToken);
        owner.setLoginAt(LocalDateTime.now());
        ownerRepository.save(owner);

        return buildOwnerResponse(owner, accessToken, refreshToken.getToken());
    }

    // ── Claim builders ───────────────────────────────────────

    private Map<String, Object> buildSchoolClaims(School school) {
        Map<String, Object> claims = new LinkedHashMap<>();
        claims.put("uid", school.getSchoolId());
        claims.put("sid", school.getSchoolId());
        claims.put("role", school.getRole() != null ? school.getRole().getName().toUpperCase() : "SCHOOL");
        return claims;
    }

    private Map<String, Object> buildTeacherClaims(AddTeacher teacher) {
        Map<String, Object> claims = new LinkedHashMap<>();
        claims.put("uid", teacher.getId());
        claims.put("sid", teacher.getSchool().getSchoolId());
        claims.put("role", teacher.getRole() != null ? teacher.getRole().getName().toUpperCase() : "TEACHER");
        if (teacher.getAcademicYear() != null) {
            claims.put("ayi", teacher.getAcademicYear().getAcademicYearId());
        }
        return claims;
    }

    private Map<String, Object> buildStudentClaims(AddStudent student) {
        Map<String, Object> claims = new LinkedHashMap<>();
        claims.put("uid", student.getId());
        claims.put("sid", student.getSchool().getSchoolId());
        claims.put("role", student.getRole() != null ? student.getRole().getName().toUpperCase() : "STUDENT");
        if (student.getAcademicYear() != null) {
            claims.put("ayi", student.getAcademicYear().getAcademicYearId());
        }
        return claims;
    }

    // ── Response builders ────────────────────────────────────

    private Map<String, Object> buildSchoolResponse(School school, String accessToken, String refreshToken, boolean isFirstLogin) {
        Map<String, Object> user = new LinkedHashMap<>();
        user.put("id", school.getSchoolId());
        user.put("name", school.getSchoolName());
        user.put("email", school.getEmail());
        user.put("phone", school.getPhoneNo());

        Map<String, Object> schoolInfo = new LinkedHashMap<>();
        schoolInfo.put("id", school.getSchoolId());
        schoolInfo.put("name", school.getSchoolName());
        schoolInfo.put("subscriptionStatus", school.getSubscriptionStatus());
        schoolInfo.put("isSubscriptionExpired", checkSubscriptionExpired(school));

        // Resolve current academic year for the school
        Long academicYearId = null;
        Optional<AcademicYear> currentYear = academicYearRepository
                .findBySchool_SchoolIdAndIsCurrentTrue(school.getSchoolId());
        if (currentYear.isPresent()) {
            academicYearId = currentYear.get().getAcademicYearId();
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("token", accessToken);
        data.put("refreshToken", refreshToken);
        data.put("user", user);
        data.put("role", "school");
        data.put("school", schoolInfo);
        data.put("academicYearId", academicYearId);
        // First-login flag: true when loginAt was null before this session
        data.put("isFirstLogin", isFirstLogin);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", "success");
        response.put("message", "Login successful");
        response.put("data", data);
        return response;
    }

    private Map<String, Object> buildTeacherResponse(AddTeacher teacher, String accessToken, String refreshToken) {
        Map<String, Object> user = new LinkedHashMap<>();
        user.put("id", teacher.getId());
        user.put("name", teacher.getTeacherName());
        user.put("email", teacher.getEmailId());
        user.put("phone", teacher.getMobileNumber());

        Map<String, Object> schoolInfo = new LinkedHashMap<>();
        schoolInfo.put("id", teacher.getSchool().getSchoolId());
        schoolInfo.put("name", teacher.getSchool().getSchoolName());
        schoolInfo.put("subscriptionStatus", teacher.getSchool().getSubscriptionStatus());
        schoolInfo.put("isSubscriptionExpired", checkSubscriptionExpired(teacher.getSchool()));

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("token", accessToken);
        data.put("refreshToken", refreshToken);
        data.put("user", user);
        data.put("role", "teacher");
        data.put("school", schoolInfo);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", "success");
        response.put("message", "Login successful");
        response.put("data", data);
        return response;
    }

    private Map<String, Object> buildStudentResponse(AddStudent student, String accessToken, String refreshToken) {

        // If password change is required, return only minimal data (no token/user info)
        if (student.isPasswordChangeRequired()) {
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("token", accessToken);
            data.put("refreshToken", refreshToken);
            data.put("studentId", student.getId());
            data.put("schoolId", student.getSchool().getSchoolId());
            data.put("passwordChangeRequired", true);

            Map<String, Object> response = new LinkedHashMap<>();
            response.put("status", "success");
            response.put("message", "Password change required");
            response.put("data", data);
            return response;
        }

        // Full response when password has already been changed
        Map<String, Object> user = new LinkedHashMap<>();
        user.put("id", student.getId());
        user.put("name", (student.getFirstName() != null ? student.getFirstName() : "") + " "
                + (student.getLastName() != null ? student.getLastName() : ""));
        user.put("email", student.getEmailId());
        user.put("phone", student.getMobileNumber());

        Map<String, Object> schoolInfo = new LinkedHashMap<>();
        schoolInfo.put("id", student.getSchool().getSchoolId());
        schoolInfo.put("name", student.getSchool().getSchoolName());
        schoolInfo.put("subscriptionStatus", student.getSchool().getSubscriptionStatus());
        schoolInfo.put("isSubscriptionExpired", checkSubscriptionExpired(student.getSchool()));

        // Resolve active year mapping to get current class/section context
        Optional<AcademicYear> activeYearOpt = academicYearRepository.findBySchool_SchoolIdAndIsCurrentTrue(student.getSchool().getSchoolId());
        Long currentAcademicYearId = activeYearOpt.map(AcademicYear::getAcademicYearId).orElse(null);
        
        Long standardId = null;
        Long divisionId = null;
        if (currentAcademicYearId != null) {
            Optional<StudentAcademicYear> activeMapping = studentAcademicYearRepository
                    .findByStudent_IdAndAcademicYear_AcademicYearId(student.getId(), currentAcademicYearId);
            if (activeMapping.isPresent()) {
                standardId = activeMapping.get().getStandard() != null ? activeMapping.get().getStandard().getStandardId() : null;
                divisionId = activeMapping.get().getDivision() != null ? activeMapping.get().getDivision().getDivisionId() : null;
            }
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("token", accessToken);
        data.put("refreshToken", refreshToken);
        data.put("user", user);
        data.put("role", "student");
        data.put("school", schoolInfo);
        data.put("academicYearId", currentAcademicYearId);
        data.put("standardId", standardId);
        data.put("divisionId", divisionId);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", "success");
        response.put("message", "Login successful");
        response.put("data", data);
        return response;
    }

    private Map<String, Object> buildParentResponse(List<AddStudent> children, String accessToken,
            String refreshToken) {
        AddStudent first = children.get(0);

        Map<String, Object> user = new LinkedHashMap<>();
        user.put("name", first.getGuardianName());
        user.put("email", first.getParentEmailId());
        user.put("phone", first.getParentMobileNumber());

        List<Map<String, Object>> childrenList = new ArrayList<>();
        // Resolve current active year first
        Optional<AcademicYear> activeYearOpt = academicYearRepository.findBySchool_SchoolIdAndIsCurrentTrue(first.getSchool().getSchoolId());
        Long currentAcademicYearId = activeYearOpt.map(AcademicYear::getAcademicYearId).orElse(null);
        String currentAcademicYearName = activeYearOpt.map(AcademicYear::getYearName).orElse(null);

        for (AddStudent child : children) {
            Map<String, Object> c = new LinkedHashMap<>();
            c.put("id", child.getId());
            c.put("name", (child.getFirstName() != null ? child.getFirstName() : "") + " "
                    + (child.getLastName() != null ? child.getLastName() : ""));
            c.put("studentId", child.getStudentId());
            c.put("schoolId", child.getSchool() != null ? child.getSchool().getSchoolId() : null);
            c.put("schoolName", child.getSchool() != null ? child.getSchool().getSchoolName() : null);
            c.put("subscriptionStatus", child.getSchool() != null ? child.getSchool().getSubscriptionStatus() : null);
            c.put("isSubscriptionExpired", child.getSchool() != null ? checkSubscriptionExpired(child.getSchool()) : false);
            
            // Query standard & division from mapping
            String standardName = null;
            String divisionName = null;
            if (currentAcademicYearId != null) {
                Optional<StudentAcademicYear> activeMapping = studentAcademicYearRepository
                        .findByStudent_IdAndAcademicYear_AcademicYearId(child.getId(), currentAcademicYearId);
                if (activeMapping.isPresent()) {
                    standardName = activeMapping.get().getStandard() != null ? activeMapping.get().getStandard().getStandard() : null;
                    divisionName = activeMapping.get().getDivision() != null ? activeMapping.get().getDivision().getDivisionName() : null;
                }
            }
            
            c.put("standard", standardName);
            c.put("division", divisionName);
            c.put("academicYearId", currentAcademicYearId);
            c.put("academicYearName", currentAcademicYearName);
            childrenList.add(c);
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("token", accessToken);
        data.put("refreshToken", refreshToken);
        data.put("user", user);
        data.put("role", "parent");
        data.put("children", childrenList);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", "success");
        response.put("message", "Login successful");
        response.put("data", data);
        return response;
    }

    private Map<String, Object> buildLogoutResponse() {
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("status", "SUCCESS");
        response.put("message", "Logged Out Successfully");
        response.put("data", null);
        return response;
    }

    // ── Owner response builder ───────────────────────────────

    private Map<String, Object> buildOwnerResponse(Owner owner, String accessToken, String refreshToken) {
        Map<String, Object> user = new LinkedHashMap<>();
        user.put("id", owner.getOwnerId());
        user.put("name", owner.getName());
        user.put("email", owner.getEmail());
        user.put("phone", owner.getPhoneNo());

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("token", accessToken);
        data.put("refreshToken", refreshToken);
        data.put("user", user);
        data.put("role", "owner");

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", "success");
        response.put("message", "Login successful");
        response.put("data", data);
        return response;
    }

    private boolean checkSubscriptionExpired(School school) {
        if (school == null) return false;
        return school.isSubscriptionExpired();
    }
}
