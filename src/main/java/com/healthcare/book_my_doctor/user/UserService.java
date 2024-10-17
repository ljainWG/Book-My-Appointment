package com.healthcare.book_my_doctor.user;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.healthcare.book_my_doctor.enums.ResponseStatus;
import com.healthcare.book_my_doctor.enums.UserDepartment;
import com.healthcare.book_my_doctor.enums.UserGender;
import com.healthcare.book_my_doctor.enums.UserRole;
import com.healthcare.book_my_doctor.envelope.ResponseEnvelope;
import com.healthcare.book_my_doctor.exceptions.UnauthorizedAccessException;
import com.healthcare.book_my_doctor.exceptions.UserAlreadyExistsException;
import com.healthcare.book_my_doctor.exceptions.UserNotFoundException;

@Service
public class UserService {

	@Autowired
	private UserRepository user2Repository;

	// Get all users based on search criteria
	public Page<UserDTO> getAllUsers(String userName, String userRealName, String userEmail, UserGender userGender,
			UserRole userRole, UserDepartment userDepartment, String userPhoneNo, LocalDate userDOB,
			Pageable pageable) {

		UserDTO currentUser = getCurrentUser();
		checkAdminOrReceptionistAccess(currentUser);

		return user2Repository.searchUsers(userName, userRealName, userEmail, userGender, userRole, userDepartment,
				userPhoneNo, userDOB, pageable);
	}

	// Create a new user
	public ResponseEnvelope createUser(CreateUserDTO createUser2DTO) {
		checkForDuplicateFields(createUser2DTO);

		String newUserUuid = generateUniqueUserUuid();

		// Create the new user
		UserDTO newUser = buildNewUser(createUser2DTO, newUserUuid);
		user2Repository.save(newUser);

		return buildSuccessResponse("User created successfully.", newUser);
	}

	// Update an existing user
	public ResponseEnvelope updateUser(String userId, UpdateUserDTO updateUser2DTO) {
		UserDTO existingUser = user2Repository.findById(userId)
				.orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));

		UserDTO currentUser = getCurrentUser();
		checkUpdateAuthorization(currentUser, existingUser);

		checkForDuplicateFields(updateUser2DTO, existingUser);
		updateUserDetails(existingUser, updateUser2DTO);

		user2Repository.save(existingUser);

		return buildSuccessResponse("User updated successfully.", existingUser);
	}

	// Delete a user by ID
	public void deleteUserById(String userId) {
		UserDTO userToDelete = user2Repository.findById(userId)
				.orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));

		UserDTO currentUser = getCurrentUser();
		checkDeleteAuthorization(currentUser, userToDelete);

		user2Repository.delete(userToDelete);
	}

