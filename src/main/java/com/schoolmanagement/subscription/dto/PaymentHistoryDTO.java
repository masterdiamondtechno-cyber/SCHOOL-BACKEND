package com.schoolmanagement.subscription.dto;

import com.schoolmanagement.subscription.entity.PaymentHistory.PaymentStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentHistoryDTO {

    private Long id;
    private Long schoolId;
    private BigDecimal amount;
    private String currency;
    private PaymentStatus status;
    private String razorpayOrderId;
    private String razorpayPaymentId;
    private String paymentMethod;
    private String invoiceNumber;
    private LocalDateTime paidAt;
    private LocalDateTime createdAt;
}
