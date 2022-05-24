package com.tweetapp.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
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
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tweetapp.document.Tags;
import com.tweetapp.document.Tweet;
import com.tweetapp.document.User;
import com.tweetapp.dto.ReplyDto;
import com.tweetapp.dto.TweetDto;
import com.tweetapp.exception.CustomException;
import com.tweetapp.service.JwtUserDetailsService;
import com.tweetapp.service.TweetService;
import com.tweetapp.service.UserService;
import com.tweetapp.util.JwtTokenUtil;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;

@RunWith(SpringRunner.class)
@ComponentScan(basePackages = "com.tweetapp")
@SpringBootTest
@AutoConfigureMockMvc
class TweetControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private TweetService tweetService;

	@MockBean
	private UserService userService;

	@Autowired
	private ObjectMapper mapper;

	@Autowired
	private ModelMapper modelMapper;

	@MockBean
	private JwtTokenUtil jwtTokenUtil;

	@MockBean
	private JwtUserDetailsService jwtUserDetailsService;

	private final String BEARER_TOKEN = "Bearer jwtToken";

	private UserDetails userDetails;
	private TweetDto tweetModel;
	private User user;
	private ReplyDto replyDto;

	@BeforeEach
	void setup() {
		user = User.builder().loginId("1akjnsdk").firstName("Ramesh").lastName("Fadatare").emailId("ramesh@gmail.com")
				.password("$2a$10$9euWhlmO4HQxx1k8VybLv.tVnSMgreL9.7Mf1kSlovtVazLcZwVBa").contactNumber(7780184807L)
				.build();
		tweetModel = TweetDto.builder().emailId("sampath@gmail.com").message("Hello").likeCount(0)
				.createdAt(LocalDateTime.now()).build();
		replyDto = ReplyDto.builder().emailId("samp@gmail.com").message("ReplyMessage").build();
		userDetails = new org.springframework.security.core.userdetails.User("sam@gmail.com",
				"$2a$10$9euWhlmO4HQxx1k8VybLv.tVnSMgreL9.7Mf1kSlovtVazLcZwVBa", new ArrayList<>());
		given(jwtTokenUtil.getUsernameFromToken("jwtToken")).willReturn("sam@gmail.com");
		given(jwtUserDetailsService.loadUserByUsername("sam@gmail.com")).willReturn(userDetails);
		given(jwtTokenUtil.validateToken("jwtToken", userDetails)).willReturn(true);
	}

	@Test
	void givenInvalidToken_whenGetAllTweets_thenThrowIllegalArgumentExceptionTest() throws Exception {
		// given
		given(jwtTokenUtil.getUsernameFromToken("jwtToken")).willThrow(IllegalArgumentException.class);
		given(tweetService.findAllTweets()).willReturn(null);
		// when
		mockMvc.perform(get("/all").header("Authorization", BEARER_TOKEN).contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isUnauthorized());
		// then
		Mockito.verify(tweetService, times(0)).findAllTweets();
	}

	@Test
	void givenInvalidExpiredToken_whenGetAllTweets_thenThrowExpiredJwtExceptionTest() throws Exception {
		// given
		given(jwtTokenUtil.getUsernameFromToken("jwtToken")).willThrow(ExpiredJwtException.class);
		given(tweetService.findAllTweets()).willReturn(null);
		// when
		mockMvc.perform(get("/all").header("Authorization", BEARER_TOKEN).contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isUnauthorized());
		// then
		Mockito.verify(tweetService, times(0)).findAllTweets();
	}

	@Test
	void givenInvalidMalformedToken_whenGetAllTweets_thenThrowMalformedJwtExceptionTest() throws Exception {
		// given
		given(jwtTokenUtil.getUsernameFromToken("jwtToken")).willThrow(MalformedJwtException.class);
		given(tweetService.findAllTweets()).willReturn(null);
		// when
		mockMvc.perform(get("/all").header("Authorization", BEARER_TOKEN).contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isUnauthorized());
		// then
		Mockito.verify(tweetService, times(0)).findAllTweets();
	}

	@Test
	void givenValidTokenAndValidMessageAndExistingEmail_whenSaveTweet_thenReturnTweetTest() throws Exception {

		// given
		User user = User.builder().loginId("1akjnsdk").firstName("Ramesh").lastName("Fadatare")
				.emailId("ramesh@gmail.com").password("$2a$10$9euWhlmO4HQxx1k8VybLv.tVnSMgreL9.7Mf1kSlovtVazLcZwVBa")
				.contactNumber(7780184807L).build();
		given(tweetService.validatePayload(tweetModel.getMessage())).willReturn(true);
		given(userService.validateEmailFromAuthHeader(user.getEmailId(), BEARER_TOKEN.substring(7))).willReturn(true);
		given(tweetService.postTweet(modelMapper.map(tweetModel, Tweet.class))).willReturn(any(Tweet.class));

		// when
		mockMvc.perform(post("/" + user.getEmailId() + "/add").header("Authorization", BEARER_TOKEN)
				.contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(tweetModel)))
				.andExpect(status().isOk());

		// then
		Mockito.verify(tweetService, times(1)).validatePayload(any(String.class));
		Mockito.verify(userService, times(1)).validateEmailFromAuthHeader(any(String.class), any(String.class));
		Mockito.verify(tweetService, times(1)).postTweet(any(Tweet.class));
	}

	@Test
	void givenValidTokenInvalidMessageAndExistingEmail_whenSaveTweet_thenThrowCustomExceptionTest() throws Exception {

		// given
		given(tweetService.validatePayload(tweetModel.getMessage())).willThrow(CustomException.class);
		given(userService.validateEmailFromAuthHeader(user.getEmailId(), BEARER_TOKEN.substring(7))).willReturn(true);
		given(tweetService.postTweet(modelMapper.map(tweetModel, Tweet.class))).willReturn(any(Tweet.class));

		// when
		mockMvc.perform(post("/" + user.getEmailId() + "/add").header("Authorization", BEARER_TOKEN)
				.contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(tweetModel)))
				.andExpect(status().isBadRequest());

		// then
		Mockito.verify(tweetService, times(1)).validatePayload(any(String.class));
		Mockito.verify(userService, times(0)).validateEmailFromAuthHeader(any(String.class), any(String.class));
		Mockito.verify(tweetService, times(0)).postTweet(any(Tweet.class));
	}

	@Test
	void givenValidTokenValidMessageAndNonExistingEmail_whenSaveTweet_thenThrowCustomExceptionTest() throws Exception {

		// given
		given(tweetService.validatePayload(tweetModel.getMessage())).willReturn(true);
		given(userService.validateEmailFromAuthHeader(user.getEmailId(), BEARER_TOKEN.substring(7)))
				.willThrow(CustomException.class);
		given(tweetService.postTweet(modelMapper.map(tweetModel, Tweet.class))).willReturn(any(Tweet.class));

		// when
		mockMvc.perform(post("/" + user.getEmailId() + "/add").header("Authorization", BEARER_TOKEN)
				.contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(tweetModel)))
				.andExpect(status().isBadRequest());
		// then
		Mockito.verify(tweetService, times(1)).validatePayload(any(String.class));
		Mockito.verify(userService, times(1)).validateEmailFromAuthHeader(any(String.class), any(String.class));
		Mockito.verify(tweetService, times(0)).postTweet(any(Tweet.class));
	}

	@Test
	void givenValidToken_whenGetAllTweets_thenReturnTweetsTest() throws Exception {

		// given
		List<Tweet> tweets = new ArrayList<>();
		tweets.add(Tweet.builder().emailId("sampath@gmail.com").message("Hello").likeCount(0)
				.createdAt(LocalDateTime.now()).build());
		tweets.add(Tweet.builder().emailId("sam@gmail.com").message("bye").likeCount(0).createdAt(LocalDateTime.now())
				.build());
		given(tweetService.findAllTweets()).willReturn(tweets);

		// when
		mockMvc.perform(get("/all").header("Authorization", BEARER_TOKEN).contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
		// then
		Mockito.verify(tweetService, times(1)).findAllTweets();
	}

	@Test
	void givenValidToken_whenGetAllTweets_thenThrowCustomExceptionTest() throws Exception {
		// given
		given(tweetService.findAllTweets()).willReturn(null);
		// when
		mockMvc.perform(get("/all").header("Authorization", BEARER_TOKEN).contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest());
		// then
		Mockito.verify(tweetService, times(1)).findAllTweets();
	}

	@Test
	void givenValidTokenExistingEmailId_whenGetAllTweetsOfUser_thenReturnUsersTest() throws Exception {
		// given
		List<Tweet> tweets = new ArrayList<>();
		tweets.add(Tweet.builder().emailId("samp@gmail.com").message("Hello").likeCount(0)
				.createdAt(LocalDateTime.now()).build());
		tweets.add(Tweet.builder().emailId("sam@gmail.com").message("bye").likeCount(0).createdAt(LocalDateTime.now())
				.build());
		given(userService.validateIfEmailIdExists("sam@gmail.com")).willReturn(true);
		given(tweetService.findAllTweetsOfAUser("sam@gmail.com")).willReturn(tweets);

		// when
		mockMvc.perform(get("/username/sam@gmail.com").header("Authorization", BEARER_TOKEN)
				.contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk());
		// then
		Mockito.verify(userService, times(1)).validateIfEmailIdExists(any(String.class));
		Mockito.verify(tweetService, times(1)).findAllTweetsOfAUser(any(String.class));
	}

	@Test
	void givenValidTokenNonExisitingEmailId_whenGetAllTweetsOfUser_thenThrowCustomExceptionTest() throws Exception {
		// given
		List<Tweet> tweets = new ArrayList<>();
		tweets.add(Tweet.builder().emailId("samp@gmail.com").message("Hello").likeCount(0)
				.createdAt(LocalDateTime.now()).build());
		tweets.add(Tweet.builder().emailId("sam@gmail.com").message("bye").likeCount(0).createdAt(LocalDateTime.now())
				.build());
		given(userService.validateIfEmailIdExists("sample@gmail.com")).willThrow(CustomException.class);
		given(tweetService.findAllTweetsOfAUser("sample@gmail.com")).willReturn(null);

		// when
		mockMvc.perform(get("/username/sample@gmail.com").header("Authorization", BEARER_TOKEN)
				.contentType(MediaType.APPLICATION_JSON)).andExpect(status().isBadRequest());
		// then
		Mockito.verify(userService, times(1)).validateIfEmailIdExists(any(String.class));
		Mockito.verify(tweetService, times(0)).findAllTweetsOfAUser(any(String.class));
	}

	@Test
	void givenValidTokenExistingEmailIdAndNoTweetsExists_whenGetAllTweetsOfUser_thenThrowCustomExceptionTest()
			throws Exception {
		// given
		given(userService.validateIfEmailIdExists("sam@gmail.com")).willReturn(true);
		given(tweetService.findAllTweetsOfAUser("sam@gmail.com")).willReturn(null);

		// when
		mockMvc.perform(get("/username/sam@gmail.com").header("Authorization", BEARER_TOKEN)
				.contentType(MediaType.APPLICATION_JSON)).andExpect(status().isBadRequest());
		// then
		Mockito.verify(userService, times(1)).validateIfEmailIdExists(any(String.class));
		Mockito.verify(tweetService, times(1)).findAllTweetsOfAUser(any(String.class));
	}

	@Test
	void givenValidTokenValidMessageAndExistingEmailAndTweetId_whenUpdateTweet_thenReturnTweetTest() throws Exception {

		// given
		Tweet tweet = Tweet.builder().emailId("samp@gmail.com").message("Hello").likeCount(0)
				.createdAt(LocalDateTime.now()).build();
		given(tweetService.validatePayload(tweetModel.getMessage())).willReturn(true);
		given(userService.validateEmailFromAuthHeader(user.getEmailId(), BEARER_TOKEN.substring(7))).willReturn(true);
		given(tweetService.findByTweetId("1234")).willReturn(tweet);
		given(tweetService.updateTweet(tweet, "NewMessage")).willReturn(tweet);
		// when
		mockMvc.perform(put("/samp@gmail.com/update/1234").header("Authorization", BEARER_TOKEN)
				.contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(tweetModel)))
				.andExpect(status().isOk());

		// then
		Mockito.verify(tweetService, times(1)).validatePayload(any(String.class));
		Mockito.verify(userService, times(1)).validateEmailFromAuthHeader(any(String.class), any(String.class));
		Mockito.verify(tweetService, times(1)).findByTweetId(any(String.class));
		Mockito.verify(tweetService, times(1)).updateTweet(any(Tweet.class), any(String.class));
	}

	@Test
	void givenValidTokenInValidMessageAndExistingEmailAndTweetId_whenUpdateTweet_thenThrowCustomExceptionTest()
			throws Exception {

		// given
		Tweet tweet = Tweet.builder().emailId("samp@gmail.com").message("Hello").likeCount(0)
				.createdAt(LocalDateTime.now()).build();
		given(tweetService.validatePayload(tweetModel.getMessage())).willThrow(CustomException.class);
		given(userService.validateEmailFromAuthHeader(user.getEmailId(), BEARER_TOKEN.substring(7))).willReturn(true);
		given(tweetService.findByTweetId("1234")).willReturn(tweet);
		given(tweetService.updateTweet(tweet, "NewMessage")).willReturn(tweet);
		// when
		mockMvc.perform(put("/samp@gmail.com/update/1234").header("Authorization", BEARER_TOKEN)
				.contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(tweetModel)))
				.andExpect(status().isBadRequest());

		// then
		Mockito.verify(tweetService, times(1)).validatePayload(any(String.class));
		Mockito.verify(userService, times(0)).validateEmailFromAuthHeader(any(String.class), any(String.class));
		Mockito.verify(tweetService, times(0)).findByTweetId(any(String.class));
		Mockito.verify(tweetService, times(0)).updateTweet(any(Tweet.class), any(String.class));
	}

	@Test
	void givenValidTokenValidMessageAndNonExistingEmailAndTweetId_whenUpdateTweet_thenThrowCustomExceptionTest()
			throws Exception {

		// given
		Tweet tweet = Tweet.builder().emailId("samp@gmail.com").message("Hello").likeCount(0)
				.createdAt(LocalDateTime.now()).build();
		given(tweetService.validatePayload(tweetModel.getMessage())).willReturn(true);
		given(userService.validateEmailFromAuthHeader("samp@gmail.com", BEARER_TOKEN.substring(7))).willReturn(true);
		given(tweetService.findByTweetId("1234")).willReturn(null);
		given(tweetService.updateTweet(tweet, "NewMessage")).willReturn(null);
		// when
		mockMvc.perform(put("/samp@gmail.com/update/1234").header("Authorization", BEARER_TOKEN)
				.contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(tweetModel)))
				.andExpect(status().isBadRequest());

		// then
		Mockito.verify(tweetService, times(1)).validatePayload(any(String.class));
		Mockito.verify(userService, times(1)).validateEmailFromAuthHeader(any(String.class), any(String.class));
		Mockito.verify(tweetService, times(1)).findByTweetId(any(String.class));
		Mockito.verify(tweetService, times(0)).updateTweet(any(Tweet.class), any(String.class));
	}

	@Test
	void givenValidTokenValidMessageAndExistingEmailAndInvalidTweetId_whenUpdateTweet_thenThrowCustomExceptionTest()
			throws Exception {

		// given
		Tweet tweet = Tweet.builder().emailId("samp@gmail.com").message("Hello").likeCount(0)
				.createdAt(LocalDateTime.now()).build();
		given(tweetService.validatePayload(tweetModel.getMessage())).willReturn(true);
		given(userService.validateEmailFromAuthHeader(user.getEmailId(), BEARER_TOKEN.substring(7))).willReturn(true);
		given(tweetService.findByTweetId("1234")).willReturn(null);
		given(tweetService.updateTweet(tweet, "NewMessage")).willReturn(null);
		// when
		mockMvc.perform(put("/samp@gmail.com/update/1234").header("Authorization", BEARER_TOKEN)
				.contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(tweetModel)))
				.andExpect(status().isBadRequest());

		// then
		Mockito.verify(tweetService, times(1)).validatePayload(any(String.class));
		Mockito.verify(userService, times(1)).validateEmailFromAuthHeader(any(String.class), any(String.class));
		Mockito.verify(tweetService, times(1)).findByTweetId(any(String.class));
		Mockito.verify(tweetService, times(0)).updateTweet(any(Tweet.class), any(String.class));
	}

	@Test
	void givenValidTokenValidMessageAndDifferentValidEmailAndValidTweetId_whenUpdateTweet_thenThrowCustomExceptionTest()
			throws Exception {

		// given
		Tweet tweet = Tweet.builder().emailId("samp1@gmail.com").message("Hello").likeCount(0)
				.createdAt(LocalDateTime.now()).build();
		given(tweetService.validatePayload(tweetModel.getMessage())).willReturn(true);
		given(userService.validateEmailFromAuthHeader(user.getEmailId(), BEARER_TOKEN.substring(7))).willReturn(true);
		given(tweetService.findByTweetId("1234")).willReturn(tweet);
		given(tweetService.updateTweet(tweet, "NewMessage")).willReturn(null);
		// when
		mockMvc.perform(put("/samp@gmail.com/update/1234").header("Authorization", BEARER_TOKEN)
				.contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(tweetModel)))
				.andExpect(status().isBadRequest());

		// then
		Mockito.verify(tweetService, times(1)).validatePayload(any(String.class));
		Mockito.verify(userService, times(1)).validateEmailFromAuthHeader(any(String.class), any(String.class));
		Mockito.verify(tweetService, times(1)).findByTweetId(any(String.class));
		Mockito.verify(tweetService, times(0)).updateTweet(any(Tweet.class), any(String.class));
	}

	@Test
	void givenValidTokenExistingEmailAndTweetId_whenDeleteTweet_thenReturnTweetTest() throws Exception {

		// given
		Tweet tweet = Tweet.builder().emailId("samp@gmail.com").message("Hello").likeCount(0)
				.createdAt(LocalDateTime.now()).build();
		given(userService.validateEmailFromAuthHeader("samp@gmail.com", BEARER_TOKEN.substring(7))).willReturn(true);
		given(tweetService.findByTweetId("1234")).willReturn(tweet);
		given(tweetService.deleteTweet(tweet)).willReturn(tweet);
		// when
		mockMvc.perform(delete("/samp@gmail.com/delete/1234").header("Authorization", BEARER_TOKEN)
				.contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(tweetModel)))
				.andExpect(status().isOk());

		// then
		Mockito.verify(userService, times(1)).validateEmailFromAuthHeader(any(String.class), any(String.class));
		Mockito.verify(tweetService, times(1)).findByTweetId(any(String.class));
		Mockito.verify(tweetService, times(1)).deleteTweet(any(Tweet.class));
	}

	@Test
	void givenValidTokenNonExistingEmailAndTweetId_whenDeleteTweet_thenThrowCustomExceptionTest() throws Exception {

		// given
		Tweet tweet = Tweet.builder().emailId("samp@gmail.com").message("Hello").likeCount(0)
				.createdAt(LocalDateTime.now()).build();
		given(userService.validateEmailFromAuthHeader("samp@gmail.com", BEARER_TOKEN.substring(7)))
				.willThrow(CustomException.class);
		given(tweetService.findByTweetId("1234")).willReturn(null);
		given(tweetService.deleteTweet(tweet)).willReturn(null);
		// when
		mockMvc.perform(delete("/samp@gmail.com/delete/1234").header("Authorization", BEARER_TOKEN)
				.contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(tweetModel)))
				.andExpect(status().isBadRequest());

		// then
		Mockito.verify(userService, times(1)).validateEmailFromAuthHeader(any(String.class), any(String.class));
		Mockito.verify(tweetService, times(0)).findByTweetId(any(String.class));
		Mockito.verify(tweetService, times(0)).deleteTweet(any(Tweet.class));
	}

	@Test
	void givenValidTokenExistingEmailAndInvalidTweetId_whenDeleteTweet_thenThrowCustomExceptionTest() throws Exception {

		// given
		Tweet tweet = Tweet.builder().emailId("samp@gmail.com").message("Hello").likeCount(0)
				.createdAt(LocalDateTime.now()).build();
		given(userService.validateEmailFromAuthHeader("samp@gmail.com", BEARER_TOKEN.substring(7))).willReturn(true);
		given(tweetService.findByTweetId("1234")).willReturn(null);
		given(tweetService.deleteTweet(tweet)).willReturn(null);
		// when
		mockMvc.perform(delete("/samp@gmail.com/delete/1234").header("Authorization", BEARER_TOKEN)
				.contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(tweetModel)))
				.andExpect(status().isBadRequest());

		// then
		Mockito.verify(userService, times(1)).validateEmailFromAuthHeader(any(String.class), any(String.class));
		Mockito.verify(tweetService, times(1)).findByTweetId(any(String.class));
		Mockito.verify(tweetService, times(0)).deleteTweet(any(Tweet.class));
	}

	@Test
	void givenValidTokenDifferentEmailAndExisitingTweetId_whenDeleteTweet_thenThrowCustomExceptionTest()
			throws Exception {

		// given
		Tweet tweet = Tweet.builder().emailId("samp1@gmail.com").message("Hello").likeCount(0)
				.createdAt(LocalDateTime.now()).build();
		given(userService.validateEmailFromAuthHeader("samp@gmail.com", BEARER_TOKEN.substring(7))).willReturn(true);
		given(tweetService.findByTweetId("1234")).willReturn(tweet);
		given(tweetService.deleteTweet(tweet)).willReturn(null);
		// when
		mockMvc.perform(delete("/samp@gmail.com/delete/1234").header("Authorization", BEARER_TOKEN)
				.contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(tweetModel)))
				.andExpect(status().isBadRequest());

		// then
		Mockito.verify(userService, times(1)).validateEmailFromAuthHeader(any(String.class), any(String.class));
		Mockito.verify(tweetService, times(1)).findByTweetId(any(String.class));
		Mockito.verify(tweetService, times(0)).deleteTweet(any(Tweet.class));
	}

	@Test
	void givenValidTokenExistingEmailAndTweetId_whenUpdateLikeCount_thenReturnTweetTest() throws Exception {

		// given
		Tweet tweet = Tweet.builder().emailId("samp1@gmail.com").message("Hello").likeCount(0)
				.createdAt(LocalDateTime.now()).build();
		given(userService.validateEmailFromAuthHeader("samp@gmail.com", BEARER_TOKEN.substring(7))).willReturn(true);
		given(tweetService.findByTweetId("1234")).willReturn(tweet);
		given(tweetService.updateLikeCount(tweet)).willReturn(tweet);
		// when
		mockMvc.perform(patch("/samp@gmail.com/like/1234").header("Authorization", BEARER_TOKEN)
				.contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(tweetModel)))
				.andExpect(status().isOk());

		// then
		Mockito.verify(userService, times(1)).validateEmailFromAuthHeader(any(String.class), any(String.class));
		Mockito.verify(tweetService, times(1)).findByTweetId(any(String.class));
		Mockito.verify(tweetService, times(1)).updateLikeCount(any(Tweet.class));
	}

	@Test
	void givenValidTokenNonExistingEmailAndTweetId_whenUpdateLikeCount_thenThrowCustomExceptionTest() throws Exception {

		// given
		Tweet tweet = Tweet.builder().emailId("samp1@gmail.com").message("Hello").likeCount(0)
				.createdAt(LocalDateTime.now()).build();
		given(userService.validateEmailFromAuthHeader("samp@gmail.com", BEARER_TOKEN.substring(7)))
				.willThrow(CustomException.class);
		given(tweetService.findByTweetId("1234")).willReturn(null);
		given(tweetService.updateLikeCount(tweet)).willReturn(null);
		// when
		mockMvc.perform(patch("/samp@gmail.com/like/1234").header("Authorization", BEARER_TOKEN)
				.contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(tweetModel)))
				.andExpect(status().isBadRequest());

		// then
		Mockito.verify(userService, times(1)).validateEmailFromAuthHeader(any(String.class), any(String.class));
		Mockito.verify(tweetService, times(0)).findByTweetId(any(String.class));
		Mockito.verify(tweetService, times(0)).updateLikeCount(any(Tweet.class));
	}

	@Test
	void givenValidTokenExistingEmailAndInvalidTweetId_whenUpdateLikeCount_thenThrowCustomExceptionTest()
			throws Exception {

		// given
		Tweet tweet = Tweet.builder().emailId("samp1@gmail.com").message("Hello").likeCount(0)
				.createdAt(LocalDateTime.now()).build();
		given(userService.validateEmailFromAuthHeader("samp@gmail.com", BEARER_TOKEN.substring(7))).willReturn(true);
		given(tweetService.findByTweetId("1234")).willReturn(null);
		given(tweetService.updateLikeCount(tweet)).willReturn(null);
		// when
		mockMvc.perform(patch("/samp@gmail.com/like/1234").header("Authorization", BEARER_TOKEN)
				.contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(tweetModel)))
				.andExpect(status().isBadRequest());

		// then
		Mockito.verify(userService, times(1)).validateEmailFromAuthHeader(any(String.class), any(String.class));
		Mockito.verify(tweetService, times(1)).findByTweetId(any(String.class));
		Mockito.verify(tweetService, times(0)).updateLikeCount(any(Tweet.class));
	}

	@Test
	void givenValidTokenSameEmailAndExisitingTweetId_whenUpdateLikeCount_thenThrowCustomExceptionTest()
			throws Exception {

		// given
		Tweet tweet = Tweet.builder().emailId("samp@gmail.com").message("Hello").likeCount(0)
				.createdAt(LocalDateTime.now()).build();
		given(userService.validateEmailFromAuthHeader("samp@gmail.com", BEARER_TOKEN.substring(7))).willReturn(true);
		given(tweetService.findByTweetId("1234")).willReturn(tweet);
		given(tweetService.updateLikeCount(tweet)).willReturn(null);
		// when
		mockMvc.perform(patch("/samp@gmail.com/like/1234").header("Authorization", BEARER_TOKEN)
				.contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(tweetModel)))
				.andExpect(status().isBadRequest());

		// then
		Mockito.verify(userService, times(1)).validateEmailFromAuthHeader(any(String.class), any(String.class));
		Mockito.verify(tweetService, times(1)).findByTweetId(any(String.class));
		Mockito.verify(tweetService, times(0)).updateLikeCount(any(Tweet.class));
	}

	@Test
	void givenValidTokenValidMessageAndExistingEmailAndTweetId_whenReplyTweet_thenReturnTweetTest() throws Exception {

		// given
		Tweet tweet = Tweet.builder().emailId("samp1@gmail.com").message("Hello").likeCount(0)
				.createdAt(LocalDateTime.now()).build();
		given(tweetService.validatePayload(replyDto.getMessage())).willReturn(true);
		given(userService.validateEmailFromAuthHeader("samp@gmail.com", BEARER_TOKEN.substring(7))).willReturn(true);
		given(tweetService.findByTweetId("1234")).willReturn(tweet);
		given(tweetService.addReply(tweet, replyDto)).willReturn(tweet);
		// when
		mockMvc.perform(patch("/samp@gmail.com/reply/1234").header("Authorization", BEARER_TOKEN)
				.contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(tweetModel)))
				.andExpect(status().isOk());

		// then
		Mockito.verify(tweetService, times(1)).validatePayload(any(String.class));
		Mockito.verify(userService, times(1)).validateEmailFromAuthHeader(any(String.class), any(String.class));
		Mockito.verify(tweetService, times(1)).findByTweetId(any(String.class));
		Mockito.verify(tweetService, times(1)).addReply(any(Tweet.class), any(ReplyDto.class));
	}

	@Test
	void givenValidTokenInValidMessageAndExistingEmailAndTweetId_whenReplyTweet_thenThrowCustomExceptionTest()
			throws Exception {

		// given
		Tweet tweet = Tweet.builder().emailId("samp1@gmail.com").message("Hello").likeCount(0)
				.createdAt(LocalDateTime.now()).build();
		given(tweetService.validatePayload(replyDto.getMessage())).willThrow(CustomException.class);
		given(userService.validateEmailFromAuthHeader("samp@gmail.com", BEARER_TOKEN.substring(7))).willReturn(true);
		given(tweetService.findByTweetId("1234")).willReturn(tweet);
		given(tweetService.addReply(tweet, replyDto)).willReturn(tweet);
		// when
		mockMvc.perform(patch("/samp@gmail.com/reply/1234").header("Authorization", BEARER_TOKEN)
				.contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(replyDto)))
				.andExpect(status().isBadRequest());

		// then
		Mockito.verify(tweetService, times(1)).validatePayload(any(String.class));
		Mockito.verify(userService, times(0)).validateEmailFromAuthHeader(any(String.class), any(String.class));
		Mockito.verify(tweetService, times(0)).findByTweetId(any(String.class));
		Mockito.verify(tweetService, times(0)).addReply(any(Tweet.class), any(ReplyDto.class));
	}

	@Test
	void givenValidTokenValidMessageAndNonExistingEmailAndTweetId_whenReplyTweet_thenThrowCustomExceptionTest()
			throws Exception {

		// given
		Tweet tweet = Tweet.builder().emailId("samp1@gmail.com").message("Hello").likeCount(0)
				.createdAt(LocalDateTime.now()).build();
		given(tweetService.validatePayload(replyDto.getMessage())).willReturn(true);
		given(userService.validateEmailFromAuthHeader("samp@gmail.com", BEARER_TOKEN.substring(7)))
				.willThrow(CustomException.class);
		given(tweetService.findByTweetId("1234")).willReturn(tweet);
		given(tweetService.addReply(tweet, replyDto)).willReturn(null);
		// when
		mockMvc.perform(patch("/samp@gmail.com/reply/1234").header("Authorization", BEARER_TOKEN)
				.contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(tweetModel)))
				.andExpect(status().isBadRequest());

		// then
		Mockito.verify(tweetService, times(1)).validatePayload(any(String.class));
		Mockito.verify(userService, times(1)).validateEmailFromAuthHeader(any(String.class), any(String.class));
		Mockito.verify(tweetService, times(0)).findByTweetId(any(String.class));
		Mockito.verify(tweetService, times(0)).addReply(any(Tweet.class), any(ReplyDto.class));
	}

	@Test
	void givenValidTokenValidMessageAndExistingEmailAndInvalidTweetId_whenReplyTweet_thenThrowCustomExceptionTest()
			throws Exception {

		// given
		Tweet tweet = Tweet.builder().emailId("samp1@gmail.com").message("Hello").likeCount(0)
				.createdAt(LocalDateTime.now()).build();
		given(tweetService.validatePayload(replyDto.getMessage())).willReturn(true);
		given(userService.validateEmailFromAuthHeader("samp@gmail.com", BEARER_TOKEN.substring(7))).willReturn(true);
		given(tweetService.findByTweetId("1234")).willReturn(null);
		given(tweetService.addReply(tweet, replyDto)).willReturn(null);
		// when
		mockMvc.perform(patch("/samp@gmail.com/reply/1234").header("Authorization", BEARER_TOKEN)
				.contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(tweetModel)))
				.andExpect(status().isBadRequest());

		// then
		Mockito.verify(tweetService, times(1)).validatePayload(any(String.class));
		Mockito.verify(userService, times(1)).validateEmailFromAuthHeader(any(String.class), any(String.class));
		Mockito.verify(tweetService, times(1)).findByTweetId(any(String.class));
		Mockito.verify(tweetService, times(0)).addReply(any(Tweet.class), any(ReplyDto.class));
	}

	@Test
	void givenValidToken_whenGetAllTags_thenReturnTagsTest() throws Exception {

		// given
		List<Tags> tags = new ArrayList<Tags>();
		tags.add(new Tags("#cts", 2));
		tags.add(new Tags("#Konga", 1));
		given(tweetService.findAllTags()).willReturn(tags);

		// when
		mockMvc.perform(get("/trends").header("Authorization", BEARER_TOKEN).contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
		// then
		Mockito.verify(tweetService, times(1)).findAllTags();
	}

	@Test
	void givenValidToken_whenGetAllTags_thenThrowCustomExceptionTest() throws Exception {
		// given
		given(tweetService.findAllTags()).willReturn(null);
		// when
		mockMvc.perform(get("/trends").header("Authorization", BEARER_TOKEN).contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest());
		// then
		Mockito.verify(tweetService, times(1)).findAllTags();
	}

	@Test
	void givenValidToken_whenGetAllTrendingTweets_thenReturnTrendingTweetsTest() throws Exception {

		// given
		List<Tweet> trendingTweets = new ArrayList<>();
		trendingTweets.add(Tweet.builder().emailId("sampath@gmail.com").message("Hello #helloworld").likeCount(0)
				.createdAt(LocalDateTime.now()).build());
		trendingTweets.add(new Tweet("143", "sam@gmail.com", "Bye #helloworld", 0, LocalDateTime.now(), null));
		given(tweetService.getTrendingTweets("helloworld")).willReturn(trendingTweets);

		// when
		mockMvc.perform(get("/trendByTag/helloworld").header("Authorization", BEARER_TOKEN)
				.contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk());
		// then
		Mockito.verify(tweetService, times(1)).getTrendingTweets("helloworld");
	}

	@Test
	void givenValidToken_whenGetAllTrendingTweets_thenThrowCustomExceptionTest() throws Exception {
		// given
		given(tweetService.getTrendingTweets("helloworld")).willReturn(null);
		// when
		mockMvc.perform(get("/trendByTag/helloworld").header("Authorization", BEARER_TOKEN)
				.contentType(MediaType.APPLICATION_JSON)).andExpect(status().isBadRequest());
		// then
		Mockito.verify(tweetService, times(1)).getTrendingTweets("helloworld");
	}

}
