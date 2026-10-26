package com.schoolmanagement.school.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.schoolmanagement.school.dto.StudyGroupDTO;
import com.schoolmanagement.school.dto.StudyGroupMemberDTO;
import com.schoolmanagement.school.entity.AcademicYear;
import com.schoolmanagement.school.entity.AddDivision;
import com.schoolmanagement.school.entity.AddStandard;
import com.schoolmanagement.school.entity.AddStudent;
import com.schoolmanagement.school.entity.AddTeacher;
import com.schoolmanagement.school.entity.School;
import com.schoolmanagement.school.entity.StudyGroup;
import com.schoolmanagement.school.entity.StudyGroupMember;
import com.schoolmanagement.school.repository.AcademicYearRepository;
import com.schoolmanagement.school.repository.AddDivisionRepository;
import com.schoolmanagement.school.repository.AddStandardRepository;
import com.schoolmanagement.school.repository.AddStudentRepository;
import com.schoolmanagement.school.repository.AddTeacherRepository;
import com.schoolmanagement.school.repository.SchoolRepository;
import com.schoolmanagement.school.repository.StudyGroupMemberRepository;
import com.schoolmanagement.school.repository.StudyGroupRepository;
import com.schoolmanagement.school.service.StudyGroupService;

@Service
public class StudyGroupServiceImpl implements StudyGroupService {

    private final StudyGroupRepository studyGroupRepository;
    private final StudyGroupMemberRepository memberRepository;
    private final SchoolRepository schoolRepository;
    private final AddTeacherRepository teacherRepository;
    private final AddStudentRepository studentRepository;
    private final AddStandardRepository standardRepository;
    private final AddDivisionRepository divisionRepository;
    private final AcademicYearRepository academicYearRepository;

    public StudyGroupServiceImpl(StudyGroupRepository studyGroupRepository,
                                  StudyGroupMemberRepository memberRepository,
                                  SchoolRepository schoolRepository,
                                  AddTeacherRepository teacherRepository,
                                  AddStudentRepository studentRepository,
                                  AddStandardRepository standardRepository,
                                  AddDivisionRepository divisionRepository,
                                  AcademicYearRepository academicYearRepository) {
        this.studyGroupRepository = studyGroupRepository;
        this.memberRepository = memberRepository;
        this.schoolRepository = schoolRepository;
        this.teacherRepository = teacherRepository;
        this.studentRepository = studentRepository;
        this.standardRepository = standardRepository;
        this.divisionRepository = divisionRepository;
        this.academicYearRepository = academicYearRepository;
    }

    // ==================== ENTITY → DTO CONVERTER ====================
    private StudyGroupDTO toGroupDTO(StudyGroup group) {
        StudyGroupDTO dto = new StudyGroupDTO();
        dto.setId(group.getId());
        dto.setGroupName(group.getGroupName());
        dto.setDescription(group.getDescription());
        dto.setIsActive(group.getIsActive());
        dto.setCreatedAt(group.getCreatedAt());

        if (group.getCreatedBy() != null) {
            dto.setCreatedByTeacherId(group.getCreatedBy().getId());
            dto.setCreatedByTeacherName(group.getCreatedBy().getTeacherName());
        }
        if (group.getAddStandard() != null) {
            dto.setStandardId(group.getAddStandard().getStandardId());
            dto.setStandardName(group.getAddStandard().getStandard());
        }
        if (group.getAddDivision() != null) {
            dto.setDivisionId(group.getAddDivision().getDivisionId());
            dto.setDivisionName(group.getAddDivision().getDivisionName());
        }
        if (group.getSchool() != null) {
            dto.setSchoolId(group.getSchool().getSchoolId());
        }
        if (group.getAcademicYear() != null) {
            dto.setAcademicYearId(group.getAcademicYear().getAcademicYearId());
        }

        Long count = memberRepository.countByStudyGroup_Id(group.getId());
        dto.setMemberCount(count);

        return dto;
    }

