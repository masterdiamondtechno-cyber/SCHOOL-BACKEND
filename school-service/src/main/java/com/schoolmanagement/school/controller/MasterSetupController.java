package com.schoolmanagement.school.controller;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.schoolmanagement.school.entity.*;
import com.schoolmanagement.school.repository.*;

@RestController
@RequestMapping("/api/master-setup")
public class MasterSetupController {

    private final AcademicYearRepository academicYearRepository;
    private final AddStandardRepository standardRepository;
    private final AddDivisionRepository divisionRepository;
    private final AddSubjectRepository subjectRepository;
    private final AddTeacherRepository teacherRepository;
    private final AddStudentRepository studentRepository;
    private final StudentAcademicYearRepository studentAcademicYearRepository;
    private final TimetableRepository timetableRepository;

    public MasterSetupController(AcademicYearRepository academicYearRepository,
                                 AddStandardRepository standardRepository,
                                 AddDivisionRepository divisionRepository,
                                 AddSubjectRepository subjectRepository,
                                 AddTeacherRepository teacherRepository,
                                 AddStudentRepository studentRepository,
                                 StudentAcademicYearRepository studentAcademicYearRepository,
                                 TimetableRepository timetableRepository) {
        this.academicYearRepository = academicYearRepository;
        this.standardRepository = standardRepository;
        this.divisionRepository = divisionRepository;
        this.subjectRepository = subjectRepository;
        this.teacherRepository = teacherRepository;
        this.studentRepository = studentRepository;
        this.studentAcademicYearRepository = studentAcademicYearRepository;
        this.timetableRepository = timetableRepository;
    }

