package com.schoolmanagement.parent_ServiceImpl;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.schoolmanagement.parent_DTO.AcademicProgressDTO;
import com.schoolmanagement.parent_DTO.SubjectProgressDTO;
import com.schoolmanagement.parent_Service.ParentService;
import com.schoolmanagement.school_admin_Entity.AddStudent;
import com.schoolmanagement.school_admin_Entity.Exam;
import com.schoolmanagement.school_admin_Entity.ExamMarks;
import com.schoolmanagement.school_admin_Repo.AddStudentRepository;
import com.schoolmanagement.school_admin_Repo.ExamMarksRepository;
import com.schoolmanagement.school_admin_Repo.ExamRepository;

@Service
public class ParentServiceImpl implements ParentService {

    private final AddStudentRepository studentRepository;
    private final ExamMarksRepository examMarksRepository;
    private final ExamRepository examRepository;

    public ParentServiceImpl(AddStudentRepository studentRepository,
                             ExamMarksRepository examMarksRepository,
                             ExamRepository examRepository) {
        this.studentRepository = studentRepository;
        this.examMarksRepository = examMarksRepository;
        this.examRepository = examRepository;
    }

    @Override
    public AcademicProgressDTO getReportCard(Long schoolId, Long academicYearId, Long studentId) {

        // 1. Fetch the student
        AddStudent student = studentRepository.findByIdAndSchool_SchoolId(studentId, schoolId)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        Long standardId = student.getAddStandard() != null ? student.getAddStandard().getStandardId() : null;
        Long divisionId = student.getAddDivision() != null ? student.getAddDivision().getDivisionId() : null;

        if (standardId == null || divisionId == null) {
            throw new RuntimeException("Student standard/division not assigned");
        }

        // 2. Find the most recent COMPLETED exam for this class
        List<Exam> exams = examRepository
                .findBySchool_SchoolIdAndAcademicYear_AcademicYearIdAndAddStandard_StandardIdAndAddDivision_DivisionId(
                        schoolId, academicYearId, standardId, divisionId);

        // Pick the latest completed exam by examDate
        Exam latestExam = exams.stream()
                .filter(e -> "COMPLETED".equalsIgnoreCase(e.getStatus()))
                .max(Comparator.comparing(Exam::getExamDate, Comparator.nullsLast(Comparator.naturalOrder())))
                .orElse(null);

        if (latestExam == null) {
            throw new RuntimeException("No completed exams found for this student's class");
        }

        // 3. Fetch this student's marks for the latest exam
        List<ExamMarks> studentMarks = examMarksRepository
                .findByExam_ExamIdAndAddStudent_IdAndSchool_SchoolIdAndAcademicYear_AcademicYearId(
                        latestExam.getExamId(), studentId, schoolId, academicYearId);

        if (studentMarks.isEmpty()) {
            throw new RuntimeException("No marks found for this student in exam: " + latestExam.getExamName());
        }

        // 4. Fetch ALL marks for this exam (all students) to calculate class averages & percentile
        List<ExamMarks> allExamMarks = examMarksRepository
                .findByExam_ExamIdAndSchool_SchoolIdAndAcademicYear_AcademicYearId(
                        latestExam.getExamId(), schoolId, academicYearId);

        // 5. Build subject-wise progress
        List<SubjectProgressDTO> subjects = new ArrayList<>();
        double studentTotalObtained = 0;
        double studentTotalMax = 0;

        for (ExamMarks mark : studentMarks) {
            if (mark.getAddSubject() == null || mark.getMarksObtained() == null || mark.getMaxMarks() == null)
                continue;

            SubjectProgressDTO dto = new SubjectProgressDTO();
            dto.setSubjectName(mark.getAddSubject().getSubjectName());
            dto.setScoreObtained(mark.getMarksObtained());
            dto.setTotalMarks(mark.getMaxMarks());

            // Grade calculation
            double percentage = (mark.getMarksObtained() / mark.getMaxMarks()) * 100;
            dto.setGrade(calculateGrade(percentage));
            dto.setStatus(percentage >= 35 ? "Pass" : "Fail");

            // Class average for this subject
            Long subjectId = mark.getAddSubject().getSubjectId();
            double classAvg = allExamMarks.stream()
                    .filter(m -> m.getAddSubject() != null
                            && m.getAddSubject().getSubjectId().equals(subjectId)
                            && m.getMarksObtained() != null && m.getMaxMarks() != null && m.getMaxMarks() > 0)
                    .mapToDouble(m -> (m.getMarksObtained() / m.getMaxMarks()) * 100)
                    .average()
                    .orElse(0);
            dto.setClassAverage(Math.round(classAvg * 10.0) / 10.0);

            subjects.add(dto);
            studentTotalObtained += mark.getMarksObtained();
            studentTotalMax += mark.getMaxMarks();
        }

        // 6. Calculate overall percentage for standing
        double overallPercentage = studentTotalMax > 0
                ? Math.round((studentTotalObtained / studentTotalMax) * 100 * 10.0) / 10.0
                : 0;

        // 7. Calculate percentile rank among all students in this exam
        // Group all marks by student and compute each student's total percentage
        var studentTotals = allExamMarks.stream()
                .filter(m -> m.getAddStudent() != null && m.getMarksObtained() != null && m.getMaxMarks() != null
                        && m.getMaxMarks() > 0)
                .collect(Collectors.groupingBy(m -> m.getAddStudent().getId()));

        List<Double> allPercentages = new ArrayList<>();
        for (var entry : studentTotals.entrySet()) {
            double totalObt = entry.getValue().stream().mapToDouble(ExamMarks::getMarksObtained).sum();
            double totalMax = entry.getValue().stream().mapToDouble(ExamMarks::getMaxMarks).sum();
            if (totalMax > 0) {
                allPercentages.add((totalObt / totalMax) * 100);
            }
        }

        // Count how many students scored less than this student
        long belowCount = allPercentages.stream().filter(p -> p < overallPercentage).count();
        double percentileRank = allPercentages.size() > 0
                ? Math.round((belowCount * 100.0 / allPercentages.size()) * 10.0) / 10.0
                : 0;

        // 8. Build the response
        AcademicProgressDTO response = new AcademicProgressDTO();
        response.setTermLabel(latestExam.getExamName() + " (Current)");
        response.setStanding(calculateStanding(overallPercentage));
        response.setStandingMessage(buildStandingMessage(percentileRank));
        response.setSubjects(subjects);

        return response;
    }

    // ===== Helper Methods =====

    private String calculateGrade(double percentage) {
        if (percentage >= 90) return "A+";
        if (percentage >= 80) return "A";
        if (percentage >= 70) return "B+";
        if (percentage >= 60) return "B";
        if (percentage >= 50) return "C";
        if (percentage >= 35) return "D";
        return "F";
    }

    private String calculateStanding(double overallPercentage) {
        if (overallPercentage >= 75) return "Distinction";
        if (overallPercentage >= 60) return "First Class";
        if (overallPercentage >= 50) return "Second Class";
        if (overallPercentage >= 35) return "Pass";
        return "Needs Improvement";
    }

    private String buildStandingMessage(double percentileRank) {
        if (percentileRank >= 90) return "Your ward is performing in the top 10% of their class.";
        if (percentileRank >= 75) return "Your ward is performing in the top 25% of their class.";
        if (percentileRank >= 50) return "Your ward is performing above average in their class.";
        if (percentileRank >= 25) return "Your ward is performing around the class average.";
        return "Your ward needs additional attention and support.";
    }
}
