package com.schoolmanagement.parent.service.impl;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.schoolmanagement.parent.dto.AcademicProgressDTO;
import com.schoolmanagement.parent.dto.ParentDashboardDTO;
import com.schoolmanagement.parent.dto.SubjectProgressDTO;
import com.schoolmanagement.parent.service.ParentService;
import com.schoolmanagement.school.entity.AddStudent;
import com.schoolmanagement.school.entity.Attendance;
import com.schoolmanagement.school.entity.Exam;
import com.schoolmanagement.school.entity.ExamMarks;
import com.schoolmanagement.school.entity.Notification;
import com.schoolmanagement.school.repository.AddStudentRepository;
import com.schoolmanagement.school.repository.AttendanceRepository;
import com.schoolmanagement.school.repository.ExamMarksRepository;
import com.schoolmanagement.school.repository.ExamRepository;
import com.schoolmanagement.school.repository.NotificationRepository;

@Service
public class ParentServiceImpl implements ParentService {

    private final AddStudentRepository studentRepository;
    private final ExamMarksRepository examMarksRepository;
    private final ExamRepository examRepository;
    private final AttendanceRepository attendanceRepository;
    private final NotificationRepository notificationRepository;

    public ParentServiceImpl(AddStudentRepository studentRepository,
                             ExamMarksRepository examMarksRepository,
                             ExamRepository examRepository,
                             AttendanceRepository attendanceRepository,
                             NotificationRepository notificationRepository) {
        this.studentRepository = studentRepository;
        this.examMarksRepository = examMarksRepository;
        this.examRepository = examRepository;
        this.attendanceRepository = attendanceRepository;
        this.notificationRepository = notificationRepository;
    }

    // ===== Dashboard Overview =====

    @Override
    public ParentDashboardDTO getDashboardOverview(Long schoolId, Long studentId, Long academicYearId) {

        // 1. Fetch the student
        AddStudent student = studentRepository.findByIdAndSchool_SchoolId(studentId, schoolId)
                .orElseThrow(() -> new RuntimeException("Student not found for studentId=" + studentId));

        // 2. Student basic info
        String studentName = (student.getFirstName() != null ? student.getFirstName() : "")
                + " " + (student.getLastName() != null ? student.getLastName() : "");
        String standard = student.getAddStandard() != null ? student.getAddStandard().getStandard() : "N/A";
        String division = student.getAddDivision() != null ? student.getAddDivision().getDivisionName() : "N/A";
        String rollNumber = student.getRollNumber() != null ? student.getRollNumber() : "";
        String dob = student.getDateOfBirth() != null
                ? student.getDateOfBirth().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))
                : "";
        String profilePhoto = student.getProfilePhoto();

        // 3. Today's Attendance
        LocalDate today = LocalDate.now();
        List<Attendance> todayRecords = attendanceRepository
                .findBySchool_SchoolIdAndAddStudent_IdAndAttendanceDateBetween(schoolId, studentId, today, today);

        String todayStatus = "NOT_MARKED";
        if (!todayRecords.isEmpty()) {
            boolean hasPresent = todayRecords.stream()
                    .anyMatch(a -> "PRESENT".equalsIgnoreCase(a.getStatus()));
            boolean hasAbsent = todayRecords.stream()
                    .anyMatch(a -> "ABSENT".equalsIgnoreCase(a.getStatus()));
            if (hasPresent) {
                todayStatus = "PRESENT";
            } else if (hasAbsent) {
                todayStatus = "ABSENT";
            }
        }

        // 4. Overall Attendance Counts (academic year filtered)
        long totalDays = attendanceRepository
                .countBySchool_SchoolIdAndAddStudent_Id(schoolId, studentId);
        long presentDays = attendanceRepository
                .countBySchool_SchoolIdAndAddStudent_IdAndStatus(schoolId, studentId, "PRESENT");
        long absentDays = totalDays - presentDays;
        double attendancePct = totalDays > 0
                ? Math.round((presentDays * 100.0 / totalDays) * 10.0) / 10.0
                : 0.0;

        // 5. Recent Marks (latest 3 marks across exams for this student/academicYear)
        List<ExamMarks> allMarks = examMarksRepository
                .findByAddStudent_IdAndSchool_SchoolIdAndAcademicYear_AcademicYearId(studentId, schoolId, academicYearId);

        List<ParentDashboardDTO.RecentMarkDTO> recentMarks = allMarks.stream()
                .filter(m -> m.getExam() != null && m.getAddSubject() != null
                        && m.getMarksObtained() != null && m.getMaxMarks() != null)
                .sorted(Comparator.comparing(
                        m -> m.getExam().getExamDate() != null ? m.getExam().getExamDate() : LocalDate.MIN,
                        Comparator.reverseOrder()))
                .limit(5)
                .map(m -> {
                    double pct = m.getMaxMarks() > 0
                            ? (m.getMarksObtained() / m.getMaxMarks()) * 100 : 0;
                    return ParentDashboardDTO.RecentMarkDTO.builder()
                            .examName(m.getExam().getExamName())
                            .subjectName(m.getAddSubject().getSubjectName())
                            .marksObtained(m.getMarksObtained())
                            .maxMarks(m.getMaxMarks())
                            .grade(calculateGrade(pct))
                            .build();
                })
                .collect(Collectors.toList());

        // 6. Notices (latest 3 active notifications for this school)
        List<Notification> allNotices = notificationRepository.findBySchool_SchoolIdAndIsActiveTrue(schoolId);
        List<ParentDashboardDTO.NoticeDTO> notices = allNotices.stream()
                .sorted(Comparator.comparing(
                        n -> n.getSentAt() != null ? n.getSentAt() : java.time.LocalDateTime.MIN,
                        Comparator.reverseOrder()))
                .limit(3)
                .map(n -> ParentDashboardDTO.NoticeDTO.builder()
                        .title(n.getTitle())
                        .message(n.getMessage())
                        .type(n.getType())
                        .sentAt(n.getSentAt() != null
                                ? n.getSentAt().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"))
                                : "")
                        .build())
                .collect(Collectors.toList());

        // 7. Build and return
        return ParentDashboardDTO.builder()
                .studentDbId(studentId)
                .studentName(studentName.trim())
                .studentUid(student.getStudentId())
                .rollNumber(rollNumber)
                .dateOfBirth(dob)
                .standard(standard)
                .division(division)
                .profilePhoto(profilePhoto)
                .todayAttendanceStatus(todayStatus)
                .totalAttendanceDays(totalDays)
                .presentDays(presentDays)
                .absentDays(absentDays)
                .attendancePercentage(attendancePct)
                .recentMarks(recentMarks)
                .notices(notices)
                .build();
    }

    // ===== Report Card =====

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

            double percentage = (mark.getMarksObtained() / mark.getMaxMarks()) * 100;
            dto.setGrade(calculateGrade(percentage));
            dto.setStatus(percentage >= 35 ? "Pass" : "Fail");

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

        // 6. Overall percentage
        double overallPercentage = studentTotalMax > 0
                ? Math.round((studentTotalObtained / studentTotalMax) * 100 * 10.0) / 10.0
                : 0;

        // 7. Percentile rank
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

        long belowCount = allPercentages.stream().filter(p -> p < overallPercentage).count();
        double percentileRank = allPercentages.size() > 0
                ? Math.round((belowCount * 100.0 / allPercentages.size()) * 10.0) / 10.0
                : 0;

        // 8. Build response
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