    private StudyGroupMemberDTO toMemberDTO(StudyGroupMember member) {
        StudyGroupMemberDTO dto = new StudyGroupMemberDTO();
        dto.setId(member.getId());
        dto.setStudyGroupId(member.getStudyGroup().getId());
        dto.setStudentId(member.getStudent().getId());
        dto.setStudentName(member.getStudent().getFirstName() + " " + member.getStudent().getLastName());
        dto.setRole(member.getRole());
        dto.setJoinedAt(member.getJoinedAt());
        return dto;
    }

    // ==================== CREATE (Teacher creates group) ====================
    @Override
    public void createStudyGroup(StudyGroupDTO dto) {
        StudyGroup group = new StudyGroup();

        // 1. Validate school
        School school = schoolRepository.findBySchoolId(dto.getSchoolId())
                .orElseThrow(() -> new RuntimeException("School not found"));
        group.setSchool(school);

        // 2. Validate teacher belongs to school
        AddTeacher teacher = teacherRepository.findById(dto.getCreatedByTeacherId())
                .orElseThrow(() -> new RuntimeException("Teacher not found"));
        if (!teacher.getSchool().getSchoolId().equals(dto.getSchoolId())) {
            throw new RuntimeException("Teacher does not belong to this school");
        }
        group.setCreatedBy(teacher);

        // 3. Validate academicYear belongs to school
        if (dto.getAcademicYearId() != null) {
            AcademicYear ay = academicYearRepository.findByAcademicYearIdAndSchool_SchoolId(dto.getAcademicYearId(), dto.getSchoolId())
                    .orElseThrow(() -> new RuntimeException("Academic Year not found for this school"));
            group.setAcademicYear(ay);
        }

        // 4. Validate standard belongs to school
        if (dto.getStandardId() != null) {
            AddStandard standard = standardRepository.findByStandardId(dto.getStandardId())
                    .orElseThrow(() -> new RuntimeException("Standard not found"));
            if (!standard.getSchool().getSchoolId().equals(dto.getSchoolId())) {
                throw new RuntimeException("Standard does not belong to this school");
            }
            group.setAddStandard(standard);
        }

        // 5. Validate division belongs to school
        if (dto.getDivisionId() != null) {
            AddDivision division = divisionRepository.findById(dto.getDivisionId())
                    .orElseThrow(() -> new RuntimeException("Division not found"));
            if (!division.getSchool().getSchoolId().equals(dto.getSchoolId())) {
                throw new RuntimeException("Division does not belong to this school");
            }
            group.setAddDivision(division);
        }

        group.setGroupName(dto.getGroupName());
        group.setDescription(dto.getDescription());
        group.setIsActive(true);

        StudyGroup saved = studyGroupRepository.save(group);
        dto.setId(saved.getId());
    }

    // ==================== GET BY ID ====================
    @Override
    public StudyGroupDTO getStudyGroupById(Long id) {
        StudyGroup group = studyGroupRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Study Group not found"));
        return toGroupDTO(group);
    }

    // ==================== UPDATE ====================
    @Override
    public void updateStudyGroup(StudyGroupDTO dto) {
        StudyGroup group = studyGroupRepository.findById(dto.getId())
                .orElseThrow(() -> new RuntimeException("Study Group not found"));

        if (dto.getGroupName() != null) group.setGroupName(dto.getGroupName());
        if (dto.getDescription() != null) group.setDescription(dto.getDescription());

        studyGroupRepository.save(group);
    }

    // ==================== DEACTIVATE ====================
    @Override
    public void deactivateStudyGroup(Long id) {
        StudyGroup group = studyGroupRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Study Group not found"));
        group.setIsActive(false);
        studyGroupRepository.save(group);
    }

    // ==================== LIST ACTIVE BY SCHOOL + ACADEMIC YEAR ====================
    @Override
    public List<StudyGroupDTO> getActiveGroupsBySchool(Long schoolId, Long academicYearId) {
        return studyGroupRepository.findBySchool_SchoolIdAndAcademicYear_AcademicYearIdAndIsActive(schoolId, academicYearId, true)
                .stream().map(this::toGroupDTO).collect(Collectors.toList());
    }

