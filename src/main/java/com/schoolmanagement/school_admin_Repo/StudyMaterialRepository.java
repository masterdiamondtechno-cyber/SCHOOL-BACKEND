package com.schoolmanagement.school_admin_Repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.schoolmanagement.school_admin_Entity.MaterialType;
import com.schoolmanagement.school_admin_Entity.StudyMaterial;

@Repository
public interface StudyMaterialRepository extends JpaRepository<StudyMaterial, Long> {

    // ── Fetch all for a school ────────────────────────────────────────────────
    List<StudyMaterial> findBySchool_SchoolId(Long schoolId);

    List<StudyMaterial> findBySchool_SchoolIdAndAcademicYear_AcademicYearId(
            Long schoolId, Long academicYearId);

    // ── Fetch with type filter ────────────────────────────────────────────────
    List<StudyMaterial> findBySchool_SchoolIdAndMaterialType(
            Long schoolId, MaterialType materialType);

    List<StudyMaterial> findBySchool_SchoolIdAndAcademicYear_AcademicYearIdAndMaterialType(
            Long schoolId, Long academicYearId, MaterialType materialType);

    // ── Fetch by division (for student access) ───────────────────────────────
    List<StudyMaterial> findByDivision_DivisionIdAndStatus(Long divisionId, String status);

    List<StudyMaterial> findByDivision_DivisionIdAndAcademicYear_AcademicYearIdAndStatus(
            Long divisionId, Long academicYearId, String status);

    // ── Fetch by teacher (for teacher's own materials) ───────────────────────
    List<StudyMaterial> findByTeacher_IdAndStatus(Long teacherId, String status);

    List<StudyMaterial> findByTeacher_IdAndSchool_SchoolId(Long teacherId, Long schoolId);

    // ── For materialId auto-generation (get last inserted record) ─────────────
    Optional<StudyMaterial> findTopByOrderByIdDesc();

    // ── Counts for stats card ─────────────────────────────────────────────────
    long countBySchool_SchoolId(Long schoolId);

    long countBySchool_SchoolIdAndAcademicYear_AcademicYearId(Long schoolId, Long academicYearId);

    long countBySchool_SchoolIdAndStatus(Long schoolId, String status);

    long countBySchool_SchoolIdAndAcademicYear_AcademicYearIdAndStatus(
            Long schoolId, Long academicYearId, String status);

    // ── Sum of all downloads for stats card ───────────────────────────────────
    @Query("SELECT COALESCE(SUM(m.downloadCount), 0) FROM StudyMaterial m " +
           "WHERE m.school.schoolId = :schoolId")
    Long sumDownloadsBySchoolId(@Param("schoolId") Long schoolId);

    @Query("SELECT COALESCE(SUM(m.downloadCount), 0) FROM StudyMaterial m " +
           "WHERE m.school.schoolId = :schoolId " +
           "AND m.academicYear.academicYearId = :academicYearId")
    Long sumDownloadsBySchoolIdAndAcademicYearId(@Param("schoolId") Long schoolId,
                                                  @Param("academicYearId") Long academicYearId);

    // ── Cleanup when school is deleted ────────────────────────────────────────
    void deleteBySchool_SchoolId(Long schoolId);
}
