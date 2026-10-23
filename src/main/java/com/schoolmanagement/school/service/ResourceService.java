package com.schoolmanagement.school.service;

import java.util.List;

import com.schoolmanagement.school.dto.ResourceDTO;

public interface ResourceService {

    void createResource(ResourceDTO dto);
    ResourceDTO getResourceById(Long id);
    void updateResource(ResourceDTO dto);
    void deleteResource(Long id);
    List<ResourceDTO> getResourcesBySchool(Long schoolId, Long academicYearId);
    List<ResourceDTO> getResourcesByStandard(Long schoolId, Long academicYearId, Long standardId);
    List<ResourceDTO> getResourcesBySubject(Long schoolId, Long academicYearId, Long subjectId);
    List<ResourceDTO> getResourcesByStandardAndSubject(Long schoolId, Long standardId, Long subjectId);
    List<ResourceDTO> getResourcesByType(Long schoolId, Long academicYearId, String resourceType);
}
