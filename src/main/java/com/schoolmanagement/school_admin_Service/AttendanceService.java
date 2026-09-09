package com.schoolmanagement.school_admin_Service;

import java.time.LocalDate;
import java.util.List;
import com.schoolmanagement.school_admin_DTO.AttendanceDTO;

public interface AttendanceService {
    void markBulk(AttendanceDTO dto);
    List<AttendanceDTO> getByDateAndSubject(Long schoolId, Long standardId, Long divisionId, LocalDate date, Long subjectId, Long academicYearId);
    List<AttendanceDTO> getByDate(Long schoolId, Long standardId, Long divisionId, LocalDate date, Long academicYearId);
    List<AttendanceDTO> getByStudent(Long schoolId, Long studentId, LocalDate from, LocalDate to, Long academicYearId);
    void update(AttendanceDTO dto);
    long countTodayPresent(Long schoolId, Long academicYearId);
    long countTodayTotal(Long schoolId, Long academicYearId);
}
