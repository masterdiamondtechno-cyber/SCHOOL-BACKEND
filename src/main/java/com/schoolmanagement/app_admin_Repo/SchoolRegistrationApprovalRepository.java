package com.schoolmanagement.app_admin_Repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.schoolmanagement.app_admin_Entity.SchoolRegistrationApproval;

@Repository
public interface SchoolRegistrationApprovalRepository extends JpaRepository<SchoolRegistrationApproval, Long> {

    Optional<SchoolRegistrationApproval> findBySchool_SchoolId(Long schoolId);

    void deleteBySchool_SchoolId(Long schoolId);
}
