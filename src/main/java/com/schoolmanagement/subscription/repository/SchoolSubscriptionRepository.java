package com.schoolmanagement.subscription.repository;

import com.schoolmanagement.subscription.entity.SchoolSubscription;
import com.schoolmanagement.subscription.entity.SchoolSubscription.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface SchoolSubscriptionRepository extends JpaRepository<SchoolSubscription, Long> {

    // School ka current subscription dhundho
    Optional<SchoolSubscription> findBySchoolIdAndStatus(Long schoolId, SubscriptionStatus status);

    // School ka koi bhi active ya trial subscription hai?
    @Query("SELECT s FROM SchoolSubscription s WHERE s.schoolId = :schoolId " +
           "AND s.status IN ('TRIAL', 'ACTIVE') AND s.endDate >= :today")
    Optional<SchoolSubscription> findActiveSubscription(
            @Param("schoolId") Long schoolId,
            @Param("today") LocalDate today);

    // Jinke trial aaj expire ho rahe hain
    List<SchoolSubscription> findByStatusAndTrialEndDate(
            SubscriptionStatus status, LocalDate trialEndDate);

    // Jinke subscription X din baad expire honge (reminder ke liye)
    @Query("SELECT s FROM SchoolSubscription s WHERE s.status = 'ACTIVE' " +
           "AND s.endDate = :reminderDate")
    List<SchoolSubscription> findSubscriptionsExpiringOn(
            @Param("reminderDate") LocalDate reminderDate);

    // Expired subscriptions
    @Query("SELECT s FROM SchoolSubscription s WHERE s.status IN ('ACTIVE', 'TRIAL') " +
           "AND s.endDate < :today")
    List<SchoolSubscription> findExpiredSubscriptions(@Param("today") LocalDate today);

    // School ki saari subscriptions history
    List<SchoolSubscription> findBySchoolIdOrderByCreatedAtDesc(Long schoolId);
}
