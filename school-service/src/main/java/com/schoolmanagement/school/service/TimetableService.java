package com.schoolmanagement.school.service;

import java.util.List;

import com.schoolmanagement.school.dto.DivisionTimetableResponseDTO;
import com.schoolmanagement.school.dto.BulkCreateResponseDTO;
import com.schoolmanagement.school.dto.ClassTimetableResponseDTO;
import com.schoolmanagement.school.dto.SchoolTimetableResponseDTO;
import com.schoolmanagement.school.dto.TeacherTimetableResponseDTO;
import com.schoolmanagement.school.dto.TeacherAppTimetableResponseDTO;
import com.schoolmanagement.school.dto.StudentAppTimetableResponseDTO;
import com.schoolmanagement.school.dto.TimetableDTO;

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