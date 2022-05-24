package com.tweetapp.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.tweetapp.document.Tags;

@Repository
public interface TagRepository extends MongoRepository<Tags, String> {

	Tags findByTagName(String tag);

	List<Tags> findAllByOrderByCountDesc();
	

}
