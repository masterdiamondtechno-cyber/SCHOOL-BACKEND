package com.schoolmanagement.school_admin_Service;

import com.schoolmanagement.school_admin_DTO.TeacherPaymentDTO;
import com.schoolmanagement.school_admin_DTO.TeacherPaymentPageDataDTO;

public interface TeacherPaymentService {

    TeacherPaymentPageDataDTO getPageData(Long schoolId, Long academicYearId, String statusFilter, String month, Integer year);

    void createPayment(TeacherPaymentDTO dto);

    void updatePayment(TeacherPaymentDTO dto);

    void deletePayment(Long teacherPaymentId, Long schoolId);

    void markAsPaid(TeacherPaymentDTO dto);
}
