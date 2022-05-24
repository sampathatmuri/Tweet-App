package com.tweetapp.document;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Document(collection = "Tags")
public class Tags {

	@Id
	@JsonIgnore
	private String id;

	private String tagName;

	private int count;

	public Tags(String tagName, int count) {
		super();
		this.tagName = tagName;
		this.count = count;
	}

}
