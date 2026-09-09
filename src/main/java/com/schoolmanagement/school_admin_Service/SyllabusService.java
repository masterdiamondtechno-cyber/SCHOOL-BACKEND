package com.schoolmanagement.school_admin_Service;

import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartFile;

import com.schoolmanagement.school_admin_DTO.SyllabusDTO;

public interface SyllabusService {

    SyllabusDTO createSyllabus(SyllabusDTO dto, MultipartFile file);

    SyllabusDTO getSyllabusById(Long id, Long schoolId);

    List<SyllabusDTO> getAllSyllabus(Long schoolId, Long academicYearId, String status);

    void updateSyllabus(SyllabusDTO dto, MultipartFile file);

    void deleteSyllabus(Long id, Long schoolId);

    SyllabusDTO updateProgress(Long id, Long schoolId, Integer completedChapters);

    Map<String, Object> getStats(Long schoolId, Long academicYearId);

    String downloadSyllabus(Long id, Long schoolId);

    void updateStatus(Long id, Long schoolId, String status);
}
