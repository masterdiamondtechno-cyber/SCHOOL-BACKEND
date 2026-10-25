package com.schoolmanagement.school.serviceImpl;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.schoolmanagement.school.dto.AddStudentDTO;
import com.schoolmanagement.school.dto.ChangePasswordDTO;
import com.schoolmanagement.school.dto.ResetPasswordDTO;
import com.schoolmanagement.school.dto.ResetPasswordsBulkDTO;
import com.schoolmanagement.school.dto.UpdatePasswordDTO;
import com.schoolmanagement.school.entity.AcademicYear;
import com.schoolmanagement.school.entity.AddDivision;
import com.schoolmanagement.school.entity.AddStandard;
import com.schoolmanagement.school.entity.AddStudent;
import com.schoolmanagement.school.entity.Role;
import com.schoolmanagement.school.entity.School;
import com.schoolmanagement.school.entity.StudentAcademicYear;
import com.schoolmanagement.school.repository.AcademicYearRepository;
import com.schoolmanagement.school.repository.AddDivisionRepository;
import com.schoolmanagement.school.repository.AddStandardRepository;
import com.schoolmanagement.school.repository.AddStudentRepository;
import com.schoolmanagement.school.repository.RoleRepository;
import com.schoolmanagement.school.repository.SchoolRepository;
import com.schoolmanagement.school.repository.StudentAcademicYearRepository;
import com.schoolmanagement.school.service.AddStudentService;
import com.schoolmanagement.school.service.EmailService;


@Service
public class AddStudentServiceImpl implements AddStudentService {

    private final AddStudentRepository studentRepository;
    private final SchoolRepository schoolRepository;
    private final AcademicYearRepository academicYearRepository;
    private final AddStandardRepository standardRepository;
    private final AddDivisionRepository divisionRepository;
    private final RoleRepository roleRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final StudentAcademicYearRepository mappingRepository;

    public AddStudentServiceImpl(AddStudentRepository studentRepository,
                                 SchoolRepository schoolRepository,
                                 AcademicYearRepository academicYearRepository,
                                 AddStandardRepository standardRepository,
                                 AddDivisionRepository divisionRepository,
                                 RoleRepository roleRepository,
                                 BCryptPasswordEncoder passwordEncoder,
                                 EmailService emailService,
                                 StudentAcademicYearRepository mappingRepository) {
        this.studentRepository = studentRepository;
        this.schoolRepository = schoolRepository;
        this.academicYearRepository = academicYearRepository;
        this.standardRepository = standardRepository;
        this.divisionRepository = divisionRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.mappingRepository = mappingRepository;
    }

