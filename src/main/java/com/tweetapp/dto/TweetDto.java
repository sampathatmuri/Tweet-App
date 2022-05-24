package com.tweetapp.dto;

import java.time.LocalDateTime;
import java.util.List;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

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
public class TweetDto {

	private String tweetId;

	private String emailId;

	@NotBlank(message = "Message is required")
	@Size(min = 1, max = 194, message = "Length should be less than 195 characters")
//	@Pattern(regexp = "((?=[^\\w!])@\\w+\\b)//g.{1,20})", message = "Message should contain atleast one alphanumeric character, max length of 20")
	private String message;

	private int likeCount;

	private LocalDateTime createdAt;

	private List<ReplyDto> replies;
}
