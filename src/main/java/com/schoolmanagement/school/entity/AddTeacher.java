package com.schoolmanagement.school.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "teacher")
public class AddTeacher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ===== FK: school =====
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "school_id", nullable = false)
    private School school;

    // ===== FK: academic_year =====
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "academic_year_id")
    private AcademicYear academicYear;

    // ===== FK: standard =====
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "standard_id")
    private AddStandard addStandard;

    // ===== FK: division =====
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "division_id")
    private AddDivision addDivision;

    // ===== FK: role =====
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id")
    private Role role;

    // ===== Personal Info =====
    @Column(nullable = false)
    private String teacherName;

    @Column(length = 11)
    private String gender;

    private LocalDate dateOfBirth;

    @Column(nullable = false, unique = true)
    private String mobileNumber;

    @Column(nullable = false, unique = true)
    private String emailId;

    // ===== Salary =====
    private String salaryType;
    private LocalDate joiningDate;
    private String annualSalary;
    private String monthlySalary;
    private String pfStatus;
    private String pfAccount;
    private String paidInHandSalary;

    // ===== Login =====
    private String token;
    private LocalDateTime loginAt;
    private LocalDateTime logoutAt;

    // ===== Other =====
    private String address;
    private String profilePhoto;
    private String aadharCardNumber;

    @Column(nullable = false)
    private String status;

    private String password;

    // ==================== GETTERS & SETTERS ====================
    
    public AcademicYear getAcademicYear() { return academicYear; }
    public void setAcademicYear(AcademicYear academicYear) { this.academicYear = academicYear; }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public School getSchool() { return school; }
    public void setSchool(School school) { this.school = school; }

    public AddStandard getAddStandard() { return addStandard; }
    public void setAddStandard(AddStandard addStandard) { this.addStandard = addStandard; }

    public AddDivision getAddDivision() { return addDivision; }
    public void setAddDivision(AddDivision addDivision) { this.addDivision = addDivision; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public String getTeacherName() { return teacherName; }
    public void setTeacherName(String teacherName) { this.teacherName = teacherName; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    public String getMobileNumber() { return mobileNumber; }
    public void setMobileNumber(String mobileNumber) { this.mobileNumber = mobileNumber; }

    public String getEmailId() { return emailId; }
    public void setEmailId(String emailId) { this.emailId = emailId; }

    public String getSalaryType() { return salaryType; }
    public void setSalaryType(String salaryType) { this.salaryType = salaryType; }

    public LocalDate getJoiningDate() { return joiningDate; }
    public void setJoiningDate(LocalDate joiningDate) { this.joiningDate = joiningDate; }

    public String getAnnualSalary() { return annualSalary; }
    public void setAnnualSalary(String annualSalary) { this.annualSalary = annualSalary; }

    public String getMonthlySalary() { return monthlySalary; }
    public void setMonthlySalary(String monthlySalary) { this.monthlySalary = monthlySalary; }

    public String getPfStatus() { return pfStatus; }
    public void setPfStatus(String pfStatus) { this.pfStatus = pfStatus; }

    public String getPfAccount() { return pfAccount; }
    public void setPfAccount(String pfAccount) { this.pfAccount = pfAccount; }

    public String getPaidInHandSalary() { return paidInHandSalary; }
    public void setPaidInHandSalary(String paidInHandSalary) { this.paidInHandSalary = paidInHandSalary; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getProfilePhoto() { return profilePhoto; }
    public void setProfilePhoto(String profilePhoto) { this.profilePhoto = profilePhoto; }

    public String getAadharCardNumber() { return aadharCardNumber; }
    public void setAadharCardNumber(String aadharCardNumber) { this.aadharCardNumber = aadharCardNumber; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public LocalDateTime getLoginAt() { return loginAt; }
    public void setLoginAt(LocalDateTime loginAt) { this.loginAt = loginAt; }

    public LocalDateTime getLogoutAt() { return logoutAt; }
    public void setLogoutAt(LocalDateTime logoutAt) { this.logoutAt = logoutAt; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime createdTimestamp;
    private LocalDateTime updatedTimestamp;

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.createdTimestamp = LocalDateTime.now();
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
        if (this.updatedTimestamp == null) {
            this.updatedTimestamp = LocalDateTime.now();
        }
    }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public LocalDateTime getCreatedTimestamp() { return createdTimestamp; }
    public void setCreatedTimestamp(LocalDateTime createdTimestamp) { this.createdTimestamp = createdTimestamp; }

    public LocalDateTime getUpdatedTimestamp() { return updatedTimestamp; }
    public void setUpdatedTimestamp(LocalDateTime updatedTimestamp) { this.updatedTimestamp = updatedTimestamp; }

    public AddTeacher() { super(); }
}
