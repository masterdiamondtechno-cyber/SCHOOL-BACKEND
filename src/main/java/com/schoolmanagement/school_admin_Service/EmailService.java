package com.schoolmanagement.school_admin_Service;

public interface EmailService {

    void sendStudentCredentials(String toEmail, String studentId, String password);
    
    void sendTeacherCredentials(String toEmail, String password);

    void sendParentCredentials(String toEmail, String childName, String password);
    
    void sendLeaveStatusUpdate(String toEmail, String teacherName, String leaveRefId,
            String status, String fromDate, String toDate,
            Integer totalDays, String remark);

    void sendStudentLeaveStatusUpdate(String toEmail, String parentName, String studentName, String leaveRefId,
            String status, String fromDate, String toDate,
            Integer totalDays, String remark);
}
