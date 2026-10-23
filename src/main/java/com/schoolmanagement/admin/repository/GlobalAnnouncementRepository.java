package com.schoolmanagement.admin.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.schoolmanagement.admin.entity.GlobalAnnouncement;

@Repository
public interface GlobalAnnouncementRepository extends JpaRepository<GlobalAnnouncement, Long> {
    List<GlobalAnnouncement> findAllByOrderByCreatedAtDesc();
}
