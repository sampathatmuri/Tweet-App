package com.tweetapp.controller;

import javax.validation.Valid;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.tweetapp.document.User;
import com.tweetapp.dto.AuthRequest;
import com.tweetapp.dto.AuthResponse;
import com.tweetapp.dto.SignupRequest;
import com.tweetapp.exception.AuthenticationException;
import com.tweetapp.exception.CustomException;
import com.tweetapp.service.EmailService;
import com.tweetapp.service.JwtUserDetailsService;
import com.tweetapp.service.UserService;
import com.tweetapp.util.JwtTokenUtil;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@CrossOrigin(origins = "*")
@RestController
public class JwtAuthenticationController {

	private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationController.class);

	@Autowired
	private AuthenticationManager authenticationManager;

	@Autowired
	private JwtTokenUtil jwtTokenUtil;

	@Autowired
	private JwtUserDetailsService userDetailsService;

	@Autowired
	private UserService userService;

	@Autowired
	private PasswordEncoder passwordEncoder;
	
	@Autowired
	private EmailService emailService;

	@ApiOperation(value = "createAuthenticationToken", notes = "This method helps user to login to the application", response = AuthResponse.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Login Successful!!"),
			@ApiResponse(code = 400, message = "Login Failed!!") })
	@PostMapping("/login")
	public ResponseEntity<AuthResponse> createAuthenticationToken(@RequestBody AuthRequest authenticationRequest)
			throws Exception {
		// authenticate the user
		authenticate(authenticationRequest.getEmailId(), authenticationRequest.getPassword());

		UserDetails userDetails = userDetailsService.loadUserByUsername(authenticationRequest.getEmailId());

		logger.info("Generating JWT token...");
		String token = jwtTokenUtil.generateToken(userDetails);
		logger.info("Token generated");
		logger.info("Login Successful!!");
		return ResponseEntity.ok(new AuthResponse(token));
	}

	@ApiOperation(value = "registerUser", notes = "This method helps user to register to the application", response = String.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Registration Successful!!"),
			@ApiResponse(code = 400, message = "Registration Failed!!") })
	@PostMapping("/register")
	public ResponseEntity<String> registerUser(@Valid @RequestBody() SignupRequest signupRequest) throws Exception {
		logger.info("Valid payload");
		logger.info("Checking if user already exists");
		User user = userService.findUserByEmailId(signupRequest.getEmailId());
		if (user != null) {
			logger.info("Registration failed as Email Id Already Exists");
			throw new CustomException("Email Id Already Exists");
		}
		logger.info("Valid new User");
		logger.info("Registering the user with emailId {} ", signupRequest.getEmailId());
		ModelMapper mapper = new ModelMapper();
		user = mapper.map(signupRequest, User.class);
		user.setPassword(passwordEncoder.encode(signupRequest.getPassword()));
		userService.registerUser(user);
		logger.info("User signed up successfully!!");
		return ResponseEntity.ok("User signed up successfully");
	}

	private void authenticate(String emailId, String password) throws AuthenticationException {
		logger.info("Authenticating the user...");
		try {
			Authentication authentication = authenticationManager
					.authenticate(new UsernamePasswordAuthenticationToken(emailId, password));
			SecurityContextHolder.getContext().setAuthentication(authentication);
			logger.info("Authentication successful");
		} catch (BadCredentialsException e) {
			logger.info("Authentication failed");
			throw new AuthenticationException("Credentials are invalid", e);
		}
	}
	
	@PostMapping("/sendOtp/{emailId}")
	public ResponseEntity<String> sendEmail(@PathVariable String emailId,@RequestBody String otp) {
		logger.info("Verifying that sendOTP request belongs to existing user");
		userService.validateIfEmailIdExists(emailId);
		emailService.sendTextEmail(emailId,otp);
		return ResponseEntity.ok("OTP sent successully");
	}

}