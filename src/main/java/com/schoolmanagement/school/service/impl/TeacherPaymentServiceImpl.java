package com.schoolmanagement.school.serviceImpl;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.schoolmanagement.school.dto.TeacherPaymentDTO;
import com.schoolmanagement.school.dto.TeacherPaymentPageDataDTO;
import com.schoolmanagement.school.entity.AcademicYear;
import com.schoolmanagement.school.entity.AddSubject;
import com.schoolmanagement.school.entity.AddTeacher;
import com.schoolmanagement.school.entity.School;
import com.schoolmanagement.school.entity.TeacherPayment;
import com.schoolmanagement.school.repository.AcademicYearRepository;
import com.schoolmanagement.school.repository.AddSubjectRepository;
import com.schoolmanagement.school.repository.AddTeacherRepository;
import com.schoolmanagement.school.repository.SchoolRepository;
import com.schoolmanagement.school.repository.TeacherPaymentRepository;
import com.schoolmanagement.school.service.TeacherPaymentService;

@Service
public class TeacherPaymentServiceImpl implements TeacherPaymentService {

    private final TeacherPaymentRepository teacherPaymentRepository;
    private final AddTeacherRepository teacherRepository;
    private final AddSubjectRepository subjectRepository;
    private final SchoolRepository schoolRepository;
    private final AcademicYearRepository academicYearRepository;

    public TeacherPaymentServiceImpl(TeacherPaymentRepository teacherPaymentRepository,
                                     AddTeacherRepository teacherRepository,
                                     AddSubjectRepository subjectRepository,
                                     SchoolRepository schoolRepository,
                                     AcademicYearRepository academicYearRepository) {
        this.teacherPaymentRepository = teacherPaymentRepository;
        this.teacherRepository = teacherRepository;
        this.subjectRepository = subjectRepository;
        this.schoolRepository = schoolRepository;
        this.academicYearRepository = academicYearRepository;
    }

    @Override
    public TeacherPaymentPageDataDTO getPageData(Long schoolId, Long academicYearId,
                                                  String statusFilter, String month, Integer year) {
        List<TeacherPayment> payments;
        if (month != null && year != null) {
            payments = teacherPaymentRepository.findBySchool_SchoolIdAndPaymentMonthAndPaymentYear(schoolId, month, year);
        } else {
            payments = teacherPaymentRepository.findBySchool_SchoolIdAndAcademicYear_AcademicYearId(schoolId, academicYearId);
        }

        if (statusFilter != null && !statusFilter.isEmpty() && !"ALL".equalsIgnoreCase(statusFilter)) {
            payments = payments.stream()
                    .filter(p -> statusFilter.equalsIgnoreCase(p.getStatus()))
                    .collect(Collectors.toList());
        }

        double totalPayroll = 0.0;
        double totalDisbursed = 0.0;
        double totalPending = 0.0;
        long pendingCount = 0;

        for (TeacherPayment p : payments) {
            double net = p.getNetAmountPaid() != null ? p.getNetAmountPaid() : 0.0;
            totalPayroll += net;
            if ("PAID".equalsIgnoreCase(p.getStatus())) {
                totalDisbursed += net;
            }
            if ("PENDING".equalsIgnoreCase(p.getStatus())) {
                totalPending += net;
                pendingCount++;
            }
        }

        String payPeriod;
        if (month != null && year != null) {
            payPeriod = formatMonthLabel(month, year);
        } else {
            LocalDate now = LocalDate.now();
            payPeriod = formatMonthLabel(now.getMonth().name(), now.getYear());
        }

        List<Map<String, Object>> paymentRows = new ArrayList<>();
        for (TeacherPayment p : payments) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("teacherPaymentId", p.getTeacherPaymentId());
            row.put("receiptNumber", p.getReceiptNumber());

            String teacherName = (p.getTeacher() != null && p.getTeacher().getTeacherName() != null)
                    ? p.getTeacher().getTeacherName() : "-";
            row.put("teacherName", teacherName);
            row.put("avatarInitials", buildAvatarInitials(teacherName));

            String department = "-";
            if (p.getTeacher() != null && p.getTeacher().getId() != null) {
                List<AddSubject> subjects = subjectRepository.findByAddTeacher_IdAndSchool_SchoolId(
                        p.getTeacher().getId(), schoolId);
                if (subjects != null && !subjects.isEmpty()) {
                    department = subjects.get(0).getSubjectName() != null
                            ? subjects.get(0).getSubjectName() : "-";
                }
            }
            row.put("department", department);

            row.put("basicSalary", p.getBasicSalary());
            row.put("bonus", p.getBonus());
            row.put("deductions", p.getDeductions());
            row.put("netAmountPaid", p.getNetAmountPaid());
            row.put("paymentMonth", p.getPaymentMonth());
            row.put("paymentYear", p.getPaymentYear());

            String monthLabel = "-";
            if (p.getPaymentMonth() != null && p.getPaymentYear() != null) {
                monthLabel = formatMonthLabel(p.getPaymentMonth(), p.getPaymentYear());
            }
            row.put("monthLabel", monthLabel);

            row.put("status", p.getStatus());
            row.put("paymentMode", p.getPaymentMode());
            row.put("paymentDate", p.getPaymentDate());
            row.put("remark", p.getRemark());
            row.put("transactionId", p.getTransactionId());

            paymentRows.add(row);
        }

