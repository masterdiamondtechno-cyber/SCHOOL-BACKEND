package com.schoolmanagement.school.service.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.schoolmanagement.school.entity.AcademicYear;
import com.schoolmanagement.school.entity.AddDivision;
import com.schoolmanagement.school.entity.AddStandard;
import com.schoolmanagement.school.entity.AddStudent;
import com.schoolmanagement.school.entity.AddTeacher;
import com.schoolmanagement.school.entity.Role;
import com.schoolmanagement.school.entity.School;
import com.schoolmanagement.school.repository.AcademicYearRepository;
import com.schoolmanagement.school.repository.AddDivisionRepository;
import com.schoolmanagement.school.repository.AddStandardRepository;
import com.schoolmanagement.school.repository.AddStudentRepository;
import com.schoolmanagement.school.repository.AddTeacherRepository;
import com.schoolmanagement.school.repository.RoleRepository;
import com.schoolmanagement.school.repository.SchoolRepository;
import com.schoolmanagement.school.entity.StudentAcademicYear;
import com.schoolmanagement.school.repository.StudentAcademicYearRepository;
import com.schoolmanagement.school.service.EmailService;
import com.schoolmanagement.school.service.ExcelService;
import java.util.Optional;

@Service
public class ExcelServiceImpl implements ExcelService {

    @Autowired private SchoolRepository schoolRepository;
    @Autowired private AcademicYearRepository academicYearRepository;
    @Autowired private AddTeacherRepository teacherRepository;
    @Autowired private AddStudentRepository studentRepository;
    @Autowired private AddStandardRepository standardRepository;
    @Autowired private AddDivisionRepository divisionRepository;
    @Autowired private RoleRepository roleRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private EmailService emailService;
    @Autowired private StudentAcademicYearRepository mappingRepository;

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // ═══════════════════════════════════════════════════════════
    // TEMPLATE GENERATION
    // ═══════════════════════════════════════════════════════════

    private static final String[] TEACHER_HEADERS = {
        "teacherName", "gender", "dateOfBirth", "mobileNumber", "emailId",
        "address", "aadharCardNumber", "salaryType", "joiningDate",
        "annualSalary", "monthlySalary", "pfStatus", "pfAccount",
        "paidInHandSalary"
    };

    private static final String[] STUDENT_HEADERS = {
        "firstName", "middleName", "lastName", "gender", "dateOfBirth",
        "mobileNumber", "emailId", "aadharCardNumber", "guardianName", "parentMobileNumber",
        "parentEmailId", "address", "rollNumber", "admissionDate",
        "feeType", "totalFee", "discount", "finalFee"
    };

    @Override
    public ByteArrayInputStream generateTeacherTemplate() {
        return createTemplate("Teachers", TEACHER_HEADERS);
    }

    @Override
    public ByteArrayInputStream generateStudentTemplate() {
        return createTemplate("Students", STUDENT_HEADERS);
    }

