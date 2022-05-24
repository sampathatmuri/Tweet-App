package com.tweetapp.document;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.TextIndexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.tweetapp.dto.ReplyDto;

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
@Document(collection = "Tweet")
public class Tweet {

	@Id
	private String tweetId;

	private String emailId;

	@TextIndexed
	private String message;

	private int likeCount;

	private LocalDateTime createdAt;

	private List<ReplyDto> replies;

	@Override
	public String toString() {
		return "Tweet [emailId=" + emailId + ", message=" + message + ", likeCount=" + likeCount + ", createdAt="
				+ createdAt + ", replies=" + replies + "]";
	}

}
