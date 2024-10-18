package com.healthcare.book_my_doctor.security;

import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.healthcare.book_my_doctor.enums.ResponseStatus;
import com.healthcare.book_my_doctor.envelope.ResponseEnvelope;
import com.healthcare.book_my_doctor.user.UserService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
public class AuthController {

	private final UserDetailsService userDetailsService;

	private final AuthenticationManager authenticationManager;

	private final PasswordEncoder passwordEncoder;

	private final JwtUtil jwtUtil;

	@Autowired
	private UserService userService;

	@PostMapping("/login")
	public ResponseEntity<ResponseEnvelope> login(@RequestBody JwtRequest user) {
		authenticationManager
				.authenticate(new UsernamePasswordAuthenticationToken(user.getUserName(), user.getPassword()));
		UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUserName());
		String jwtToken = jwtUtil.generateToken(userDetails.getUsername());
		ResponseEnvelope envelope = ResponseEnvelope.builder().status(ResponseStatus.SUCCESS)
				.message("Logged in successfully.").data(Map.of("jwtToken", jwtToken)).error(null)
				.timeStamp(LocalDateTime.now()).build();
		return ResponseEntity.ok(envelope);
	}

	@PostMapping("/logout")
	public ResponseEntity<ResponseEnvelope> logoutUser(@RequestHeader("Authorization") String authorizationHeader) {
		// Check if the header is present and well-formed
		if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
			ResponseEnvelope errorResponse = ResponseEnvelope.builder().status(ResponseStatus.ERROR)
					.message("Invalid token").data(null).error("Invalid token").timeStamp(LocalDateTime.now()).build();

			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
		}

		// Extract the JWT token (removing "Bearer " prefix)
		String jwtToken = authorizationHeader.substring(7);

		// Add the token to the blacklist
		jwtUtil.blacklistToken(jwtToken);

		// Create success response
		ResponseEnvelope successResponse = ResponseEnvelope.builder().status(ResponseStatus.SUCCESS)
				.message("Logged out successfully").data(null).error(null).timeStamp(LocalDateTime.now()).build();

		// Respond with success message
		return ResponseEntity.status(HttpStatus.OK).body(successResponse);
	}

}