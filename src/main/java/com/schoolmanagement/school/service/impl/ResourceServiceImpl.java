package com.schoolmanagement.school.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.schoolmanagement.school.dto.ResourceDTO;
import com.schoolmanagement.school.entity.AcademicYear;
import com.schoolmanagement.school.entity.AddStandard;
import com.schoolmanagement.school.entity.AddSubject;
import com.schoolmanagement.school.entity.Resource;
import com.schoolmanagement.school.entity.School;
import com.schoolmanagement.school.repository.AcademicYearRepository;
import com.schoolmanagement.school.repository.AddStandardRepository;
import com.schoolmanagement.school.repository.AddSubjectRepository;
import com.schoolmanagement.school.repository.ResourceRepository;
import com.schoolmanagement.school.repository.SchoolRepository;
import com.schoolmanagement.school.service.ResourceService;

@Service
public class ResourceServiceImpl implements ResourceService {

    private final ResourceRepository resourceRepository;
    private final SchoolRepository schoolRepository;
    private final AddStandardRepository standardRepository;
    private final AddSubjectRepository subjectRepository;
    private final AcademicYearRepository academicYearRepository;

    public ResourceServiceImpl(ResourceRepository resourceRepository,
                                SchoolRepository schoolRepository,
                                AddStandardRepository standardRepository,
                                AddSubjectRepository subjectRepository,
                                AcademicYearRepository academicYearRepository) {
        this.resourceRepository = resourceRepository;
        this.schoolRepository = schoolRepository;
        this.standardRepository = standardRepository;
        this.subjectRepository = subjectRepository;
        this.academicYearRepository = academicYearRepository;
    }

    // ==================== ENTITY → DTO CONVERTER ====================
    private ResourceDTO toDTO(Resource r) {
        ResourceDTO dto = new ResourceDTO();
        dto.setId(r.getId());
        dto.setTitle(r.getTitle());
        dto.setResourceType(r.getResourceType());
        dto.setFileUrl(r.getFileUrl());
        dto.setUploadedBy(r.getUploadedBy());
        dto.setCreatedAt(r.getCreatedAt());

        if (r.getSchool() != null) dto.setSchoolId(r.getSchool().getSchoolId());
        if (r.getAcademicYear() != null) dto.setAcademicYearId(r.getAcademicYear().getAcademicYearId());

        if (r.getAddSubject() != null) {
            dto.setSubjectId(r.getAddSubject().getSubjectId());
            dto.setSubjectName(r.getAddSubject().getSubjectName());
        }
        if (r.getAddStandard() != null) {
            dto.setStandardId(r.getAddStandard().getStandardId());
            dto.setStandardName(r.getAddStandard().getStandard());
        }

        return dto;
    }

    @Override
    public void createResource(ResourceDTO dto) {
        Resource resource = new Resource();

        // 1. Validate school
        School school = schoolRepository.findBySchoolId(dto.getSchoolId())
                .orElseThrow(() -> new RuntimeException("School not found"));
        resource.setSchool(school);

        // 2. Validate academicYear belongs to school
        if (dto.getAcademicYearId() != null) {
            AcademicYear ay = academicYearRepository.findByAcademicYearIdAndSchool_SchoolId(dto.getAcademicYearId(), dto.getSchoolId())
                    .orElseThrow(() -> new RuntimeException("Academic Year not found for this school"));
            resource.setAcademicYear(ay);
        }

        // 3. Validate standard belongs to school
        if (dto.getStandardId() != null) {
            AddStandard standard = standardRepository.findByStandardId(dto.getStandardId())
                    .orElseThrow(() -> new RuntimeException("Standard not found"));
            if (!standard.getSchool().getSchoolId().equals(dto.getSchoolId())) {
                throw new RuntimeException("Standard does not belong to this school");
            }
            resource.setAddStandard(standard);
        }

        // 4. Validate subject belongs to school
        if (dto.getSubjectId() != null) {
            AddSubject subject = subjectRepository.findBySubjectId(dto.getSubjectId())
                    .orElseThrow(() -> new RuntimeException("Subject not found"));
            if (!subject.getSchool().getSchoolId().equals(dto.getSchoolId())) {
                throw new RuntimeException("Subject does not belong to this school");
            }
            resource.setAddSubject(subject);
        }

        resource.setTitle(dto.getTitle());
        resource.setResourceType(dto.getResourceType());
        resource.setFileUrl(dto.getFileUrl());
        resource.setUploadedBy(dto.getUploadedBy());

        resourceRepository.save(resource);
    }

    @Override
    public ResourceDTO getResourceById(Long id) {
        Resource resource = resourceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Resource not found"));
        return toDTO(resource);
    }

    @Override
    public void updateResource(ResourceDTO dto) {
        Resource resource = resourceRepository.findById(dto.getId())
                .orElseThrow(() -> new RuntimeException("Resource not found"));

        if (dto.getTitle() != null) resource.setTitle(dto.getTitle());
        if (dto.getResourceType() != null) resource.setResourceType(dto.getResourceType());
        if (dto.getFileUrl() != null) resource.setFileUrl(dto.getFileUrl());
        if (dto.getUploadedBy() != null) resource.setUploadedBy(dto.getUploadedBy());

        if (dto.getSubjectId() != null) {
            resource.setAddSubject(subjectRepository.findBySubjectId(dto.getSubjectId()).orElse(null));
        }
        if (dto.getStandardId() != null) {
            resource.setAddStandard(standardRepository.findByStandardId(dto.getStandardId()).orElse(null));
        }

        resourceRepository.save(resource);
    }

    @Override
    public void deleteResource(Long id) {
        Resource resource = resourceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Resource not found"));
        resourceRepository.delete(resource);
    }

    @Override
    public List<ResourceDTO> getResourcesBySchool(Long schoolId, Long academicYearId) {
        return resourceRepository.findBySchool_SchoolIdAndAcademicYear_AcademicYearId(schoolId, academicYearId)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<ResourceDTO> getResourcesByStandard(Long schoolId, Long academicYearId, Long standardId) {
        return resourceRepository.findBySchool_SchoolIdAndAcademicYear_AcademicYearIdAndAddStandard_StandardId(schoolId, academicYearId, standardId)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<ResourceDTO> getResourcesBySubject(Long schoolId, Long academicYearId, Long subjectId) {
        return resourceRepository.findBySchool_SchoolIdAndAcademicYear_AcademicYearIdAndAddSubject_SubjectId(schoolId, academicYearId, subjectId)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<ResourceDTO> getResourcesByStandardAndSubject(Long schoolId, Long standardId, Long subjectId) {
        return resourceRepository.findBySchool_SchoolIdAndAddStandard_StandardIdAndAddSubject_SubjectId(
                        schoolId, standardId, subjectId)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<ResourceDTO> getResourcesByType(Long schoolId, Long academicYearId, String resourceType) {
        return resourceRepository.findBySchool_SchoolIdAndAcademicYear_AcademicYearIdAndResourceType(schoolId, academicYearId, resourceType)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }
}

