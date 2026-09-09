package com.schoolmanagement.app_admin_Repo;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.schoolmanagement.app_admin_Entity.GlobalAnnouncement;

@Repository
public interface GlobalAnnouncementRepository extends JpaRepository<GlobalAnnouncement, Long> {
    List<GlobalAnnouncement> findAllByOrderByCreatedAtDesc();
}
