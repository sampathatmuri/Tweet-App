package com.tweetapp.repository;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.TextCriteria;
import org.springframework.data.mongodb.core.query.TextQuery;

import com.tweetapp.document.Tweet;

public class TweetCustomRepositoryImpl implements TweetCustomRepository {

	@Autowired
	private MongoTemplate template;

	public TweetCustomRepositoryImpl(MongoTemplate template) {
		this.template = template;
	}

	@Override
	public List<Tweet> getTrendingTweets(String tagName) {
		TextCriteria criteria = TextCriteria.forDefaultLanguage().matchingPhrase(tagName);

		Query query = TextQuery.queryText(criteria).with(Sort.by("likeCount").descending());
		return template.find(query, Tweet.class);
	}

}
