package com.schoolmanagement.school_admin_Repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.schoolmanagement.school_admin_Entity.Notification;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findBySchool_SchoolId(Long schoolId);

    List<Notification> findBySchool_SchoolIdAndAcademicYear_AcademicYearId(Long schoolId, Long academicYearId);

    List<Notification> findBySchool_SchoolIdAndIsActiveTrue(Long schoolId);

    List<Notification> findBySchool_SchoolIdAndAcademicYear_AcademicYearIdAndIsActiveTrue(Long schoolId, Long academicYearId);

    Optional<Notification> findByNotificationIdAndSchool_SchoolId(Long notificationId, Long schoolId);

    List<Notification> findTop5ByTypeAndIsActiveTrueOrderBySentAtDesc(String type);

    void deleteBySchool_SchoolId(Long schoolId);
}
