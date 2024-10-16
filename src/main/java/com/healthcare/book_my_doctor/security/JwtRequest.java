package com.healthcare.book_my_doctor.security;

import lombok.Data;

@Data
public class JwtRequest {
	String userName;
	String password;
}
