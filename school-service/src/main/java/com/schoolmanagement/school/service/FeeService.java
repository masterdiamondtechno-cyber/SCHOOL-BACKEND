package com.schoolmanagement.school.service;

import java.util.List;
import com.schoolmanagement.school.dto.FeeCollectionPageDataDTO;
import com.schoolmanagement.school.dto.FeeStructureDTO;
import com.schoolmanagement.school.dto.FeePaymentDTO;

public interface FeeService {
    // Fee Structure
    void createStructure(FeeStructureDTO dto);
    List<FeeStructureDTO> getStructuresByStandard(Long schoolId, Long standardId, Long academicYearId);
    void updateStructure(FeeStructureDTO dto);
    void deleteStructure(Long feeStructureId, Long schoolId);

    // Fee Payment — Page Load (single consolidated call)
    FeeCollectionPageDataDTO getPageData(Long schoolId, Long academicYearId);

    // Fee Payment — CRUD
    void createPayment(FeePaymentDTO dto);
    List<FeePaymentDTO> getPaymentsByStudent(Long schoolId, Long studentId, Long academicYearId);
    List<FeePaymentDTO> getAllPayments(Long schoolId, Long academicYearId);
    void updatePayment(FeePaymentDTO dto);
    void deletePayment(Long paymentId, Long schoolId);
}

