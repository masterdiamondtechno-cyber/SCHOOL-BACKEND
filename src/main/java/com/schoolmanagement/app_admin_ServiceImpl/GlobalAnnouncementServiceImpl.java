package com.schoolmanagement.app_admin_ServiceImpl;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.schoolmanagement.app_admin_DTO.GlobalAnnouncementDTO;
import com.schoolmanagement.app_admin_Entity.GlobalAnnouncement;
import com.schoolmanagement.app_admin_Repo.GlobalAnnouncementRepository;
import com.schoolmanagement.app_admin_Service.GlobalAnnouncementService;

@Service
public class GlobalAnnouncementServiceImpl implements GlobalAnnouncementService {

    @Autowired
    private GlobalAnnouncementRepository repository;

    @Override
    public GlobalAnnouncement createAnnouncement(GlobalAnnouncementDTO dto) {
        GlobalAnnouncement announcement = new GlobalAnnouncement();
        announcement.setTitle(dto.getTitle());
        announcement.setMessage(dto.getMessage());
        announcement.setTargetAudience(dto.getTargetAudience());
        announcement.setSentBy(dto.getSentBy() != null ? dto.getSentBy() : "Super Admin");
        announcement.setSentAt(LocalDateTime.now());
        return repository.save(announcement);
    }

    @Override
    public List<GlobalAnnouncement> getAllAnnouncements() {
        return repository.findAllByOrderByCreatedAtDesc();
    }

    @Override
    public void deleteAnnouncement(Long id) {
        if (!repository.existsById(id)) {
            throw new RuntimeException("Announcement not found with id: " + id);
        }
        repository.deleteById(id);
    }
}
