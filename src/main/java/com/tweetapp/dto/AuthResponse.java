package com.tweetapp.dto;

public class AuthResponse {

	private String jwttoken;

	public AuthResponse(String jwttoken) {
		this.jwttoken = jwttoken;
	}

	public String getToken() {
		return this.jwttoken;
	}
}