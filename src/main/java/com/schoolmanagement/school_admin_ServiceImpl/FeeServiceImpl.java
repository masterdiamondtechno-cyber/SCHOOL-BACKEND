package com.schoolmanagement.school_admin_ServiceImpl;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.schoolmanagement.school_admin_DTO.FeeCollectionPageDataDTO;
import com.schoolmanagement.school_admin_DTO.FeeCollectionSummaryDTO;
import com.schoolmanagement.school_admin_DTO.FeePaymentDTO;
import com.schoolmanagement.school_admin_DTO.FeeStructureDTO;
import com.schoolmanagement.school_admin_Entity.*;
import com.schoolmanagement.school_admin_Repo.*;
import com.schoolmanagement.school_admin_Service.FeeService;

@Service
public class FeeServiceImpl implements FeeService {

    private final FeeStructureRepository feeStructureRepository;
    private final FeePaymentRepository feePaymentRepository;
    private final SchoolRepository schoolRepository;
    private final AcademicYearRepository academicYearRepository;
    private final AddStandardRepository standardRepository;
    private final AddStudentRepository studentRepository;
    private final StudentAcademicYearRepository studentAcademicYearRepository;

    public FeeServiceImpl(FeeStructureRepository feeStructureRepository,
                          FeePaymentRepository feePaymentRepository,
                          SchoolRepository schoolRepository,
                          AcademicYearRepository academicYearRepository,
                          AddStandardRepository standardRepository,
                          AddStudentRepository studentRepository,
                          StudentAcademicYearRepository studentAcademicYearRepository) {
        this.feeStructureRepository = feeStructureRepository;
        this.feePaymentRepository = feePaymentRepository;
        this.schoolRepository = schoolRepository;
        this.academicYearRepository = academicYearRepository;
        this.standardRepository = standardRepository;
        this.studentRepository = studentRepository;
        this.studentAcademicYearRepository = studentAcademicYearRepository;
    }

    // ==================== ENTITY → DTO HELPERS ====================

    private FeeStructureDTO toFeeStructureDTO(FeeStructure fs) {
        FeeStructureDTO dto = new FeeStructureDTO();
        dto.setFeeStructureId(fs.getFeeStructureId());
        dto.setSchoolId(fs.getSchool().getSchoolId());
        if (fs.getAcademicYear() != null)
            dto.setAcademicYearId(fs.getAcademicYear().getAcademicYearId());
        dto.setStandardId(fs.getAddStandard().getStandardId());
        dto.setFeeName(fs.getFeeName());
        dto.setFeeType(fs.getFeeType());
        dto.setAmount(fs.getAmount());
        dto.setDueDate(fs.getDueDate());
        dto.setDescription(fs.getDescription());
        return dto;
    }

    /**
     * Enriched mapper: populates all UI display fields in addition to core fields.
     * className = standard + "-" + division (e.g. "10-A")
     * amountDue  = totalAmount - amountPaid  (null-safe)
     */
    private FeePaymentDTO toFeePaymentDTO(FeePayment fp) {
        FeePaymentDTO dto = new FeePaymentDTO();

        // Core fields
        dto.setPaymentId(fp.getPaymentId());
        dto.setSchoolId(fp.getSchool().getSchoolId());
        if (fp.getAcademicYear() != null)
            dto.setAcademicYearId(fp.getAcademicYear().getAcademicYearId());
        dto.setStudentId(fp.getAddStudent().getId());
        if (fp.getFeeStructure() != null)
            dto.setFeeStructureId(fp.getFeeStructure().getFeeStructureId());
        dto.setAmountPaid(fp.getAmountPaid() != null ? fp.getAmountPaid() : 0.0);
        dto.setPaymentDate(fp.getPaymentDate());
        dto.setPaymentMode(fp.getPaymentMode());
        dto.setTransactionId(fp.getTransactionId());
        dto.setReceiptNumber(fp.getReceiptNumber());
        dto.setStatus(fp.getStatus());
        dto.setRemark(fp.getRemark());

        // ── UI display fields ──
        AddStudent student = fp.getAddStudent();

        // Student full name
        String fullName = buildFullName(student.getFirstName(), student.getMiddleName(), student.getLastName());
        dto.setStudentName(fullName);

        // Roll No (use studentId string, e.g. "STU001")
        dto.setRollNo(student.getStudentId());

        // Class name: standard + "-" + division resolved from StudentAcademicYear mapping
        String standardName = "";
        String divisionName = "";
        
        Long yearId = fp.getAcademicYear() != null ? fp.getAcademicYear().getAcademicYearId() : null;
        if (yearId == null && student.getSchool() != null) {
            yearId = academicYearRepository.findBySchool_SchoolIdAndIsCurrentTrue(student.getSchool().getSchoolId())
                    .map(AcademicYear::getAcademicYearId).orElse(null);
        }
        
        if (yearId != null) {
            Optional<StudentAcademicYear> activeMapping = studentAcademicYearRepository
                    .findByStudent_IdAndAcademicYear_AcademicYearId(student.getId(), yearId);
            if (activeMapping.isPresent()) {
                standardName = (activeMapping.get().getStandard() != null) ? activeMapping.get().getStandard().getStandard() : "";
                divisionName = (activeMapping.get().getDivision() != null) ? activeMapping.get().getDivision().getDivisionName() : "";
            }
        }
        
        if (standardName.isEmpty() && divisionName.isEmpty()) {
            standardName = (student.getAddStandard() != null) ? student.getAddStandard().getStandard() : "";
            divisionName = (student.getAddDivision() != null) ? student.getAddDivision().getDivisionName() : "";
        }

        String className = standardName.isEmpty() ? divisionName
                : (divisionName.isEmpty() ? standardName : standardName + "-" + divisionName);
        dto.setClassName(className);

        // Resolve student registered fee (finalFee or totalFee)
        Double studentRegisteredFee = student.getFinalFee() != null ? student.getFinalFee() : student.getTotalFee();
        
        // Fee structure fields
        FeeStructure fs = fp.getFeeStructure();
        double total = 0.0;
        
        if (studentRegisteredFee != null && studentRegisteredFee > 0.0) {
            total = studentRegisteredFee;
        } else if (fs != null && fs.getAmount() != null) {
            total = fs.getAmount();
        } else {
            total = fp.getAmountPaid() != null ? fp.getAmountPaid() : 0.0;
        }

        dto.setTotalAmount(total);
        double paid = fp.getAmountPaid() != null ? fp.getAmountPaid() : 0.0;
        dto.setAmountDue(Math.max(0.0, total - paid));

        if (fs != null) {
            dto.setFeeName(fs.getFeeName());
            dto.setFeeType(fs.getFeeType());
            dto.setDueDate(fs.getDueDate());
        } else {
            dto.setFeeName(student.getFeeType() != null ? student.getFeeType() : "General Fees");
            dto.setFeeType(student.getFeeType() != null ? student.getFeeType() : "TUITION");
            dto.setDueDate(null);
        }

        return dto;
    }

