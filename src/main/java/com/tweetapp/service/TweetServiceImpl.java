package com.tweetapp.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tweetapp.document.Tags;
import com.tweetapp.document.Tweet;
import com.tweetapp.dto.ReplyDto;
import com.tweetapp.exception.CustomException;
import com.tweetapp.repository.TagRepository;
import com.tweetapp.repository.TweetRepository;

@Service
public class TweetServiceImpl implements TweetService {

	private static final Logger logger = LoggerFactory.getLogger(TweetServiceImpl.class);

	private static final String INVALID_PAYLOAD = "Please Enter all the mandatory fields";

	@Autowired
	private TweetRepository tweetRepository;

	@Autowired
	private TagRepository tagRepository;

	public TweetServiceImpl(TweetRepository tweetRepository, TagRepository tagRepository) {
		super();
		this.tweetRepository = tweetRepository;
		this.tagRepository = tagRepository;
	}

	@Transactional
	@Override
	public Tweet postTweet(Tweet tweet) {
		saveAllTags(tweet.getMessage().toLowerCase());
		tweet.setCreatedAt(LocalDateTime.now());
		return tweetRepository.save(tweet);
	}

	@Override
	public List<Tweet> findAllTweets() {
		return tweetRepository.findAll();
	}

	@Override
	public List<Tweet> findAllTweetsOfAUser(String emailId) {
		return tweetRepository.findAllByEmailId(emailId);
	}

	@Override
	public Tweet findByTweetId(String tweetId) {
		return tweetRepository.findByTweetId(tweetId);
	}

	// Need to change, logic not finished
	@Transactional
	@Override
	public Tweet addReply(Tweet tweet, ReplyDto replyDto) {
		replyDto.setCreatedAt(LocalDateTime.now());
		List<ReplyDto> replies = tweet.getReplies();
		if (tweet.getReplies() == null) {
			replies = Arrays.asList(replyDto);
		} else {
			replies.add(replyDto);
		}
		tweet.setReplies(replies);
		return tweetRepository.save(tweet);
	}

	@Transactional
	@Override
	public Tweet updateTweet(Tweet tweet, String message) {
		saveAllTags(message.toLowerCase());
		tweet.setMessage(message);
		tweet.setCreatedAt(LocalDateTime.now());
		return tweetRepository.save(tweet);

	}

	@Override
	public Tweet updateLikeCount(Tweet tweet) {
		tweet.setLikeCount(tweet.getLikeCount() + 1);
		return tweetRepository.save(tweet);
	}

	@Override
	public Tweet deleteTweet(Tweet tweet) {
		tweetRepository.delete(tweet);
		return tweet;
	}

	@Override
	public Boolean validatePayload(String message) {
		Boolean isPayloadValid = !message.isBlank();
		if (Boolean.FALSE.equals(isPayloadValid)) {
			logger.info("Invalid add tweet request payload");
			throw new CustomException(INVALID_PAYLOAD);
		}
		return true;
	}

	@Override
	public List<Tweet> getTrendingTweets(String tagName) {
		return tweetRepository.getTrendingTweets("#" + tagName);
	}

	@Override
	public List<Tags> findAllTags() {
		return tagRepository.findAllByOrderByCountDesc();
	}

	public void saveAllTags(String message) {
		List<String> allTags = getAllTags(message);
		for (String tags : allTags) {
			Tags tag = tagRepository.findByTagName(tags);
			if (tag != null && tag.getTagName().equalsIgnoreCase(tags)) {
				tag.setCount(tag.getCount() + 1);
				tagRepository.save(tag);
			} else {
				tagRepository.save(new Tags(tags, 1));
			}
		}
	}

	private List<String> getAllTags(String message) {

		String filteredMessage = message.replaceAll("[,.\\n\\t]", " ");
		String[] splittedMessage = filteredMessage.split(" ");
		HashSet<String> result = new HashSet<>();
		for (String word : splittedMessage) {
			if (word.contains("#") && word.length() >= 2) {
				String[] m = word.split("#");
				for (String k : m) {
					if (k.length() > 0 && !result.contains(k)) {
						result.add("#" + k);
					}

				}

			}

		}
		return new ArrayList<>(result);
	}

}
