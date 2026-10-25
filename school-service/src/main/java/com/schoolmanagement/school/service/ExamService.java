package com.schoolmanagement.school.service;

import java.util.List;
import com.schoolmanagement.school.dto.ExamDTO;
import com.schoolmanagement.school.dto.ExamMarksDTO;
import com.schoolmanagement.school.dto.HallTicketDTO;

public interface ExamService {
    void createExam(ExamDTO dto);
    List<ExamDTO> getAllBySchoolId(Long schoolId, Long academicYearId);
    List<ExamDTO> getByDivision(Long schoolId, Long standardId, Long divisionId, Long academicYearId);
    ExamDTO getById(Long examId, Long schoolId);
    void updateExam(ExamDTO dto);
    void deleteExam(Long examId, Long schoolId);
    void saveMarksBulk(ExamMarksDTO dto);
    List<ExamMarksDTO> getMarksByExam(Long examId, Long schoolId, Long academicYearId);
    List<ExamMarksDTO> getMarksByStudent(Long examId, Long studentId, Long schoolId, Long academicYearId);

    // Student-facing APIs
    List<ExamDTO> getExamsByStudent(Long schoolId, Long academicYearId, Long studentId);
    HallTicketDTO getHallTicket(Long schoolId, Long academicYearId, Long studentId, Long examId);
}

