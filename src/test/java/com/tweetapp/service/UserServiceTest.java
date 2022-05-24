package com.tweetapp.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit4.SpringRunner;

import com.tweetapp.document.User;
import com.tweetapp.dto.SignupRequest;
import com.tweetapp.exception.CustomException;
import com.tweetapp.repository.UserRepository;
import com.tweetapp.util.JwtTokenUtil;

@RunWith(SpringRunner.class)
class UserServiceTest {

	private UserRepository userRepository;

	private PasswordEncoder passwordEncoder;

	private UserServiceImpl userService;

	private JwtTokenUtil jwtTokenUtil;

	private User user;

	private SignupRequest userModel;

	@BeforeEach
	void setup() {
		userRepository = Mockito.mock(UserRepository.class);
		passwordEncoder = Mockito.mock(PasswordEncoder.class);
		jwtTokenUtil = Mockito.mock(JwtTokenUtil.class);
		userService = new UserServiceImpl(userRepository, passwordEncoder, jwtTokenUtil);
		userModel = SignupRequest.builder().firstName("Sampath").lastName("Atmuri").emailId("sam@gmail.com")
				.password("sam123").contactNumber(7780184807L).build();
		user = User.builder().firstName("Sampath").lastName("Atmuri").emailId("sam@gmail.com")
				.password("$2a$10$9euWhlmO4HQxx1k8VybLv.tVnSMgreL9.7Mjk1kSlovtVazLcZwVBa").contactNumber(7780184807L)
				.build();
	}

	@Test
	void givenSignupRequest_whenRegisterUser_thenSaveUserTest() {
		// given
		ModelMapper mapper = new ModelMapper();
		user = mapper.map(userModel, User.class);
		given(userRepository.save(user)).willReturn(user);
		// when
		User savedUser = userService.registerUser(user);
		// then
		assertThat(savedUser).isNotNull();
		verify(userRepository, Mockito.times(1)).save(user);
	}

	@Test
	void givenExisitingUsers_whenFindAllUsers_thenReturnUsersTests() {
		// given
		List<User> users = new ArrayList<>();
		users.add(user);
		users.add(new User("2", "ramesh", "sadsd", "asdasd@gmail.com", "abcd123", 895623147L));
		given(userRepository.findAll()).willReturn(users);
		// when
		List<User> allUsers = userService.findAllUsers();
		// then
		assertThat(allUsers).hasSize(2);
		verify(userRepository, Mockito.times(1)).findAll();
	}

	@Test
	void givenNonExistingUsers_whenFindAllUsers_thenReturnEmptyTests() {
		// given
		given(userRepository.findAll()).willReturn(null);
		// when
		List<User> allUsers = userService.findAllUsers();
		// then
		assertThat(allUsers).isNull();
		verify(userRepository, Mockito.times(1)).findAll();
	}

	@Test
	void givenExisitingEmail_whenFindUserByEmailId_thenReturnUserTest() {
		// given
		given(userRepository.findByEmailId("sam@gmail.com")).willReturn(user);
		// when
		User userFound = userService.findUserByEmailId("sam@gmail.com");
		// then
		assertThat(userFound).isNotNull();
		verify(userRepository, Mockito.times(1)).findByEmailId("sam@gmail.com");
	}

	@Test
	void givenNonExisitingEmail_whenFindUserByEmailId_thenReturnUserTest() {
		// given
		given(userRepository.findByEmailId("sam@gmail.com")).willReturn(null);
		// when
		User userFound = userService.findUserByEmailId("sam@gmail.com");
		// then
		assertThat(userFound).isNull();
		verify(userRepository, Mockito.times(1)).findByEmailId("sam@gmail.com");
	}

	@Test
	void givenValidNewPassword_whenChangePassword_thenModifyPasswordTest() {
		// given
		given(userRepository.save(user)).willReturn(user);
		// when
		User userChanged = userService.changePassword(user, "newPass");
		// then
		assertThat(userChanged).isNotNull();
		verify(userRepository, Mockito.times(1)).save(user);

	}

	@Test
	void givenInvalidNewPassword_whenChangePassword_thenModifyPasswordTest() {
		// given
		given(userRepository.save(user)).willReturn(null);
		// when
		User userChanged = userService.changePassword(user, "newPass");
		// then
		assertThat(userChanged).isNull();
		verify(userRepository, Mockito.times(1)).save(user);

	}

	@Test
	void givenExistingEmail_whenValidateIfEmailIdExists_thenReturnTrueTest() {
		// given
		given(userRepository.findByEmailId("samp@gmail.com")).willReturn(user);
		// when
		Boolean isExists = userService.validateIfEmailIdExists("samp@gmail.com");
		// then
		assertTrue(isExists);
		verify(userRepository, Mockito.times(1)).findByEmailId("samp@gmail.com");
	}

	@Test
	void givenNonExistingEmail_whenValidateIfEmailIdExists_thenThrowCustomExceptionTest() {
		// given
		given(userRepository.findByEmailId("samp@gmail.com")).willReturn(null);
		// when
		assertThatThrownBy(() -> userService.validateIfEmailIdExists("samp@gmail.com")) // NOSONAR
				.isInstanceOf(CustomException.class);
		// then
		verify(userRepository, Mockito.times(1)).findByEmailId("samp@gmail.com");
	}

	@Test
	void givenExistingEmail_whenValidateEmailFromAuthHeader_thenReturnTrueTest() {
		// given
		given(jwtTokenUtil.getUsernameFromToken("jwtToken")).willReturn("samp@gmail.com");
		// when
		Boolean isExists = userService.validateEmailFromAuthHeader("samp@gmail.com", "jwtToken");
		// then
		assertTrue(isExists);
		verify(jwtTokenUtil, Mockito.times(1)).getUsernameFromToken("jwtToken");
	}

	@Test
	void givenNonExistingEmail_whenalidateEmailFromAuthHeader_thenThrowCustomExceptionTest() {
		// given
		given(jwtTokenUtil.getUsernameFromToken("jwtToken")).willReturn(null);
		// when
		assertThatThrownBy(() -> userService.validateEmailFromAuthHeader("samp@gmail.com", "jwtToken")) // NOSONAR
				.isInstanceOf(CustomException.class);
		// then
		verify(jwtTokenUtil, Mockito.times(1)).getUsernameFromToken("jwtToken");
	}
}
