package com.schoolmanagement.subscription.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payment_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Kaun school ne pay kiya
    @Column(nullable = false)
    private Long schoolId;

    // Kis subscription ke liye
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_id", nullable = false)
    private SchoolSubscription subscription;

    // Kitna pay kiya
    @Column(nullable = false)
    private BigDecimal amount;

    // Currency (INR)
    @Column(nullable = false)
    private String currency;

    // Payment status
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    // Razorpay ke IDs
    private String razorpayOrderId;
    private String razorpayPaymentId;
    private String razorpaySignature;

    // Payment method (card, upi, netbanking)
    private String paymentMethod;

    // Invoice number (unique)
    @Column(unique = true)
    private String invoiceNumber;

    // Failure reason (agar fail hua)
    private String failureReason;

    // Kab pay kiya
    private LocalDateTime paidAt;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        // Unique Invoice number generate: INV-2026-XXXXXX
        if (invoiceNumber == null) {
            long rand = (long) (Math.random() * 900000) + 100000;
            invoiceNumber = "INV-" + java.time.LocalDate.now().getYear() + "-" + rand;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum PaymentStatus {
        PENDING,    // Payment shuru nahi hui
        SUCCESS,    // Payment ho gayi ✅
        FAILED,     // Payment fail ❌
        REFUNDED    // Refund ho gayi
    }
}