    // ==================== LIST BY STANDARD ====================
    @Override
    public List<StudyGroupDTO> getGroupsByStandard(Long schoolId, Long academicYearId, Long standardId) {
        return studyGroupRepository.findBySchool_SchoolIdAndAcademicYear_AcademicYearIdAndAddStandard_StandardIdAndIsActive(
                        schoolId, academicYearId, standardId, true)
                .stream().map(this::toGroupDTO).collect(Collectors.toList());
    }

    // ==================== ADD STUDENT TO GROUP (Teacher adds) ====================
    @Override
    public void addStudentToGroup(StudyGroupMemberDTO dto) {
        StudyGroup group = studyGroupRepository.findById(dto.getStudyGroupId())
                .orElseThrow(() -> new RuntimeException("Study Group not found"));

        // Check group is active
        if (!group.getIsActive()) {
            throw new RuntimeException("Study Group is not active");
        }

        AddStudent student = studentRepository.findById(dto.getStudentId())
                .orElseThrow(() -> new RuntimeException("Student not found"));

        // Validate student belongs to same school as group
        if (!student.getSchool().getSchoolId().equals(group.getSchool().getSchoolId())) {
            throw new RuntimeException("Student does not belong to the same school as this group");
        }

        // Check if already a member
        if (memberRepository.existsByStudyGroup_IdAndStudent_Id(dto.getStudyGroupId(), dto.getStudentId())) {
            throw new RuntimeException("Student is already a member of this group");
        }

        StudyGroupMember member = new StudyGroupMember();
        member.setStudyGroup(group);
        member.setStudent(student);
        member.setSchool(group.getSchool());
        member.setAcademicYear(group.getAcademicYear());
        member.setRole("MEMBER");

        memberRepository.save(member);
    }

    // ==================== BULK ADD STUDENTS TO GROUP ====================
    @Override
    public void addStudentsToGroup(StudyGroupMemberDTO dto) {
        StudyGroup group = studyGroupRepository.findById(dto.getStudyGroupId())
                .orElseThrow(() -> new RuntimeException("Study Group not found"));

        if (!group.getIsActive()) {
            throw new RuntimeException("Study Group is not active");
        }

        int added = 0;
        int skipped = 0;

        for (Long studentId : dto.getStudentIds()) {
            // Skip if already a member
            if (memberRepository.existsByStudyGroup_IdAndStudent_Id(dto.getStudyGroupId(), studentId)) {
                skipped++;
                continue;
            }

            AddStudent student = studentRepository.findById(studentId)
                    .orElseThrow(() -> new RuntimeException("Student with ID " + studentId + " not found"));

            // Validate same school
            if (!student.getSchool().getSchoolId().equals(group.getSchool().getSchoolId())) {
                throw new RuntimeException("Student ID " + studentId + " does not belong to the same school as this group");
            }

            StudyGroupMember member = new StudyGroupMember();
            member.setStudyGroup(group);
            member.setStudent(student);
            member.setSchool(group.getSchool());
            member.setAcademicYear(group.getAcademicYear());
            member.setRole("MEMBER");
            memberRepository.save(member);
            added++;
        }
    }

    // ==================== REMOVE STUDENT FROM GROUP ====================
    @Override
    public void removeStudentFromGroup(Long studyGroupId, Long studentId) {
        StudyGroupMember member = memberRepository.findByStudyGroup_IdAndStudent_Id(studyGroupId, studentId)
                .orElseThrow(() -> new RuntimeException("Student is not a member of this group"));
        memberRepository.delete(member);
    }

    // ==================== GET GROUP MEMBERS ====================
    @Override
    public List<StudyGroupMemberDTO> getGroupMembers(Long studyGroupId) {
        return memberRepository.findByStudyGroup_Id(studyGroupId)
                .stream().map(this::toMemberDTO).collect(Collectors.toList());
    }

    // ==================== GET GROUPS BY STUDENT ====================
    @Override
    public List<StudyGroupDTO> getGroupsByStudent(Long studentId, Long academicYearId) {
        List<StudyGroupMember> memberships = memberRepository
                .findByStudent_IdAndAcademicYear_AcademicYearId(studentId, academicYearId);

        return memberships.stream()
                .map(m -> toGroupDTO(m.getStudyGroup()))
                .collect(Collectors.toList());
    }
}

