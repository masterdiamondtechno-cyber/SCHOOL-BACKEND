package com.schoolmanagement.admin.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.schoolmanagement.admin.entity.SubscriptionAssignmentHistory;

@Repository
public interface SubscriptionAssignmentHistoryRepository extends JpaRepository<SubscriptionAssignmentHistory, Long> {
    List<SubscriptionAssignmentHistory> findBySchoolIdOrderByIdDesc(Long schoolId);
}
