package com.schoolmanagement.school_admin_Service;

import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartFile;

import com.schoolmanagement.school_admin_DTO.StudyMaterialDTO;

public interface StudyMaterialService {

    // Upload a new study material (S3 storage)
    // uploadedByRole: "SCHOOL" or "TEACHER"
    void createStudyMaterial(StudyMaterialDTO dto, MultipartFile file, String uploadedByRole);

    // Get a single material by DB id (used for edit modal prefill)
    StudyMaterialDTO getStudyMaterialById(Long id);

    // Get all materials for a school — materialType is optional (null = All Types)
    // schoolId context: for SCHOOL role, returns all in school
    List<StudyMaterialDTO> getAllBySchool(Long schoolId, Long academicYearId, String materialType);

    // Get materials for a specific division (used by STUDENT role)
    // Returns only "Published" materials for that division
    List<StudyMaterialDTO> getByDivision(Long divisionId, Long academicYearId);

    // Get materials uploaded by a specific teacher (used by TEACHER role)
    List<StudyMaterialDTO> getByTeacher(Long teacherId, Long schoolId);

    // Update metadata; optionally replace the file if a new one is provided
    // teacherId is passed for ownership check when role is TEACHER
    void updateStudyMaterial(StudyMaterialDTO dto, MultipartFile file, Long requestingTeacherId, String requestingRole);

    // Delete material record + S3 object
    // teacherId / role used for ownership check
    void deleteStudyMaterial(Long id, Long requestingTeacherId, String requestingRole);

    // Increment downloadCount and return a fresh S3 presigned download URL
    String downloadStudyMaterial(Long id, Long requestingTeacherId, String requestingRole, Long requestingDivisionId);

    // Stat cards: totalMaterials, published, totalDownloads
    Map<String, Object> getStats(Long schoolId, Long academicYearId);
}
