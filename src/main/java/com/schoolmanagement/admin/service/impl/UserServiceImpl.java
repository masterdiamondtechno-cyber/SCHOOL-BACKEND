package com.schoolmanagement.admin.serviceImpl;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.schoolmanagement.Config.JwtUtil;
import com.schoolmanagement.admin.dto.AdminRegisterRequestDTO;
import com.schoolmanagement.admin.dto.AdminVerifyRequestDTO;
import com.schoolmanagement.admin.entity.User;
import com.schoolmanagement.owner.entity.Owner;
import com.schoolmanagement.admin.repository.UserRepository;
import com.schoolmanagement.owner.repository.OwnerRepository;
import com.schoolmanagement.admin.service.EmailService;
import com.schoolmanagement.admin.service.UserService;
import com.schoolmanagement.school.dto.LoginRequestDTO;
import com.schoolmanagement.school.repository.RoleRepository;

@Service
public class UserServiceImpl implements UserService {

    @Autowired private UserRepository userRepository;
    @Autowired private OwnerRepository ownerRepository;
    @Autowired private JwtUtil jwtUtil;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private EmailService emailService;
    @Autowired private RoleRepository roleRepository;

    // ── Helper ───────────────────────────────────────────────
    private Map<String, Object> buildClaims(User user) {
        Map<String, Object> claims = new LinkedHashMap<>();
        claims.put("uid",  user.getUserId());
        claims.put("role", "APP_ADMIN");
        return claims;
    }

