package com.healthcare.book_my_doctor.exceptions;

public class UnauthorizedAccessException extends RuntimeException {
	public UnauthorizedAccessException(String message) {
		super(message);
	}
}