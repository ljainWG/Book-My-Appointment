package com.healthcare.book_my_doctor.user;

import java.time.LocalDate;

import com.healthcare.book_my_doctor.enums.UserGender;

import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateUserDTO {

	@Size(min = 5, message = "Username must be at least 5 characters")
	@Pattern(regexp = "^[a-zA-Z0-9!@#$%^&*()_+=]*$", message = "Username must contain alphanumeric characters and allowed special symbols")
	private String userName;

	@Pattern(regexp = "^[a-zA-Z\\s]*$", message = "Real name must contain only alphabetic characters and spaces")
	private String userRealName;

	@Size(min = 8, message = "Password must be at least 8 characters")
	@Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$", message = "Password must be at least 8 characters long and contain upper and lower case letters, numbers, and special characters.")
	// regex is causing some issue due to regex not able to creat or update user
	// please look into this
	private String userPassword;

	@Email(message = "Email should be valid")
	@Pattern(regexp = ".+@(gmail\\.com|yahoo\\.com|watchguard\\.com)$", message = "Email must have a valid domain")
	private String userEmail;

	@Enumerated(EnumType.STRING)
	private UserGender userGender;

	@Size(min = 10, max = 10, message = "Phone number must be 10 numbers")
	@Pattern(regexp = "^[0-9]{10}$", message = "Phone number must be exactly 10 digits")
	private String userPhoneNo;

	@Size(min = 5, message = "Address must be at least 5 characters")
	@Pattern(regexp = "^(?=.*[a-zA-Z].{5,})[a-zA-Z0-9\\s,]*$", message = "Address must have at least 5 alphabetic characters and may include numbers and spaces")
	private String userAddress;

	@Column(name = "user_experience")
	private Integer userExperience; // Only for doctors

	@Past(message = "Date of birth must be in the past")
	@Column(name = "user_dob", nullable = false)
	private LocalDate userDOB;

}
