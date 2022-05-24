package com.tweetapp.dto;

import java.time.LocalDateTime;

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
public class ReplyDto {
	private String emailId;
	private LocalDateTime createdAt;
//	@NotBlank(message = "Message is required")
//	@Size(min = 1, max = 194, message = "Length should be less than 195 characters")
//	@Pattern(regexp = "((?=[^\\w!])@\\w+\\b)//g.{1,20})", message = "Message should contain atleast one alphanumeric character, max length of 20")
	private String message;
}
