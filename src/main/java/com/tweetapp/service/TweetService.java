package com.tweetapp.service;

import java.util.List;

import com.tweetapp.document.Tags;
import com.tweetapp.document.Tweet;
import com.tweetapp.dto.ReplyDto;

public interface TweetService {

	public Tweet postTweet(Tweet tweet);

	public List<Tweet> findAllTweets();

	public List<Tweet> findAllTweetsOfAUser(String loginId);

	public Tweet findByTweetId(String tweetId);

	public Tweet updateTweet(Tweet tweet, String message);

	public Tweet updateLikeCount(Tweet tweet);

	public Tweet deleteTweet(Tweet tweet);

	public Tweet addReply(Tweet tweet, ReplyDto message);

	public Boolean validatePayload(String message);
	
	public List<Tweet> getTrendingTweets(String tagName);
	
	public List<Tags> findAllTags();

}
