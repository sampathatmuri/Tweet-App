package com.tweetapp.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tweetapp.document.User;
import com.tweetapp.exception.CustomException;
import com.tweetapp.repository.UserRepository;
import com.tweetapp.util.JwtTokenUtil;

@Service
public class UserServiceImpl implements UserService {

	private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

	private static final String EMAIL_ID_DOESNT_EXIST_MESSAGE = "Email Id doesn't exists";

	private static final String INVALID_USERNAME = "Invalid User as Email Id doesn't exists";

	private static final String VALID_USER = "Valid User";

	private UserRepository userRepository;

	private PasswordEncoder passwordEncoder;

	private JwtTokenUtil jwtTokenUtil;

	// constructor injection
	@Autowired
	public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtTokenUtil jwtTokenUtil) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.jwtTokenUtil = jwtTokenUtil;
	}

	@Transactional
	@Override
	public User registerUser(User user) {
		return userRepository.save(user);
	}

	@Override
	public List<User> findAllUsers() {
		return userRepository.findAll();
	}

	@Override
	public User findUserByEmailId(String emailId) {
		return userRepository.findByEmailId(emailId);
	}

	@Override
	public User changePassword(User user, String newPassword) {
		user.setPassword(passwordEncoder.encode(newPassword));
		return userRepository.save(user);
	}

	@Override
	public Boolean validateIfEmailIdExists(String emailId) {

		User user = userRepository.findByEmailId(emailId);
		if (user == null) {
			logger.info(INVALID_USERNAME);
			throw new CustomException(EMAIL_ID_DOESNT_EXIST_MESSAGE);
		}
		logger.info("User found : {}", user);
		logger.info(VALID_USER);
		return true;
	}

	@Override
	public Boolean validateEmailFromAuthHeader(String emailId, String jwtToken) {
		String currentUserEmailId = jwtTokenUtil.getUsernameFromToken(jwtToken);
		if (!emailId.equals(currentUserEmailId)) {
			logger.info("You don't have access");
			throw new CustomException("You don't have access");
		}
		logger.info(VALID_USER);
		return true;
	}

}
