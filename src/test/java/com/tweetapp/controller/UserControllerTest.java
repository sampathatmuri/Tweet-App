package com.tweetapp.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tweetapp.document.User;
import com.tweetapp.exception.CustomException;
import com.tweetapp.service.JwtUserDetailsService;
import com.tweetapp.service.UserService;
import com.tweetapp.util.JwtTokenUtil;

@RunWith(SpringRunner.class)
@ComponentScan(basePackages = "com.tweetapp")
@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private UserService userService;

	@Autowired
	private ObjectMapper mapper;

	@MockBean
	private JwtTokenUtil jwtTokenUtil;

	@MockBean
	private JwtUserDetailsService jwtUserDetailsService;

	private final String BEARER_TOKEN = "Bearer jwtToken";

	private UserDetails userDetails;

	@BeforeEach
	void setup() {
		userDetails = new org.springframework.security.core.userdetails.User("sam@gmail.com",
				"$2a$10$9euWhlmO4HQxx1k8VybLv.tVnSMgreL9.7Mf1kSlovtVazLcZwVBa", new ArrayList<>());
		given(jwtTokenUtil.getUsernameFromToken("jwtToken")).willReturn("sam@gmail.com");
		given(jwtUserDetailsService.loadUserByUsername("sam@gmail.com")).willReturn(userDetails);
		given(jwtTokenUtil.validateToken("jwtToken", userDetails)).willReturn(true);

	}

	@Test
	void givenValidToken_whenGetAllUsers_thenReturnUsersTest() throws Exception {

		// given
		List<User> users = new ArrayList<>();
		users.add(User.builder().loginId("1").firstName("Sampath").lastName("Atmuri").emailId("sam@gmail.com")
				.password("$2a$10$9euWhlmO4HQxx1k8VybLv.tVnSMgreL9.7Mjk1kSlovtVazLcZwVBa").contactNumber(7780184807L)
				.build());
		users.add(User.builder().loginId("1akjnsdk").firstName("Ramesh").lastName("Fadatare")
				.emailId("ramesh@gmail.com").password("$2a$10$9euWhlmO4HQxx1k8VybLv.tVnSMgreL9.7Mf1kSlovtVazLcZwVBa")
				.contactNumber(7780184807L).build());
		given(userService.findAllUsers()).willReturn(users);

		// when
		mockMvc.perform(get("/users/all").header("Authorization", BEARER_TOKEN).contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());

		// then
		Mockito.verify(userService, times(1)).findAllUsers();
	}

	@Test
	void givenValidToken_whenGetAllUsers_thenReturnCustomExceptionTest() throws Exception {

		// given
		given(userService.findAllUsers()).willReturn(null);

		// when
		mockMvc.perform(get("/users/all").header("Authorization", BEARER_TOKEN).contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest());

		// then
		Mockito.verify(userService, times(1)).findAllUsers();
	}

	@Test
	void givenValidTokenExistingEmail_whenSearchUser_thenReturnUserTest() throws Exception {

		// given
		User user = User.builder().loginId("1").firstName("Sampath").lastName("Atmuri").emailId("sam@gmail.com")
				.password("$2a$10$9euWhlmO4HQxx1k8VybLv.tVnSMgreL9.7Mjk1kSlovtVazLcZwVBa").contactNumber(7780184807L)
				.build();

		given(userService.findUserByEmailId(user.getEmailId())).willReturn(user);

		// when
		mockMvc.perform(get("/user/search/" + user.getEmailId()).header("Authorization", BEARER_TOKEN)
				.contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk());

		// then
		Mockito.verify(userService, times(1)).findUserByEmailId("sam@gmail.com");
	}

	@Test
	void givenValidTokenNonExistingEmail_whenSearchUser_thenReturnCustomExceptionTest() throws Exception {

		// given
		given(userService.findUserByEmailId(any(String.class))).willReturn(null);

		// when
		mockMvc.perform(get("/user/search/" + any(String.class)).header("Authorization", BEARER_TOKEN)
				.contentType(MediaType.APPLICATION_JSON)).andExpect(status().isBadRequest());

		// then
		Mockito.verify(userService, times(1)).findUserByEmailId(any(String.class));
	}

	@Test
	void givenValidTokenExistingEmail_whenResetPassword_thenReturnSuccessStatusTest() throws Exception {

		// given
		User user = User.builder().loginId("1").firstName("Sampath").lastName("Atmuri").emailId("sam@gmail.com")
				.password("$2a$10$9euWhlmO4HQxx1k8VybLv.tVnSMgreL9.7Mjk1kSlovtVazLcZwVBa").contactNumber(7780184807L)
				.build();
		given(userService.validateIfEmailIdExists(user.getEmailId())).willReturn(true);
		given(userService.findUserByEmailId(user.getEmailId())).willReturn(user);
		given(userService.changePassword(user, "newPassword12")).willReturn(user);

		// when
		mockMvc.perform(put("/" + user.getEmailId() + "/forgot").header("Authorization", BEARER_TOKEN)
				.contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString("newPassword12")))
				.andExpect(status().isOk());

		// then
		Mockito.verify(userService, times(1)).validateIfEmailIdExists(any(String.class));
		Mockito.verify(userService, times(1)).findUserByEmailId(any(String.class));
		Mockito.verify(userService, times(1)).changePassword(any(User.class), any(String.class));
	}

	@Test
	void givenValidTokenOtherExistingEmail_whenResetPassword_thenThrowCustomExceptionTest() throws Exception {

		// given
		User user = User.builder().loginId("1").firstName("Sampath").lastName("Atmuri").emailId("sam@gmail.com")
				.password("$2a$10$9euWhlmO4HQxx1k8VybLv.tVnSMgreL9.7Mjk1kSlovtVazLcZwVBa").contactNumber(7780184807L)
				.build();
		given(userService.validateIfEmailIdExists(user.getEmailId()))
				.willThrow(CustomException.class);
		given(userService.findUserByEmailId(user.getEmailId())).willReturn(null);
		given(userService.changePassword(user, "newPassword12")).willReturn(null);

		// when
		mockMvc.perform(put("/" + user.getEmailId() + "/forgot").header("Authorization", BEARER_TOKEN)
				.contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString("newPassword12")))
				.andExpect(status().isBadRequest());

		// then
		Mockito.verify(userService, times(1)).validateIfEmailIdExists(any(String.class));
		Mockito.verify(userService, times(0)).findUserByEmailId(any(String.class));
		Mockito.verify(userService, times(0)).changePassword(any(User.class), any(String.class));
	}

}