    /** Concatenates non-null name parts with a space. */
    private String buildFullName(String first, String middle, String last) {
        StringBuilder sb = new StringBuilder();
        if (first != null && !first.isBlank()) sb.append(first.trim());
        if (middle != null && !middle.isBlank()) {
            if (sb.length() > 0) sb.append(" ");
            sb.append(middle.trim());
        }
        if (last != null && !last.isBlank()) {
            if (sb.length() > 0) sb.append(" ");
            sb.append(last.trim());
        }
        return sb.toString();
    }

    // ==================== FEE STRUCTURE ====================

    @Override
    public void createStructure(FeeStructureDTO dto) {
        FeeStructure fs = new FeeStructure();
        fs.setSchool(schoolRepository.findBySchoolId(dto.getSchoolId())
                .orElseThrow(() -> new RuntimeException("School not found")));
        if (dto.getAcademicYearId() != null)
            fs.setAcademicYear(academicYearRepository.findById(dto.getAcademicYearId()).orElse(null));
        fs.setAddStandard(standardRepository.findByStandardId(dto.getStandardId())
                .orElseThrow(() -> new RuntimeException("Standard not found")));
        fs.setFeeName(dto.getFeeName());
        fs.setFeeType(dto.getFeeType());
        fs.setAmount(dto.getAmount());
        fs.setDueDate(dto.getDueDate());
        fs.setDescription(dto.getDescription());
        feeStructureRepository.save(fs);
    }

    @Override
    public List<FeeStructureDTO> getStructuresByStandard(Long schoolId, Long standardId, Long academicYearId) {
        return feeStructureRepository
                .findBySchool_SchoolIdAndAddStandard_StandardIdAndAcademicYear_AcademicYearId(schoolId, standardId, academicYearId)
                .stream().map(this::toFeeStructureDTO).collect(Collectors.toList());
    }

    @Override
    public void updateStructure(FeeStructureDTO dto) {
        FeeStructure fs = feeStructureRepository
                .findByFeeStructureIdAndSchool_SchoolId(dto.getFeeStructureId(), dto.getSchoolId())
                .orElseThrow(() -> new RuntimeException("Fee structure not found"));
        if (dto.getFeeName() != null) fs.setFeeName(dto.getFeeName());
        if (dto.getFeeType() != null) fs.setFeeType(dto.getFeeType());
        if (dto.getAmount() != null) fs.setAmount(dto.getAmount());
        if (dto.getDueDate() != null) fs.setDueDate(dto.getDueDate());
        if (dto.getDescription() != null) fs.setDescription(dto.getDescription());
        feeStructureRepository.save(fs);
    }

    @Override
    public void deleteStructure(Long feeStructureId, Long schoolId) {
        FeeStructure fs = feeStructureRepository
                .findByFeeStructureIdAndSchool_SchoolId(feeStructureId, schoolId)
                .orElseThrow(() -> new RuntimeException("Fee structure not found"));
        feeStructureRepository.delete(fs);
    }

    // ==================== FEE PAYMENT — PAGE LOAD ====================

