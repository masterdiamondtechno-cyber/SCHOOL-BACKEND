package com.schoolmanagement.subscription.repository;

import com.schoolmanagement.subscription.entity.PaymentHistory;
import com.schoolmanagement.subscription.entity.PaymentHistory.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentHistoryRepository extends JpaRepository<PaymentHistory, Long> {

    // School ki saari payments
    List<PaymentHistory> findBySchoolIdOrderByCreatedAtDesc(Long schoolId);

    // Razorpay payment ID se dhundho (webhook ke liye)
    Optional<PaymentHistory> findByRazorpayPaymentId(String razorpayPaymentId);

    // Razorpay order ID se dhundho
    Optional<PaymentHistory> findByRazorpayOrderId(String razorpayOrderId);

    // School ki successful payments
    List<PaymentHistory> findBySchoolIdAndStatus(Long schoolId, PaymentStatus status);

    // Invoice number se dhundho
    Optional<PaymentHistory> findByInvoiceNumber(String invoiceNumber);
}
