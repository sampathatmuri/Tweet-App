package com.tweetapp.service;

import java.util.List;

import com.tweetapp.document.User;

public interface UserService {

	public User registerUser(User user);

	public List<User> findAllUsers();

	public User findUserByEmailId(String emailId);

	public Boolean validateIfEmailIdExists(String emailId);

	public User changePassword(User user, String newPassword);

	Boolean validateEmailFromAuthHeader(String emailId, String jwtToken);

}
