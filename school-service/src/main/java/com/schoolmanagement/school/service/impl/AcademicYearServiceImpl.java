package com.schoolmanagement.school.service.impl;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.schoolmanagement.school.dto.AcademicYearDTO;
import com.schoolmanagement.school.entity.AcademicYear;
import com.schoolmanagement.school.entity.School;
import com.schoolmanagement.school.entity.DashboardItem;
import com.schoolmanagement.school.repository.AcademicYearRepository;
import com.schoolmanagement.school.repository.SchoolRepository;
import com.schoolmanagement.school.repository.DashboardItemRepository;
import com.schoolmanagement.school.service.AcademicYearService;

@Service
public class AcademicYearServiceImpl implements AcademicYearService {

    private final AcademicYearRepository academicYearRepository;
    private final SchoolRepository schoolRepository;
    private final DashboardItemRepository dashboardItemRepository;

    public AcademicYearServiceImpl(AcademicYearRepository academicYearRepository,
                                   SchoolRepository schoolRepository,
                                   DashboardItemRepository dashboardItemRepository) {
        this.academicYearRepository = academicYearRepository;
        this.schoolRepository = schoolRepository;
        this.dashboardItemRepository = dashboardItemRepository;
    }

    private AcademicYearDTO mapToDTO(AcademicYear entity) {
        AcademicYearDTO dto = new AcademicYearDTO();
        dto.setAcademicYearId(entity.getAcademicYearId());
        dto.setSchoolId(entity.getSchool().getSchoolId());
        dto.setYearName(entity.getYearName());
        dto.setStartDate(entity.getStartDate());
        dto.setEndDate(entity.getEndDate());
        dto.setIsCurrent(entity.getIsCurrent());
        dto.setStatus(entity.getStatus());
        return dto;
    }

    @Override
    @Transactional
    public AcademicYearDTO create(AcademicYearDTO dto) {
        School school = schoolRepository.findBySchoolId(dto.getSchoolId())
                .orElseThrow(() -> new RuntimeException("School not found"));

        if (academicYearRepository.existsBySchool_SchoolIdAndYearName(dto.getSchoolId(), dto.getYearName())) {
            throw new RuntimeException("Academic year already exists for this school");
        }

        AcademicYear ay = new AcademicYear();
        ay.setSchool(school);
        ay.setYearName(dto.getYearName());
        ay.setStartDate(dto.getStartDate());
        ay.setEndDate(dto.getEndDate());

        String incomingStatus = dto.getStatus() != null ? dto.getStatus().trim().toUpperCase() : "ACTIVE";
        
        if ("ACTIVE".equals(incomingStatus)) {
            // Deactivate all others for this school
            List<AcademicYear> allYears = academicYearRepository.findBySchool_SchoolId(dto.getSchoolId());
            for (AcademicYear other : allYears) {
                other.setIsCurrent(false);
                other.setIsActive(false);
                if ("ACTIVE".equalsIgnoreCase(other.getStatus())) {
                    other.setStatus("COMPLETED");
                }
            }
            academicYearRepository.saveAll(allYears);

            ay.setIsCurrent(true);
            ay.setIsActive(true);
            ay.setStatus("ACTIVE");
        } else {
            ay.setIsCurrent(dto.getIsCurrent() != null ? dto.getIsCurrent() : false);
            ay.setIsActive(dto.getIsCurrent() != null ? dto.getIsCurrent() : false);
            ay.setStatus(incomingStatus);
        }

        AcademicYear savedAy = academicYearRepository.save(ay);
        seedDefaultDashboardItems(school, savedAy);

        return mapToDTO(savedAy);
    }

    private void seedDefaultDashboardItems(School school, AcademicYear academicYear) {
        String[] schoolWidgets = {
            "Student Progress Chart", "Teacher Attendance Report", "Standard wise Student Count",
            "Fee Collection Report", "Teacher Count", "Student Count", "School Info"
        };
        String[] teacherWidgets = {
            "Today Schedule", "Subject Wise Attendance", "Announcements"
        };
        String[] studentWidgets = {
            "Monthly Attendance", "Syllabus Progress", "Teachers Feedback", "Upcoming Assessments"
        };
        String[] parentWidgets = {
            "Ward Overview", "Ward Attendance Stats", "Now Ongoing",
            "Recent Academic Performance", "Parent Noticeboard", "Live Safety Tracker"
        };

        seedRoleWidgets(school, academicYear, "school", schoolWidgets);
        seedRoleWidgets(school, academicYear, "teacher", teacherWidgets);
        seedRoleWidgets(school, academicYear, "student", studentWidgets);
        seedRoleWidgets(school, academicYear, "parent", parentWidgets);
    }

    private void seedRoleWidgets(School school, AcademicYear academicYear, String role, String[] widgets) {
        for (String widgetName : widgets) {
            boolean exists = dashboardItemRepository
                .existsByDashboardItemNameAndRoleAndSchool_SchoolIdAndAcademicYear_AcademicYearId(
                    widgetName, role, school.getSchoolId(), academicYear.getAcademicYearId()
                );
            if (!exists) {
                DashboardItem item = new DashboardItem();
                item.setSchool(school);
                item.setAcademicYear(academicYear);
                item.setDashboardItemName(widgetName);
                item.setRole(role);
                dashboardItemRepository.save(item);
            }
        }
    }

