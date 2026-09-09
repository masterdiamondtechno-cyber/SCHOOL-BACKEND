package com.schoolmanagement.school_admin_Entity;

import java.time.LocalDateTime;



import com.schoolmanagement.app_admin_Entity.SubscriptionPlan;
import com.schoolmanagement.owner.entity.Owner;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

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
@Table(name = "school")
public class School {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "school_id")
	private Long schoolId;

	private String schoolName;

	@Column(nullable = false, unique = true)
	private String udiseNo;

	private String state;
	private String district;
	private String taluka;
	private String pincode;
	private String password;

	private String schoolAddress;

	@Column(length = 2048)
	private String schoolLogo;
	private String s3Key;

	@Column(nullable = false, unique = true)
	private String email;

	@Column(nullable = false, unique = true)
	private String phoneNo;

	private LocalDateTime loginAt;
	private LocalDateTime logoutAt;

	private String otp;
	private LocalDateTime otpGeneratedAt;
	private String otpVerify;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "role_id")
	@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
	private Role role;

	// ===== FK: owner (nullable — existing schools without owner continue working) =====
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "owner_id", nullable = true)
	@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
	private Owner owner;

	@Column(nullable = false)
	private String registrationStatus = "PENDING";

	private String schoolStatus;

	private String token;

	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
	private LocalDateTime createdTimestamp;
	private LocalDateTime updatedTimestamp;

	private String subscriptionStatus;

	private LocalDateTime trialEndDate;

	public LocalDateTime getTrialEndDate() {
		return trialEndDate;
	}

	public void setTrialEndDate(LocalDateTime trialEndDate) {
		this.trialEndDate = trialEndDate;
	}

	private String subscriptionSource = "TRIAL";

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "subscription_plan_id", nullable = true)
	@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
	private SubscriptionPlan subscriptionPlan;

	private LocalDateTime subscriptionStartDate;
	private LocalDateTime subscriptionEndDate;
	private String subscriptionDuration;

	@Column(name = "assigned_by")
	private Long assignedBy;

	@Column(name = "assignment_reason", length = 1024)
	private String assignmentReason;

	private LocalDateTime manualAssignmentDate;



	private String principalName;

	@PrePersist
	public void onCreate() {
		this.createdAt = LocalDateTime.now();
		this.updatedAt = LocalDateTime.now();
		this.createdTimestamp = LocalDateTime.now();

		if (this.registrationStatus == null) {
			this.registrationStatus = "PENDING";
		}
	}

	@PreUpdate
	public void onUpdate() {
		this.updatedAt = LocalDateTime.now();
		if (this.updatedTimestamp == null) {
			this.updatedTimestamp = LocalDateTime.now();
		}
	}
	// ---------------------------------------------------------

	public Long getSchoolId() {
		return schoolId;
	}

	public void setSchoolId(Long schoolId) {
		this.schoolId = schoolId;
	}

	public String getSchoolName() {
		return schoolName;
	}

	public void setSchoolName(String schoolName) {
		this.schoolName = schoolName;
	}

	public String getUdiseNo() {
		return udiseNo;
	}

	public void setUdiseNo(String udiseNo) {
		this.udiseNo = udiseNo;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getDistrict() {
		return district;
	}

	public void setDistrict(String district) {
		this.district = district;
	}

	public String getTaluka() {
		return taluka;
	}

	public void setTaluka(String taluka) {
		this.taluka = taluka;
	}

	public String getPincode() {
		return pincode;
	}

	public void setPincode(String pincode) {
		this.pincode = pincode;
	}

	public String getSchoolAddress() {
		return schoolAddress;
	}

	public void setSchoolAddress(String schoolAddress) {
		this.schoolAddress = schoolAddress;
	}

	public String getSchoolLogo() {
		return schoolLogo;
	}

	public void setSchoolLogo(String schoolLogo) {
		this.schoolLogo = schoolLogo;
	}

	public String getS3Key() {
		return s3Key;
	}

	public void setS3Key(String s3Key) {
		this.s3Key = s3Key;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPhoneNo() {
		return phoneNo;
	}

	public void setPhoneNo(String phoneNo) {
		this.phoneNo = phoneNo;
	}

	public LocalDateTime getLoginAt() {
		return loginAt;
	}

	public void setLoginAt(LocalDateTime loginAt) {
		this.loginAt = loginAt;
	}

	public LocalDateTime getLogoutAt() {
		return logoutAt;
	}

	public void setLogoutAt(LocalDateTime logoutAt) {
		this.logoutAt = logoutAt;
	}

	public String getOtp() {
		return otp;
	}

	public void setOtp(String otp) {
		this.otp = otp;
	}

	public LocalDateTime getOtpGeneratedAt() {
		return otpGeneratedAt;
	}

	public void setOtpGeneratedAt(LocalDateTime otpGeneratedAt) {
		this.otpGeneratedAt = otpGeneratedAt;
	}

	public String getOtpVerify() {
		return otpVerify;
	}

	public void setOtpVerify(String otpVerify) {
		this.otpVerify = otpVerify;
	}

	public Role getRole() {
		return role;
	}

	public void setRole(Role role) {
		this.role = role;
	}

	public String getRegistrationStatus() {
		return registrationStatus;
	}

	public void setRegistrationStatus(String registrationStatus) {
		this.registrationStatus = registrationStatus;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(LocalDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}

	public String getSubscriptionStatus() {
		return subscriptionStatus;
	}

	public void setSubscriptionStatus(String subscriptionStatus) {
		this.subscriptionStatus = subscriptionStatus;
	}

	public String getSchoolStatus() {
		return schoolStatus;
	}

	public void setSchoolStatus(String schoolStatus) {
		this.schoolStatus = schoolStatus;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public School(Long schoolId, String schoolName, String udiseNo, String state, String district, String taluka,
			String pincode, String password, String schoolAddress, String schoolLogo, String s3Key, String email, String phoneNo,
			LocalDateTime loginAt, LocalDateTime logoutAt, String otp, LocalDateTime otpGeneratedAt, String otpVerify,
			Role role, String registrationStatus, String schoolStatus, String token, LocalDateTime createdAt,
			LocalDateTime updatedAt, String subscriptionStatus) {
		super();
		this.schoolId = schoolId;
		this.schoolName = schoolName;
		this.udiseNo = udiseNo;
		this.state = state;
		this.district = district;
		this.taluka = taluka;
		this.pincode = pincode;
		this.password = password;
		this.schoolAddress = schoolAddress;
		this.schoolLogo = schoolLogo;
		this.s3Key = s3Key;
		this.email = email;
		this.phoneNo = phoneNo;
		this.loginAt = loginAt;
		this.logoutAt = logoutAt;
		this.otp = otp;
		this.otpGeneratedAt = otpGeneratedAt;
		this.otpVerify = otpVerify;
		this.role = role;
		this.registrationStatus = registrationStatus;
		this.schoolStatus = schoolStatus;
		this.token = token;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
		this.subscriptionStatus = subscriptionStatus;
	}

	public LocalDateTime getCreatedTimestamp() { return createdTimestamp; }
	public void setCreatedTimestamp(LocalDateTime createdTimestamp) { this.createdTimestamp = createdTimestamp; }

	public LocalDateTime getUpdatedTimestamp() { return updatedTimestamp; }
	public void setUpdatedTimestamp(LocalDateTime updatedTimestamp) { this.updatedTimestamp = updatedTimestamp; }

	public String getPrincipalName() { return principalName; }
	public void setPrincipalName(String principalName) { this.principalName = principalName; }

	public Owner getOwner() { return owner; }
	public void setOwner(Owner owner) { this.owner = owner; }

	public School() {
		super();
	}

	public String getSubscriptionSource() {
		return subscriptionSource;
	}

	public void setSubscriptionSource(String subscriptionSource) {
		this.subscriptionSource = subscriptionSource;
	}

	public SubscriptionPlan getSubscriptionPlan() {
		return subscriptionPlan;
	}

	public void setSubscriptionPlan(SubscriptionPlan subscriptionPlan) {
		this.subscriptionPlan = subscriptionPlan;
	}

	public LocalDateTime getSubscriptionStartDate() {
		return subscriptionStartDate;
	}

	public void setSubscriptionStartDate(LocalDateTime subscriptionStartDate) {
		this.subscriptionStartDate = subscriptionStartDate;
	}

	public LocalDateTime getSubscriptionEndDate() {
		return subscriptionEndDate;
	}

	public void setSubscriptionEndDate(LocalDateTime subscriptionEndDate) {
		this.subscriptionEndDate = subscriptionEndDate;
	}

	public String getSubscriptionDuration() {
		return subscriptionDuration;
	}

	public void setSubscriptionDuration(String subscriptionDuration) {
		this.subscriptionDuration = subscriptionDuration;
	}

	public Long getAssignedBy() {
		return assignedBy;
	}

	public void setAssignedBy(Long assignedBy) {
		this.assignedBy = assignedBy;
	}

	public String getAssignmentReason() {
		return assignmentReason;
	}

	public void setAssignmentReason(String assignmentReason) {
		this.assignmentReason = assignmentReason;
	}

	public LocalDateTime getManualAssignmentDate() {
		return manualAssignmentDate;
	}

	public void setManualAssignmentDate(LocalDateTime manualAssignmentDate) {
		this.manualAssignmentDate = manualAssignmentDate;
	}

	public boolean isSubscriptionExpired() {
		LocalDateTime now = LocalDateTime.now();
		if (this.subscriptionSource == null || "TRIAL".equalsIgnoreCase(this.subscriptionSource)) {
			return this.trialEndDate != null && this.trialEndDate.isBefore(now);
		}
		if ("MANUAL".equalsIgnoreCase(this.subscriptionSource) || "PURCHASED".equalsIgnoreCase(this.subscriptionSource)) {
			return this.subscriptionEndDate != null && this.subscriptionEndDate.isBefore(now);
		}
		return true;
	}

	public boolean getIsSubscriptionExpired() {
		return isSubscriptionExpired();
	}

}