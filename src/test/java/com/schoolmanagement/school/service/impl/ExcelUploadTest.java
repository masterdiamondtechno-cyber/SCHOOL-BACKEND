package C:.Users.Asus.Desktop.school-management-restructured.src.test.com.schoolmanagement.school.service.impl;

import com.schoolmanagement.school.entity.*;
import com.schoolmanagement.school.repository.*;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class ExcelUploadTest {

    @Autowired
    private ExcelServiceImpl excelService;

    @Autowired
    private SchoolRepository schoolRepository;

    @Autowired
    private AcademicYearRepository academicYearRepository;

    @Autowired
    private AddStandardRepository standardRepository;

    @Autowired
    private AddDivisionRepository divisionRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private AddStudentRepository studentRepository;

    @Autowired
    private AddTeacherRepository teacherRepository;

    @Autowired
    private StudentAcademicYearRepository mappingRepository;

    private School school;
    private AcademicYear academicYear;
    private AddStandard standard;
    private AddDivision division;
    private Role teacherRole;
    private Role studentRole;

    @BeforeEach
    public void setup() {
        school = new School();
        school.setSchoolName("Greenwood High");
        school.setUdiseNo("UDISE999");
        school.setEmail("greenwood@test.com");
        school.setPhoneNo("1234567890");
        school.setRegistrationStatus("APPROVED");
        school = schoolRepository.save(school);

        academicYear = new AcademicYear();
        academicYear.setYearName("2026-2027");
        academicYear.setStartDate(LocalDate.of(2026, 6, 1));
        academicYear.setEndDate(LocalDate.of(2027, 5, 31));
        academicYear.setSchool(school);
        academicYear.setIsCurrent(true);
        academicYear = academicYearRepository.save(academicYear);

        standard = new AddStandard();
        standard.setStandard("10th Standard");
        standard.setSchool(school);
        standard = standardRepository.save(standard);

        division = new AddDivision();
        division.setDivisionName("Division A");
        division.setAddStandard(standard);
        division.setAcademicYear(academicYear);
        division.setSchool(school);
        division = divisionRepository.save(division);

        // Ensure roles are in the DB or created
        teacherRole = roleRepository.findById(3L).orElseGet(() -> {
            Role r = new Role();
            r.setRoleId(3L);
            r.setName("TEACHER");
            return roleRepository.save(r);
        });

        studentRole = roleRepository.findById(4L).orElseGet(() -> {
            Role r = new Role();
            r.setRoleId(4L);
            r.setName("STUDENT");
            return roleRepository.save(r);
        });
    }

    @Test
    public void testStudentUpload() throws IOException {
        String[] headers = {
            "firstName", "middleName", "lastName", "gender", "dateOfBirth",
            "mobileNumber", "emailId", "aadharCardNumber", "guardianName", "parentMobileNumber",
            "parentEmailId", "address", "rollNumber", "admissionDate",
            "feeType", "totalFee", "discount", "finalFee"
        };

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Students");
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                headerRow.createCell(i).setCellValue(headers[i]);
            }

            Row dataRow = sheet.createRow(1);
            dataRow.createCell(0).setCellValue("John");
            dataRow.createCell(1).setCellValue("M");
            dataRow.createCell(2).setCellValue("Doe");
            dataRow.createCell(3).setCellValue("Male");
            dataRow.createCell(4).setCellValue("2010-05-15");
            dataRow.createCell(5).setCellValue("9876543210");
            dataRow.createCell(6).setCellValue("john.doe@test.com");
            dataRow.createCell(7).setCellValue("123456789012");
            dataRow.createCell(8).setCellValue("Richard Doe");
            dataRow.createCell(9).setCellValue("9876543211");
            dataRow.createCell(10).setCellValue("richard.doe@test.com");
            dataRow.createCell(11).setCellValue("123 Main St");
            dataRow.createCell(12).setCellValue("101");
            dataRow.createCell(13).setCellValue("2026-06-01");
            dataRow.createCell(14).setCellValue("Standard");
            dataRow.createCell(15).setCellValue(5000);
            dataRow.createCell(16).setCellValue(500);
            dataRow.createCell(17).setCellValue(4500);

            workbook.write(out);
            MockMultipartFile file = new MockMultipartFile("file", "students.xlsx",
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", out.toByteArray());

            Map<String, Object> response = excelService.uploadStudents(file, school.getSchoolId(),
                    academicYear.getAcademicYearId(), standard.getStandardId(), division.getDivisionId());

            System.out.println("Student upload response: " + response);
            assertEquals(1, response.get("totalRows"));
            assertEquals(1, response.get("successCount"));
            assertEquals(0, response.get("failedCount"));

            List<AddStudent> students = studentRepository.findBySchool_SchoolId(school.getSchoolId());
            assertEquals(1, students.size());
            AddStudent student = students.get(0);
            System.out.println("Created Student ID: " + student.getStudentId());
            System.out.println("Created Student PRN: " + student.getPrnNumber());
            assertNotNull(student.getStudentId(), "Student ID should not be null!");
            assertNotNull(student.getPrnNumber(), "PRN Number should not be null!");
            
            // Check if mapping was created
            List<StudentAcademicYear> mappings = mappingRepository.findByStudent_Id(student.getId());
            assertFalse(mappings.isEmpty(), "Student academic year mapping should not be empty!");
        }
    }

    @Test
    public void testTeacherUpload() throws IOException {
        String[] headers = {
            "teacherName", "gender", "dateOfBirth", "mobileNumber", "emailId",
            "address", "aadharCardNumber", "salaryType", "joiningDate",
            "annualSalary", "monthlySalary", "pfStatus", "pfAccount",
            "paidInHandSalary"
        };

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Teachers");
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                headerRow.createCell(i).setCellValue(headers[i]);
            }

            Row dataRow = sheet.createRow(1);
            dataRow.createCell(0).setCellValue("Jane Smith");
            dataRow.createCell(1).setCellValue("Female");
            dataRow.createCell(2).setCellValue("1985-08-20");
            dataRow.createCell(3).setCellValue("8765432109");
            dataRow.createCell(4).setCellValue("jane.smith@test.com");
            dataRow.createCell(5).setCellValue("456 Oak St");
            dataRow.createCell(6).setCellValue("123456789012");
            dataRow.createCell(7).setCellValue("Monthly");
            dataRow.createCell(8).setCellValue("2020-01-01");
            dataRow.createCell(9).setCellValue("600000");
            dataRow.createCell(10).setCellValue("50000");
            dataRow.createCell(11).setCellValue("Active");
            dataRow.createCell(12).setCellValue("PF9999");
            dataRow.createCell(13).setCellValue("45000");

            workbook.write(out);
            MockMultipartFile file = new MockMultipartFile("file", "teachers.xlsx",
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", out.toByteArray());

            Map<String, Object> response = excelService.uploadTeachers(file, school.getSchoolId(),
                    academicYear.getAcademicYearId(), standard.getStandardId(), division.getDivisionId());

            System.out.println("Teacher upload response: " + response);
            assertEquals(1, response.get("totalRows"));
            assertEquals(1, response.get("successCount"));
            assertEquals(0, response.get("failedCount"));

            List<AddTeacher> teachers = teacherRepository.findBySchool_SchoolIdAndAcademicYear_AcademicYearId(
                    school.getSchoolId(), academicYear.getAcademicYearId());
            assertEquals(1, teachers.size());
            AddTeacher teacher = teachers.get(0);
            assertEquals("Jane Smith", teacher.getTeacherName());
            assertEquals("ACTIVE", teacher.getStatus());
            
            // Check if teacher is assigned as class teacher in division or standard
            AddDivision updatedDiv = divisionRepository.findById(division.getDivisionId()).orElseThrow();
            assertNotNull(updatedDiv.getClassTeacher(), "Class teacher should be set for division!");
            assertEquals(teacher.getId(), updatedDiv.getClassTeacher().getId());
        }
    }

    @Test
    public void testTeacherUploadWithoutClass() throws IOException {
        String[] headers = {
            "teacherName", "gender", "dateOfBirth", "mobileNumber", "emailId",
            "address", "aadharCardNumber", "salaryType", "joiningDate",
            "annualSalary", "monthlySalary", "pfStatus", "pfAccount",
            "paidInHandSalary"
        };

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Teachers");
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                headerRow.createCell(i).setCellValue(headers[i]);
            }

            Row dataRow = sheet.createRow(1);
            dataRow.createCell(0).setCellValue("Jane Smith Without Class");
            dataRow.createCell(1).setCellValue("Female");
            dataRow.createCell(2).setCellValue("1985-08-20");
            dataRow.createCell(3).setCellValue("8765432101");
            dataRow.createCell(4).setCellValue("jane.smith.n@test.com");
            dataRow.createCell(5).setCellValue("456 Oak St");
            dataRow.createCell(6).setCellValue("123456789012");
            dataRow.createCell(7).setCellValue("Monthly");
            dataRow.createCell(8).setCellValue("2020-01-01");
            dataRow.createCell(9).setCellValue("600000");
            dataRow.createCell(10).setCellValue("50000");
            dataRow.createCell(11).setCellValue("Active");
            dataRow.createCell(12).setCellValue("PF9999");
            dataRow.createCell(13).setCellValue("45000");

            workbook.write(out);
            MockMultipartFile file = new MockMultipartFile("file", "teachers.xlsx",
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", out.toByteArray());

            Map<String, Object> response = excelService.uploadTeachers(file, school.getSchoolId(),
                    academicYear.getAcademicYearId(), null, null);

            System.out.println("Teacher upload without class response: " + response);
            assertEquals(1, response.get("totalRows"));
            assertEquals(1, response.get("successCount"));
            assertEquals(0, response.get("failedCount"));

            List<AddTeacher> teachers = teacherRepository.findBySchool_SchoolIdAndAcademicYear_AcademicYearId(
                    school.getSchoolId(), academicYear.getAcademicYearId());
            AddTeacher teacher = teachers.stream()
                    .filter(t -> t.getTeacherName().equals("Jane Smith Without Class"))
                    .findFirst().orElseThrow();
            assertNull(teacher.getAddStandard());
            assertNull(teacher.getAddDivision());
        }
    }

    @Test
    public void testStudentUploadWithDuplicateParents() throws IOException {
        String[] headers = {
            "firstName", "middleName", "lastName", "gender", "dateOfBirth",
            "mobileNumber", "emailId", "aadharCardNumber", "guardianName", "parentMobileNumber",
            "parentEmailId", "address", "rollNumber", "admissionDate",
            "feeType", "totalFee", "discount", "finalFee"
        };

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Students");
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                headerRow.createCell(i).setCellValue(headers[i]);
            }

            // Sibling 1
            Row row1 = sheet.createRow(1);
            row1.createCell(0).setCellValue("John");
            row1.createCell(1).setCellValue("M");
            row1.createCell(2).setCellValue("Doe");
            row1.createCell(3).setCellValue("Male");
            row1.createCell(4).setCellValue("2010-05-15");
            row1.createCell(5).setCellValue("9876543201");
            row1.createCell(6).setCellValue("john.doe.sib1@test.com");
            row1.createCell(7).setCellValue("123456789012");
            row1.createCell(8).setCellValue("Richard Doe");
            row1.createCell(9).setCellValue("9876543211");
            row1.createCell(10).setCellValue("richard.doe@test.com");
            row1.createCell(11).setCellValue("123 Main St");
            row1.createCell(12).setCellValue("101");
            row1.createCell(13).setCellValue("2026-06-01");
            row1.createCell(14).setCellValue("Standard");
            row1.createCell(15).setCellValue(5000);
            row1.createCell(16).setCellValue(500);
            row1.createCell(17).setCellValue(4500);

            // Sibling 2 (Same parent details)
            Row row2 = sheet.createRow(2);
            row2.createCell(0).setCellValue("Jane");
            row2.createCell(1).setCellValue("M");
            row2.createCell(2).setCellValue("Doe");
            row2.createCell(3).setCellValue("Female");
            row2.createCell(4).setCellValue("2012-07-20");
            row2.createCell(5).setCellValue("9876543202");
            row2.createCell(6).setCellValue("jane.doe.sib2@test.com");
            row2.createCell(7).setCellValue("123456789013");
            row2.createCell(8).setCellValue("Richard Doe");
            row2.createCell(9).setCellValue("9876543211");
            row2.createCell(10).setCellValue("richard.doe@test.com"); // duplicate parent email
            row2.createCell(11).setCellValue("123 Main St");
            row2.createCell(12).setCellValue("102");
            row2.createCell(13).setCellValue("2026-06-01");
            row2.createCell(14).setCellValue("Standard");
            row2.createCell(15).setCellValue(5000);
            row2.createCell(16).setCellValue(500);
            row2.createCell(17).setCellValue(4500);

            workbook.write(out);
            MockMultipartFile file = new MockMultipartFile("file", "students.xlsx",
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", out.toByteArray());

            Map<String, Object> response = excelService.uploadStudents(file, school.getSchoolId(),
                    academicYear.getAcademicYearId(), standard.getStandardId(), division.getDivisionId());

            System.out.println("Sibling upload response: " + response);
            assertEquals(2, response.get("totalRows"));
            assertEquals(2, response.get("successCount"));
            assertEquals(0, response.get("failedCount"));

            List<AddStudent> students = studentRepository.findBySchool_SchoolId(school.getSchoolId());
            AddStudent sib1 = students.stream().filter(s -> s.getFirstName().equals("John")).findFirst().orElseThrow();
            AddStudent sib2 = students.stream().filter(s -> s.getFirstName().equals("Jane")).findFirst().orElseThrow();

            assertNotNull(sib1.getParentPassword());
            assertNotNull(sib2.getParentPassword());
            assertEquals(sib1.getParentPassword(), sib2.getParentPassword(), "Siblings should share the exact same parent credentials password!");
        }
    }

    @Test
    public void generateSampleExcelFiles() throws IOException {
        // 1. Teacher Sample File
        String[] teacherHeaders = {
            "teacherName", "gender", "dateOfBirth", "mobileNumber", "emailId",
            "address", "aadharCardNumber", "salaryType", "joiningDate",
            "annualSalary", "monthlySalary", "pfStatus", "pfAccount",
            "paidInHandSalary"
        };
        try (Workbook workbook = new XSSFWorkbook();
             java.io.FileOutputStream fileOut = new java.io.FileOutputStream("sample_teacher_upload.xlsx")) {
            Sheet sheet = workbook.createSheet("Teachers");
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < teacherHeaders.length; i++) {
                headerRow.createCell(i).setCellValue(teacherHeaders[i]);
            }
            
            Row r1 = sheet.createRow(1);
            r1.createCell(0).setCellValue("Amit Sharma");
            r1.createCell(1).setCellValue("Male");
            r1.createCell(2).setCellValue("1988-12-05");
            r1.createCell(3).setCellValue("9876540001");
            r1.createCell(4).setCellValue("amit.sharma@example.com");
            r1.createCell(5).setCellValue("Sector 15, Noida");
            r1.createCell(6).setCellValue("998877665544");
            r1.createCell(7).setCellValue("Monthly");
            r1.createCell(8).setCellValue("2021-08-16");
            r1.createCell(9).setCellValue("720000");
            r1.createCell(10).setCellValue("60000");
            r1.createCell(11).setCellValue("ACTIVE");
            r1.createCell(12).setCellValue("PF12345");
            r1.createCell(13).setCellValue("55000");

            Row r2 = sheet.createRow(2);
            r2.createCell(0).setCellValue("Priya Patel");
            r2.createCell(1).setCellValue("Female");
            r2.createCell(2).setCellValue("1991-04-22");
            r2.createCell(3).setCellValue("9876540002");
            r2.createCell(4).setCellValue("priya.patel@example.com");
            r2.createCell(5).setCellValue("Drive Road, Ahmedabad");
            r2.createCell(6).setCellValue("887766554433");
            r2.createCell(7).setCellValue("Monthly");
            r2.createCell(8).setCellValue("2022-06-01");
            r2.createCell(9).setCellValue("660000");
            r2.createCell(10).setCellValue("55000");
            r2.createCell(11).setCellValue("ACTIVE");
            r2.createCell(12).setCellValue("PF54321");
            r2.createCell(13).setCellValue("50000");

            workbook.write(fileOut);
        }

        // 2. Student Sample File
        String[] studentHeaders = {
            "firstName", "middleName", "lastName", "gender", "dateOfBirth",
            "mobileNumber", "emailId", "aadharCardNumber", "guardianName", "parentMobileNumber",
            "parentEmailId", "address", "rollNumber", "admissionDate",
            "feeType", "totalFee", "discount", "finalFee"
        };
        try (Workbook workbook = new XSSFWorkbook();
             java.io.FileOutputStream fileOut = new java.io.FileOutputStream("sample_student_upload.xlsx")) {
            Sheet sheet = workbook.createSheet("Students");
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < studentHeaders.length; i++) {
                headerRow.createCell(i).setCellValue(studentHeaders[i]);
            }

            // Sibling 1
            Row r1 = sheet.createRow(1);
            r1.createCell(0).setCellValue("Aarav");
            r1.createCell(1).setCellValue("K");
            r1.createCell(2).setCellValue("Mehta");
            r1.createCell(3).setCellValue("Male");
            r1.createCell(4).setCellValue("2012-09-10");
            r1.createCell(5).setCellValue("9811223344");
            r1.createCell(6).setCellValue("aarav.mehta@example.com");
            r1.createCell(7).setCellValue("112233445566");
            r1.createCell(8).setCellValue("Rajesh Mehta");
            r1.createCell(9).setCellValue("9811223399");
            r1.createCell(10).setCellValue("rajesh.mehta@example.com");
            r1.createCell(11).setCellValue("45 Park Avenue, Mumbai");
            r1.createCell(12).setCellValue("12");
            r1.createCell(13).setCellValue("2026-06-01");
            r1.createCell(14).setCellValue("Standard");
            r1.createCell(15).setCellValue(80000);
            r1.createCell(16).setCellValue(5000);
            r1.createCell(17).setCellValue(75000);

            // Sibling 2 (Same parent details)
            Row r2 = sheet.createRow(2);
            r2.createCell(0).setCellValue("Ira");
            r2.createCell(1).setCellValue("K");
            r2.createCell(2).setCellValue("Mehta");
            r2.createCell(3).setCellValue("Female");
            r2.createCell(4).setCellValue("2015-05-18");
            r2.createCell(5).setCellValue("9811223345");
            r2.createCell(6).setCellValue("ira.mehta@example.com");
            r2.createCell(7).setCellValue("112233445577");
            r2.createCell(8).setCellValue("Rajesh Mehta");
            r2.createCell(9).setCellValue("9811223399");
            r2.createCell(10).setCellValue("rajesh.mehta@example.com");
            r2.createCell(11).setCellValue("45 Park Avenue, Mumbai");
            r2.createCell(12).setCellValue("18");
            r2.createCell(13).setCellValue("2026-06-01");
            r2.createCell(14).setCellValue("Standard");
            r2.createCell(15).setCellValue(80000);
            r2.createCell(16).setCellValue(10000);
            r2.createCell(17).setCellValue(70000);

            workbook.write(fileOut);
        }
    }
}


