package com.tweetapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication
@EnableMongoRepositories(basePackages = "com.tweetapp.repository")
public class FseTweetAppApplication {

	public static void main(String[] args) {
		SpringApplication.run(FseTweetAppApplication.class, args);
	}

}
