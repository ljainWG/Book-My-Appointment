package com.healthcare.book_my_doctor.security;

import java.util.Collections;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import com.healthcare.book_my_doctor.user.UserDTO;
import com.healthcare.book_my_doctor.user.UserRepository;

@Component
public class UserDetailsServiceImpl implements UserDetailsService {
	@Autowired
	private UserRepository userRepository;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		Optional<UserDTO> userRes = userRepository.findByUserName(username);
		if (userRes.isEmpty())
			throw new UsernameNotFoundException("Could not findUser with username = " + username);
		UserDTO user = userRes.get();
		return new org.springframework.security.core.userdetails.User(username, user.getUserPassword(),
				Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
	}
}