    @PostMapping("/status")
    public ResponseEntity<Map<String, Object>> getMasterSetupStatus(@RequestBody Map<String, Object> payload) {
        Map<String, Object> response = new LinkedHashMap<>();
        try {
            Object schoolIdObj = payload.get("schoolId");
            if (schoolIdObj == null) {
                response.put("code", 400); response.put("status", "FAILED");
                response.put("message", "schoolId is required"); response.put("data", null);
                return ResponseEntity.badRequest().body(response);
            }
            Long schoolId = Long.parseLong(schoolIdObj.toString());

            // 1. Resolve Academic Year
            List<AcademicYear> academicYears = academicYearRepository.findBySchool_SchoolId(schoolId);
            long academicYearCount = academicYears.size();
            boolean academicYearCreated = academicYearCount > 0;

            Optional<AcademicYear> activeYearOpt = academicYears.stream()
                    .filter(ay -> ay.getIsCurrent() != null && ay.getIsCurrent())
                    .findFirst();
            boolean academicYearActive = activeYearOpt.isPresent();
            Long academicYearId = activeYearOpt.map(AcademicYear::getAcademicYearId).orElse(null);

            // If an academicYearId was explicitly provided in request body, use that as context
            Object ayIdObj = payload.get("academicYearId");
            if (ayIdObj != null) {
                academicYearId = Long.parseLong(ayIdObj.toString());
            }

            // 2. Class / Standard count
            long classCount = 0;
            if (academicYearId != null) {
                classCount = standardRepository.findBySchool_SchoolIdAndAcademicYear_AcademicYearId(schoolId, academicYearId).size();
            } else {
                classCount = standardRepository.findBySchool_SchoolId(schoolId).size();
            }
            boolean classesCreated = classCount > 0;

            // 3. Section / Division count
            long sectionCount = 0;
            if (academicYearId != null) {
                sectionCount = divisionRepository.findBySchool_SchoolIdAndAcademicYear_AcademicYearId(schoolId, academicYearId).size();
            } else {
                sectionCount = divisionRepository.findBySchool_SchoolId(schoolId).size();
            }
            boolean sectionsCreated = sectionCount > 0;

            // 4. Subject count
            long subjectCount = 0;
            if (academicYearId != null) {
                subjectCount = subjectRepository.findBySchool_SchoolIdAndAcademicYear_AcademicYearId(schoolId, academicYearId).size();
            } else {
                subjectCount = subjectRepository.findAll().stream()
                        .filter(s -> s.getSchool() != null && s.getSchool().getSchoolId().equals(schoolId))
                        .count();
            }
            boolean subjectsCreated = subjectCount > 0;

            // 5. Teachers count
            long teacherCount = 0;
            List<AddTeacher> teachers;
            if (academicYearId != null) {
                teachers = teacherRepository.findBySchool_SchoolIdAndAcademicYear_AcademicYearId(schoolId, academicYearId);
                teacherCount = teachers.size();
                if (teacherCount == 0) {
                    teachers = teacherRepository.findBySchool_SchoolId(schoolId);
                    teacherCount = teachers.size();
                }
            } else {
                teachers = teacherRepository.findBySchool_SchoolId(schoolId);
                teacherCount = teachers.size();
            }
            boolean teachersCreated = teacherCount > 0;

            // 6. Teacher Mappings count
            long teacherMappingCount = teachers.stream()
                    .filter(t -> t.getAddStandard() != null || t.getAddDivision() != null)
                    .count();
            
            long subjectMappingCount = 0;
            if (academicYearId != null) {
                subjectMappingCount = subjectRepository.findBySchool_SchoolIdAndAcademicYear_AcademicYearId(schoolId, academicYearId).stream()
                        .filter(s -> s.getAddTeacher() != null)
                        .count();
            } else {
                subjectMappingCount = subjectRepository.findAll().stream()
                        .filter(s -> s.getSchool() != null && s.getSchool().getSchoolId().equals(schoolId) && s.getAddTeacher() != null)
                        .count();
            }
            
            long totalMappingCount = teacherMappingCount + subjectMappingCount;

            long timetableMappingCount = 0;
            if (academicYearId != null) {
                timetableMappingCount = timetableRepository.findBySchool_SchoolIdAndAcademicYear_AcademicYearId(schoolId, academicYearId).size();
            } else {
                timetableMappingCount = timetableRepository.findBySchool_SchoolId(schoolId).size();
            }
            
            boolean teacherMappingsCreated = (totalMappingCount > 0) || (timetableMappingCount > 0);

            // 7. Students count
            long studentCount = studentRepository.countBySchool_SchoolId(schoolId);
            boolean studentsCreated = studentCount > 0;

            // 8. Student allocations count
            long studentAllocationCount = 0;
            if (academicYearId != null) {
                studentAllocationCount = studentAcademicYearRepository.countStudentsBySchoolAndYear(schoolId, academicYearId);
            }
            boolean studentAllocationsCreated = studentAllocationCount > 0;

            // Assemble status data
            Map<String, Object> data = new LinkedHashMap<>();
            
            // Step 1
            Map<String, Object> step1 = new LinkedHashMap<>();
            step1.put("completed", academicYearCreated && academicYearActive);
            step1.put("count", academicYearCount);
            step1.put("hasActive", academicYearActive);
            data.put("step1_academicYear", step1);

            // Step 2
            Map<String, Object> step2 = new LinkedHashMap<>();
            step2.put("completed", classesCreated);
            step2.put("count", classCount);
            data.put("step2_classes", step2);

            // Step 3
            Map<String, Object> step3 = new LinkedHashMap<>();
            step3.put("completed", sectionsCreated);
            step3.put("count", sectionCount);
            data.put("step3_sections", step3);

            // Step 4
            Map<String, Object> step4 = new LinkedHashMap<>();
            step4.put("completed", subjectsCreated);
            step4.put("count", subjectCount);
            data.put("step4_subjects", step4);

            // Step 5
            Map<String, Object> step5 = new LinkedHashMap<>();
            step5.put("completed", teachersCreated);
            step5.put("count", teacherCount);
            data.put("step5_teachers", step5);

            // Step 6
            Map<String, Object> step6 = new LinkedHashMap<>();
            step6.put("completed", teacherMappingsCreated);
            step6.put("mappedTeachersCount", totalMappingCount);
            step6.put("timetableSlotsCount", timetableMappingCount);
            data.put("step6_teacherMappings", step6);

            // Step 7
            Map<String, Object> step7 = new LinkedHashMap<>();
            step7.put("completed", studentsCreated);
            step7.put("count", studentCount);
            data.put("step7_students", step7);

            // Step 8
            Map<String, Object> step8 = new LinkedHashMap<>();
            step8.put("completed", studentAllocationsCreated);
            step8.put("count", studentAllocationCount);
            data.put("step8_studentAllocations", step8);

            // Context details
            data.put("contextAcademicYearId", academicYearId);
            data.put("wizardCompleted", academicYearCreated && academicYearActive && classesCreated && sectionsCreated && subjectsCreated && teachersCreated && studentsCreated && studentAllocationsCreated);

            response.put("code", 200); response.put("status", "SUCCESS");
            response.put("message", "Master setup status fetched successfully");
            response.put("data", data);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("code", 500); response.put("status", "FAILED");
            response.put("message", e.getMessage()); response.put("data", null);
            return ResponseEntity.internalServerError().body(response);
        }
    }
}
