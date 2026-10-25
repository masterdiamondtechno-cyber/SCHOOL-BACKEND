package com.schoolmanagement.school.service;

import java.io.ByteArrayInputStream;
import java.util.Map;

import org.springframework.web.multipart.MultipartFile;

public interface ExcelService {

    ByteArrayInputStream generateTeacherTemplate();

    ByteArrayInputStream generateStudentTemplate();

    Map<String, Object> uploadTeachers(MultipartFile file, Long schoolId, Long academicYearId, Long standardId, Long divisionId);

    Map<String, Object> uploadStudents(MultipartFile file, Long schoolId, Long academicYearId, Long standardId, Long divisionId);
}
