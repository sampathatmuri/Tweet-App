package com.tweetapp.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.tweetapp.document.Tweet;

@Repository
public interface TweetRepository extends MongoRepository<Tweet, String>, TweetCustomRepository{

	List<Tweet> findAllByEmailId(String emailId);
	
	Tweet findByTweetId(String tweetId);

}