//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//	 														Helper methods

	// Role check methods
	public boolean isAdmin(UserDTO user) {
		return user.getUserRole() == UserRole.ADMIN;
	}

	public boolean isReceptionist(UserDTO user) {
		return user.getUserRole() == UserRole.RECEPTIONIST;
	}

	public boolean isDoctor(UserDTO user) {
		return user.getUserRole() == UserRole.DOCTOR; // Assuming DOCTOR is a role in UserRole enum
	}

	public boolean isPatient(UserDTO user) {
		return user.getUserRole() == UserRole.PATIENT; // Assuming PATIENT is a role in UserRole enum
	}

	// Modified authorization checks
	public void checkAdminOrReceptionistAccess(UserDTO currentUser) {
		if (!isAdmin(currentUser) && !isReceptionist(currentUser)) {
			throw new UnauthorizedAccessException("You are not authorized to access this endpoint.");
		}
	}

	public void checkUpdateAuthorization(UserDTO currentUser, UserDTO existingUser) {
		if (!isAdmin(currentUser) && !isCurrentUser(currentUser, existingUser)) {
			throw new UnauthorizedAccessException("You are not authorized to update this user.");
		}
	}

	public void checkDeleteAuthorization(UserDTO currentUser, UserDTO userToDelete) {
		if (!isCurrentUser(currentUser, userToDelete) && !isAdmin(currentUser)) {
			throw new UnauthorizedAccessException("You do not have permission to delete this user.");
		}
	}

	public boolean isCurrentUser(UserDTO currentUser, UserDTO user) {
		return user.getUserUuid().equals(currentUser.getUserUuid());
	}

	// Get the current authenticated user
	public UserDTO getCurrentUser() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication != null && authentication.isAuthenticated()) {
			String username = authentication.getName();
			return user2Repository.findByUserName(username)
					.orElseThrow(() -> new UserNotFoundException("User not found: " + username));
		}
		throw new UnauthorizedAccessException("User is not authenticated.");
	}

	// Build a success response
	private ResponseEnvelope buildSuccessResponse(String message, UserDTO data) {
		return ResponseEnvelope.builder().status(ResponseStatus.SUCCESS).message(message).data(data)
				.timeStamp(LocalDateTime.now()).build();
	}

	// Build a new user from CreateUser2DTO
	private UserDTO buildNewUser(CreateUserDTO createUser2DTO, String userUuid) {
		return UserDTO.builder().userUuid(userUuid).userName(createUser2DTO.getUserName())
				.userRealName(createUser2DTO.getUserRealName())
				.userPassword(hashPassword(createUser2DTO.getUserPassword())).userEmail(createUser2DTO.getUserEmail())
				.userGender(createUser2DTO.getUserGender()).userRole(createUser2DTO.getUserRole())
				.userDepartment(createUser2DTO.getUserDepartment()).userPhoneNo(createUser2DTO.getUserPhoneNo())
				.userAddress(createUser2DTO.getUserAddress()).userExperience(createUser2DTO.getUserExperience())
				.userDOB(createUser2DTO.getUserDOB()).userCreatedAt(LocalDateTime.now())
				.userUpdatedAt(LocalDateTime.now()).build();
	}

	// Update user details
	private void updateUserDetails(UserDTO existingUser, UpdateUserDTO updateUser2DTO) {
		if (updateUser2DTO.getUserName() != null) {
			existingUser.setUserName(updateUser2DTO.getUserName());
		}
		if (updateUser2DTO.getUserRealName() != null) {
			existingUser.setUserRealName(updateUser2DTO.getUserRealName());
		}
		if (updateUser2DTO.getUserPassword() != null) {
			existingUser.setUserPassword(hashPassword(updateUser2DTO.getUserPassword()));
		}
		if (updateUser2DTO.getUserEmail() != null) {
			existingUser.setUserEmail(updateUser2DTO.getUserEmail());
		}
		if (updateUser2DTO.getUserGender() != null) {
			existingUser.setUserGender(updateUser2DTO.getUserGender());
		}
		if (updateUser2DTO.getUserPhoneNo() != null) {
			existingUser.setUserPhoneNo(updateUser2DTO.getUserPhoneNo());
		}
		if (updateUser2DTO.getUserAddress() != null) {
			existingUser.setUserAddress(updateUser2DTO.getUserAddress());
		}
		if (updateUser2DTO.getUserExperience() != null) {
			existingUser.setUserExperience(updateUser2DTO.getUserExperience());
		}
		if (updateUser2DTO.getUserDOB() != null) {
			existingUser.setUserDOB(updateUser2DTO.getUserDOB());
		}
	}

	// Check for duplicate fields when creating a user
	private void checkForDuplicateFields(CreateUserDTO createUser2DTO) {
		if (user2Repository.existsByUserName(createUser2DTO.getUserName())) {
			throw new UserAlreadyExistsException("Username already exists.");
		}
		if (user2Repository.existsByUserEmail(createUser2DTO.getUserEmail())) {
			throw new UserAlreadyExistsException("Email already exists.");
		}
		if (user2Repository.existsByUserPhoneNo(createUser2DTO.getUserPhoneNo())) {
			throw new UserAlreadyExistsException("Phone number already exists.");
		}
	}

	// Check for duplicate fields when updating a user
	private void checkForDuplicateFields(UpdateUserDTO updateUser2DTO, UserDTO existingUser) {
		List<String> duplicateFields = new ArrayList<>();

		if (updateUser2DTO.getUserName() != null && !updateUser2DTO.getUserName().equals(existingUser.getUserName())
				&& user2Repository.existsByUserName(updateUser2DTO.getUserName())) {
			duplicateFields.add("Username");
		}
		if (updateUser2DTO.getUserEmail() != null && !updateUser2DTO.getUserEmail().equals(existingUser.getUserEmail())
				&& user2Repository.existsByUserEmail(updateUser2DTO.getUserEmail())) {
			duplicateFields.add("Email");
		}
		if (updateUser2DTO.getUserPhoneNo() != null
				&& !updateUser2DTO.getUserPhoneNo().equals(existingUser.getUserPhoneNo())
				&& user2Repository.existsByUserPhoneNo(updateUser2DTO.getUserPhoneNo())) {
			duplicateFields.add("Phone Number");
		}

		if (!duplicateFields.isEmpty()) {
			String errorMessage = "The following fields already exist in the database: "
					+ String.join(", ", duplicateFields);
			throw new UserAlreadyExistsException(errorMessage);
		}
	}

	// Password handling
	private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

	public String hashPassword(String plainPassword) {
		return passwordEncoder.encode(plainPassword);
	}

	public boolean checkPassword(String plainPassword, String hashedPassword) {
		return passwordEncoder.matches(plainPassword, hashedPassword);
	}

	// Generate a random string of given length
	private static final SecureRandom random = new SecureRandom();

	public static String generateRandomString(int length) {
		byte[] randomBytes = new byte[length];
		random.nextBytes(randomBytes);
		return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes).substring(0, length);
	}

	// Generate a unique UUID
	private String generateUniqueUserUuid() {
		String newUserUuid;
		do {
			newUserUuid = generateRandomString(16);
		} while (user2Repository.existsById(newUserUuid));
		return newUserUuid;
	}
}
