package com.tweetapp.service;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tweetapp.document.User;

@Service
@Transactional
public class JwtUserDetailsService implements UserDetailsService {

	private static final Logger logger = LoggerFactory.getLogger(JwtUserDetailsService.class);

	@Autowired
	private UserService userService;

	@Override
	public UserDetails loadUserByUsername(String emailId) throws UsernameNotFoundException {
		logger.info("Fetching user details of {} from database", emailId);
		User user = userService.findUserByEmailId(emailId);
		if (user == null) {
			throw new UsernameNotFoundException("User not found with emailId: " + emailId);
		}
		logger.info("Fetched successfully");
		return new org.springframework.security.core.userdetails.User(user.getEmailId(), user.getPassword(),
				new ArrayList<>());
	}
}