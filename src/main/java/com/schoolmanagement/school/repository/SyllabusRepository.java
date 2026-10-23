package com.schoolmanagement.school.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.schoolmanagement.school.entity.Syllabus;

@Repository
public interface SyllabusRepository extends JpaRepository<Syllabus, Long> {

    Optional<Syllabus> findTopByOrderByIdDesc();

    Optional<Syllabus> findByIdAndSchool_SchoolId(Long id, Long schoolId);

    // ── Get All (with optional status filter) ─────────────────────────────
    List<Syllabus> findBySchool_SchoolIdAndAcademicYear_AcademicYearId(
            Long schoolId, Long academicYearId);

    List<Syllabus> findBySchool_SchoolIdAndAcademicYear_AcademicYearIdAndStatus(
            Long schoolId, Long academicYearId, String status);

    // ── Get by teacher ────────────────────────────────────────────────────
    List<Syllabus> findByTeacher_IdAndSchool_SchoolId(Long teacherId, Long schoolId);

    List<Syllabus> findByTeacher_IdAndSchool_SchoolIdAndAcademicYear_AcademicYearId(
            Long teacherId, Long schoolId, Long academicYearId);

    // ── Counts for stats ──────────────────────────────────────────────────
    long countBySchool_SchoolIdAndAcademicYear_AcademicYearId(
            Long schoolId, Long academicYearId);

    long countBySchool_SchoolIdAndAcademicYear_AcademicYearIdAndStatus(
            Long schoolId, Long academicYearId, String status);

    // ── Cleanup ───────────────────────────────────────────────────────────
    void deleteBySchool_SchoolId(Long schoolId);
}
