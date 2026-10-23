package com.schoolmanagement.school.serviceImpl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.schoolmanagement.school.dto.NotificationDTO;
import com.schoolmanagement.school.entity.*;
import com.schoolmanagement.school.repository.*;
import com.schoolmanagement.school.service.NotificationService;

@Service
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final SchoolRepository schoolRepository;
    private final AcademicYearRepository academicYearRepository;
    private final AddStandardRepository standardRepository;
    private final AddDivisionRepository divisionRepository;

    public NotificationServiceImpl(NotificationRepository notificationRepository,
                                   SchoolRepository schoolRepository,
                                   AcademicYearRepository academicYearRepository,
                                   AddStandardRepository standardRepository,
                                   AddDivisionRepository divisionRepository) {
        this.notificationRepository = notificationRepository;
        this.schoolRepository = schoolRepository;
        this.academicYearRepository = academicYearRepository;
        this.standardRepository = standardRepository;
        this.divisionRepository = divisionRepository;
    }

    // ==================== ENTITY → DTO HELPER ====================

    private NotificationDTO toDTO(Notification n) {
        NotificationDTO dto = new NotificationDTO();
        dto.setNotificationId(n.getNotificationId());
        dto.setSchoolId(n.getSchool().getSchoolId());
        if (n.getAcademicYear() != null)
            dto.setAcademicYearId(n.getAcademicYear().getAcademicYearId());
        dto.setTitle(n.getTitle());
        dto.setMessage(n.getMessage());
        dto.setType(n.getType());
        dto.setTargetAudience(n.getTargetAudience());
        if (n.getTargetStandard() != null)
            dto.setTargetStandardId(n.getTargetStandard().getStandardId());
        if (n.getTargetDivision() != null)
            dto.setTargetDivisionId(n.getTargetDivision().getDivisionId());
        dto.setSentBy(n.getSentBy());
        dto.setSentAt(n.getSentAt());
        dto.setExpiryDate(n.getExpiryDate());
        dto.setIsActive(n.getIsActive());
        return dto;
    }

    // ==================== CRUD ====================

    @Override
    public void create(NotificationDTO dto) {
        Notification n = new Notification();
        n.setSchool(schoolRepository.findBySchoolId(dto.getSchoolId())
                .orElseThrow(() -> new RuntimeException("School not found")));
        if (dto.getAcademicYearId() != null)
            n.setAcademicYear(academicYearRepository.findById(dto.getAcademicYearId())
                    .orElseThrow(() -> new RuntimeException("Academic Year not found")));
        n.setTitle(dto.getTitle());
        n.setMessage(dto.getMessage());
        n.setType(dto.getType());
        n.setTargetAudience(dto.getTargetAudience());
        n.setSentBy(dto.getSentBy());
        n.setExpiryDate(dto.getExpiryDate());

        if (dto.getTargetStandardId() != null)
            n.setTargetStandard(standardRepository.findByStandardId(dto.getTargetStandardId()).orElse(null));
        if (dto.getTargetDivisionId() != null)
            n.setTargetDivision(divisionRepository.findByDivisionId(dto.getTargetDivisionId()).orElse(null));

        notificationRepository.save(n);
    }

    @Override
    public List<NotificationDTO> getAllBySchoolId(Long schoolId, Long academicYearId) {
        return notificationRepository.findBySchool_SchoolIdAndAcademicYear_AcademicYearId(schoolId, academicYearId)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<NotificationDTO> getActiveBySchoolId(Long schoolId, Long academicYearId) {
        return notificationRepository.findBySchool_SchoolIdAndAcademicYear_AcademicYearIdAndIsActiveTrue(schoolId, academicYearId)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public void update(NotificationDTO dto) {
        Notification n = notificationRepository
                .findByNotificationIdAndSchool_SchoolId(dto.getNotificationId(), dto.getSchoolId())
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        if (dto.getTitle() != null) n.setTitle(dto.getTitle());
        if (dto.getMessage() != null) n.setMessage(dto.getMessage());
        if (dto.getType() != null) n.setType(dto.getType());
        if (dto.getTargetAudience() != null) n.setTargetAudience(dto.getTargetAudience());
        if (dto.getExpiryDate() != null) n.setExpiryDate(dto.getExpiryDate());
        notificationRepository.save(n);
    }

    @Override
    public void delete(Long notificationId, Long schoolId) {
        Notification n = notificationRepository
                .findByNotificationIdAndSchool_SchoolId(notificationId, schoolId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        notificationRepository.delete(n);
    }

    @Override
    public void deactivate(Long notificationId, Long schoolId) {
        Notification n = notificationRepository
                .findByNotificationIdAndSchool_SchoolId(notificationId, schoolId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        n.setIsActive(false);
        notificationRepository.save(n);
    }
}
