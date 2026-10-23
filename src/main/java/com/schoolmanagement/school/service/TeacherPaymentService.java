package com.schoolmanagement.school.service;

import com.schoolmanagement.school.dto.TeacherPaymentDTO;
import com.schoolmanagement.school.dto.TeacherPaymentPageDataDTO;

public interface TeacherPaymentService {

    TeacherPaymentPageDataDTO getPageData(Long schoolId, Long academicYearId, String statusFilter, String month, Integer year);

    void createPayment(TeacherPaymentDTO dto);

    void updatePayment(TeacherPaymentDTO dto);

    void deletePayment(Long teacherPaymentId, Long schoolId);

    void markAsPaid(TeacherPaymentDTO dto);
}
