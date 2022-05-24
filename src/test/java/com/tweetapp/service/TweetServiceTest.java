package com.tweetapp.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.mockito.Mockito;

import com.tweetapp.document.Tags;
import com.tweetapp.document.Tweet;
import com.tweetapp.dto.ReplyDto;
import com.tweetapp.exception.CustomException;
import com.tweetapp.repository.TagRepository;
import com.tweetapp.repository.TweetRepository;

class TweetServiceTest {

	private TweetRepository tweetRepository;

	private TweetService tweetService;

	private Tweet tweet;

	private TagRepository tagRepository;

	List<Tweet> allTweets = new ArrayList<>();

	@BeforeEach
	void setup() {
		tweetRepository = Mockito.mock(TweetRepository.class);
		tagRepository = Mockito.mock(TagRepository.class);
		tweetService = new TweetServiceImpl(tweetRepository, tagRepository);
		tweet = Tweet.builder().emailId("sampath@gmail.com").message("Hello #helloworld").likeCount(0)
				.createdAt(LocalDateTime.now()).build();
		allTweets.add(tweet);
		allTweets.add(new Tweet("143", "sam@gmail.com", "Bye #helloworld", 0, LocalDateTime.now(), null));

	}

	@Test
	void postTweetPositiveTest() {
		given(tweetRepository.save(tweet)).willReturn(tweet);
		Tweet savedTweet = tweetService.postTweet(tweet);
		assertNotNull(savedTweet);
	}

	@Test
	void getAllTweetsTest() {
		given(tweetRepository.findAll()).willReturn(allTweets);
		assertNotNull(tweetService.findAllTweets());
		assertEquals(2, allTweets.size());
	}

	@Test
	void getAllTweetsOfAUserPositiveTest() {
		given(tweetRepository.findAllByEmailId("sampathatmuri31@gmail.com")).willReturn(allTweets);
		List<Tweet> tweets = tweetService.findAllTweetsOfAUser("sampathatmuri31@gmail.com");
		assertNotNull(tweets);
		assertEquals(2, tweets.size());
	}

	@Test
	void getAllTweetsOfAUserNegativeTest() {
		given(tweetRepository.findAllByEmailId("sampathatmuri31@gmail.com")).willReturn(null);
		List<Tweet> tweets = tweetService.findAllTweetsOfAUser("sampathatmuri31@gmail.com");
		assertThat(tweets).isNull();
	}

	@Test
	void getTweetByIdTest() {
		Tweet tweetData = new Tweet("143", "sam@gmail.com", "Bye", 0, LocalDateTime.now(), null);
		given(tweetRepository.findByTweetId("123")).willReturn(tweetData);
		assertNotNull(tweetService.findByTweetId("123"));
	}

	@Test
	void deleteTweetTest() {
		BDDMockito.willDoNothing().given(tweetRepository).delete(tweet);
		tweetService.deleteTweet(tweet);
		Mockito.verify(tweetRepository, times(1)).delete(tweet);
	}

	@Test
	void updateTweetTest() {
		Tweet updateTweet = Tweet.builder().emailId("sampath@gmail.com").message("Update Message").likeCount(0)
				.createdAt(LocalDateTime.now()).build();
		given(tweetRepository.save(tweet)).willReturn(updateTweet);
		Tweet result = tweetService.updateTweet(tweet, "Update Message");
		assertThat(result).isNotNull();
		Mockito.verify(tweetRepository, times(1)).save(tweet);
	}

	@Test
	void updateLikeCountTest() {
		Tweet updateTweet = Tweet.builder().emailId("sampath@gmail.com").message("Message").likeCount(1)
				.createdAt(LocalDateTime.now()).build();
		given(tweetRepository.save(tweet)).willReturn(updateTweet);
		Tweet result = tweetService.updateLikeCount(tweet);
		assertThat(result).isNotNull();
		Mockito.verify(tweetRepository, times(1)).save(tweet);
	}

	@Test
	void addFirstReplyTest() {
		tweet.setTweetId("123");
		ReplyDto replyDto = ReplyDto.builder().message("Message").emailId("sampath@gmail.com").build();
		List<ReplyDto> replies = new ArrayList<>();
		replies.add(replyDto);
		Tweet updatedTweet = Tweet.builder().tweetId("123").emailId("sampath@gmail.com").message("Hello").likeCount(0)
				.createdAt(LocalDateTime.now()).replies(replies).build();
		given(tweetRepository.save(tweet)).willReturn(updatedTweet);
		Tweet savedTweet = tweetService.addReply(tweet, replyDto);
		assertNotNull(savedTweet);
	}

	@Test
	void addNthReplyTest() {
		tweet.setTweetId("123");
		ReplyDto replyDto = ReplyDto.builder().message("Message").emailId("sampath@gmail.com").build();
		List<ReplyDto> replies = new ArrayList<>();
		replies.add(replyDto);
		tweet.setReplies(replies);
		Tweet updatedTweet = Tweet.builder().tweetId("123").emailId("sampath@gmail.com").message("Hello").likeCount(0)
				.createdAt(LocalDateTime.now()).replies(replies).build();
		given(tweetRepository.save(tweet)).willReturn(updatedTweet);
		Tweet savedTweet = tweetService.addReply(tweet, replyDto);
		assertNotNull(savedTweet);
	}

	@Test
	void validatePayloadTest() {
		String message = "Non empty message";
		assertTrue(tweetService.validatePayload(message));
	}

	@Test
	void validateEmptyPayloadTest() {
		String message = "";
		assertThatThrownBy(() -> tweetService.validatePayload(message)) // NOSONAR
				.isInstanceOf(CustomException.class);
	}

	@Test
	void findAllTagsTest() {
		List<Tags> tag = new ArrayList<Tags>();
		tag.add(new Tags("#cts", 2));
		tag.add(new Tags("#Konga", 1));
		given(tagRepository.findAllByOrderByCountDesc()).willReturn(tag);
		assertNotNull(tweetService.findAllTags());
	}

	@Test
	void getTrendingTweetsTest() {
		given(tweetRepository.getTrendingTweets("helloworld")).willReturn(allTweets);
		assertNotNull(tweetService.getTrendingTweets("helloworld"));
	}
}