    // ==================== ENTITY → DTO CONVERTER ====================
    private AddStudentDTO toDTO(AddStudent s) {
        AddStudentDTO dto = new AddStudentDTO();
        dto.setId(s.getId());
        dto.setSchoolId(s.getSchool() != null ? s.getSchool().getSchoolId() : null);

        // Default standard/division/academicYear/rollNumber from student entity
        AddStandard standardEntity = s.getAddStandard();
        AddDivision divisionEntity = s.getAddDivision();
        AcademicYear academicYearEntity = s.getAcademicYear();
        String rollNo = s.getRollNumber();

        // Resolve mappings in StudentAcademicYear repository if they exist
        if (s.getAcademicYear() != null) {
            java.util.Optional<StudentAcademicYear> mappingOpt = mappingRepository
                .findByStudent_IdAndAcademicYear_AcademicYearId(s.getId(), s.getAcademicYear().getAcademicYearId());
            if (mappingOpt.isPresent()) {
                StudentAcademicYear mapping = mappingOpt.get();
                if (mapping.getStandard() != null) standardEntity = mapping.getStandard();
                if (mapping.getDivision() != null) divisionEntity = mapping.getDivision();
                if (mapping.getRollNo() != null) rollNo = mapping.getRollNo();
            }
        } else {
            List<StudentAcademicYear> mappings = mappingRepository.findByStudent_Id(s.getId());
            if (mappings != null && !mappings.isEmpty()) {
                StudentAcademicYear mapping = mappings.get(0);
                if (mapping.getAcademicYear() != null) academicYearEntity = mapping.getAcademicYear();
                if (mapping.getStandard() != null) standardEntity = mapping.getStandard();
                if (mapping.getDivision() != null) divisionEntity = mapping.getDivision();
                if (mapping.getRollNo() != null) rollNo = mapping.getRollNo();
            }
        }

        if (academicYearEntity != null) {
            dto.setAcademicYearId(academicYearEntity.getAcademicYearId());
        }
        if (standardEntity != null) {
            dto.setStandardId(standardEntity.getStandardId());
            dto.setStandard(standardEntity.getStandard());
        }
        if (divisionEntity != null) {
            dto.setDivisionId(divisionEntity.getDivisionId());
            dto.setDivisionName(divisionEntity.getDivisionName());
        }

        dto.setPrnNumber(s.getPrnNumber());
        dto.setRollNumber(rollNo);
        dto.setFirstName(s.getFirstName());
        dto.setMiddleName(s.getMiddleName());
        dto.setLastName(s.getLastName());
        dto.setGender(s.getGender());
        dto.setDateOfBirth(s.getDateOfBirth());
        dto.setMobileNumber(s.getMobileNumber());
        dto.setEmailId(s.getEmailId());
        dto.setGuardianName(s.getGuardianName());
        dto.setParentMobileNumber(s.getParentMobileNumber());
        dto.setParentEmailId(s.getParentEmailId());
        dto.setAdmissionDate(s.getAdmissionDate());
        dto.setFeeType(s.getFeeType());
        dto.setTotalFee(s.getTotalFee());
        dto.setDiscount(s.getDiscount());
        dto.setFinalFee(s.getFinalFee());
        dto.setAddress(s.getAddress());
        dto.setStatus(s.getStatus());
        dto.setProfilePhoto(s.getProfilePhoto());
        dto.setAadharNumber(s.getAadharNumber());
        dto.setPasswordChangeRequired(s.isPasswordChangeRequired());
        return dto;
    }

