package com.schoolmanagement.school.serviceImpl;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.schoolmanagement.school.dto.AttendanceDTO;
import com.schoolmanagement.school.entity.*;
import com.schoolmanagement.school.repository.*;
import com.schoolmanagement.school.service.AttendanceService;

@Service
public class AttendanceServiceImpl implements AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final SchoolRepository schoolRepository;
    private final AddStandardRepository standardRepository;
    private final AddDivisionRepository divisionRepository;
    private final AddSubjectRepository subjectRepository;
    private final AddStudentRepository studentRepository;
    private final AddTeacherRepository teacherRepository;
    private final AcademicYearRepository academicYearRepository;

    public AttendanceServiceImpl(AttendanceRepository attendanceRepository, SchoolRepository schoolRepository,
                                 AddStandardRepository standardRepository, AddDivisionRepository divisionRepository,
                                 AddSubjectRepository subjectRepository, AddStudentRepository studentRepository,
                                 AddTeacherRepository teacherRepository, AcademicYearRepository academicYearRepository) {
        this.attendanceRepository = attendanceRepository;
        this.schoolRepository = schoolRepository;
        this.standardRepository = standardRepository;
        this.divisionRepository = divisionRepository;
        this.subjectRepository = subjectRepository;
        this.studentRepository = studentRepository;
        this.teacherRepository = teacherRepository;
        this.academicYearRepository = academicYearRepository;
    }

    // ==================== ENTITY → DTO HELPER ====================

    private AttendanceDTO toDTO(Attendance att) {
        AttendanceDTO dto = new AttendanceDTO();
        dto.setAttendanceId(att.getAttendanceId());
        dto.setSchoolId(att.getSchool().getSchoolId());
        if (att.getAcademicYear() != null)
            dto.setAcademicYearId(att.getAcademicYear().getAcademicYearId());
        dto.setStandardId(att.getAddStandard().getStandardId());
        dto.setDivisionId(att.getAddDivision().getDivisionId());
        dto.setSubjectId(att.getAddSubject().getSubjectId());
        dto.setStudentId(att.getAddStudent().getId());
        if (att.getMarkedByTeacher() != null)
            dto.setMarkedBy(att.getMarkedByTeacher().getId());
        dto.setAttendanceDate(att.getAttendanceDate());
        dto.setStatus(att.getStatus());
        dto.setRemark(att.getRemark());
        dto.setMarkedAt(att.getMarkedAt());
        return dto;
    }

    // ==================== MARK BULK (PER LECTURE) ====================

    @Override
    public void markBulk(AttendanceDTO dto) {
        School school = schoolRepository.findBySchoolId(dto.getSchoolId())
                .orElseThrow(() -> new RuntimeException("School not found"));
        AddStandard standard = standardRepository.findByStandardId(dto.getStandardId())
                .orElseThrow(() -> new RuntimeException("Standard not found"));
        AddDivision division = divisionRepository.findByDivisionId(dto.getDivisionId())
                .orElseThrow(() -> new RuntimeException("Division not found"));
        AddSubject subject = subjectRepository.findBySubjectId(dto.getSubjectId())
                .orElseThrow(() -> new RuntimeException("Subject not found"));

        AcademicYear academicYear = academicYearRepository.findById(dto.getAcademicYearId())
                .orElseThrow(() -> new RuntimeException("Academic Year not found"));

        AddTeacher markedBy = null;
        if (dto.getMarkedBy() != null) {
            markedBy = teacherRepository.findById(dto.getMarkedBy())
                    .orElseThrow(() -> new RuntimeException("Teacher (markedBy) not found"));
        }

        LocalDate date = dto.getAttendanceDate() != null ? dto.getAttendanceDate() : LocalDate.now();

        for (AttendanceDTO.AttendanceEntryDTO entry : dto.getEntries()) {
            // Check if already marked for this student + date + subject
            Optional<Attendance> existing = attendanceRepository
                    .findBySchool_SchoolIdAndAddStudent_IdAndAttendanceDateAndAddSubject_SubjectId(
                            dto.getSchoolId(), entry.getStudentId(), date, dto.getSubjectId());

            Attendance att;
            if (existing.isPresent()) {
                att = existing.get(); // Update existing record
            } else {
                att = new Attendance();
                att.setSchool(school);
                att.setAddStandard(standard);
                att.setAddDivision(division);
                att.setAddSubject(subject);
                att.setAcademicYear(academicYear);
                att.setAttendanceDate(date);

                AddStudent student = studentRepository.findById(entry.getStudentId())
                        .orElseThrow(() -> new RuntimeException("Student not found: " + entry.getStudentId()));
                att.setAddStudent(student);
            }

            att.setStatus(entry.getStatus());
            att.setRemark(entry.getRemark());
            att.setMarkedByTeacher(markedBy);
            attendanceRepository.save(att);
        }
    }

    // ==================== GET METHODS ====================

    @Override
    public List<AttendanceDTO> getByDateAndSubject(Long schoolId, Long standardId, Long divisionId, LocalDate date, Long subjectId, Long academicYearId) {
        return attendanceRepository
                .findBySchool_SchoolIdAndAddStandard_StandardIdAndAddDivision_DivisionIdAndAttendanceDateAndAddSubject_SubjectIdAndAcademicYear_AcademicYearId(
                        schoolId, standardId, divisionId, date, subjectId, academicYearId)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<AttendanceDTO> getByDate(Long schoolId, Long standardId, Long divisionId, LocalDate date, Long academicYearId) {
        return attendanceRepository
                .findBySchool_SchoolIdAndAddStandard_StandardIdAndAddDivision_DivisionIdAndAttendanceDateAndAcademicYear_AcademicYearId(
                        schoolId, standardId, divisionId, date, academicYearId)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<AttendanceDTO> getByStudent(Long schoolId, Long studentId, LocalDate from, LocalDate to, Long academicYearId) {
        return attendanceRepository
                .findBySchool_SchoolIdAndAddStudent_IdAndAttendanceDateBetweenAndAcademicYear_AcademicYearId(schoolId, studentId, from, to, academicYearId)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public void update(AttendanceDTO dto) {
        Attendance att = attendanceRepository.findById(dto.getAttendanceId())
                .orElseThrow(() -> new RuntimeException("Attendance record not found"));
        if (dto.getStatus() != null) att.setStatus(dto.getStatus());
        if (dto.getRemark() != null) att.setRemark(dto.getRemark());
        attendanceRepository.save(att);
    }

    @Override
    public long countTodayPresent(Long schoolId, Long academicYearId) {
        return attendanceRepository.countBySchool_SchoolIdAndAttendanceDateAndStatusAndAcademicYear_AcademicYearId(
                schoolId, LocalDate.now(), "PRESENT", academicYearId);
    }

    @Override
    public long countTodayTotal(Long schoolId, Long academicYearId) {
        return attendanceRepository.countBySchool_SchoolIdAndAttendanceDateAndAcademicYear_AcademicYearId(schoolId, LocalDate.now(), academicYearId);
    }
}
