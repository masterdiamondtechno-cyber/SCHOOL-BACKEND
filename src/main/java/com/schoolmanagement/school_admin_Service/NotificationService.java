package com.schoolmanagement.school_admin_Service;

import java.util.List;
import com.schoolmanagement.school_admin_DTO.NotificationDTO;

public interface NotificationService {
    void create(NotificationDTO dto);
    List<NotificationDTO> getAllBySchoolId(Long schoolId, Long academicYearId);
    List<NotificationDTO> getActiveBySchoolId(Long schoolId, Long academicYearId);
    void update(NotificationDTO dto);
    void delete(Long notificationId, Long schoolId);
    void deactivate(Long notificationId, Long schoolId);
}
