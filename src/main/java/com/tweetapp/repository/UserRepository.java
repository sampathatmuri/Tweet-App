package com.tweetapp.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.tweetapp.document.User;

@Repository
public interface UserRepository extends MongoRepository<User, String> {

	public User findByEmailId(String emailId);

}
