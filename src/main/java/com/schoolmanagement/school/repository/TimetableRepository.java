package com.schoolmanagement.school.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.schoolmanagement.school.entity.Timetable;

@Repository
public interface TimetableRepository extends JpaRepository<Timetable, Long> {

    List<Timetable> findBySchool_SchoolIdAndAddStandard_StandardIdAndAddDivision_DivisionId(
            Long schoolId, Long standardId, Long divisionId);

    List<Timetable> findBySchool_SchoolIdAndAddStandard_StandardIdAndAddDivision_DivisionIdAndAcademicYear_AcademicYearId(
            Long schoolId, Long standardId, Long divisionId, Long academicYearId);

    List<Timetable> findBySchool_SchoolIdAndAddTeacher_Id(Long schoolId, Long teacherId);

    List<Timetable> findBySchool_SchoolIdAndAddTeacher_IdAndAcademicYear_AcademicYearId(Long schoolId, Long teacherId, Long academicYearId);

    Optional<Timetable> findByTimetableIdAndSchool_SchoolId(Long timetableId, Long schoolId);

    List<Timetable> findBySchool_SchoolId(Long schoolId);

    List<Timetable> findBySchool_SchoolIdAndAcademicYear_AcademicYearId(Long schoolId, Long academicYearId);

    void deleteBySchool_SchoolId(Long schoolId);
}
