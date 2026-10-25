package com.schoolmanagement.school.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TeacherPaymentDTO {

    // Request fields
    private Long teacherPaymentId;
    private Long schoolId;
    private Long academicYearId;
    private Long teacherId;
    private String paymentMonth;
    private Integer paymentYear;
    private Double basicSalary;
    private Double deductions;
    private Double bonus;
    private Double netAmountPaid;
    private String paymentMode;
    private String transactionId;
    private String receiptNumber;
    private LocalDate paymentDate;
    private String status;
    private String remark;

    // Filter fields
    private String statusFilter;

    // Response/display fields (populated in responses only)
    private String teacherName;
    private String department;
    private String avatarInitials;
    private String monthLabel;

}
