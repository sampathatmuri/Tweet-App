package com.tweetapp.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.tweetapp.document.User;
import com.tweetapp.repository.UserRepository;

@SpringBootTest
class JwtUserDetailsServiceTest {

	@Mock
	private UserRepository userRepository;

	@Mock
	private UserServiceImpl userService;

	@InjectMocks
	private JwtUserDetailsService jwtUserDetailsService;

	private User user;

	@BeforeEach
	void setUp() throws Exception {
		user = User.builder().firstName("Ramesh").lastName("Fadatare").emailId("ramesh@gmail.com").password("abcd389")
				.contactNumber(7780184807L).build();
	}

	// JUnit test for loadUserByUserName method
	@DisplayName("JUnit test for loadUserByUserName method which throws exception")
	@Test
	void givenNonExisitingEmailId_whenLoadUserByUserName_thenReturnUsernameNotFoundExceptionTest() {
		// given - precondition or setup
		// given(userRepository.findByEmailId(user.getEmailId())).willReturn(null);
		given(userService.findUserByEmailId(user.getEmailId())).willReturn(null);

		// when - action or the behaviour that we are going test
		assertThatThrownBy(() -> jwtUserDetailsService.loadUserByUsername(user.getEmailId())) // NOSONAR
				.isInstanceOf(UsernameNotFoundException.class);
		// then
		verify(userService, Mockito.times(1)).findUserByEmailId(user.getEmailId());
	}

	// JUnit test for loadUserByUserName method
	@DisplayName("JUnit test for loadUserByUserName method")
	@Test
	void givenExisitingEmailId_whenLoadUserByUserName_thenReturnUserDetailsTest() {
		// given
		given(userRepository.findByEmailId(user.getEmailId())).willReturn(user);
		given(userService.findUserByEmailId(user.getEmailId())).willReturn(user);

		// when
		UserDetails userDetails = jwtUserDetailsService.loadUserByUsername(user.getEmailId());

		// then
		assertThat(userDetails).isNotNull();
//		verify(userRepository, Mockito.times(1)).findByEmailId(user.getEmailId());
		verify(userService, Mockito.times(1)).findUserByEmailId(user.getEmailId());
	}

}
