package com.schoolmanagement.app_admin_Service;

import java.util.List;

import com.schoolmanagement.app_admin_Entity.GlobalAnnouncement;
import com.schoolmanagement.app_admin_DTO.GlobalAnnouncementDTO;

public interface GlobalAnnouncementService {
    GlobalAnnouncement createAnnouncement(GlobalAnnouncementDTO dto);
    List<GlobalAnnouncement> getAllAnnouncements();
    void deleteAnnouncement(Long id);
}
