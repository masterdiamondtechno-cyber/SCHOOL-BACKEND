package com.schoolmanagement.school.service;

import java.util.List;
import com.schoolmanagement.school.dto.NotificationDTO;

public interface NotificationService {
    void create(NotificationDTO dto);
    List<NotificationDTO> getAllBySchoolId(Long schoolId, Long academicYearId);
    List<NotificationDTO> getActiveBySchoolId(Long schoolId, Long academicYearId);
    void update(NotificationDTO dto);
    void delete(Long notificationId, Long schoolId);
    void deactivate(Long notificationId, Long schoolId);
}
