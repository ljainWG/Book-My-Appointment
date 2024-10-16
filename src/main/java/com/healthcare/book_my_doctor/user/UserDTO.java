package com.healthcare.book_my_doctor.user;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.healthcare.book_my_doctor.enums.UserDepartment;
import com.healthcare.book_my_doctor.enums.UserGender;
import com.healthcare.book_my_doctor.enums.UserRole;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "Users") // Specifies the table name in the database
public class UserDTO {

	@Id
	@Column(name = "user_uuid", nullable = false, length = 255, unique = true)
	private String userUuid;

	@Column(name = "user_name", nullable = false, length = 255)
	private String userName;

	@Column(name = "user_real_name", nullable = false, length = 255)
	private String userRealName;

	@Column(name = "user_password", nullable = false, length = 255)
	private String userPassword;

	@Column(name = "user_email", nullable = false, length = 255)
	private String userEmail;

	@Enumerated(EnumType.STRING)
	@Column(name = "user_gender", nullable = false, length = 20)
	private UserGender userGender;

	@Enumerated(EnumType.STRING)
	@Column(name = "user_role", nullable = false, length = 20)
	private UserRole userRole;

	@Enumerated(EnumType.STRING)
	@Column(name = "user_department", nullable = false, length = 20)
	private UserDepartment userDepartment;

	@Column(name = "user_phone_no", nullable = false, length = 10)
	private String userPhoneNo;

	@Column(name = "user_address", nullable = false, columnDefinition = "TEXT")
	private String userAddress;

	@Column(name = "user_avg_rating")
	private Integer userAvgRating; // Only for doctors

	@Column(name = "user_no_of_review")
	private Integer userNoOfReview; // for patients no of reviews given by him // for doctors no of reviews given to
									// him

	@Column(name = "user_experience")
	private Integer userExperience; // Only for doctors

	@Column(name = "user_dob", nullable = false)
	private LocalDate userDOB;

	@Column(name = "user_created_at")
	private LocalDateTime userCreatedAt;

	@Column(name = "user_updated_at")
	private LocalDateTime userUpdatedAt;

}
