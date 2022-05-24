package com.tweetapp.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PasswordReset {
	@NotBlank(message = "Atleast one character is required")
	@Pattern(regexp = "((?=.*\\d)(?=.*[a-zA-Z])\\S{6,20})", message = "Your password must be 6-20 characters long, contain letters and numbers, and must not contain spaces, special characters.")
	private String newPassword;

}
