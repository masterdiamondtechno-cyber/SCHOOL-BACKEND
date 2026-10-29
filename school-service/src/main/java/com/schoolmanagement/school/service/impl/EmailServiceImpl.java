package com.schoolmanagement.school.service.impl;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.schoolmanagement.school.service.EmailService;

@Service
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    public EmailServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void sendStudentCredentials(String toEmail, String studentId, String password) {

        SimpleMailMessage message = new SimpleMailMessage();

        message.setTo(toEmail);
        message.setSubject("Student Login Credentials");

        message.setText(
                "Dear Student,\n\n" +
                "Your account has been created successfully.\n\n" +
                "Student ID : " + studentId + "\n" +
                "Password : " + password + "\n\n" +
                "Please login and change your password.\n\n" +
                "Thank You."
        );

        mailSender.send(message);
    }
    
    @Override
    public void sendTeacherCredentials(String toEmail, String password) {

        SimpleMailMessage message = new SimpleMailMessage();

        message.setTo(toEmail);
        message.setSubject("Teacher Login Credentials");

        message.setText(
                "Welcome to School Management System\n\n" +
                "Your login credentials:\n" +
                "Email: " + toEmail + "\n" +
                "Password: " + password + "\n\n" +
                "Please change password after login."
        );

        mailSender.send(message);
    }

    @Override
    public void sendParentCredentials(String toEmail, String childName, String password) {

        SimpleMailMessage message = new SimpleMailMessage();

        message.setTo(toEmail);
        message.setSubject("Parent Login Credentials");

        message.setText(
                "Dear Parent,\n\n" +
                "Your child '" + childName + "' has been registered successfully.\n\n" +
                "Your Parent Login Credentials:\n" +
                "Email: " + toEmail + "\n" +
                "Password: " + password + "\n\n" +
                "Use these credentials to login and view your child's progress.\n\n" +
                "Thank You."
        );

        mailSender.send(message);
    }
    
 // ── ADD THIS METHOD to existing EmailServiceImpl.java ─────────────────────────
 // Add it after sendParentCredentials() method. Do NOT remove existing methods.

 @Override
 public void sendLeaveStatusUpdate(String toEmail, String teacherName, String leaveRefId,
                                   String status, String fromDate, String toDate,
                                   Integer totalDays, String remark) {

     SimpleMailMessage message = new SimpleMailMessage();
     message.setTo(toEmail);

     if ("APPROVED".equals(status)) {
         message.setSubject("Leave Request Approved - " + leaveRefId);
         message.setText(
                 "Dear " + teacherName + ",\n\n" +
                 "Your leave request has been APPROVED.\n\n" +
                 "Leave Reference : " + leaveRefId + "\n" +
                 "From Date       : " + fromDate + "\n" +
                 "To Date         : " + toDate + "\n" +
                 "Total Days      : " + totalDays + "\n" +
                 (remark != null && !remark.isBlank() ? "Remark          : " + remark + "\n" : "") +
                 "\nPlease ensure proper handover before your leave begins.\n\n" +
                 "Thank You."
         );
     } else {
         message.setSubject("Leave Request Rejected - " + leaveRefId);
         message.setText(
                 "Dear " + teacherName + ",\n\n" +
                 "Your leave request has been REJECTED.\n\n" +
                 "Leave Reference : " + leaveRefId + "\n" +
                 "From Date       : " + fromDate + "\n" +
                 "To Date         : " + toDate + "\n" +
                 "Total Days      : " + totalDays + "\n" +
                 (remark != null && !remark.isBlank() ? "Reason          : " + remark + "\n" : "") +
                 "\nFor queries, please contact your school administration.\n\n" +
                 "Thank You."
         );
     }

     mailSender.send(message);
 }

  @Override
  public void sendStudentLeaveStatusUpdate(String toEmail, String parentName, String studentName, String leaveRefId,
                                          String status, String fromDate, String toDate,
                                          Integer totalDays, String remark) {

      SimpleMailMessage message = new SimpleMailMessage();
      message.setTo(toEmail);

      String recipientName = (parentName != null && !parentName.isBlank()) ? parentName : "Parent";

      if ("APPROVED".equals(status)) {
          message.setSubject("Student Leave Request Approved - " + leaveRefId);
          message.setText(
                  "Dear " + recipientName + ",\n\n" +
                  "The leave request for your child, " + studentName + ", has been APPROVED.\n\n" +
                  "Leave Reference : " + leaveRefId + "\n" +
                  "From Date       : " + fromDate + "\n" +
                  "To Date         : " + toDate + "\n" +
                  "Total Days      : " + totalDays + "\n" +
                  (remark != null && !remark.isBlank() ? "Remark          : " + remark + "\n" : "") +
                  "\nThank You."
          );
      } else {
          message.setSubject("Student Leave Request Rejected - " + leaveRefId);
          message.setText(
                  "Dear " + recipientName + ",\n\n" +
                  "The leave request for your child, " + studentName + ", has been REJECTED.\n\n" +
                  "Leave Reference : " + leaveRefId + "\n" +
                  "From Date       : " + fromDate + "\n" +
                  "To Date         : " + toDate + "\n" +
                  "Total Days      : " + totalDays + "\n" +
                  (remark != null && !remark.isBlank() ? "Reason          : " + remark + "\n" : "") +
                  "\nFor queries, please contact the school administration.\n\n" +
                  "Thank You."
          );
      }

      mailSender.send(message);
  }
}
