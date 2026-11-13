package com.campusconnect.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SignupRequest {

    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 50, message = "First name must be 2-50 characters")
    @Pattern(regexp = "^[a-zA-Z\\s'-]+$", message = "First name must contain letters only")
    private String firstName;

    @Size(max = 50, message = "Last name must be under 50 characters")
    @Pattern(regexp = "^[a-zA-Z\\s'-]*$", message = "Last name must contain letters only")
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Enter a valid email address")
    @Size(max = 100, message = "Email must be under 100 characters")
    private String email;

    @NotBlank(message = "Password is required")
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$",
        message = "Password must be at least 8 characters with an uppercase letter, lowercase letter, and a number"
    )
    private String password;

    @Pattern(regexp = "^$|^[6-9]\\d{9}$", message = "Enter a valid 10-digit mobile number")
    private String phoneNumber;

    private Long collegeId;
}