    @Override
    public void createStudent(AddStudentDTO dto) {

        // Validations for compulsory student fields
        if (dto.getFirstName() == null || dto.getFirstName().trim().isEmpty()) {
            throw new RuntimeException("First name is compulsory");
        }
        if (dto.getLastName() == null || dto.getLastName().trim().isEmpty()) {
            throw new RuntimeException("Last name is compulsory");
        }
        if (dto.getAadharNumber() == null || dto.getAadharNumber().trim().isEmpty()) {
            throw new RuntimeException("Aadhar Card Number is compulsory");
        }

        // Validations for compulsory guardian details
        if (dto.getGuardianName() == null || dto.getGuardianName().trim().isEmpty()) {
            throw new RuntimeException("Guardian Name is compulsory");
        }
        if (dto.getParentMobileNumber() == null || dto.getParentMobileNumber().trim().isEmpty()) {
            throw new RuntimeException("Emergency Guardian mobile number is compulsory");
        }
        if (dto.getParentEmailId() == null || dto.getParentEmailId().trim().isEmpty()) {
            throw new RuntimeException("Emergency Guardian email ID is compulsory");
        }

        // Normalize student contact info (make optional)
        String studentEmail = (dto.getEmailId() == null || dto.getEmailId().trim().isEmpty()) ? null : dto.getEmailId().trim();
        String studentMobile = (dto.getMobileNumber() == null || dto.getMobileNumber().trim().isEmpty()) ? null : dto.getMobileNumber().trim();

        // Check for duplicate email
        if (studentEmail != null) {
            if (studentRepository.existsByEmailIdAndSchool_SchoolId(studentEmail, dto.getSchoolId())) {
                throw new RuntimeException("Email already exists");
            }
        }

        // Check for duplicate mobile number
        if (studentMobile != null) {
            if (studentRepository.existsByMobileNumberAndSchool_SchoolId(studentMobile, dto.getSchoolId())) {
                throw new RuntimeException("Mobile number already exists");
            }
        }

        AddStudent student = new AddStudent();

        // Resolve FK entities
        School school = schoolRepository.findBySchoolId(dto.getSchoolId())
                .orElseThrow(() -> new RuntimeException("School not found"));
        student.setSchool(school);

        if (dto.getStandardId() != null) {
            AddStandard standard = standardRepository.findByStandardId(dto.getStandardId())
                    .orElseThrow(() -> new RuntimeException("Standard not found"));
            student.setAddStandard(standard);
        }

        if (dto.getDivisionId() != null) {
            AddDivision division = divisionRepository.findByDivisionId(dto.getDivisionId())
                    .orElseThrow(() -> new RuntimeException("Division not found"));
            student.setAddDivision(division);
        }

        if (dto.getAcademicYearId() != null) {
            AcademicYear ay = academicYearRepository.findById(dto.getAcademicYearId())
                    .orElseThrow(() -> new RuntimeException("Academic Year not found"));
            student.setAcademicYear(ay);
        }

        // Set role (Student = roleId 4)
        Role role = roleRepository.findById(4L).orElse(null);
        student.setRole(role);

        // ===== Auto-Generate Unique PRN =====
        // Format: PRN-[YEAR]-[SCHOOL_ID]-[RANDOM 5 DIGITS]
        int currentYear = LocalDate.now().getYear();
        String randomSuffix = String.format("%05d", new SecureRandom().nextInt(100000));
        String generatedPrn = "PRN-" + currentYear + "-" + (dto.getSchoolId() != null ? dto.getSchoolId() : 0) + "-" + randomSuffix;
        student.setPrnNumber(generatedPrn);

        // Set roll number manually assigned by admin
        student.setRollNumber(dto.getRollNumber());

        // Copy personal fields
        student.setFirstName(dto.getFirstName().trim());
        student.setMiddleName(dto.getMiddleName() != null ? dto.getMiddleName().trim() : null);
        student.setLastName(dto.getLastName().trim());
        student.setGender(dto.getGender());
        student.setDateOfBirth(dto.getDateOfBirth());
        student.setMobileNumber(studentMobile);
        student.setEmailId(studentEmail);
        student.setGuardianName(dto.getGuardianName().trim());
        student.setParentMobileNumber(dto.getParentMobileNumber().trim());
        student.setParentEmailId(dto.getParentEmailId().trim());
        student.setAdmissionDate(dto.getAdmissionDate());
        student.setFeeType(dto.getFeeType());
        student.setTotalFee(dto.getTotalFee());
        student.setDiscount(dto.getDiscount());
        student.setFinalFee(dto.getFinalFee());
        student.setAddress(dto.getAddress());
        student.setProfilePhoto(dto.getProfilePhoto());
        student.setAadharNumber(dto.getAadharNumber());
        student.setStatus("ACTIVE");

        // Generate Student ID
        String schoolName = school.getSchoolName();
        String cleanedSchoolName = schoolName.replaceAll("\\s+", "").toLowerCase();
        String prefix = cleanedSchoolName.length() >= 4
                ? cleanedSchoolName.substring(0, 4) : cleanedSchoolName;

        Optional<AddStudent> lastStudentOpt = studentRepository.findTopByOrderByIdDesc();
        int nextNumber = 1;
        if (lastStudentOpt.isPresent() && lastStudentOpt.get().getStudentId() != null) {
            String lastStudentId = lastStudentOpt.get().getStudentId();
            String numberPart = lastStudentId.substring(4);
            nextNumber = Integer.parseInt(numberPart) + 1;
        }
        student.setStudentId(prefix + String.format("%02d", nextNumber));

        // Default password = DOB in ddMMyyyy format (e.g., 15032010)
        String dobPassword;
        if (student.getDateOfBirth() != null) {
            dobPassword = student.getDateOfBirth().format(DateTimeFormatter.ofPattern("ddMMyyyy"));
        } else {
            dobPassword = "12345678"; // fallback if DOB is null
        }
        student.setPassword(passwordEncoder.encode(dobPassword));
        student.setPasswordChangeRequired(true);

        studentRepository.save(student);

        // Also save mapping to StudentAcademicYear history
        if (dto.getAcademicYearId() != null && dto.getStandardId() != null && dto.getDivisionId() != null) {
            StudentAcademicYear mapping = new StudentAcademicYear();
            mapping.setStudent(student);
            mapping.setAcademicYear(student.getAcademicYear());
            mapping.setStandard(student.getAddStandard());
            mapping.setDivision(student.getAddDivision());
            mapping.setRollNo(student.getRollNumber());
            mapping.setStatus("ACTIVE");
            mappingRepository.save(mapping);
        }

        // Send email with aadhar as username and DOB as temp password (non-blocking)
        if (dto.getEmailId() != null && !dto.getEmailId().isEmpty()) {
            try {
                String username = student.getAadharNumber() != null ? student.getAadharNumber() : student.getStudentId();
                emailService.sendStudentCredentials(dto.getEmailId(), username, dobPassword);
            } catch (Exception e) {
                System.err.println("Failed to send email to " + dto.getEmailId() + ": " + e.getMessage());
            }
        }

        // ===== Parent Password Logic =====
        if (dto.getParentEmailId() != null && !dto.getParentEmailId().trim().isEmpty()) {
            // Check if another child already has this parent with a password
            java.util.Optional<AddStudent> existingParent = studentRepository
                    .findFirstByParentEmailIdAndParentPasswordIsNotNull(dto.getParentEmailId());

            if (existingParent.isPresent()) {
                // Reuse existing parent password (no new email)
                student.setParentPassword(existingParent.get().getParentPassword());
                studentRepository.save(student);
            } else {
                // Generate new parent password
                String parentPlainPassword = generateRandomPassword();
                student.setParentPassword(passwordEncoder.encode(parentPlainPassword));
                studentRepository.save(student);

                // Send parent credentials email
                String childName = (dto.getFirstName() != null ? dto.getFirstName() : "") + " "
                        + (dto.getLastName() != null ? dto.getLastName() : "");
                try {
                    emailService.sendParentCredentials(dto.getParentEmailId(), childName.trim(), parentPlainPassword);
                } catch (Exception e) {
                    System.err.println("Failed to send parent email to " + dto.getParentEmailId() + ": " + e.getMessage());
                }
            }
        }
    }

