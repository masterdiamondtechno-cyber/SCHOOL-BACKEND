package com.schoolmanagement.owner.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OwnerRegisterRequestDTO {

    @NotBlank(message = "Full name is required")
    private String fullName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[0-9]{10}$", message = "Phone number must be 10 digits")
    private String phone;

    @NotBlank(message = "Aadhar Card is required")
    private String aadharCard;

    @NotBlank(message = "PAN Card is required")
    private String panCard;

    @NotBlank(message = "DOB is required")
    private String dob;

    @NotBlank(message = "Gender is required")
    private String gender;

    @NotBlank(message = "Address is required")
    private String address;

    @NotBlank(message = "Full name as per Aadhar is required")
    private String fullNameAsPerAadhar;

    @NotBlank(message = "Registration type is required (SINGLE or MULTIPLE)")
    private String registrationType; // SINGLE or MULTIPLE

}
