package com.schoolmanagement.app_admin_Repo;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.schoolmanagement.app_admin_Entity.SubscriptionAssignmentHistory;

@Repository
public interface SubscriptionAssignmentHistoryRepository extends JpaRepository<SubscriptionAssignmentHistory, Long> {
    List<SubscriptionAssignmentHistory> findBySchoolIdOrderByIdDesc(Long schoolId);
}
