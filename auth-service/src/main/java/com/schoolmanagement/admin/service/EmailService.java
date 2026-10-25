package com.schoolmanagement.admin.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class EmailService {
    
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(EmailService.class);
    
    @Autowired
    private JavaMailSender mailSender;

    @Async
    public void sendTicketCreationEmailAsync(String toEmail, String userName, String ticketId) {
        log.info("Attempting to send ticket creation email for Ticket ID: {}", ticketId);
        try {
            if (toEmail == null || toEmail.trim().isEmpty()) {
                log.warn("Creator email not available for Ticket ID: {}. Skipping email notification.", ticketId);
                return;
            }

            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject("Ticket Created Successfully - " + ticketId);

            message.setText(
                    "Dear " + userName + ",\n\n" +
                    "Your support ticket has been successfully created.\n\n" +
                    "Ticket ID: " + ticketId + "\n\n" +
                    "Our team will review your request and update you shortly.\n\n" +
                    "Thank you,\n" +
                    "Support Team"
            );

            mailSender.send(message);
            log.info("Ticket creation email sent successfully to {} for Ticket ID: {}", toEmail, ticketId);
        } catch (Exception e) {
            log.error("Failed to send ticket creation email for Ticket ID: {}. Error: {}", ticketId, e.getMessage(), e);
        }
    }

    @Async
    public void sendTicketResolutionEmailAsync(String toEmail, String userName, String ticketId, String resolutionRemarks) {
        log.info("Attempting to send ticket resolution email for Ticket ID: {}", ticketId);
        try {
            if (toEmail == null || toEmail.trim().isEmpty()) {
                log.warn("Creator email not available for Ticket ID: {}. Skipping email notification.", ticketId);
                return;
            }

            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject("Ticket Closed - " + ticketId);

            String remarks = (resolutionRemarks != null && !resolutionRemarks.trim().isEmpty())
                    ? resolutionRemarks
                    : "No remarks provided";

            message.setText(
                    "Dear " + userName + ",\n\n" +
                    "Your support ticket has been successfully closed.\n\n" +
                    "Ticket ID: " + ticketId + "\n\n" +
                    "Resolution Notes:\n" +
                    remarks + "\n\n" +
                    "Thank you,\n" +
                    "Support Team"
            );

            mailSender.send(message);
            log.info("Ticket resolution email sent successfully to {} for Ticket ID: {}", toEmail, ticketId);
        } catch (Exception e) {
            log.error("Failed to send ticket resolution email for Ticket ID: {}. Error: {}", ticketId, e.getMessage(), e);
        }
    }


    public void sendPasswordEmail(String toEmail, String password) {

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("School Account Approved");

        message.setText(
                "Congratulations!\n\n" +
                "Your school has been approved.\n\n" +
                "Your Login Password: " + password +
                "\n\nPlease change your password after first login."
        );

        mailSender.send(message);
    }
    
    public void sendRejectionEmail(String toEmail, String reason) {

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("School Registration Rejected");

        message.setText(
                "Dear School,\n\n" +
                "We regret to inform you that your registration has been rejected.\n\n" +
                "Reason:\n" + reason +
                "\n\nPlease correct the issue and register again.\n\n" +
                "Thank You."
        );

        mailSender.send(message);
    }

    public void sendRegistrationOtpEmail(String toEmail, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Admin Registration - OTP");
        message.setText("Your OTP for Admin Registration is: " + otp + "\nValid for 5 minutes.");
        mailSender.send(message);
    }

    public void sendLoginOtpEmail(String toEmail, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Admin Login - OTP");
        message.setText("Your OTP for Admin Login is: " + otp + "\nValid for 5 minutes.");
        mailSender.send(message);
    }

    public void sendAdminPasswordEmail(String toEmail, String password) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Admin Account Created");
        message.setText("Congratulations! Your Admin account is now active.\n\n" +
                        "Your Login Password: " + password + "\n\n" +
                        "Please change your password after your first login.");
        mailSender.send(message);
    }

    public void sendOwnerApprovalEmail(String toEmail, String password) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Owner Account Approved");
        message.setText("Congratulations!\n\n" +
                        "Your owner account has been approved by the administrator.\n\n" +
                        "Your Login Password: " + password + "\n\n" +
                        "Please change your password after your first login.");
        mailSender.send(message);
    }

    public void sendOwnerRejectionEmail(String toEmail, String reason) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Owner Registration Rejected");
        message.setText("Dear Applicant,\n\n" +
                        "We regret to inform you that your owner registration has been rejected.\n\n" +
                        "Reason:\n" + (reason != null ? reason : "No reason provided") +
                        "\n\nPlease correct the issue and register again.\n\n" +
                        "Thank You.");
        mailSender.send(message);
    }
}