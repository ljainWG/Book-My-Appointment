package com.healthcare.book_my_doctor.user;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.healthcare.book_my_doctor.enums.ResponseStatus;
import com.healthcare.book_my_doctor.enums.UserDepartment;
import com.healthcare.book_my_doctor.enums.UserGender;
import com.healthcare.book_my_doctor.enums.UserRole;
import com.healthcare.book_my_doctor.envelope.ResponseEnvelope;

@RestController
public class UserController {

	@Autowired
	private UserService user2Service;

	@GetMapping("/users")
	public ResponseEnvelope getAllUsers(@RequestParam(required = false) String userName,
			@RequestParam(required = false) String userRealName, @RequestParam(required = false) String userEmail,
			@RequestParam(required = false) UserGender userGender, @RequestParam(required = false) UserRole userRole,
			@RequestParam(required = false) UserDepartment userDepartment,
			@RequestParam(required = false) String userPhoneNo, @RequestParam(required = false) LocalDate userDOB,
			@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {

		Pageable pageable = PageRequest.of(page, size);
		Page<UserDTO> usersPage = user2Service.getAllUsers(userName, userRealName, userEmail, userGender, userRole,
				userDepartment, userPhoneNo, userDOB, pageable);

		return ResponseEnvelope.builder().status(ResponseStatus.SUCCESS).message("Users retrieved successfully")
				.data(usersPage.getContent()).currentPageNo(usersPage.getNumber())
				.totalNoOfRecords((int) usersPage.getTotalElements()).totalNoOfPages(usersPage.getTotalPages())
				.recordsPerPage(usersPage.getSize()).timeStamp(LocalDateTime.now()).build();
	}

	@PostMapping("/api/auth/users")
	public ResponseEntity<ResponseEnvelope> createUser(@RequestBody CreateUserDTO createUser2DTO) {

		ResponseEnvelope response = user2Service.createUser(createUser2DTO);

//        return ResponseEntity.status(HttpStatus.CREATED)
//                .body(ResponseEnvelopeWithPagination2.builder()
//                        .status(ApiResponseStatus.SUCCESS)
//                        .message("User created successfully.")
//                        .data(createUser2DTO) 
//                        .timeStamp(LocalDateTime.now())
//                        .build());
		return new ResponseEntity<>(response, HttpStatus.CREATED);
	}

	@PatchMapping("/user/{userId}")
	public ResponseEntity<ResponseEnvelope> updateUser(@PathVariable String userId,
			@RequestBody UpdateUserDTO updateUser2DTO) {

		ResponseEnvelope response = user2Service.updateUser(userId, updateUser2DTO);

		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@DeleteMapping("/user/{userId}")
	public ResponseEntity<ResponseEnvelope> deleteUser(@PathVariable String userId) {

		user2Service.deleteUserById(userId);

		ResponseEnvelope response = ResponseEnvelope.builder().status(ResponseStatus.SUCCESS)
				.message("User deleted successfully.").timeStamp(LocalDateTime.now()).build();

		return ResponseEntity.ok(response);
	}
}
