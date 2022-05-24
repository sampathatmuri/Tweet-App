package com.tweetapp.controller;

import java.util.List;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.tweetapp.document.Tweet;
import com.tweetapp.document.User;
import com.tweetapp.dto.PasswordReset;
import com.tweetapp.exception.CustomException;
import com.tweetapp.service.UserService;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@CrossOrigin(origins = "*")
@RestController
public class UserController {

	private static final Logger logger = LoggerFactory.getLogger(UserController.class);

	@Autowired
	private UserService userService;

	@GetMapping("/users/all")
	@ApiOperation(value = "getAllUsers", notes = "This method helps user to see other users information", response = User.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "User able to see other users info"),
			@ApiResponse(code = 400, message = "Unable to other user details") })
	public ResponseEntity<List<User>> getAllUsers() {
		logger.info("Fetching all users...");
		List<User> users = userService.findAllUsers();
		if (users == null) {
			logger.info("No users found");
			throw new CustomException("No users found");
		}
		logger.info("Users found : {}", users);
		return ResponseEntity.ok(users);
	}

	@GetMapping("/user/search/{emailId}")
	@ApiOperation(value = "searchUser", notes = "This method helps user to see user details based on user id", response = Tweet.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "User able to see the info"),
			@ApiResponse(code = 400, message = "User unable to see the info") })
	public ResponseEntity<User> searchUser(
			@ApiParam(value = "emailId", name = "emailId", type = "String", example = "abc@gmail.com", required = true) @PathVariable String emailId) {
		logger.info("Searching user with emailId {}", emailId);
		User user = userService.findUserByEmailId(emailId);
		if (user == null) {
			logger.info("Search Failed!!!");
			logger.info("User not found with emailId: {}", emailId);
			throw new CustomException("User not found with emailId: " + emailId);
		}
		logger.info("User found : {}", user);
		return ResponseEntity.ok(user);
	}

	@PutMapping("/{emailId}/forgot")
	@ApiOperation(value = "resetPassword", notes = "This method helps user to reset their password", response = String.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Password updated successfully"),
			@ApiResponse(code = 400, message = "Password was not updated") })
	public ResponseEntity<String> resetPassword(
			@ApiParam(value = "emailId", name = "emailId", type = "String", example = "abc@gmail.com", required = true) @PathVariable String emailId,
			@Valid @RequestBody PasswordReset passwordReset) {
		logger.info("Trying to reset password for user: {}", emailId);
		logger.info("Verifying that reset request belongs to the currently loggedin user");
		userService.validateIfEmailIdExists(emailId);
		User user = userService.findUserByEmailId(emailId);
		logger.info("Valid user and is allowed to change password");
		logger.info("Now changing password...");
		// Save New Password
		userService.changePassword(user, passwordReset.getNewPassword());
		logger.info("Password Changed Successfully");
		return ResponseEntity.ok("Password Changed Successfully");
	}

}
