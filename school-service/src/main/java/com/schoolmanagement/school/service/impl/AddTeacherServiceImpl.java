package com.schoolmanagement.school.serviceImpl;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.schoolmanagement.school.dto.AddTeacherDTO;
import com.schoolmanagement.school.entity.AcademicYear;
import com.schoolmanagement.school.entity.AddDivision;
import com.schoolmanagement.school.entity.AddStandard;
import com.schoolmanagement.school.entity.AddTeacher;
import com.schoolmanagement.school.entity.Role;
import com.schoolmanagement.school.entity.School;
import com.schoolmanagement.school.repository.AcademicYearRepository;
import com.schoolmanagement.school.repository.AddDivisionRepository;
import com.schoolmanagement.school.repository.AddStandardRepository;
import com.schoolmanagement.school.repository.AddTeacherRepository;
import com.schoolmanagement.school.repository.RoleRepository;
import com.schoolmanagement.school.repository.SchoolRepository;
import com.schoolmanagement.school.service.AddTeacherService;
import com.schoolmanagement.school.service.EmailService;

@Service
public class AddTeacherServiceImpl implements AddTeacherService {

    private final AddTeacherRepository teacherRepository;
    private final SchoolRepository schoolRepository;
    private final AddStandardRepository standardRepository;
    private final AddDivisionRepository divisionRepository;
    private final RoleRepository roleRepository;
    private final AcademicYearRepository academicYearRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public AddTeacherServiceImpl(AddTeacherRepository teacherRepository,
            SchoolRepository schoolRepository,
            AddStandardRepository standardRepository,
            AddDivisionRepository divisionRepository,
            RoleRepository roleRepository,
            AcademicYearRepository academicYearRepository,
            BCryptPasswordEncoder passwordEncoder,
            EmailService emailService) {
        this.teacherRepository = teacherRepository;
        this.schoolRepository = schoolRepository;
        this.standardRepository = standardRepository;
        this.divisionRepository = divisionRepository;
        this.roleRepository = roleRepository;
        this.academicYearRepository = academicYearRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    // ==================== ENTITY → DTO CONVERTER ====================
    private AddTeacherDTO toDTO(AddTeacher t) {
        AddTeacherDTO dto = new AddTeacherDTO();
        dto.setId(t.getId());
        dto.setSchoolId(t.getSchool() != null ? t.getSchool().getSchoolId() : null);
        dto.setAcademicYearId(t.getAcademicYear() != null ? t.getAcademicYear().getAcademicYearId() : null);
        dto.setStandardId(t.getAddStandard() != null ? t.getAddStandard().getStandardId() : null);
        dto.setDivisionId(t.getAddDivision() != null ? t.getAddDivision().getDivisionId() : null);
        dto.setStandardName(t.getAddStandard() != null ? t.getAddStandard().getStandard() : null);
        dto.setDivisionName(t.getAddDivision() != null ? t.getAddDivision().getDivisionName() : null);

        // Fallback: If standardId is null, resolve dynamically from standard and division configurations
        if (dto.getStandardId() == null && t.getSchool() != null) {
            try {
                List<AddStandard> standards = standardRepository.findBySchool_SchoolId(t.getSchool().getSchoolId());
                for (AddStandard std : standards) {
                    if (std.getClassTeacher() != null && std.getClassTeacher().getId().equals(t.getId())) {
                        dto.setStandardId(std.getStandardId());
                        dto.setStandardName(std.getStandard());
                        List<AddDivision> divisions = divisionRepository.findByAddStandard_StandardIdAndAcademicYear_AcademicYearId(
                            std.getStandardId(), t.getAcademicYear() != null ? t.getAcademicYear().getAcademicYearId() : null
                        );
                        if (divisions != null && !divisions.isEmpty()) {
                            dto.setDivisionId(divisions.get(0).getDivisionId());
                            dto.setDivisionName(divisions.get(0).getDivisionName());
                        }
                        break;
                    }
                }
            } catch (Exception e) {
                System.err.println("Failed to resolve dynamic class teacher info: " + e.getMessage());
            }
        }
        dto.setTeacherName(t.getTeacherName());
        dto.setGender(t.getGender());
        dto.setDateOfBirth(t.getDateOfBirth());
        dto.setMobileNumber(t.getMobileNumber());
        dto.setEmailId(t.getEmailId());
        dto.setSalaryType(t.getSalaryType());
        dto.setJoiningDate(t.getJoiningDate());
        dto.setAnnualSalary(t.getAnnualSalary());
        dto.setMonthlySalary(t.getMonthlySalary());
        dto.setPfStatus(t.getPfStatus());
        dto.setPfAccount(t.getPfAccount());
        dto.setPaidInHandSalary(t.getPaidInHandSalary());
        dto.setAddress(t.getAddress());
        dto.setProfilePhoto(t.getProfilePhoto());
        dto.setAadharCardNumber(t.getAadharCardNumber());
        dto.setStatus(t.getStatus());
        return dto;
    }

    @Override
    public void createTeacher(AddTeacherDTO dto) {

        // Check for duplicate email
        if (dto.getEmailId() != null && !dto.getEmailId().trim().isEmpty()) {
            if (teacherRepository.existsBySchool_SchoolIdAndEmailId(dto.getSchoolId(), dto.getEmailId())) {
                throw new RuntimeException("Email already exists");
            }
        }

        // Check for duplicate mobile number
        if (dto.getMobileNumber() != null && !dto.getMobileNumber().trim().isEmpty()) {
            if (teacherRepository.existsBySchool_SchoolIdAndMobileNumber(dto.getSchoolId(), dto.getMobileNumber())) {
                throw new RuntimeException("Mobile number already exists");
            }
        }

        AddTeacher teacher = new AddTeacher();

        // Resolve FK entities
        School school = schoolRepository.findBySchoolId(dto.getSchoolId())
                .orElseThrow(() -> new RuntimeException("School not found"));
        teacher.setSchool(school);

        // standardId & batchId are optional — teacher just registers to school
        if (dto.getStandardId() != null) {
            AddStandard standard = standardRepository.findByStandardId(dto.getStandardId())
                    .orElseThrow(() -> new RuntimeException("Standard not found"));
            teacher.setAddStandard(standard);
        }
        if (dto.getDivisionId() != null) {
            AddDivision division = divisionRepository.findByDivisionId(dto.getDivisionId())
                    .orElseThrow(() -> new RuntimeException("Division not found"));
            teacher.setAddDivision(division);
        }
        if (dto.getAcademicYearId() != null) {
            AcademicYear ay = academicYearRepository.findById(dto.getAcademicYearId())
                    .orElseThrow(() -> new RuntimeException("Academic Year not found"));
            teacher.setAcademicYear(ay);
        }

        // Set role (Teacher = roleId 3)
        Role role = roleRepository.findById(3L).orElse(null);
        teacher.setRole(role);

        // Copy fields
        teacher.setTeacherName(dto.getTeacherName());
        teacher.setGender(dto.getGender());
        teacher.setDateOfBirth(dto.getDateOfBirth());
        teacher.setMobileNumber(dto.getMobileNumber());
        teacher.setEmailId(dto.getEmailId());
        teacher.setSalaryType(dto.getSalaryType());
        teacher.setJoiningDate(dto.getJoiningDate());
        teacher.setAnnualSalary(dto.getAnnualSalary());
        teacher.setMonthlySalary(dto.getMonthlySalary());
        teacher.setPfStatus(dto.getPfStatus());
        teacher.setPfAccount(dto.getPfAccount());
        teacher.setPaidInHandSalary(dto.getPaidInHandSalary());
        teacher.setAddress(dto.getAddress());
        teacher.setProfilePhoto(dto.getProfilePhoto());
        teacher.setAadharCardNumber(dto.getAadharCardNumber());
        teacher.setStatus(dto.getStatus() != null ? dto.getStatus() : "ACTIVE");

        // Generate password
        String rawPassword = "Teacher@" + (int) (Math.random() * 10000);
        teacher.setPassword(passwordEncoder.encode(rawPassword));

        teacherRepository.save(teacher);

        // Update division / standard with this teacher as class teacher
        if (teacher.getAddDivision() != null) {
            AddDivision division = teacher.getAddDivision();
            division.setClassTeacher(teacher);
            divisionRepository.save(division);
        } else if (teacher.getAddStandard() != null) {
            AddStandard standard = teacher.getAddStandard();
            standard.setClassTeacher(teacher);
            standardRepository.save(standard);
        }

        // Send email (non-blocking — teacher is already saved)
        try {
            emailService.sendTeacherCredentials(dto.getEmailId(), rawPassword);
        } catch (Exception e) {
            // Log but don't fail — teacher is already created
            System.err.println("Failed to send email to " + dto.getEmailId() + ": " + e.getMessage());
        }
    }

    @Override
    public List<AddTeacherDTO> getAllBySchoolId(Long schoolId, Long academicYearId) {
        return teacherRepository.findBySchool_SchoolIdAndAcademicYear_AcademicYearId(schoolId, academicYearId)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public AddTeacherDTO getByIdAndSchoolId(Long id, Long schoolId) {
        AddTeacher teacher = teacherRepository.findByIdAndSchool_SchoolId(id, schoolId).orElse(null);
        return teacher != null ? toDTO(teacher) : null;
    }

    @Override
    public boolean deleteTeacher(Long id, Long schoolId) {
        Optional<AddTeacher> teacherOpt = teacherRepository.findByIdAndSchool_SchoolId(id, schoolId);
        if (teacherOpt.isPresent()) {
            teacherRepository.delete(teacherOpt.get());
            return true;
        }
        return false;
    }

    @Override
    public List<AddTeacherDTO> getTeachersByStatus(Long schoolId, Long academicYearId, String status) {
        return teacherRepository
                .findBySchool_SchoolIdAndAcademicYear_AcademicYearIdAndStatus(schoolId, academicYearId, status)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public long countBySchoolIdAndStatus(Long schoolId, Long academicYearId, String status) {
        return teacherRepository.countBySchool_SchoolIdAndAcademicYear_AcademicYearIdAndStatus(schoolId, academicYearId,
                status);
    }

    @Override
    public long countBySchoolId(Long schoolId, Long academicYearId) {
        return teacherRepository.countBySchool_SchoolIdAndAcademicYear_AcademicYearId(schoolId, academicYearId);
    }

    @Override
    public boolean schoolExists(Long schoolId) {
        return teacherRepository.existsBySchool_SchoolId(schoolId);
    }

    @Override
    public boolean isMobileExists(Long schoolId, String mobileNumber) {
        return teacherRepository.existsBySchool_SchoolIdAndMobileNumber(schoolId, mobileNumber);
    }

    @Override
    public boolean isEmailExists(Long schoolId, String emailId) {
        return teacherRepository.existsBySchool_SchoolIdAndEmailId(schoolId, emailId);
    }

    @Override
    public void changeTeacherStatus(Long id, Long schoolId, String status) {
        AddTeacher teacher = teacherRepository.findByIdAndSchool_SchoolId(id, schoolId)
                .orElseThrow(() -> new RuntimeException("Teacher not found with given id and schoolId"));
        teacher.setStatus(status.toUpperCase());
        teacherRepository.save(teacher);
    }

    @Override
    public List<Map<String, Object>> getTeacherIdAndNameBySchoolId(Long schoolId, Long academicYearId) {
        if (!teacherRepository.existsBySchool_SchoolId(schoolId)) {
            throw new RuntimeException("SchoolId not found in database");
        }
        List<AddTeacher> teachers = teacherRepository.findBySchool_SchoolIdAndAcademicYear_AcademicYearId(schoolId,
                academicYearId);
        List<Map<String, Object>> result = new ArrayList<>();
        for (AddTeacher teacher : teachers) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", teacher.getId());
            map.put("teacherName", teacher.getTeacherName());
            result.add(map);
        }
        return result;
    }

    @Override
    public void updateTeacher(AddTeacherDTO dto) {
        AddTeacher teacher = teacherRepository.findByIdAndSchool_SchoolId(dto.getId(), dto.getSchoolId())
                .orElseThrow(() -> new RuntimeException("Teacher not found with given id and schoolId"));

        teacher.setTeacherName(dto.getTeacherName());
        teacher.setGender(dto.getGender());
        teacher.setDateOfBirth(dto.getDateOfBirth());
        teacher.setMobileNumber(dto.getMobileNumber());
        teacher.setEmailId(dto.getEmailId());
        teacher.setSalaryType(dto.getSalaryType());
        teacher.setJoiningDate(dto.getJoiningDate());
        teacher.setAnnualSalary(dto.getAnnualSalary());
        teacher.setMonthlySalary(dto.getMonthlySalary());
        teacher.setPfStatus(dto.getPfStatus());
        teacher.setPfAccount(dto.getPfAccount());
        teacher.setPaidInHandSalary(dto.getPaidInHandSalary());
        teacher.setAddress(dto.getAddress());
        teacher.setProfilePhoto(dto.getProfilePhoto());
        teacher.setAadharCardNumber(dto.getAadharCardNumber());
        if (dto.getStatus() != null)
            teacher.setStatus(dto.getStatus());

        if (dto.getStandardId() != null) {
            teacher.setAddStandard(standardRepository.findByStandardId(dto.getStandardId()).orElse(null));
        } else {
            teacher.setAddStandard(null);
        }
        if (dto.getDivisionId() != null) {
            teacher.setAddDivision(divisionRepository.findByDivisionId(dto.getDivisionId()).orElse(null));
        } else {
            teacher.setAddDivision(null);
        }

        teacherRepository.save(teacher);

        // Update division / standard with this teacher as class teacher
        if (teacher.getAddDivision() != null) {
            AddDivision division = teacher.getAddDivision();
            division.setClassTeacher(teacher);
            divisionRepository.save(division);
        } else if (teacher.getAddStandard() != null) {
            AddStandard standard = teacher.getAddStandard();
            standard.setClassTeacher(teacher);
            standardRepository.save(standard);
        }
    }

    @Override
    public int resetPasswordsBulk(com.schoolmanagement.school.dto.ResetTeacherPasswordsBulkDTO dto) {
        schoolRepository.findBySchoolId(dto.getSchoolId())
                .orElseThrow(() -> new RuntimeException("School not found"));

        int count = 0;
        for (Long teacherId : dto.getTeacherIds()) {
            AddTeacher teacher = teacherRepository.findByIdAndSchool_SchoolId(teacherId, dto.getSchoolId())
                    .orElseThrow(() -> new RuntimeException("Teacher with ID " + teacherId + " not found in this school"));

            String dobPassword;
            if (teacher.getDateOfBirth() != null) {
                dobPassword = teacher.getDateOfBirth().format(java.time.format.DateTimeFormatter.ofPattern("ddMMyyyy"));
            } else {
                dobPassword = "12345678";
            }
            teacher.setPassword(passwordEncoder.encode(dobPassword));
            teacherRepository.save(teacher);
            count++;
        }

        return count;
    }
}