    private ByteArrayInputStream createTemplate(String sheetName, String[] headers) {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet(sheetName);

            // Header style
            CellStyle headerStyle = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            headerStyle.setFont(font);

            // Write header row
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
                sheet.setColumnWidth(i, 5000);
            }

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate template: " + e.getMessage());
        }
    }

    // ═══════════════════════════════════════════════════════════
    // TEACHER UPLOAD
    // ═══════════════════════════════════════════════════════════

    @Override
    public Map<String, Object> uploadTeachers(MultipartFile file, Long schoolId, Long academicYearId, Long standardId, Long divisionId) {
        School school = schoolRepository.findById(schoolId)
                .orElseThrow(() -> new RuntimeException("School not found with id: " + schoolId));

        AcademicYear academicYear = academicYearRepository.findById(academicYearId)
                .orElseThrow(() -> new RuntimeException("Academic Year not found with id: " + academicYearId));

        AddStandard standard = null;
        if (standardId != null) {
            standard = standardRepository.findById(standardId)
                    .orElseThrow(() -> new RuntimeException("Standard not found with id: " + standardId));
        }

        AddDivision division = null;
        if (divisionId != null) {
            division = divisionRepository.findById(divisionId)
                    .orElseThrow(() -> new RuntimeException("Division not found with id: " + divisionId));
        }

        Role teacherRole = roleRepository.findById(3L)
                .orElseThrow(() -> new RuntimeException("Teacher role (id=3) not found"));

        List<Map<String, Object>> failedRows = new ArrayList<>();
        int successCount = 0;
        int totalRows = 0;

        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);

            Row headerRow = sheet.getRow(0);
            if (headerRow == null) throw new RuntimeException("Excel file is empty");

            Map<String, Integer> colMap = new LinkedHashMap<>();
            for (int i = 0; i < headerRow.getLastCellNum(); i++) {
                Cell cell = headerRow.getCell(i);
                if (cell != null) {
                    colMap.put(cell.getStringCellValue().trim(), i);
                }
            }

            for (int rowIdx = 1; rowIdx <= sheet.getLastRowNum(); rowIdx++) {
                Row row = sheet.getRow(rowIdx);
                if (row == null || isRowEmpty(row)) continue;
                totalRows++;

                try {
                    String teacherName = getCellString(row, colMap.get("teacherName"));
                    String mobileNumber = getCellString(row, colMap.get("mobileNumber"));
                    String emailId = getCellString(row, colMap.get("emailId"));

                    if (teacherName == null || teacherName.isBlank())
                        throw new RuntimeException("'teacherName' is required");
                    if (mobileNumber == null || mobileNumber.isBlank())
                        throw new RuntimeException("'mobileNumber' is required");
                    if (emailId == null || emailId.isBlank())
                        throw new RuntimeException("'emailId' is required");

                    if (teacherRepository.existsBySchool_SchoolIdAndEmailId(schoolId, emailId))
                        throw new RuntimeException("Email '" + emailId + "' already exists");
                    if (teacherRepository.existsBySchool_SchoolIdAndMobileNumber(schoolId, mobileNumber))
                        throw new RuntimeException("Mobile '" + mobileNumber + "' already exists");

                    AddTeacher teacher = new AddTeacher();
                    teacher.setSchool(school);
                    teacher.setAcademicYear(academicYear);
                    teacher.setAddStandard(standard);
                    teacher.setAddDivision(division);
                    teacher.setRole(teacherRole);
                    teacher.setTeacherName(teacherName);
                    teacher.setGender(getCellString(row, colMap.get("gender")));
                    teacher.setDateOfBirth(parseDateSafe(getCellString(row, colMap.get("dateOfBirth"))));
                    teacher.setMobileNumber(mobileNumber);
                    teacher.setEmailId(emailId);
                    teacher.setAddress(getCellString(row, colMap.get("address")));
                    teacher.setAadharCardNumber(getCellString(row, colMap.get("aadharCardNumber")));
                    teacher.setSalaryType(getCellString(row, colMap.get("salaryType")));
                    teacher.setJoiningDate(parseDateSafe(getCellString(row, colMap.get("joiningDate"))));
                    teacher.setAnnualSalary(getCellString(row, colMap.get("annualSalary")));
                    teacher.setMonthlySalary(getCellString(row, colMap.get("monthlySalary")));
                    teacher.setPfStatus(getCellString(row, colMap.get("pfStatus")));
                    teacher.setPfAccount(getCellString(row, colMap.get("pfAccount")));
                    teacher.setPaidInHandSalary(getCellString(row, colMap.get("paidInHandSalary")));
                    teacher.setStatus("ACTIVE");
                    teacher.setPassword(passwordEncoder.encode(mobileNumber));

                    teacherRepository.save(teacher);

                    // Update division / standard with this teacher as class teacher
                    if (teacher.getAddDivision() != null) {
                        AddDivision div = teacher.getAddDivision();
                        div.setClassTeacher(teacher);
                        divisionRepository.save(div);
                    } else if (teacher.getAddStandard() != null) {
                        AddStandard std = teacher.getAddStandard();
                        std.setClassTeacher(teacher);
                        standardRepository.save(std);
                    }

                    successCount++;

                    // Send login credentials email
                    try {
                        emailService.sendTeacherCredentials(emailId, mobileNumber);
                    } catch (Exception emailEx) {
                        // Email failure should not block the upload
                    }

                } catch (Exception e) {
                    Map<String, Object> error = new LinkedHashMap<>();
                    error.put("row", rowIdx + 1);
                    error.put("error", e.getMessage());
                    failedRows.add(error);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read Excel file: " + e.getMessage());
        }

        return buildUploadResponse(totalRows, successCount, failedRows);
    }

    // ═══════════════════════════════════════════════════════════
    // STUDENT UPLOAD
    // ═══════════════════════════════════════════════════════════

    @Override
    public Map<String, Object> uploadStudents(MultipartFile file, Long schoolId, Long academicYearId, Long standardId, Long divisionId) {
        School school = schoolRepository.findById(schoolId)
                .orElseThrow(() -> new RuntimeException("School not found with id: " + schoolId));

        AcademicYear academicYear = academicYearRepository.findById(academicYearId)
                .orElseThrow(() -> new RuntimeException("Academic Year not found with id: " + academicYearId));

        AddStandard standard = standardRepository.findById(standardId)
                .orElseThrow(() -> new RuntimeException("Standard not found with id: " + standardId));

        AddDivision division = divisionRepository.findById(divisionId)
                .orElseThrow(() -> new RuntimeException("Division not found with id: " + divisionId));

        Role studentRole = roleRepository.findById(4L)
                .orElseThrow(() -> new RuntimeException("Student role (id=4) not found"));

        List<Map<String, Object>> failedRows = new ArrayList<>();
        int successCount = 0;
        int totalRows = 0;

        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);

            Row headerRow = sheet.getRow(0);
            if (headerRow == null) throw new RuntimeException("Excel file is empty");

            Map<String, Integer> colMap = new LinkedHashMap<>();
            for (int i = 0; i < headerRow.getLastCellNum(); i++) {
                Cell cell = headerRow.getCell(i);
                if (cell != null) {
                    colMap.put(cell.getStringCellValue().trim(), i);
                }
            }

            String schoolName = school.getSchoolName();
            String cleanedSchoolName = schoolName.replaceAll("\\s+", "").toLowerCase();
            String prefix = cleanedSchoolName.length() >= 4
                    ? cleanedSchoolName.substring(0, 4) : cleanedSchoolName;

            Optional<AddStudent> lastStudentOpt = studentRepository.findTopByOrderByIdDesc();
            int nextNumber = 1;
            if (lastStudentOpt.isPresent() && lastStudentOpt.get().getStudentId() != null) {
                String lastStudentId = lastStudentOpt.get().getStudentId();
                String numberPart = lastStudentId.substring(4);
                try {
                    nextNumber = Integer.parseInt(numberPart) + 1;
                } catch (NumberFormatException e) {
                    nextNumber = (int) (studentRepository.count() + 1);
                }
            }

            for (int rowIdx = 1; rowIdx <= sheet.getLastRowNum(); rowIdx++) {
                Row row = sheet.getRow(rowIdx);
                if (row == null || isRowEmpty(row)) continue;
                totalRows++;

                try {
                    String firstName = getCellString(row, colMap.get("firstName"));
                    String lastName = getCellString(row, colMap.get("lastName"));
                    String mobileNumber = getCellString(row, colMap.get("mobileNumber"));
                    String emailId = getCellString(row, colMap.get("emailId"));
                    String aadharCardNumber = getCellString(row, colMap.get("aadharCardNumber"));

                    if (firstName == null || firstName.isBlank())
                        throw new RuntimeException("'firstName' is required");
                    if (lastName == null || lastName.isBlank())
                        throw new RuntimeException("'lastName' is required");
                    if (mobileNumber == null || mobileNumber.isBlank())
                        throw new RuntimeException("'mobileNumber' is required");
                    if (emailId == null || emailId.isBlank())
                        throw new RuntimeException("'emailId' is required");
                    if (aadharCardNumber == null || aadharCardNumber.isBlank())
                        throw new RuntimeException("'aadharCardNumber' is required");

                    if (studentRepository.existsByEmailIdAndSchool_SchoolId(emailId, schoolId))
                        throw new RuntimeException("Email '" + emailId + "' already exists");
                    if (studentRepository.existsByMobileNumberAndSchool_SchoolId(mobileNumber, schoolId))
                        throw new RuntimeException("Mobile '" + mobileNumber + "' already exists");

                    AddStudent student = new AddStudent();
                    student.setSchool(school);
                    student.setAcademicYear(academicYear);
                    student.setAddStandard(standard);
                    student.setAddDivision(division);
                    student.setRole(studentRole);
                    
                    // Generate Unique PRN
                    int currentYear = LocalDate.now().getYear();
                    String randomSuffix = String.format("%05d", new java.security.SecureRandom().nextInt(100000));
                    String generatedPrn = "PRN-" + currentYear + "-" + (schoolId != null ? schoolId : 0) + "-" + randomSuffix;
                    student.setPrnNumber(generatedPrn);

                    // Generate Student ID
                    student.setStudentId(prefix + String.format("%02d", nextNumber++));

                    student.setFirstName(firstName);
                    student.setMiddleName(getCellString(row, colMap.get("middleName")));
                    student.setLastName(lastName);
                    student.setGender(getCellString(row, colMap.get("gender")));
                    student.setDateOfBirth(parseDateSafe(getCellString(row, colMap.get("dateOfBirth"))));
                    student.setMobileNumber(mobileNumber);
                    student.setEmailId(emailId);
                    student.setAadharNumber(aadharCardNumber);
                    student.setGuardianName(getCellString(row, colMap.get("guardianName")));
                    student.setParentMobileNumber(getCellString(row, colMap.get("parentMobileNumber")));
                    student.setParentEmailId(getCellString(row, colMap.get("parentEmailId")));
                    student.setAddress(getCellString(row, colMap.get("address")));
                    student.setRollNumber(getCellString(row, colMap.get("rollNumber")));
                    student.setAdmissionDate(parseDateSafe(getCellString(row, colMap.get("admissionDate"))));
                    student.setFeeType(getCellString(row, colMap.get("feeType")));
                    student.setTotalFee(parseDoubleSafe(getCellString(row, colMap.get("totalFee"))));
                    student.setDiscount(parseDoubleSafe(getCellString(row, colMap.get("discount"))));
                    student.setFinalFee(parseDoubleSafe(getCellString(row, colMap.get("finalFee"))));
                    student.setStatus("ACTIVE");
                    
                    String dobPassword;
                    if (student.getDateOfBirth() != null) {
                        dobPassword = student.getDateOfBirth().format(DateTimeFormatter.ofPattern("ddMMyyyy"));
                    } else {
                        dobPassword = "12345678";
                    }
                    student.setPassword(passwordEncoder.encode(dobPassword));
                    student.setPasswordChangeRequired(true);

                    // ===== Parent Password Logic =====
                    if (student.getParentEmailId() != null && !student.getParentEmailId().trim().isEmpty()) {
                        Optional<AddStudent> existingParent = studentRepository
                                .findFirstByParentEmailIdAndParentPasswordIsNotNull(student.getParentEmailId());

                        if (existingParent.isPresent()) {
                            student.setParentPassword(existingParent.get().getParentPassword());
                        } else {
                            String parentPlainPassword = generateRandomPassword();
                            student.setParentPassword(passwordEncoder.encode(parentPlainPassword));
                            
                            // Send parent credentials email
                            String childName = (firstName != null ? firstName : "") + " "
                                    + (lastName != null ? lastName : "");
                            try {
                                emailService.sendParentCredentials(student.getParentEmailId(), childName.trim(), parentPlainPassword);
                            } catch (Exception e) {
                                // Log/ignore
                            }
                        }
                    }

                    studentRepository.save(student);

                    // Save mapping to StudentAcademicYear history
                    StudentAcademicYear mapping = new StudentAcademicYear();
                    mapping.setStudent(student);
                    mapping.setAcademicYear(student.getAcademicYear());
                    mapping.setStandard(student.getAddStandard());
                    mapping.setDivision(student.getAddDivision());
                    mapping.setRollNo(student.getRollNumber());
                    mapping.setStatus("ACTIVE");
                    mappingRepository.save(mapping);

                    successCount++;

                    // Send login credentials email
                    try {
                        emailService.sendStudentCredentials(emailId, student.getStudentId(), dobPassword);
                    } catch (Exception emailEx) {
                        // Email failure should not block the upload
                    }

                } catch (Exception e) {
                    Map<String, Object> error = new LinkedHashMap<>();
                    error.put("row", rowIdx + 1);
                    error.put("error", e.getMessage());
                    failedRows.add(error);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read Excel file: " + e.getMessage());
        }

        return buildUploadResponse(totalRows, successCount, failedRows);
    }

    // ═══════════════════════════════════════════════════════════
    // HELPERS
    // ═══════════════════════════════════════════════════════════

    private String getCellString(Row row, Integer colIdx) {
        if (colIdx == null) return null;
        Cell cell = row.getCell(colIdx);
        if (cell == null) return null;

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getLocalDateTimeCellValue().toLocalDate().toString();
                }
                double val = cell.getNumericCellValue();
                if (val == Math.floor(val) && !Double.isInfinite(val)) {
                    return String.valueOf((long) val);
                }
                return String.valueOf(val);
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case BLANK:
                return null;
            default:
                return null;
        }
    }

    private boolean isRowEmpty(Row row) {
        for (int c = row.getFirstCellNum(); c < row.getLastCellNum(); c++) {
            Cell cell = row.getCell(c);
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                return false;
            }
        }
        return true;
    }

    private LocalDate parseDateSafe(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return LocalDate.parse(value.trim(), DATE_FORMAT);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private Double parseDoubleSafe(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Map<String, Object> buildUploadResponse(int totalRows, int successCount, List<Map<String, Object>> failedRows) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("totalRows", totalRows);
        response.put("successCount", successCount);
        response.put("failedCount", failedRows.size());
        response.put("failedRows", failedRows);
        return response;
    }

    private String generateRandomPassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        java.security.SecureRandom random = new java.security.SecureRandom();
        StringBuilder password = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            password.append(chars.charAt(random.nextInt(chars.length())));
        }
        return password.toString();
    }
}
