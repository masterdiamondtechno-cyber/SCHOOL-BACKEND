package com.schoolmanagement.school.serviceImpl;

import com.schoolmanagement.school.dto.EmployeeDTO;
import com.schoolmanagement.school.entity.AcademicYear;
import com.schoolmanagement.school.entity.Employee;
import com.schoolmanagement.school.entity.Role;
import com.schoolmanagement.school.entity.School;
import com.schoolmanagement.school.repository.AcademicYearRepository;
import com.schoolmanagement.school.repository.EmployeeRepository;
import com.schoolmanagement.school.repository.RoleRepository;
import com.schoolmanagement.school.repository.SchoolRepository;
import com.schoolmanagement.school.service.EmployeeService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final SchoolRepository schoolRepository;
    private final AcademicYearRepository academicYearRepository;
    private final RoleRepository roleRepository;

    public EmployeeServiceImpl(EmployeeRepository employeeRepository,
                               SchoolRepository schoolRepository,
                               AcademicYearRepository academicYearRepository,
                               RoleRepository roleRepository) {
        this.employeeRepository = employeeRepository;
        this.schoolRepository = schoolRepository;
        this.academicYearRepository = academicYearRepository;
        this.roleRepository = roleRepository;
    }

    private EmployeeDTO toDTO(Employee e) {
        EmployeeDTO dto = new EmployeeDTO();
        dto.setId(e.getId());
        dto.setSchoolId(e.getSchool().getSchoolId());
        if (e.getAcademicYear() != null)
            dto.setAcademicYearId(e.getAcademicYear().getAcademicYearId());
        if (e.getRole() != null) {
            dto.setRoleId(e.getRole().getRoleId());
            dto.setRoleName(e.getRole().getName());
        }
        dto.setEmployeeName(e.getEmployeeName());
        dto.setEmailId(e.getEmailId());
        dto.setMobileNumber(e.getMobileNumber());
        dto.setJoiningDate(e.getJoiningDate());
        dto.setSalary(e.getSalary());
        dto.setEducation(e.getEducation());
        dto.setGender(e.getGender());
        dto.setEmployeeType(e.getEmployeeType());
        dto.setStatus(e.getStatus());
        dto.setAddress(e.getAddress());
        dto.setProfilePhoto(e.getProfilePhoto());
        dto.setAadharCardNumber(e.getAadharCardNumber());
        return dto;
    }

    @Override
    public void addEmployee(EmployeeDTO dto) {
        if (employeeRepository.existsBySchool_SchoolIdAndEmailId(dto.getSchoolId(), dto.getEmailId()))
            throw new RuntimeException("Email already registered");
        if (employeeRepository.existsBySchool_SchoolIdAndMobileNumber(dto.getSchoolId(), dto.getMobileNumber()))
            throw new RuntimeException("Mobile already registered");

        Employee e = new Employee();

        School school = schoolRepository.findBySchoolId(dto.getSchoolId())
                .orElseThrow(() -> new RuntimeException("School not found"));
        e.setSchool(school);

        if (dto.getAcademicYearId() != null) {
            AcademicYear ay = academicYearRepository.findById(dto.getAcademicYearId())
                    .orElseThrow(() -> new RuntimeException("Academic year not found"));
            e.setAcademicYear(ay);
        }

        if (dto.getRoleId() != null) {
            Role role = roleRepository.findById(dto.getRoleId())
                    .orElseThrow(() -> new RuntimeException("Role not found"));
            e.setRole(role);
        }

        e.setEmployeeName(dto.getEmployeeName());
        e.setEmailId(dto.getEmailId());
        e.setMobileNumber(dto.getMobileNumber());
        e.setJoiningDate(dto.getJoiningDate());
        e.setSalary(dto.getSalary());
        e.setEducation(dto.getEducation());
        e.setGender(dto.getGender());
        e.setEmployeeType(dto.getEmployeeType() != null ? dto.getEmployeeType() : "OTHER");
        e.setStatus(dto.getStatus() != null ? dto.getStatus() : "ACTIVE");
        e.setAddress(dto.getAddress());
        e.setProfilePhoto(dto.getProfilePhoto());
        e.setAadharCardNumber(dto.getAadharCardNumber());

        employeeRepository.save(e);
    }

    @Override
    public void updateEmployee(EmployeeDTO dto) {
        Employee e = employeeRepository.findByIdAndSchool_SchoolId(dto.getId(), dto.getSchoolId())
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        if (dto.getEmployeeName() != null) e.setEmployeeName(dto.getEmployeeName());
        if (dto.getEmailId() != null) e.setEmailId(dto.getEmailId());
        if (dto.getMobileNumber() != null) e.setMobileNumber(dto.getMobileNumber());
        if (dto.getJoiningDate() != null) e.setJoiningDate(dto.getJoiningDate());
        if (dto.getSalary() != null) e.setSalary(dto.getSalary());
        if (dto.getEducation() != null) e.setEducation(dto.getEducation());
        if (dto.getGender() != null) e.setGender(dto.getGender());
        if (dto.getEmployeeType() != null) e.setEmployeeType(dto.getEmployeeType());
        if (dto.getStatus() != null) e.setStatus(dto.getStatus());
        if (dto.getAddress() != null) e.setAddress(dto.getAddress());
        if (dto.getProfilePhoto() != null) e.setProfilePhoto(dto.getProfilePhoto());
        if (dto.getAadharCardNumber() != null) e.setAadharCardNumber(dto.getAadharCardNumber());

        if (dto.getRoleId() != null) {
            Role role = roleRepository.findById(dto.getRoleId())
                    .orElseThrow(() -> new RuntimeException("Role not found"));
            e.setRole(role);
        }

        employeeRepository.save(e);
    }

    @Override
    public void deleteEmployee(Long id, Long schoolId) {
        Employee e = employeeRepository.findByIdAndSchool_SchoolId(id, schoolId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));
        employeeRepository.delete(e);
    }

    @Override
    public List<EmployeeDTO> getActive(Long schoolId, Long academicYearId) {
        List<Employee> list = (academicYearId != null)
                ? employeeRepository.findBySchool_SchoolIdAndAcademicYear_AcademicYearIdAndStatus(schoolId, academicYearId, "ACTIVE")
                : employeeRepository.findBySchool_SchoolIdAndStatus(schoolId, "ACTIVE");
        return list.stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<EmployeeDTO> getInactive(Long schoolId, Long academicYearId) {
        List<Employee> list = (academicYearId != null)
                ? employeeRepository.findBySchool_SchoolIdAndAcademicYear_AcademicYearIdAndStatus(schoolId, academicYearId, "INACTIVE")
                : employeeRepository.findBySchool_SchoolIdAndStatus(schoolId, "INACTIVE");
        return list.stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<EmployeeDTO> getAll(Long schoolId, Long academicYearId) {
        List<Employee> list = (academicYearId != null)
                ? employeeRepository.findBySchool_SchoolIdAndAcademicYear_AcademicYearId(schoolId, academicYearId)
                : employeeRepository.findBySchool_SchoolId(schoolId);
        return list.stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public EmployeeDTO getById(Long id, Long schoolId) {
        Employee e = employeeRepository.findByIdAndSchool_SchoolId(id, schoolId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));
        return toDTO(e);
    }

    @Override
    public long countActive(Long schoolId) {
        return employeeRepository.countBySchool_SchoolIdAndStatus(schoolId, "ACTIVE");
    }

    @Override
    public long countInactive(Long schoolId) {
        return employeeRepository.countBySchool_SchoolIdAndStatus(schoolId, "INACTIVE");
    }

    @Override
    public long countTotal(Long schoolId) {
        return employeeRepository.countBySchool_SchoolId(schoolId);
    }

    @Override
    public boolean checkEmail(Long schoolId, String emailId) {
        return employeeRepository.existsBySchool_SchoolIdAndEmailId(schoolId, emailId);
    }

    @Override
    public boolean checkMobile(Long schoolId, String mobileNumber) {
        return employeeRepository.existsBySchool_SchoolIdAndMobileNumber(schoolId, mobileNumber);
    }

    @Override
    public void changeStatus(Long id, Long schoolId, String status) {
        Employee e = employeeRepository.findByIdAndSchool_SchoolId(id, schoolId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));
        if (!status.equals("ACTIVE") && !status.equals("INACTIVE"))
            throw new RuntimeException("Invalid status");
        e.setStatus(status);
        employeeRepository.save(e);
    }
}