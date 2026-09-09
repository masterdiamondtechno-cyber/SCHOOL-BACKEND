package com.schoolmanagement.school_admin_DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FeePaymentDTO {

    // ── Core payment fields (used for both requests and responses) ──
    private Long paymentId;
    private Long schoolId;
    private Long academicYearId;
    private Long studentId;
    private Long feeStructureId;
    private Double amountPaid;
    private LocalDate paymentDate;
    private String paymentMode;
    private String transactionId;
    private String status;
    private String remark;
    private String receiptNumber;

    // For report filters
    private Integer month;
    private Integer year;

    // ── UI display fields (populated in responses only) ──
    private String studentName;  // e.g. "Yogesh Yadav"
    private String className;    // e.g. "10-A" (standard + division)
    private String rollNo;       // student roll number / studentId
    private String feeName;      // e.g. "Tuition Fee"
    private String feeType;      // e.g. "YEARLY", "MONTHLY"
    private Double totalAmount;  // from FeeStructure.amount
    private Double amountDue;    // totalAmount - amountPaid
    private LocalDate dueDate;   // from FeeStructure.dueDate

}

