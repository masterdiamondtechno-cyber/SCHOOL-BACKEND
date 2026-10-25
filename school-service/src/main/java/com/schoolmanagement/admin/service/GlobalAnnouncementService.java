package com.schoolmanagement.admin.service;

import java.util.List;

import com.schoolmanagement.admin.entity.GlobalAnnouncement;
import com.schoolmanagement.admin.dto.GlobalAnnouncementDTO;

public interface GlobalAnnouncementService {
    GlobalAnnouncement createAnnouncement(GlobalAnnouncementDTO dto);
    List<GlobalAnnouncement> getAllAnnouncements();
    void deleteAnnouncement(Long id);
}
