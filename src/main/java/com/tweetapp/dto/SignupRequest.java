package com.tweetapp.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

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
public class SignupRequest {

	@NotBlank(message = "Firstname is required")
	private String firstName;

	@NotBlank(message = "LastName is required")
	private String lastName;

	@NotBlank(message = "EmailId is required")
	private String emailId;

	@NotBlank(message = "Password is required")
	@Pattern(regexp = "((?=.*\\d)(?=.*[a-zA-Z])\\S{6,20})", message = "Your password must be 6-20 characters long, contain letters and numbers, and must not contain spaces, special characters.")
	private String password;

	@NotNull(message = "Contact number is required")
	private Long contactNumber;

}
