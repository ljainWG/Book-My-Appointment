package com.healthcare.book_my_doctor.exceptions;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import com.healthcare.book_my_doctor.enums.ResponseStatus;
import com.healthcare.book_my_doctor.envelope.ResponseEnvelope;

@ControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(UserNotFoundException.class)
	public ResponseEntity<ResponseEnvelope> handleUserNotFoundException(UserNotFoundException ex) {
		ResponseEnvelope response = ResponseEnvelope.builder().status(ResponseStatus.ERROR).message(ex.getMessage())
				.data(null).error("User Not Found").timeStamp(LocalDateTime.now()).build();
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
	}

	@ExceptionHandler(UserAlreadyExistsException.class)
	public ResponseEntity<ResponseEnvelope> handleUserAlreadyExistsException(UserAlreadyExistsException ex) {
		ResponseEnvelope response = ResponseEnvelope.builder().status(ResponseStatus.ERROR).message(ex.getMessage())
				.data(null).error("User Already Exists").timeStamp(LocalDateTime.now()).build();
		return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
	}

	// Handle other exceptions similarly

	@ExceptionHandler(UnauthorizedAccessException.class)
	public ResponseEntity<ResponseEnvelope> handleUnauthorizedException(UnauthorizedAccessException ex,
			WebRequest request) {
		ResponseEnvelope response = ResponseEnvelope.builder().status(ResponseStatus.ERROR).message(ex.getMessage())
				.data(null).error("Unauthorized Access").timeStamp(LocalDateTime.now()).build();
		return new ResponseEntity<>(response, HttpStatus.FORBIDDEN); // Return 403 Forbidden
	}

	@ExceptionHandler(Exception.class)
	public final ResponseEntity<ResponseEnvelope> handleAllException(Exception exception, WebRequest request) {
		ResponseEnvelope response = ResponseEnvelope.builder().status(ResponseStatus.ERROR)
				.message("An unexpected error occurred.").data(null).error(exception.getMessage())
				.timeStamp(LocalDateTime.now()).build();
		return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	protected ResponseEntity<ResponseEnvelope> handleMethodArgumentNotValid(MethodArgumentNotValidException exception,
			WebRequest request) {
		List<String> errorMessages = exception.getFieldErrors().stream()
				.map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
				.collect(Collectors.toList());

		String defaultErrorMessage = "Validation failed for fields: " + String.join(", ", errorMessages);

		ResponseEnvelope response = ResponseEnvelope.builder().status(ResponseStatus.ERROR).message(defaultErrorMessage)
				.data(null).error("Validation Error").timeStamp(LocalDateTime.now()).build();

		return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
	}

}
