package com.tweetapp.controller;

import java.util.List;

import javax.validation.Valid;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import com.tweetapp.document.Tags;
import com.tweetapp.document.Tweet;
import com.tweetapp.dto.ReplyDto;
import com.tweetapp.dto.TweetDto;
import com.tweetapp.exception.CustomException;
import com.tweetapp.service.TweetService;
import com.tweetapp.service.UserService;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@CrossOrigin(origins = "*")
@RestController
public class TweetController {

	private static final Logger logger = LoggerFactory.getLogger(TweetController.class);

	@Autowired
	private TweetService tweetService;

	@Autowired
	private UserService userService;

	@Autowired
	private ModelMapper modelMapper;

	private static final String VALIDATION_MESSAGE = "Valid payload";

	private static final String TWEET_DOESNT_EXIST_WITH_ID = "No tweet exists with id : {}";

	@ApiOperation(value = "saveTweet", notes = "This method helps user to save tweet", response = Tweet.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Tweet saved Successfully!!"),
			@ApiResponse(code = 400, message = "Tweet unsaved!!") })
	@PostMapping("/{emailId}/add")
	public ResponseEntity<Tweet> saveTweet(
			@ApiParam(value = "emailId", name = "emailId", type = "String", example = "abc@gmail.com", required = true) @PathVariable String emailId,
			@Valid @RequestBody TweetDto tweetModel,
			@RequestHeader(name = "Authorization", required = true) String authHeader) {
		logger.info("Validating add tweet request payload and checking if user exists");
		tweetService.validatePayload(tweetModel.getMessage());
		logger.info(VALIDATION_MESSAGE);
		userService.validateEmailFromAuthHeader(emailId, authHeader.substring(7));
		logger.info("Saving tweet for the user with emailId {} ", emailId);
		tweetModel.setEmailId(emailId);
		Tweet tweet = tweetService.postTweet(modelMapper.map(tweetModel, Tweet.class));
		logger.info("Tweet saved successfully");
		return ResponseEntity.ok(tweet);
	}

	@GetMapping("/all")
	@ApiOperation(value = "getAllTweets", notes = "This method helps user to get all tweets", response = Tweet.class)
	public ResponseEntity<List<Tweet>> getAllTweets() {
		logger.info("Fetching all tweets...");
		List<Tweet> tweets = tweetService.findAllTweets();
		if (tweets == null) {
			logger.info("No tweets found");
			throw new CustomException("No tweets found");
		}
		logger.info("Tweets found : {}", tweets);
		return ResponseEntity.ok(tweets);
	}

	@GetMapping("/username/{emailId}")
	@ApiOperation(value = "getAllTweetsOfAUser", notes = "This method helps user to get all tweets of a user", response = Tweet.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Please find all the tweets posted by the user"),
			@ApiResponse(code = 400, message = "Tweets not yet posted by the user") })
	public ResponseEntity<List<Tweet>> getAllTweetsOfAUser(
			@ApiParam(value = "emailId", name = "emailId", type = "String", example = "abc@gmail.com", required = true) @PathVariable String emailId) {
		logger.info("Validating if user exists");
		userService.validateIfEmailIdExists(emailId);
		logger.info("Fetching all tweet of user {}", emailId);
		List<Tweet> tweets = tweetService.findAllTweetsOfAUser(emailId);
		if (tweets == null || tweets.isEmpty()) {
			logger.info("No tweets posted by the user: {}", emailId);
			throw new CustomException("No tweets posted by the user:" + emailId);
		}
		logger.info("Tweets found : {}", tweets);
		return ResponseEntity.ok(tweets);
	}

	@PutMapping("/{emailId}/update/{tweetId}")
	@ApiOperation(value = "UpdateTweet", notes = "This method helps user to update his tweet", response = Tweet.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Password updated sucessfully"),
			@ApiResponse(code = 400, message = "password failed to update") })
	public ResponseEntity<Tweet> updateTweet(
			@ApiParam(value = "emailId", name = "emailId", type = "String", example = "abc@gmail.com", required = true) @PathVariable String emailId,
			@ApiParam(value = "tweetId", name = "tweetId", type = "String", example = "SAM123", required = true) @PathVariable String tweetId,
			@Valid @RequestBody TweetDto tweetModel,
			@RequestHeader(name = "Authorization", required = true) String authHeader) {
		logger.info("Validating update tweet request payload and checking if user exists");
		tweetService.validatePayload(tweetModel.getMessage());
		logger.info(VALIDATION_MESSAGE);
		userService.validateEmailFromAuthHeader(emailId, authHeader.substring(7));
		logger.info("Checking if tweet exists and verifying that tweet belongs to the user : {}", emailId);
		Tweet tweet = tweetService.findByTweetId(tweetId);
		if (tweet == null) {
			logger.info(TWEET_DOESNT_EXIST_WITH_ID, tweetId);
			throw new CustomException("Update failed as no tweet exists with given tweetId");
		}
		if (!tweet.getEmailId().equals(emailId)) {
			logger.info("You can only update the tweets posted by you");
			throw new CustomException("You can only update the tweets posted by you");
		}
		logger.info("Valid tweet and is allowed to update");
		logger.info("Updating tweet for user : {}", emailId);
		tweet = tweetService.updateTweet(tweet, tweetModel.getMessage());
		logger.info("Updated successfully");
		return ResponseEntity.ok(tweet);
	}

	@DeleteMapping("/{emailId}/delete/{tweetId}")
	@ApiOperation(value = "deleteTweet", notes = "This method helps user to delete his/her tweet", response = Tweet.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Tweet Deleted sucessfully"),
			@ApiResponse(code = 400, message = "Failed to delete the tweet") })
	public ResponseEntity<Tweet> deleteTweet(
			@ApiParam(value = "emailId", name = "emailId", type = "String", example = "abc@gmail.com", required = true) @PathVariable String emailId,
			@ApiParam(value = "tweetId", name = "tweetId", type = "String", example = "SAM123", required = true) @PathVariable String tweetId,
			@RequestHeader(name = "Authorization", required = true) String authHeader) {
		userService.validateEmailFromAuthHeader(emailId, authHeader.substring(7));
		logger.info("Checking if tweet exists and verifying that tweet belongs to the user : {}", emailId);
		Tweet tweet = tweetService.findByTweetId(tweetId);
		if (tweet == null) {
			logger.info(TWEET_DOESNT_EXIST_WITH_ID, tweetId);
			throw new CustomException("Delete failed as no tweet exists with given tweetId");
		}
		if (!tweet.getEmailId().equals(emailId)) {
			logger.info("You can only delete the tweets posted by you");
			throw new CustomException("You can only delete the tweets posted by you");
		}
		logger.info("Valid tweet and is allowed to delete");
		logger.info("Deleting tweet for user : {}", emailId);
		tweet = tweetService.deleteTweet(tweet);
		logger.info("Deleted successfully");
		return ResponseEntity.ok(tweet);
	}

	@PatchMapping("/{emailId}/like/{tweetId}")
	@ApiOperation(value = "updateLikeCount", notes = "This method helps user give like to a particular tweet", response = Tweet.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Tweet like count updated successfully"),
			@ApiResponse(code = 400, message = "Unable to update the like count") })
	public ResponseEntity<Tweet> updateLikeCount(
			@ApiParam(value = "emailId", name = "emailId", type = "String", example = "abc@gmail.com", required = true) @PathVariable String emailId,
			@ApiParam(value = "tweetId", name = "tweetId", type = "String", example = "SAM123", required = true) @PathVariable String tweetId,
			@RequestHeader(name = "Authorization", required = true) String authHeader) {
		userService.validateEmailFromAuthHeader(emailId, authHeader.substring(7));
		logger.info("Checking if tweet exists");
		Tweet tweet = tweetService.findByTweetId(tweetId);
		if (tweet == null) {
			logger.info(TWEET_DOESNT_EXIST_WITH_ID, tweetId);
			throw new CustomException("Update likes failed as no tweet exists with given tweetId");
		}
		if (tweet.getEmailId().equals(emailId)) {
			logger.info("You can only like the tweets posted by others");
			throw new CustomException("You can only like the tweets posted by others");
		}
		logger.info("Valid tweet and is allowed to update likes count");
		logger.info("Updating likes for the tweet : {}", tweetId);
		tweet = tweetService.updateLikeCount(tweet);
		logger.info("Likes updated successfully");
		return ResponseEntity.ok(tweet);
	}

	@PatchMapping("/{emailId}/reply/{tweetId}")
	@ApiOperation(value = "replyTweet", notes = "This method helps user to add reply to a tweet", response = Tweet.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Reply added to a Tweet successfully"),
			@ApiResponse(code = 400, message = "Unable to add reply to the tweet") })
	public ResponseEntity<Tweet> replyTweet(
			@ApiParam(value = "emailId", name = "emailId", type = "String", example = "abc@gmail.com", required = true) @PathVariable String emailId,
			@ApiParam(value = "tweetId", name = "tweetId", type = "String", example = "SAM123", required = true) @PathVariable String tweetId,
			@Valid @RequestBody ReplyDto replyModel,
			@RequestHeader(name = "Authorization", required = true) String authHeader) {
		logger.info("Validating reply tweet request payload and checking if user exists");
		tweetService.validatePayload(replyModel.getMessage());
		logger.info(VALIDATION_MESSAGE);
		userService.validateEmailFromAuthHeader(emailId, authHeader.substring(7));
		logger.info("Checking if tweet exists");
		Tweet tweet = tweetService.findByTweetId(tweetId);
		if (tweet == null) {
			logger.info(TWEET_DOESNT_EXIST_WITH_ID, tweetId);
			throw new CustomException("Reply failed as no tweet exists with given tweetId");
		}
		logger.info("Valid tweet and is allowed to reply");
		logger.info("Adding reply for the tweet : {}", tweetId);
		replyModel.setEmailId(emailId);
		tweet = tweetService.addReply(tweet, replyModel);
		logger.info("Reply added successfully");
		return ResponseEntity.ok(tweet);
	}

	@GetMapping("/trends")
	@ApiOperation(value = "getTrendingTags", notes = "This method helps user to get current trending tags", response = Tweet.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "User able to see trending tags"),
			@ApiResponse(code = 400, message = "Unable to see trending tags") })
	public ResponseEntity<List<Tags>> getTrendingTags() {
		logger.info("Fetching all trending tags...");
		List<Tags> tags = tweetService.findAllTags();
		if (tags == null) {
			logger.info("No trending tags found");
			throw new CustomException("No trending tags found");
		}
		logger.info("Trending tags found : {}", tags);
		return ResponseEntity.ok(tags);
	}

	@GetMapping("/trendByTag/{tagname}")
	@ApiOperation(value = "getTrendingTweetsByTagName", notes = "This method helps user to get tweets based on trending tags", response = Tweet.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "User able to see tweets based on trending tags"),
			@ApiResponse(code = 400, message = "Tag Name doesn't exist") })
	public ResponseEntity<List<Tweet>> getTrendingTweetsByTagName(
			@ApiParam(value = "tagname", name = "tagname", type = "tagname", example = "cts", required = true) @PathVariable String tagname) {
		logger.info("Fetching all trending tweets...");
		List<Tweet> tweets = tweetService.getTrendingTweets(tagname);
		if (tweets == null || tweets.isEmpty()) {
			logger.info("No trending tweets found");
			throw new CustomException("No trending tweets found");
		}
		logger.info("Trending Tweets found : {}", tweets);
		return ResponseEntity.ok(tweets);
	}
}
