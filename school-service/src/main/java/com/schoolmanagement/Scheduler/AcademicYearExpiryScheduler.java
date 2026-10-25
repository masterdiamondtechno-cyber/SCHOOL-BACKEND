package com.schoolmanagement.Scheduler;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.schoolmanagement.school.entity.AcademicYear;
import com.schoolmanagement.school.repository.AcademicYearRepository;

@Component
public class AcademicYearExpiryScheduler {

    @Autowired
    private AcademicYearRepository academicYearRepository;
    
    @Autowired
    private com.schoolmanagement.school.repository.TeacherLeaveRepository teacherLeaveRepository;
     

    /**
     * Runs every day at midnight (00:00).
     * Checks if any academic year's endDate has passed.
     * If yes → sets status = "inactive" and isCurrent = false.
     */
    @Scheduled(cron = "0 0 0 * * *")
    public void deactivateExpiredAcademicYears() {

        LocalDate today = LocalDate.now();

        List<AcademicYear> expiredYears = academicYearRepository.findExpiredActiveAcademicYears(today);

        if (expiredYears.isEmpty()) {
            System.out.println("[AcademicYear Scheduler] No expired academic years found. Date: " + today);
            return;
        }

        for (AcademicYear ay : expiredYears) {
            ay.setStatus("inactive");
            ay.setIsCurrent(false);
            ay.setUpdatedAt(LocalDateTime.now());
            System.out.println("[AcademicYear Scheduler] Deactivated: " + ay.getYearName()
                    + " (ID: " + ay.getAcademicYearId() + ", School: " + ay.getSchool().getSchoolId() + ")");
        }

        academicYearRepository.saveAll(expiredYears);
        System.out.println("[AcademicYear Scheduler] Total deactivated: " + expiredYears.size() + " | Date: " + today);
    }
    
    @Scheduled(cron = "0 5 0 * * *")
    public void autoExpireStalePendingLeaves() {
     
        LocalDate today = LocalDate.now();
     
        List<com.schoolmanagement.school.entity.TeacherLeave> staleLeaves =
                teacherLeaveRepository.findStalePendingLeaves(today);
     
        if (staleLeaves.isEmpty()) {
            System.out.println("[LeaveScheduler] No stale pending leaves found. Date: " + today);
            return;
        }
     
        for (com.schoolmanagement.school.entity.TeacherLeave leave : staleLeaves) {
            leave.setStatus("CANCELLED");
            leave.setReviewRemark("Auto-cancelled: leave date passed without school action");
            leave.setUpdatedAt(LocalDateTime.now());
            System.out.println("[LeaveScheduler] Auto-cancelled leave: " + leave.getLeaveRefId()
                    + " | Teacher: " + leave.getTeacher().getTeacherName()
                    + " | From: " + leave.getFromDate());
        }
     
        teacherLeaveRepository.saveAll(staleLeaves);
        System.out.println("[LeaveScheduler] Total auto-cancelled: " + staleLeaves.size() + " | Date: " + today);
    }
}
