package com.schoolmanagement.subscription.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionEmailService {

    private final JavaMailSender mailSender;

    /**
     * 1. 14-Day Free Trial Start Confirmation Email
     */
    @Async
    public void sendTrialStartedEmail(String toEmail, String schoolName, LocalDate trialEndDate) {
        try {
            if (toEmail == null || toEmail.isBlank()) return;

            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setTo(toEmail);
            msg.setSubject("Welcome to Master Diamond SaaS - 14 Days Free Trial Started! 🎉");
            msg.setText(
                    "Dear " + schoolName + " Team,\n\n" +
                    "Welcome aboard! Your 14-day free trial on the Master Diamond School Management Platform is now active.\n\n" +
                    "Trial Details:\n" +
                    "• Plan: BASIC Plan (Free Trial)\n" +
                    "• Trial Valid Until: " + trialEndDate + "\n" +
                    "• Students Capacity: Up to 500 Students\n" +
                    "• Teachers Capacity: Up to 30 Teachers\n\n" +
                    "You can log in and explore all academic, attendance, examination, and fee management features.\n\n" +
                    "Need assistance? Reply to this email or visit https://masterdiamond.in\n\n" +
                    "Warm regards,\n" +
                    "Master Diamond SaaS Team"
            );
            mailSender.send(msg);
            log.info("📧 Trial welcome email sent to: {}", toEmail);
        } catch (Exception e) {
            log.warn("Failed to send trial email to {}: {}", toEmail, e.getMessage());
        }
    }

    /**
     * 2. Renewal Reminder Email (5 days before expiry)
     */
    @Async
    public void sendRenewalReminderEmail(String toEmail, String schoolName, String planName,
                                         LocalDate expiryDate, long daysRemaining) {
        try {
            if (toEmail == null || toEmail.isBlank()) return;

            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setTo(toEmail);
            msg.setSubject("Action Required: Your School Subscription Expires in " + daysRemaining + " Days ⏳");
            msg.setText(
                    "Dear " + schoolName + " Administrator,\n\n" +
                    "This is a gentle reminder that your " + planName + " subscription on Master Diamond is expiring soon.\n\n" +
                    "Subscription Details:\n" +
                    "• Current Plan: " + planName + "\n" +
                    "• Expiry Date: " + expiryDate + "\n" +
                    "• Days Remaining: " + daysRemaining + " days\n\n" +
                    "To avoid interruption in student attendance, online fees, and portal access, please renew your subscription:\n" +
                    "👉 Visit https://masterdiamond.in/plans to renew.\n\n" +
                    "Warm regards,\n" +
                    "Master Diamond Billing Team"
            );
            mailSender.send(msg);
            log.info("📧 Renewal reminder email sent to: {}", toEmail);
        } catch (Exception e) {
            log.warn("Failed to send renewal reminder to {}: {}", toEmail, e.getMessage());
        }
    }

    /**
     * 3. Subscription Expired Alert Email
     */
    @Async
    public void sendSubscriptionExpiredEmail(String toEmail, String schoolName, String planName, LocalDate expiryDate) {
        try {
            if (toEmail == null || toEmail.isBlank()) return;

            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setTo(toEmail);
            msg.setSubject("Important: Your School Subscription has Expired ⚠️");
            msg.setText(
                    "Dear " + schoolName + " Administrator,\n\n" +
                    "Your " + planName + " subscription on Master Diamond expired on " + expiryDate + ".\n\n" +
                    "Your school account is currently restricted from regular operations (attendance, fee collection, exam records).\n\n" +
                    "To restore full access immediately, please renew your plan:\n" +
                    "👉 https://masterdiamond.in/plans\n\n" +
                    "Thank you,\n" +
                    "Master Diamond Billing Team"
            );
            mailSender.send(msg);
            log.info("📧 Expiry alert email sent to: {}", toEmail);
        } catch (Exception e) {
            log.warn("Failed to send expiry email to {}: {}", toEmail, e.getMessage());
        }
    }

    /**
     * 4. Payment Success & Invoice Email
     */
    @Async
    public void sendPaymentSuccessInvoiceEmail(String toEmail, String schoolName, String planName,
                                              String billingCycle, BigDecimal amount,
                                              String invoiceNumber, String paymentId,
                                              LocalDate validTill) {
        try {
            if (toEmail == null || toEmail.isBlank()) return;

            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setTo(toEmail);
            msg.setSubject("Payment Receipt & Invoice #" + invoiceNumber + " - Master Diamond ✅");
            msg.setText(
                    "Dear " + schoolName + " Administrator,\n\n" +
                    "Thank you for your payment! Your subscription has been successfully activated.\n\n" +
                    "─── INVOICE DETAILS ───\n" +
                    "• Invoice Number: " + invoiceNumber + "\n" +
                    "• Payment ID: " + paymentId + "\n" +
                    "• Plan: " + planName + " (" + billingCycle + ")\n" +
                    "• Amount Paid: ₹" + amount + " INR\n" +
                    "• Valid Until: " + validTill + "\n" +
                    "• Status: PAID / ACTIVE\n\n" +
                    "You and your staff now have uninterrupted access to all subscribed features.\n\n" +
                    "Best regards,\n" +
                    "Master Diamond Billing Department"
            );
            mailSender.send(msg);
            log.info("📧 Payment invoice email sent to: {} for invoice: {}", toEmail, invoiceNumber);
        } catch (Exception e) {
            log.warn("Failed to send invoice email to {}: {}", toEmail, e.getMessage());
        }
    }
}
