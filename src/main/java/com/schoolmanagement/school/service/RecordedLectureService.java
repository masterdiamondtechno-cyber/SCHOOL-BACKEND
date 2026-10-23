package com.schoolmanagement.school.service;

import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartFile;

import com.schoolmanagement.school.dto.LectureViewDTO;
import com.schoolmanagement.school.dto.RecordedLectureDTO;

public interface RecordedLectureService {

    // Upload a new recorded lecture (S3 storage)
    void createLecture(RecordedLectureDTO dto, MultipartFile file, String uploadedByRole);

    // Get a single lecture by DB id
    RecordedLectureDTO getLectureById(Long id);

    // Get all lectures for a school — optional filters for status, subject, and specific lectureId
    List<RecordedLectureDTO> getAllBySchool(Long schoolId, Long academicYearId,
                                            String status, Long subjectId, 
                                            Long lectureId, Long requestingTeacherId);

    // Get lectures for a specific division (used by STUDENT role — Published only)
    List<RecordedLectureDTO> getByDivision(Long divisionId, Long academicYearId);

    // Get lectures uploaded by a specific teacher (used by TEACHER role)
    List<RecordedLectureDTO> getByTeacher(Long teacherId, Long schoolId);

    // Update metadata; optionally replace the video file if a new one is provided
    void updateLecture(RecordedLectureDTO dto, MultipartFile file,
                       Long requestingTeacherId, String requestingRole);

    // Delete lecture record + S3 object
    void deleteLecture(Long id, Long requestingTeacherId, String requestingRole);

    // Generate secure stream link and record student view (if student)
    String streamLecture(Long id, Long schoolId, Long academicYearId, 
                         Long requestingTeacherId, String role, 
                         Long studentDivisionId, String studentEmail);

    // Record a student view when streaming
    void recordView(Long lectureId, Long studentId);

    // Get list of students who viewed a specific lecture (optionally filtered by school/year)
    List<LectureViewDTO> getLectureViewers(Long schoolId, Long academicYearId, Long lectureId);

    // Stat cards: totalLectures, published, totalViews
    Map<String, Object> getStats(Long schoolId, Long academicYearId);
}
