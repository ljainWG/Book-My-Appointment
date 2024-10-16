package com.healthcare.book_my_doctor.security;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtil {

	private String SECRET_KEY = "TaK+HaV^uvCHEFsEVfypW#7g9^k*Z8$V";

	private SecretKey getSigningKey() {
		return Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
	}

	public String extractUsername(String token) {
		Claims claims = extractAllClaims(token);
		return claims.getSubject();
	}

	public Date extractExpiration(String token) {
		return extractAllClaims(token).getExpiration();
	}

	private Claims extractAllClaims(String token) {
		return Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token).getPayload();
	}

	private Boolean isTokenExpired(String token) {
		return extractExpiration(token).before(new Date());
	}

	public String generateToken(String username) {
		Map<String, Object> claims = new HashMap<>();
		return createToken(claims, username);
	}

	private String createToken(Map<String, Object> claims, String subject) {
		return Jwts.builder().claims(claims).subject(subject).header().empty().add("typ", "JWT").and()
				.issuedAt(new Date(System.currentTimeMillis()))
				.expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60)) // 5 minutes expiration time
				.signWith(getSigningKey()).compact();
	}

	@Autowired
	TokenBlacklistService blacklistService;

	public Boolean validateToken(String token) {
		if (blacklistService.isTokenBlacklisted(token)) {
			return false; // Token is invalid if it’s blacklisted
		}
		return !isTokenExpired(token);
	}

	public void blacklistToken(String token) {
		Claims claims = extractAllClaims(token);
		long expirationTime = claims.getExpiration().getTime(); // Get the token's expiration time
		blacklistService.blacklistToken(token); // Store token with expiration in blacklist
	}

}