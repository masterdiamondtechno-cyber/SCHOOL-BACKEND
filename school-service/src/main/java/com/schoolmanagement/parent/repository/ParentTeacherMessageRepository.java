package com.schoolmanagement.parent.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.schoolmanagement.parent.entity.ParentTeacherMessage;

@Repository
public interface ParentTeacherMessageRepository extends JpaRepository<ParentTeacherMessage, Long> {

    // Conversation between a student's parent and teacher/school
    List<ParentTeacherMessage> findBySchool_SchoolIdAndStudent_IdOrderBySentAtAsc(Long schoolId, Long studentId);

    // All conversations for a school
    List<ParentTeacherMessage> findBySchool_SchoolIdOrderBySentAtDesc(Long schoolId);

    // All conversations for a specific teacher
    List<ParentTeacherMessage> findBySchool_SchoolIdAndTeacher_IdOrderBySentAtDesc(Long schoolId, Long teacherId);

    // Messages relevant to teacher: either specifically for this teacher or general parent queries with teacher null
    List<ParentTeacherMessage> findBySchool_SchoolIdAndTeacher_IdOrSchool_SchoolIdAndTeacherIsNullOrderBySentAtDesc(
            Long schoolId1, Long teacherId, Long schoolId2);
}
