package com.tweetapp.repository;

import java.util.List;

import com.tweetapp.document.Tweet;

public interface TweetCustomRepository {
	
	List<Tweet> getTrendingTweets(String tagName);

}