    @Override
    public Map<String, Object> login(LoginRequestDTO request) {
        Map<String, Object> response = new HashMap<>();

        User user = userRepository
                .findByUsernameOrEmail(request.getUsername(), request.getUsername())
                .orElseThrow(() -> new RuntimeException("Invalid Credentials"));

        if (!"ACTIVE".equalsIgnoreCase(user.getStatus()))
            throw new RuntimeException("Admin account is Inactive");

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword()))
            throw new RuntimeException("Invalid Password");

        if (user.getRole() == null || !"ADMIN".equalsIgnoreCase(user.getRole().getName())) {
            throw new RuntimeException("User does not possess the correct role");
        }

        String token = jwtUtil.generateToken(user.getEmail(), buildClaims(user));

        user.setToken(token);
        user.setLoginAt(LocalDateTime.now());
        userRepository.save(user);

        Map<String, Object> userInfo = new LinkedHashMap<>();
        userInfo.put("id",    user.getUserId());
        userInfo.put("name",  user.getUsername());
        userInfo.put("email", user.getEmail());
        userInfo.put("phone", user.getPhone());

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("token",  token);
        data.put("user",   userInfo);
        data.put("role",   "superadmin");
        data.put("school", null);

        response.put("status",  "success");
        response.put("message", "Login successful");
        response.put("data", data);
        return response;
    }

    @Override
    public Map<String, Object> logout(String token, Long userId, String email) {
        Map<String, Object> response = new HashMap<>();

        User user = userRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid Token"));

        if (!user.getUserId().equals(userId) ||
            !user.getEmail().equals(email))
            throw new RuntimeException("Admin details do not match");

        user.setToken(null);
        user.setLogoutAt(LocalDateTime.now());
        userRepository.save(user);

        response.put("code", 200);
        response.put("status", "SUCCESS");
        response.put("message", "Admin Logged Out Successfully");
        response.put("data", null);
        return response;
    }

    @Override
    public Map<String, Object> registerInitiate(AdminRegisterRequestDTO request) {
        Map<String, Object> response = new HashMap<>();

        // Check if email or phone already exists in EITHER table
        if (userRepository.findByEmailOrPhone(request.getEmail(), request.getPhone()).isPresent() ||
            ownerRepository.findByEmailOrPhoneNo(request.getEmail(), request.getPhone()).isPresent()) {
            throw new RuntimeException("Email or Phone already registered");
        }

        String otp = String.format("%06d", new Random().nextInt(1000000));
        
        if (request.getRoleId() != null && request.getRoleId() == 6L) {
            // Register as Owner
            Owner owner = new Owner();
            owner.setFullName(request.getFullName());
            owner.setName(request.getFullName()); // Sync both name fields
            owner.setEmail(request.getEmail());
            owner.setPhoneNo(request.getPhone());
            owner.setUsername(request.getEmail());
            owner.setPassword("PENDING");
            owner.setStatus("PENDING_VERIFICATION");
            owner.setOtp(otp);
            owner.setOtpGeneratedAt(LocalDateTime.now());
            
            owner.setAadharCard(request.getAadharCard());
            owner.setPanCard(request.getPanCard());
            owner.setDob(request.getDob());
            owner.setGender(request.getGender());
            owner.setAddress(request.getAddress());
            owner.setFullNameAsPerAadhar(request.getFullNameAsPerAadhar());
            owner.setRegistrationType(request.getRegistrationType() != null ? request.getRegistrationType().toUpperCase() : "SINGLE");

            // Set role to OWNER (Id 6)
            roleRepository.findById(6L).ifPresent(owner::setRole);
            ownerRepository.save(owner);
        } else {
            // Register as Admin (User)
            User user = new User();
            user.setFullName(request.getFullName());
            user.setEmail(request.getEmail());
            user.setPhone(request.getPhone());
            user.setUsername(request.getEmail());
            user.setPassword("PENDING");
            user.setStatus("PENDING_VERIFICATION");
            user.setOtp(otp);
            user.setOtpGeneratedAt(LocalDateTime.now());
            
            user.setAadharCard(request.getAadharCard());
            user.setPanCard(request.getPanCard());
            user.setDob(request.getDob());
            user.setGender(request.getGender());
            user.setAddress(request.getAddress());
            user.setFullNameAsPerAadhar(request.getFullNameAsPerAadhar());

            // Default role ID from request or 1
            Long rid = request.getRoleId() != null ? request.getRoleId() : 1L;
            roleRepository.findById(rid).ifPresent(user::setRole);
            userRepository.save(user);
        }

        emailService.sendRegistrationOtpEmail(request.getEmail(), otp);

        response.put("status", "success");
        response.put("message", "OTP sent to " + request.getEmail());
        return response;
    }

    @Override
    public Map<String, Object> registerVerify(AdminVerifyRequestDTO request) {
        Map<String, Object> response = new HashMap<>();
        String rawPassword = UUID.randomUUID().toString().substring(0, 8);

        // Check Owner table first
        java.util.Optional<Owner> ownerOpt = ownerRepository.findByEmailOrPhoneNo(request.getUsername(), request.getUsername());
        if (ownerOpt.isPresent()) {
            Owner owner = ownerOpt.get();
            verifyOtp(owner.getOtp(), owner.getOtpGeneratedAt(), request.getOtp());
            owner.setOtp(null);

            if ("MULTIPLE".equalsIgnoreCase(owner.getRegistrationType())) {
                // MULTIPLE schools → needs admin approval
                owner.setStatus("PENDING_APPROVAL");
                ownerRepository.save(owner);

                Map<String, Object> pendingResponse = new HashMap<>();
                pendingResponse.put("status", "success");
                pendingResponse.put("message", "Registration verified. Your account is pending admin approval. You will receive login credentials via email once approved.");
                return pendingResponse;
            } else {
                // SINGLE school → activate immediately
                owner.setPassword(passwordEncoder.encode(rawPassword));
                owner.setStatus("ACTIVE");
                ownerRepository.save(owner);

                emailService.sendAdminPasswordEmail(owner.getEmail(), rawPassword);
                return buildVerifyResponse(owner.getOwnerId(), owner.getFullName(), owner.getEmail(), owner.getPhoneNo(), "owner");
            }
        }

        // Fallback to User (Admin) table
        User user = userRepository.findByEmailOrPhone(request.getUsername(), request.getUsername())
                .orElseThrow(() -> new RuntimeException("Registration not found"));

        verifyOtp(user.getOtp(), user.getOtpGeneratedAt(), request.getOtp());

        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setStatus("ACTIVE");
        user.setOtp(null);
        userRepository.save(user);
        
        emailService.sendAdminPasswordEmail(user.getEmail(), rawPassword);
        return buildVerifyResponse(user.getUserId(), user.getFullName(), user.getEmail(), user.getPhone(), "admin");
    }

    private void verifyOtp(String dbOtp, LocalDateTime generatedAt, String providedOtp) {
        if (dbOtp == null || !dbOtp.equals(providedOtp)) {
            throw new RuntimeException("Invalid OTP");
        }
        if (generatedAt.plusMinutes(5).isBefore(LocalDateTime.now())) {
            throw new RuntimeException("OTP expired");
        }
    }

    private Map<String, Object> buildVerifyResponse(Long id, String name, String email, String phone, String role) {
        Map<String, Object> userInfo = new LinkedHashMap<>();
        userInfo.put("id", id);
        userInfo.put("name", name);
        userInfo.put("email", email);
        userInfo.put("phone", phone);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("user", userInfo);
        data.put("role", role);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Registration verified, password sent to email. Please login to continue.");
        response.put("data", data);
        return response;
    }

    @Override
    public Map<String, Object> loginInitiate(String username, String password) {
        Map<String, Object> response = new HashMap<>();

        User user = userRepository.findByUsernameOrEmail(username, username)
                .orElseThrow(() -> new RuntimeException("Admin not found"));

        if (!"ACTIVE".equalsIgnoreCase(user.getStatus())) {
            throw new RuntimeException("Account is not active");
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        if (user.getRole() == null || !"ADMIN".equalsIgnoreCase(user.getRole().getName())) {
            throw new RuntimeException("User does not possess the correct role");
        }

        String otp = String.format("%06d", new Random().nextInt(1000000));
        user.setOtp(otp);
        user.setOtpGeneratedAt(LocalDateTime.now());
        userRepository.save(user);

        emailService.sendLoginOtpEmail(user.getEmail(), otp);

        Map<String, Object> data = new HashMap<>();
        data.put("otpRequired", true);

        response.put("status", "success");
        response.put("message", "OTP sent to your email");
        response.put("data", data);
        return response;
    }

    // ── OWNER REGISTRATION ──────────────────────────────────────

    @Override
    public Map<String, Object> registerOwnerInitiate(com.schoolmanagement.owner.dto.OwnerRegisterRequestDTO request) {
        Map<String, Object> response = new HashMap<>();

        // Check if email or phone already exists in EITHER table
        if (userRepository.findByEmailOrPhone(request.getEmail(), request.getPhone()).isPresent() ||
            ownerRepository.findByEmailOrPhoneNo(request.getEmail(), request.getPhone()).isPresent()) {
            throw new RuntimeException("Email or Phone already registered");
        }

        String otp = String.format("%06d", new Random().nextInt(1000000));

        Owner owner = new Owner();
        owner.setFullName(request.getFullName());
        owner.setName(request.getFullName());
        owner.setEmail(request.getEmail());
        owner.setPhoneNo(request.getPhone());
        owner.setUsername(request.getEmail());
        owner.setPassword("PENDING");
        owner.setStatus("PENDING_VERIFICATION");
        owner.setOtp(otp);
        owner.setOtpGeneratedAt(LocalDateTime.now());

        owner.setAadharCard(request.getAadharCard());
        owner.setPanCard(request.getPanCard());
        owner.setDob(request.getDob());
        owner.setGender(request.getGender());
        owner.setAddress(request.getAddress());
        owner.setFullNameAsPerAadhar(request.getFullNameAsPerAadhar());
        owner.setRegistrationType(request.getRegistrationType() != null ? request.getRegistrationType().toUpperCase() : "SINGLE");

        // Set role to OWNER (Id 6)
        roleRepository.findById(6L).ifPresent(owner::setRole);
        ownerRepository.save(owner);

        emailService.sendRegistrationOtpEmail(request.getEmail(), otp);

        response.put("status", "success");
        response.put("message", "OTP sent to " + request.getEmail());
        return response;
    }

    @Override
    public Map<String, Object> registerOwnerVerify(com.schoolmanagement.owner.dto.OwnerVerifyRequestDTO request) {

        Owner owner = ownerRepository.findByEmailOrPhoneNo(request.getUsername(), request.getUsername())
                .orElseThrow(() -> new RuntimeException("Registration not found"));

        verifyOtp(owner.getOtp(), owner.getOtpGeneratedAt(), request.getOtp());
        owner.setOtp(null);

        if ("MULTIPLE".equalsIgnoreCase(owner.getRegistrationType())) {
            // MULTIPLE schools → needs admin approval
            owner.setStatus("PENDING_APPROVAL");
            ownerRepository.save(owner);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Registration verified. Your account is pending admin approval. You will receive login credentials via email once approved.");
            return response;
        } else {
            // SINGLE school → activate immediately
            String rawPassword = UUID.randomUUID().toString().substring(0, 8);
            owner.setPassword(passwordEncoder.encode(rawPassword));
            owner.setStatus("ACTIVE");
            ownerRepository.save(owner);

            emailService.sendAdminPasswordEmail(owner.getEmail(), rawPassword);
            return buildVerifyResponse(owner.getOwnerId(), owner.getFullName(), owner.getEmail(), owner.getPhoneNo(), "owner");
        }
    }

}