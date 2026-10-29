package com.schoolmanagement.school.service.impl;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.schoolmanagement.school.dto.UserManagementDTO;
import com.schoolmanagement.school.entity.AddStudent;
import com.schoolmanagement.school.entity.AddTeacher;
import com.schoolmanagement.school.repository.AddStudentRepository;
import com.schoolmanagement.school.repository.AddTeacherRepository;
import com.schoolmanagement.school.service.UserManagementService;

@Service
public class UserManagementServiceImpl implements UserManagementService {

    @Autowired
    private AddTeacherRepository teacherRepository;

    @Autowired
    private AddStudentRepository studentRepository;

    @Override
    public List<UserManagementDTO> getAllUsers(Long schoolId, Long academicYearId) {

        List<UserManagementDTO> users = new ArrayList<>();

        // ── Fetch teachers ──────────────────────────────────────
        List<AddTeacher> teachers = teacherRepository
                .findBySchool_SchoolIdAndAcademicYear_AcademicYearId(schoolId, academicYearId);

        for (AddTeacher t : teachers) {
            UserManagementDTO dto = new UserManagementDTO();

            // Common
            dto.setId(t.getId());
            dto.setUserType("teacher");
            dto.setName(t.getTeacherName());
            dto.setEmail(t.getEmailId());
            dto.setPhone(t.getMobileNumber());
            dto.setRole(t.getRole() != null ? t.getRole().getName() : "teacher");
            dto.setStatus(t.getStatus());
            dto.setGender(t.getGender());
            dto.setDateOfBirth(t.getDateOfBirth());
            dto.setAddress(t.getAddress());
            dto.setProfilePhoto(t.getProfilePhoto());
            dto.setCreatedAt(t.getCreatedAt());

            // Context
            dto.setSchoolId(t.getSchool() != null ? t.getSchool().getSchoolId() : null);
            dto.setAcademicYearId(t.getAcademicYear() != null ? t.getAcademicYear().getAcademicYearId() : null);
            dto.setStandardId(t.getAddStandard() != null ? t.getAddStandard().getStandardId() : null);
            dto.setDivisionId(t.getAddDivision() != null ? t.getAddDivision().getDivisionId() : null);

            // Teacher-specific
            dto.setSalaryType(t.getSalaryType());
            dto.setJoiningDate(t.getJoiningDate());
            dto.setAnnualSalary(t.getAnnualSalary());
            dto.setMonthlySalary(t.getMonthlySalary());
            dto.setPfStatus(t.getPfStatus());
            dto.setPfAccount(t.getPfAccount());
            dto.setPaidInHandSalary(t.getPaidInHandSalary());
            dto.setAadharCardNumber(t.getAadharCardNumber());

            users.add(dto);
        }

        // ── Fetch students ──────────────────────────────────────
        List<AddStudent> students = studentRepository
                .findBySchool_SchoolIdAndAcademicYear_AcademicYearId(schoolId, academicYearId);

        for (AddStudent s : students) {
            UserManagementDTO dto = new UserManagementDTO();

            // Common
            dto.setId(s.getId());
            dto.setUserType("student");

            // Build full name
            StringBuilder nameBuilder = new StringBuilder();
            if (s.getFirstName() != null) nameBuilder.append(s.getFirstName());
            if (s.getMiddleName() != null) nameBuilder.append(" ").append(s.getMiddleName());
            if (s.getLastName() != null) nameBuilder.append(" ").append(s.getLastName());
            dto.setName(nameBuilder.toString().trim());

            dto.setEmail(s.getEmailId());
            dto.setPhone(s.getMobileNumber());
            dto.setRole(s.getRole() != null ? s.getRole().getName() : "student");
            dto.setStatus(s.getStatus());
            dto.setGender(s.getGender());
            dto.setDateOfBirth(s.getDateOfBirth());
            dto.setAddress(s.getAddress());
            dto.setProfilePhoto(s.getProfilePhoto());
            dto.setCreatedAt(s.getCreatedAt());

            // Context
            dto.setSchoolId(s.getSchool() != null ? s.getSchool().getSchoolId() : null);
            dto.setAcademicYearId(s.getAcademicYear() != null ? s.getAcademicYear().getAcademicYearId() : null);
            dto.setStandardId(s.getAddStandard() != null ? s.getAddStandard().getStandardId() : null);
            dto.setDivisionId(s.getAddDivision() != null ? s.getAddDivision().getDivisionId() : null);

            // Student-specific
            dto.setStudentId(s.getStudentId());
            dto.setPrnNumber(s.getPrnNumber());
            dto.setRollNumber(s.getRollNumber());
            dto.setFirstName(s.getFirstName());
            dto.setMiddleName(s.getMiddleName());
            dto.setLastName(s.getLastName());
            dto.setGuardianName(s.getGuardianName());
            dto.setParentMobileNumber(s.getParentMobileNumber());
            dto.setParentEmailId(s.getParentEmailId());
            dto.setAdmissionDate(s.getAdmissionDate());
            dto.setFeeType(s.getFeeType());
            dto.setTotalFee(s.getTotalFee());
            dto.setDiscount(s.getDiscount());
            dto.setFinalFee(s.getFinalFee());
            dto.setAadharNumber(s.getAadharNumber());

            users.add(dto);
        }

        // Sort by createdAt descending (newest first), nulls last
        users.sort(Comparator.comparing(
                UserManagementDTO::getCreatedAt,
                Comparator.nullsLast(Comparator.reverseOrder())));

        return users;
    }
}
