package com.tweetapp.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tweetapp.document.User;
import com.tweetapp.dto.AuthRequest;
import com.tweetapp.dto.SignupRequest;
import com.tweetapp.service.JwtUserDetailsService;
import com.tweetapp.service.UserService;
import com.tweetapp.util.JwtTokenUtil;

@RunWith(SpringRunner.class)
@ComponentScan(basePackages = "com.tweetapp")
@SpringBootTest
@AutoConfigureMockMvc
class JwtAutheticationControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private JwtUserDetailsService jwtUserDetailsService;

	@MockBean
	private JwtTokenUtil jwtTokenUtil;

	@MockBean
	private AuthenticationManager authenticationManager;

	@MockBean
	private UserService userService;

	@Autowired
	private ObjectMapper mapper;

	private static AuthRequest authRequest;

	private static UserDetails userDetails;

	private static UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken;

	private static SignupRequest signupRequest;

	private static User user;

	@Autowired
	private ModelMapper modelMapper;

	@BeforeAll
	static void setUp() throws Exception {
		authRequest = AuthRequest.builder().emailId("ramesh@gmail.com")
				.password("$2a$10$9euWhlmO4HQxx1k8VybLv.tVnSMgreL9.7Mf1kSlovtVazLcZwVBa").build();
		usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(authRequest.getEmailId(),
				authRequest.getPassword());
		userDetails = new org.springframework.security.core.userdetails.User(authRequest.getEmailId(),
				authRequest.getPassword(), new ArrayList<>());

		signupRequest = SignupRequest.builder().firstName("Ramesh").lastName("Fadatare").emailId("ramesh@gmail.com")
				.password("abcd389").contactNumber(7780184807L).build();

		user = User.builder().loginId("1akjnsdk").firstName("Ramesh").lastName("Fadatare").emailId("ramesh@gmail.com")
				.password("$2a$10$9euWhlmO4HQxx1k8VybLv.tVnSMgreL9.7Mf1kSlovtVazLcZwVBa").contactNumber(7780184807L)
				.build();
	}

	@Test
	void givenValidCredentials_whenCreateAuthenticationToken_thenReturnTokenTest() throws Exception {
		// given
		given(authenticationManager.authenticate(usernamePasswordAuthenticationToken))
				.willReturn(usernamePasswordAuthenticationToken);
		given(jwtUserDetailsService.loadUserByUsername(authRequest.getEmailId())).willReturn(userDetails);
		given(jwtTokenUtil.generateToken(userDetails)).willReturn("jwtToken");

		// when
		mockMvc.perform(MockMvcRequestBuilders.post("/login").contentType(MediaType.APPLICATION_JSON).content(
				mapper.writeValueAsString(new AuthRequest(authRequest.getEmailId(), authRequest.getPassword()))))
				.andExpect(status().isOk());
		// then
		Mockito.verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
		Mockito.verify(jwtUserDetailsService, times(1)).loadUserByUsername(any(String.class));
		Mockito.verify(jwtTokenUtil, times(1)).generateToken(any(UserDetails.class));
	}

	@Test
	void givenInValidCredentials_whenCreateAuthenticationToken_thenReturnUnauthorizedResponseTest() throws Exception {
		// given
		given(authenticationManager.authenticate(usernamePasswordAuthenticationToken))
				.willThrow(BadCredentialsException.class);
		given(jwtUserDetailsService.loadUserByUsername(authRequest.getEmailId()))
				.willThrow(UsernameNotFoundException.class);
		given(jwtTokenUtil.generateToken(userDetails)).willReturn(any(String.class));

		// when
		mockMvc.perform(MockMvcRequestBuilders.post("/login").contentType(MediaType.APPLICATION_JSON).content(
				mapper.writeValueAsString(new AuthRequest(authRequest.getEmailId(), authRequest.getPassword()))))
				.andExpect(status().isUnauthorized());
		// then
		Mockito.verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
		Mockito.verify(jwtUserDetailsService, times(0)).loadUserByUsername(any(String.class));
		Mockito.verify(jwtTokenUtil, times(0)).generateToken(any(UserDetails.class));

	}

	@Test
	void givenValidPayloadNonExistingUserRequest_whenRegisterUser_thenSaveUserTest() throws Exception {
		// given
		User newUser = modelMapper.map(signupRequest, User.class);
		given(userService.findUserByEmailId(signupRequest.getEmailId())).willReturn(null);
		given(userService.registerUser(newUser)).willReturn(any(User.class));

		// when
		ObjectMapper mapper = new ObjectMapper();
		mockMvc.perform(MockMvcRequestBuilders.post("/register").contentType(MediaType.APPLICATION_JSON)
				.content(mapper.writeValueAsString(signupRequest))).andExpect(status().isOk());
		// then
		Mockito.verify(userService, times(1)).findUserByEmailId(any(String.class));
		Mockito.verify(userService, times(1)).registerUser(any(User.class));
	}

	@Test
	void givenValidPayloadExistingUserRequest_whenRegisterUser_thenThrowCustomExceptionTest() throws Exception {
		// given
		User newUser = modelMapper.map(signupRequest, User.class);
		given(userService.findUserByEmailId(signupRequest.getEmailId())).willReturn(user);
		given(userService.registerUser(newUser)).willReturn(any(User.class));

		// when
		ObjectMapper mapper = new ObjectMapper();
		mockMvc.perform(MockMvcRequestBuilders.post("/register").contentType(MediaType.APPLICATION_JSON)
				.content(mapper.writeValueAsString(signupRequest))).andExpect(status().isBadRequest());
		// then
		Mockito.verify(userService, times(1)).findUserByEmailId(any(String.class));
		Mockito.verify(userService, times(0)).registerUser(any(User.class));
	}

	@Test
	void givenInValidPayloadUserRequest__whenRegisterUser_thenThrowCustomExceptionTest() throws Exception {
		// given
		signupRequest.setFirstName("");
		User newUser = modelMapper.map(signupRequest, User.class);
		given(userService.findUserByEmailId(signupRequest.getEmailId())).willReturn(null);
		given(userService.registerUser(newUser)).willReturn(null);

		// when
		ObjectMapper mapper = new ObjectMapper();
		mockMvc.perform(MockMvcRequestBuilders.post("/register").contentType(MediaType.APPLICATION_JSON)
				.content(mapper.writeValueAsString(signupRequest))).andExpect(status().isBadRequest());
		Mockito.verify(userService, times(0)).findUserByEmailId(any(String.class));
		Mockito.verify(userService, times(0)).registerUser(any(User.class));
	}
}
