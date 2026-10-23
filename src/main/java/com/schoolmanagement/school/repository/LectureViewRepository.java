package com.schoolmanagement.school.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.schoolmanagement.school.entity.LectureView;

public interface LectureViewRepository extends JpaRepository<LectureView, Long> {

    List<LectureView> findByLecture_Id(Long lectureId);

    List<LectureView> findByLecture_School_SchoolIdAndLecture_AcademicYear_AcademicYearId(Long schoolId, Long academicYearId);

    long countByLecture_Id(Long lectureId);
}