    /**
     * Single consolidated call for the Fee Collection UI page.
     * Returns summary stats (4 cards) + enriched payment records (table).
     */
    @Override
    public FeeCollectionPageDataDTO getPageData(Long schoolId, Long academicYearId) {
        List<FeePayment> payments = feePaymentRepository
                .findBySchool_SchoolIdAndAcademicYear_AcademicYearId(schoolId, academicYearId);

        // Enrich records
        List<FeePaymentDTO> records = payments.stream()
                .map(this::toFeePaymentDTO)
                .collect(Collectors.toList());

        // Compute summary stats
        double totalCollected = records.stream()
                .mapToDouble(r -> r.getAmountPaid() != null ? r.getAmountPaid() : 0.0)
                .sum();

        double totalPending = records.stream()
                .mapToDouble(r -> r.getAmountDue() != null ? r.getAmountDue() : 0.0)
                .sum();

        long paidCount = records.stream()
                .filter(r -> "PAID".equalsIgnoreCase(r.getStatus()))
                .count();

        long partialCount = records.stream()
                .filter(r -> "PARTIAL".equalsIgnoreCase(r.getStatus()))
                .count();

        long unpaidCount = records.stream()
                .filter(r -> "UNPAID".equalsIgnoreCase(r.getStatus()))
                .count();

        FeeCollectionSummaryDTO summary = new FeeCollectionSummaryDTO(
                totalCollected, totalPending, paidCount, partialCount, unpaidCount
        );

        return new FeeCollectionPageDataDTO(summary, records);
    }

    // ==================== FEE PAYMENT — CRUD ====================

    @Override
    public void createPayment(FeePaymentDTO dto) {
        FeePayment fp = new FeePayment();
        fp.setSchool(schoolRepository.findBySchoolId(dto.getSchoolId())
                .orElseThrow(() -> new RuntimeException("School not found")));
        if (dto.getAcademicYearId() != null)
            fp.setAcademicYear(academicYearRepository.findById(dto.getAcademicYearId())
                    .orElseThrow(() -> new RuntimeException("Academic Year not found")));
        fp.setAddStudent(studentRepository.findById(dto.getStudentId())
                .orElseThrow(() -> new RuntimeException("Student not found")));
        if (dto.getFeeStructureId() != null)
            fp.setFeeStructure(feeStructureRepository.findById(dto.getFeeStructureId()).orElse(null));
        fp.setAmountPaid(dto.getAmountPaid());
        fp.setPaymentDate(dto.getPaymentDate());
        fp.setPaymentMode(dto.getPaymentMode());
        fp.setTransactionId(dto.getTransactionId());
        fp.setStatus(dto.getStatus() != null ? dto.getStatus() : "PAID");
        fp.setRemark(dto.getRemark());

        // Auto-generate receipt number
        Optional<FeePayment> lastPayment = feePaymentRepository.findTopByOrderByPaymentIdDesc();
        long nextNum = lastPayment.isPresent() ? lastPayment.get().getPaymentId() + 1 : 1;
        fp.setReceiptNumber("RCP-" + String.format("%06d", nextNum));

        feePaymentRepository.save(fp);
    }

    @Override
    public List<FeePaymentDTO> getPaymentsByStudent(Long schoolId, Long studentId, Long academicYearId) {
        return feePaymentRepository
                .findBySchool_SchoolIdAndAddStudent_IdAndAcademicYear_AcademicYearId(schoolId, studentId, academicYearId)
                .stream().map(this::toFeePaymentDTO).collect(Collectors.toList());
    }

    @Override
    public List<FeePaymentDTO> getAllPayments(Long schoolId, Long academicYearId) {
        return feePaymentRepository
                .findBySchool_SchoolIdAndAcademicYear_AcademicYearId(schoolId, academicYearId)
                .stream().map(this::toFeePaymentDTO).collect(Collectors.toList());
    }

    @Override
    public void updatePayment(FeePaymentDTO dto) {
        FeePayment fp = feePaymentRepository
                .findByPaymentIdAndSchool_SchoolId(dto.getPaymentId(), dto.getSchoolId())
                .orElseThrow(() -> new RuntimeException("Payment record not found"));

        if (dto.getAmountPaid() != null) fp.setAmountPaid(dto.getAmountPaid());
        if (dto.getPaymentDate() != null) fp.setPaymentDate(dto.getPaymentDate());
        if (dto.getPaymentMode() != null) fp.setPaymentMode(dto.getPaymentMode());
        if (dto.getTransactionId() != null) fp.setTransactionId(dto.getTransactionId());
        if (dto.getRemark() != null) fp.setRemark(dto.getRemark());
        if (dto.getStatus() != null) fp.setStatus(dto.getStatus());
        if (dto.getFeeStructureId() != null)
            fp.setFeeStructure(feeStructureRepository.findById(dto.getFeeStructureId()).orElse(null));

        feePaymentRepository.save(fp);
    }

    @Override
    public void deletePayment(Long paymentId, Long schoolId) {
        FeePayment fp = feePaymentRepository
                .findByPaymentIdAndSchool_SchoolId(paymentId, schoolId)
                .orElseThrow(() -> new RuntimeException("Payment record not found"));
        feePaymentRepository.delete(fp);
    }
}
