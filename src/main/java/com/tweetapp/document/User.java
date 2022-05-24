package com.tweetapp.document;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document(collection = "User")
public class User {

	@Id
	@JsonIgnore
	private String loginId;

	private String firstName;

	private String lastName;

	private String emailId;

	@JsonIgnore
	private String password;

	private Long contactNumber;

	@Override
	public String toString() {
		return "User [firstName=" + firstName + ", lastName=" + lastName + ", emailId=" + emailId + ", contactNumber="
				+ contactNumber + "]";
	}

}
