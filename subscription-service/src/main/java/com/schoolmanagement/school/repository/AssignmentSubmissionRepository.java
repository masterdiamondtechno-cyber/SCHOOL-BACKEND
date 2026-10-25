package com.schoolmanagement.school.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.schoolmanagement.school.entity.AssignmentSubmission;

@Repository
public interface AssignmentSubmissionRepository extends JpaRepository<AssignmentSubmission, Long> {

    List<AssignmentSubmission> findByStudent_IdAndSchool_SchoolId(Long studentId, Long schoolId);

    List<AssignmentSubmission> findByAssignment_IdAndSchool_SchoolId(Long assignmentId, Long schoolId);

    List<AssignmentSubmission> findByStudent_IdAndAssignment_Id(Long studentId, Long assignmentId);

    boolean existsByStudent_IdAndAssignment_Id(Long studentId, Long assignmentId);

    Long countByAssignment_Id(Long assignmentId);

    Long countByAssignment_IdAndStatus(Long assignmentId, String status);

    Long countByStudent_IdAndStatus(Long studentId, String status);

    void deleteBySchool_SchoolId(Long schoolId);
}