    @Override
    public List<AcademicYearDTO> getAllBySchoolId(Long schoolId) {
        return academicYearRepository.findBySchool_SchoolId(schoolId)
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    @Override
    public AcademicYearDTO getById(Long academicYearId, Long schoolId) {
        AcademicYear entity = academicYearRepository.findByAcademicYearIdAndSchool_SchoolId(academicYearId, schoolId)
                .orElseThrow(() -> new RuntimeException("Academic Year not found"));
        return mapToDTO(entity);
    }

    @Override
    public AcademicYearDTO getCurrent(Long schoolId) {
        AcademicYear entity = academicYearRepository.findBySchool_SchoolIdAndIsCurrentTrue(schoolId)
                .orElseThrow(() -> new RuntimeException("No current academic year set"));
        return mapToDTO(entity);
    }

    @Override
    @Transactional
    public AcademicYearDTO update(AcademicYearDTO dto) {
        // Check school exists
        schoolRepository.findBySchoolId(dto.getSchoolId())
                .orElseThrow(() -> new RuntimeException("School not found"));
        // Check academic year exists
        AcademicYear ay = academicYearRepository.findById(dto.getAcademicYearId())
                .orElseThrow(() -> new RuntimeException("Academic Year not found"));
        // Check association
        if (!ay.getSchool().getSchoolId().equals(dto.getSchoolId())) {
            throw new RuntimeException("Academic Year does not belong to the given School");
        }

        if (dto.getYearName() != null) ay.setYearName(dto.getYearName());
        if (dto.getStartDate() != null) ay.setStartDate(dto.getStartDate());
        if (dto.getEndDate() != null) ay.setEndDate(dto.getEndDate());
        if (dto.getIsCurrent() != null) ay.setIsCurrent(dto.getIsCurrent());
        
        if (dto.getStatus() != null) {
            String incomingStatus = dto.getStatus().trim().toUpperCase();
            if ("ACTIVE".equals(incomingStatus)) {
                // Deactivate all others for this school
                List<AcademicYear> allYears = academicYearRepository.findBySchool_SchoolId(dto.getSchoolId());
                for (AcademicYear other : allYears) {
                    if (!other.getAcademicYearId().equals(ay.getAcademicYearId())) {
                        other.setIsCurrent(false);
                        other.setIsActive(false);
                        if ("ACTIVE".equalsIgnoreCase(other.getStatus())) {
                            other.setStatus("COMPLETED");
                        }
                    }
                }
                academicYearRepository.saveAll(allYears);

                ay.setIsCurrent(true);
                ay.setIsActive(true);
                ay.setStatus("ACTIVE");
            } else {
                ay.setIsCurrent(false);
                ay.setIsActive(false);
                ay.setStatus(incomingStatus);
            }
        }
        
        return mapToDTO(academicYearRepository.save(ay));
    }

    @Override
    public void delete(Long academicYearId, Long schoolId) {
        // Check school exists
        schoolRepository.findBySchoolId(schoolId)
                .orElseThrow(() -> new RuntimeException("School not found"));
        // Check academic year exists
        AcademicYear ay = academicYearRepository.findById(academicYearId)
                .orElseThrow(() -> new RuntimeException("Academic Year not found"));
        // Check association
        if (!ay.getSchool().getSchoolId().equals(schoolId)) {
            throw new RuntimeException("Academic Year does not belong to the given School");
        }
        academicYearRepository.delete(ay);
    }

    @Override
    @Transactional
    public void activate(Long academicYearId, Long schoolId) {
        // Find the academic year to activate
        AcademicYear toActivate = academicYearRepository
                .findByAcademicYearIdAndSchool_SchoolId(academicYearId, schoolId)
                .orElseThrow(() -> new RuntimeException("Academic Year not found"));

        if ("ARCHIVED".equalsIgnoreCase(toActivate.getStatus())) {
            throw new RuntimeException("Cannot activate an ARCHIVED academic year.");
        }

        // Deactivate all others for this school
        List<AcademicYear> allYears = academicYearRepository.findBySchool_SchoolId(schoolId);
        for (AcademicYear ay : allYears) {
            ay.setIsCurrent(false);
            ay.setIsActive(false);
            if ("ACTIVE".equalsIgnoreCase(ay.getStatus())) {
                ay.setStatus("COMPLETED"); // Mark old active year as completed
            }
        }
        academicYearRepository.saveAll(allYears);

        // Activate target
        toActivate.setIsCurrent(true);
        toActivate.setIsActive(true);
        toActivate.setStatus("ACTIVE");
        academicYearRepository.save(toActivate);
    }

    @Override
    @Transactional
    public void archive(Long academicYearId, Long schoolId) {
        AcademicYear toArchive = academicYearRepository
                .findByAcademicYearIdAndSchool_SchoolId(academicYearId, schoolId)
                .orElseThrow(() -> new RuntimeException("Academic Year not found"));

        if (toArchive.getIsCurrent() != null && toArchive.getIsCurrent()) {
            throw new RuntimeException("Cannot archive the currently active academic year. Please activate another year first.");
        }

        toArchive.setStatus("ARCHIVED");
        academicYearRepository.save(toArchive);
    }
}