    private String generateRandomPassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            password.append(chars.charAt(random.nextInt(chars.length())));
        }
        return password.toString();
    }

    @Override
    public AddStudentDTO getStudentByIdAndSchoolId(AddStudentDTO dto) {
        AddStudent student = studentRepository.findByIdAndSchool_SchoolId(dto.getId(), dto.getSchoolId())
                .orElseThrow(() -> new RuntimeException("Student Not Found"));
        return toDTO(student);
    }

    @Override
    public void updateStudentByIdAndSchoolId(AddStudentDTO dto) {
        AddStudent student = studentRepository.findByIdAndSchool_SchoolId(dto.getId(), dto.getSchoolId())
                .orElseThrow(() -> new RuntimeException("Student Not Found"));

        // Validations for compulsory student fields
        if (dto.getFirstName() == null || dto.getFirstName().trim().isEmpty()) {
            throw new RuntimeException("First name is compulsory");
        }
        if (dto.getLastName() == null || dto.getLastName().trim().isEmpty()) {
            throw new RuntimeException("Last name is compulsory");
        }
        if (dto.getAadharNumber() == null || dto.getAadharNumber().trim().isEmpty()) {
            throw new RuntimeException("Aadhar Card Number is compulsory");
        }

        // Validations for compulsory guardian details
        if (dto.getGuardianName() == null || dto.getGuardianName().trim().isEmpty()) {
            throw new RuntimeException("Guardian Name is compulsory");
        }
        if (dto.getParentMobileNumber() == null || dto.getParentMobileNumber().trim().isEmpty()) {
            throw new RuntimeException("Emergency Guardian mobile number is compulsory");
        }
        if (dto.getParentEmailId() == null || dto.getParentEmailId().trim().isEmpty()) {
            throw new RuntimeException("Emergency Guardian email ID is compulsory");
        }

        // Normalize student contact info (make optional)
        String studentEmail = (dto.getEmailId() == null || dto.getEmailId().trim().isEmpty()) ? null : dto.getEmailId().trim();
        String studentMobile = (dto.getMobileNumber() == null || dto.getMobileNumber().trim().isEmpty()) ? null : dto.getMobileNumber().trim();

        // Check duplicate email
        if (studentEmail != null && !studentEmail.equalsIgnoreCase(student.getEmailId())) {
            if (studentRepository.existsByEmailIdAndSchool_SchoolId(studentEmail, dto.getSchoolId())) {
                throw new RuntimeException("Email already exists");
            }
        }

        // Check duplicate mobile number
        if (studentMobile != null && !studentMobile.equalsIgnoreCase(student.getMobileNumber())) {
            if (studentRepository.existsByMobileNumberAndSchool_SchoolId(studentMobile, dto.getSchoolId())) {
                throw new RuntimeException("Mobile number already exists");
            }
        }

        student.setFirstName(dto.getFirstName().trim());
        student.setMiddleName(dto.getMiddleName() != null ? dto.getMiddleName().trim() : null);
        student.setLastName(dto.getLastName().trim());
        student.setGender(dto.getGender());
        student.setDateOfBirth(dto.getDateOfBirth());
        student.setMobileNumber(studentMobile);
        student.setEmailId(studentEmail);
        student.setGuardianName(dto.getGuardianName().trim());
        student.setParentMobileNumber(dto.getParentMobileNumber().trim());
        student.setParentEmailId(dto.getParentEmailId().trim());
        student.setAdmissionDate(dto.getAdmissionDate());
        student.setFeeType(dto.getFeeType());
        student.setTotalFee(dto.getTotalFee());
        student.setDiscount(dto.getDiscount());
        student.setFinalFee(dto.getFinalFee());
        student.setAddress(dto.getAddress());
        student.setProfilePhoto(dto.getProfilePhoto());
        student.setAadharNumber(dto.getAadharNumber().trim());
        if (dto.getStatus() != null) {
            student.setStatus(dto.getStatus().trim().toUpperCase());
        }

        if (dto.getStandardId() != null) {
            student.setAddStandard(standardRepository.findByStandardId(dto.getStandardId()).orElse(null));
        }
        if (dto.getDivisionId() != null) {
            student.setAddDivision(divisionRepository.findByDivisionId(dto.getDivisionId()).orElse(null));
        }

        studentRepository.save(student);

        // Update mapping table for the current active academic year and sync status
        Optional<AcademicYear> currentYear = academicYearRepository.findBySchool_SchoolIdAndIsCurrentTrue(dto.getSchoolId());
        if (currentYear.isPresent()) {
            AcademicYear ay = currentYear.get();
            Optional<StudentAcademicYear> mappingOpt = mappingRepository
                    .findByStudent_IdAndAcademicYear_AcademicYearId(student.getId(), ay.getAcademicYearId());
            
            StudentAcademicYear mapping = mappingOpt.orElseGet(() -> {
                StudentAcademicYear newMap = new StudentAcademicYear();
                newMap.setStudent(student);
                newMap.setAcademicYear(ay);
                newMap.setStatus("ACTIVE");
                return newMap;
            });
            
            if (dto.getStandardId() != null || dto.getDivisionId() != null) {
                mapping.setStandard(student.getAddStandard());
                mapping.setDivision(student.getAddDivision());
                mapping.setRollNo(student.getRollNumber());
            }
            if (student.getStatus() != null) {
                mapping.setStatus(student.getStatus());
            }
            mappingRepository.save(mapping);
        } else if (student.getAcademicYear() != null) {
            Optional<StudentAcademicYear> mappingOpt = mappingRepository
                    .findByStudent_IdAndAcademicYear_AcademicYearId(student.getId(), student.getAcademicYear().getAcademicYearId());
            if (mappingOpt.isPresent()) {
                StudentAcademicYear mapping = mappingOpt.get();
                if (student.getStatus() != null) {
                    mapping.setStatus(student.getStatus());
                }
                mappingRepository.save(mapping);
            }
        }
    }

    @Override
    public void deleteStudentByIdAndSchoolId(AddStudentDTO dto) {
        AddStudent student = studentRepository.findByIdAndSchool_SchoolId(dto.getId(), dto.getSchoolId())
                .orElseThrow(() -> new RuntimeException("Student Not Found"));
        studentRepository.delete(student);
    }

    @Override
    public List<AddStudentDTO> getAllActiveStudentsBySchoolId(AddStudentDTO dto) {
        return mappingRepository.findStudentsBySchoolAndYearAndStatus(dto.getSchoolId(), dto.getAcademicYearId(), "ACTIVE")
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<AddStudentDTO> getAllInactiveStudentsBySchoolId(AddStudentDTO dto) {
        return mappingRepository.findStudentsBySchoolAndYearAndStatus(dto.getSchoolId(), dto.getAcademicYearId(), "INACTIVE")
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public Long countActiveStudentsBySchoolId(AddStudentDTO dto) {
        return mappingRepository.countStudentsBySchoolAndYearAndStatus(dto.getSchoolId(), dto.getAcademicYearId(), "ACTIVE");
    }

    @Override
    public Long countInactiveStudentsBySchoolId(AddStudentDTO dto) {
        return mappingRepository.countStudentsBySchoolAndYearAndStatus(dto.getSchoolId(), dto.getAcademicYearId(), "INACTIVE");
    }

    @Override
    public Long totalRegisteredStudentsBySchoolId(AddStudentDTO dto) {
        return mappingRepository.countStudentsBySchoolAndYear(dto.getSchoolId(), dto.getAcademicYearId());
    }

    @Override
    public List<AddStudentDTO> getAllStudentsBySchoolId(AddStudentDTO dto) {
        return mappingRepository.findStudentsBySchoolAndYear(dto.getSchoolId(), dto.getAcademicYearId())
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public boolean isEmailAlreadyPresent(AddStudentDTO dto) {
        return studentRepository.existsByEmailIdAndSchool_SchoolId(dto.getEmailId(), dto.getSchoolId());
    }

    @Override
    public boolean isMobileNumberAlreadyPresent(AddStudentDTO dto) {
        return studentRepository.existsByMobileNumberAndSchool_SchoolId(dto.getMobileNumber(), dto.getSchoolId());
    }

    @Override
    public boolean isSchoolIdPresent(Long schoolId) {
        return studentRepository.existsBySchool_SchoolId(schoolId);
    }

    @Override
    public void updateStudentStatus(AddStudentDTO dto) {
        AddStudent student = studentRepository.findByIdAndSchool_SchoolId(dto.getId(), dto.getSchoolId())
                .orElseThrow(() -> new RuntimeException("Student Not Found"));
        String newStatus = dto.getStatus().trim().toUpperCase();
        student.setStatus(newStatus);
        studentRepository.save(student);

        // Update StudentAcademicYear mapping table status
        Long ayId = student.getAcademicYear() != null ? student.getAcademicYear().getAcademicYearId() : null;
        if (ayId == null) {
            Optional<AcademicYear> currentYear = academicYearRepository.findBySchool_SchoolIdAndIsCurrentTrue(dto.getSchoolId());
            if (currentYear.isPresent()) {
                ayId = currentYear.get().getAcademicYearId();
            }
        }
        if (ayId != null) {
            Optional<StudentAcademicYear> mappingOpt = mappingRepository
                    .findByStudent_IdAndAcademicYear_AcademicYearId(student.getId(), ayId);
            if (mappingOpt.isPresent()) {
                StudentAcademicYear mapping = mappingOpt.get();
                mapping.setStatus(newStatus);
                mappingRepository.save(mapping);
            }
        }
    }

    @Override
    public List<AddStudentDTO> getStudentsByDivision(Long schoolId, Long standardId, Long divisionId) {
        return mappingRepository.findStudentsByDivision(
                schoolId, standardId, divisionId)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public void changePassword(ChangePasswordDTO dto) {
        // Validate school first
        schoolRepository.findBySchoolId(dto.getSchoolId())
                .orElseThrow(() -> new RuntimeException("School not found"));

        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            throw new RuntimeException("New password and confirm password do not match");
        }

        AddStudent student = studentRepository.findByIdAndSchool_SchoolId(dto.getStudentId(), dto.getSchoolId())
                .orElseThrow(() -> new RuntimeException("Student not found in this school"));

        if (!passwordEncoder.matches(dto.getCurrentPassword(), student.getPassword())) {
            throw new RuntimeException("Current password is incorrect");
        }

        student.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        student.setPasswordChangeRequired(false);
        studentRepository.save(student);
    }

    @Override
    public void updatePassword(UpdatePasswordDTO dto) {
        // Validate school first
        schoolRepository.findBySchoolId(dto.getSchoolId())
                .orElseThrow(() -> new RuntimeException("School not found"));

        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            throw new RuntimeException("New password and confirm password do not match");
        }

        AddStudent student = studentRepository.findByIdAndSchool_SchoolId(dto.getStudentId(), dto.getSchoolId())
                .orElseThrow(() -> new RuntimeException("Student not found in this school"));

        if (!passwordEncoder.matches(dto.getOldPassword(), student.getPassword())) {
            throw new RuntimeException("Old password is incorrect");
        }

        student.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        studentRepository.save(student);
    }

    @Override
    public void resetPassword(ResetPasswordDTO dto) {
        // Validate school first
        schoolRepository.findBySchoolId(dto.getSchoolId())
                .orElseThrow(() -> new RuntimeException("School not found"));

        AddStudent student = studentRepository.findByIdAndSchool_SchoolId(dto.getStudentId(), dto.getSchoolId())
                .orElseThrow(() -> new RuntimeException("Student not found in this school"));

        // Reset password to DOB
        String dobPassword;
        if (student.getDateOfBirth() != null) {
            dobPassword = student.getDateOfBirth().format(DateTimeFormatter.ofPattern("ddMMyyyy"));
        } else {
            dobPassword = "12345678";
        }
        student.setPassword(passwordEncoder.encode(dobPassword));
        student.setPasswordChangeRequired(true);
        studentRepository.save(student);
    }

    @Override
    public int migratePasswordsToDob(Long schoolId) {
        List<AddStudent> students = studentRepository.findBySchool_SchoolId(schoolId);
        int count = 0;

        for (AddStudent student : students) {
            if (student.getDateOfBirth() != null) {
                String dobPassword = student.getDateOfBirth().format(DateTimeFormatter.ofPattern("ddMMyyyy"));
                student.setPassword(passwordEncoder.encode(dobPassword));
            } else {
                student.setPassword(passwordEncoder.encode("12345678"));
            }
            student.setPasswordChangeRequired(true);
            studentRepository.save(student);
            count++;
        }

        return count;
    }

    @Override
    public int resetPasswordsBulk(ResetPasswordsBulkDTO dto) {
        // Validate school first
        schoolRepository.findBySchoolId(dto.getSchoolId())
                .orElseThrow(() -> new RuntimeException("School not found"));

        int count = 0;
        for (Long studentId : dto.getStudentIds()) {
            AddStudent student = studentRepository.findByIdAndSchool_SchoolId(studentId, dto.getSchoolId())
                    .orElseThrow(() -> new RuntimeException("Student with ID " + studentId + " not found in this school"));

            String dobPassword;
            if (student.getDateOfBirth() != null) {
                dobPassword = student.getDateOfBirth().format(DateTimeFormatter.ofPattern("ddMMyyyy"));
            } else {
                dobPassword = "12345678";
            }
            student.setPassword(passwordEncoder.encode(dobPassword));
            student.setPasswordChangeRequired(true);
            studentRepository.save(student);
            count++;
        }

        return count;
    }
}
