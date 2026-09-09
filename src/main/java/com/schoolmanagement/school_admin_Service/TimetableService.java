package com.schoolmanagement.school_admin_Service;

import java.util.List;

import com.schoolmanagement.school_admin_DTO.DivisionTimetableResponseDTO;
import com.schoolmanagement.school_admin_DTO.BulkCreateResponseDTO;
import com.schoolmanagement.school_admin_DTO.ClassTimetableResponseDTO;
import com.schoolmanagement.school_admin_DTO.SchoolTimetableResponseDTO;
import com.schoolmanagement.school_admin_DTO.TeacherTimetableResponseDTO;
import com.schoolmanagement.school_admin_DTO.TeacherAppTimetableResponseDTO;
import com.schoolmanagement.school_admin_DTO.StudentAppTimetableResponseDTO;
import com.schoolmanagement.school_admin_DTO.TimetableDTO;

public interface TimetableService {
    TimetableDTO create(TimetableDTO dto);
    BulkCreateResponseDTO createBulk(List<TimetableDTO> entries);
    List<TimetableDTO> getByDivision(Long schoolId, Long standardId, Long divisionId, Long academicYearId);
    DivisionTimetableResponseDTO getByDivisionStructured(Long schoolId, Long standardId, Long divisionId, Long academicYearId);
    List<TimetableDTO> getByTeacher(Long schoolId, Long teacherId, Long academicYearId);
    TeacherTimetableResponseDTO getTeacherDashboard(Long schoolId, Long teacherId, Long academicYearId, String date);
    Object getByClass(Long schoolId, Long teacherId, Long standardId, Long divisionId, Long academicYearId, String date, String type);
    SchoolTimetableResponseDTO getBySchool(Long schoolId, Long academicYearId, String date);
    TimetableDTO update(TimetableDTO dto);
    void delete(Long timetableId, Long schoolId);
    TeacherAppTimetableResponseDTO getTeacherAppTimetable(String type, String date, Long teacherId, Long schoolId, Long academicYearId);
    StudentAppTimetableResponseDTO getStudentTimetable(String type, String date, Long studentId, Long schoolId, Long academicYearId);
}