        TeacherPaymentPageDataDTO pageData = new TeacherPaymentPageDataDTO();
        pageData.setTotalPayroll(totalPayroll);
        pageData.setTotalDisbursed(totalDisbursed);
        pageData.setTotalPending(totalPending);
        pageData.setPendingCount(pendingCount);
        pageData.setPayPeriod(payPeriod);
        pageData.setPayments(paymentRows);

        return pageData;
    }

    @Override
    public void createPayment(TeacherPaymentDTO dto) {
        AddTeacher teacher = teacherRepository.findByIdAndSchool_SchoolId(dto.getTeacherId(), dto.getSchoolId())
                .orElseThrow(() -> new RuntimeException("Teacher not found"));

        if (teacherPaymentRepository.existsByTeacher_IdAndPaymentMonthAndPaymentYear(
                dto.getTeacherId(), dto.getPaymentMonth(), dto.getPaymentYear())) {
            throw new RuntimeException("Payment already exists for this teacher for " + dto.getPaymentMonth() + " " + dto.getPaymentYear());
        }

        // Validate values
        if (dto.getBasicSalary() != null && dto.getBasicSalary() < 0) {
            throw new RuntimeException("Basic salary cannot be negative");
        }
        if (dto.getBonus() != null && dto.getBonus() < 0) {
            throw new RuntimeException("Bonus cannot be negative");
        }
        if (dto.getDeductions() != null && dto.getDeductions() < 0) {
            throw new RuntimeException("Deductions cannot be negative");
        }

        double basic = dto.getBasicSalary() != null ? dto.getBasicSalary() : 0.0;
        double ded = dto.getDeductions() != null ? dto.getDeductions() : 0.0;
        double bon = dto.getBonus() != null ? dto.getBonus() : 0.0;
        double net = basic - ded + bon;

        if (net < 0) {
            throw new RuntimeException("Net salary cannot be negative (Deductions cannot exceed Basic Salary + Bonus)");
        }

        TeacherPayment tp = new TeacherPayment();

        School school = schoolRepository.findBySchoolId(dto.getSchoolId())
                .orElseThrow(() -> new RuntimeException("School not found"));
        tp.setSchool(school);

        if (dto.getAcademicYearId() != null) {
            AcademicYear ay = academicYearRepository.findById(dto.getAcademicYearId())
                    .orElseThrow(() -> new RuntimeException("Academic Year not found"));
            tp.setAcademicYear(ay);
        }

        tp.setTeacher(teacher);
        tp.setPaymentMonth(dto.getPaymentMonth());
        tp.setPaymentYear(dto.getPaymentYear());
        tp.setBasicSalary(dto.getBasicSalary());
        tp.setDeductions(dto.getDeductions());
        tp.setBonus(dto.getBonus());
        tp.setNetAmountPaid(net);

        tp.setPaymentMode(dto.getPaymentMode());
        tp.setTransactionId(dto.getTransactionId());
        tp.setPaymentDate(dto.getPaymentDate());
        tp.setRemark(dto.getRemark());
        tp.setStatus(dto.getStatus() != null ? dto.getStatus() : "PENDING");

        if (dto.getReceiptNumber() != null && !dto.getReceiptNumber().isEmpty()) {
            tp.setReceiptNumber(dto.getReceiptNumber());
        } else {
            String maxReceipt = teacherPaymentRepository.findMaxReceiptNumber(dto.getSchoolId());
            int nextNum;
            if (maxReceipt == null || maxReceipt.isEmpty()) {
                nextNum = 1;
            } else {
                nextNum = Integer.parseInt(maxReceipt.replace("#PAY", "")) + 1;
            }
            tp.setReceiptNumber(String.format("#PAY%03d", nextNum));
        }

        teacherPaymentRepository.save(tp);
    }

    @Override
    public void updatePayment(TeacherPaymentDTO dto) {
        TeacherPayment tp = teacherPaymentRepository
                .findByTeacherPaymentIdAndSchool_SchoolId(dto.getTeacherPaymentId(), dto.getSchoolId())
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        // Validate values (taking previous values if current is null)
        double basic = dto.getBasicSalary() != null ? dto.getBasicSalary() : (tp.getBasicSalary() != null ? tp.getBasicSalary() : 0.0);
        double ded = dto.getDeductions() != null ? dto.getDeductions() : (tp.getDeductions() != null ? tp.getDeductions() : 0.0);
        double bon = dto.getBonus() != null ? dto.getBonus() : (tp.getBonus() != null ? tp.getBonus() : 0.0);

        if (basic < 0) {
            throw new RuntimeException("Basic salary cannot be negative");
        }
        if (ded < 0) {
            throw new RuntimeException("Deductions cannot be negative");
        }
        if (bon < 0) {
            throw new RuntimeException("Bonus cannot be negative");
        }

        double net = basic - ded + bon;
        if (net < 0) {
            throw new RuntimeException("Net salary cannot be negative (Deductions cannot exceed Basic Salary + Bonus)");
        }

        // Validate duplicate month/year for the teacher if changed
        String targetMonth = dto.getPaymentMonth() != null ? dto.getPaymentMonth() : tp.getPaymentMonth();
        Integer targetYear = dto.getPaymentYear() != null ? dto.getPaymentYear() : tp.getPaymentYear();

        if (dto.getPaymentMonth() != null || dto.getPaymentYear() != null) {
            List<TeacherPayment> existingPayments = teacherPaymentRepository.findByTeacher_IdAndSchool_SchoolId(tp.getTeacher().getId(), dto.getSchoolId());
            for (TeacherPayment existing : existingPayments) {
                if (!existing.getTeacherPaymentId().equals(tp.getTeacherPaymentId()) &&
                        existing.getPaymentMonth().equalsIgnoreCase(targetMonth) &&
                        existing.getPaymentYear().equals(targetYear)) {
                    throw new RuntimeException("Payment already exists for this teacher for " + targetMonth + " " + targetYear);
                }
            }
        }

        boolean salaryChanged = false;
        if (dto.getBasicSalary() != null) { tp.setBasicSalary(dto.getBasicSalary()); salaryChanged = true; }
        if (dto.getDeductions() != null) { tp.setDeductions(dto.getDeductions()); salaryChanged = true; }
        if (dto.getBonus() != null) { tp.setBonus(dto.getBonus()); salaryChanged = true; }

        if (salaryChanged) {
            tp.setNetAmountPaid(net);
        }

        if (dto.getPaymentMonth() != null) tp.setPaymentMonth(dto.getPaymentMonth());
        if (dto.getPaymentYear() != null) tp.setPaymentYear(dto.getPaymentYear());
        if (dto.getPaymentMode() != null) tp.setPaymentMode(dto.getPaymentMode());
        if (dto.getTransactionId() != null) tp.setTransactionId(dto.getTransactionId());
        if (dto.getPaymentDate() != null) tp.setPaymentDate(dto.getPaymentDate());
        if (dto.getStatus() != null) tp.setStatus(dto.getStatus());
        if (dto.getRemark() != null) tp.setRemark(dto.getRemark());
        if (dto.getReceiptNumber() != null) tp.setReceiptNumber(dto.getReceiptNumber());

        teacherPaymentRepository.save(tp);
    }

    @Override
    public void deletePayment(Long teacherPaymentId, Long schoolId) {
        TeacherPayment tp = teacherPaymentRepository
                .findByTeacherPaymentIdAndSchool_SchoolId(teacherPaymentId, schoolId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));
        teacherPaymentRepository.delete(tp);
    }

    @Override
    public void markAsPaid(TeacherPaymentDTO dto) {
        TeacherPayment tp = teacherPaymentRepository
                .findByTeacherPaymentIdAndSchool_SchoolId(dto.getTeacherPaymentId(), dto.getSchoolId())
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        tp.setStatus("PAID");
        tp.setPaymentMode(dto.getPaymentMode());
        tp.setPaymentDate(dto.getPaymentDate());
        if (dto.getTransactionId() != null) tp.setTransactionId(dto.getTransactionId());
        if (dto.getRemark() != null) tp.setRemark(dto.getRemark());

        teacherPaymentRepository.save(tp);
    }

    private String buildAvatarInitials(String name) {
        if (name == null || name.trim().isEmpty() || "-".equals(name)) return "-";
        String[] parts = name.trim().split("\\s+");
        StringBuilder initials = new StringBuilder();
        for (String part : parts) {
            if (!part.isEmpty()) {
                initials.append(part.charAt(0));
            }
        }
        return initials.toString().toUpperCase();
    }

    private String formatMonthLabel(String month, int year) {
        if (month == null || month.isEmpty()) return "-";
        return month.substring(0, 1).toUpperCase() + month.substring(1).toLowerCase() + " " + year;
    }
}
