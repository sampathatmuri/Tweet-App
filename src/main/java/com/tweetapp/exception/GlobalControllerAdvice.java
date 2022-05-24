package com.tweetapp.exception;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.tweetapp.dto.APIErrorResponse;

@RestControllerAdvice
class GlobalControllerAdvice extends ResponseEntityExceptionHandler {

	private static final Logger log = LoggerFactory.getLogger(GlobalControllerAdvice.class);

	@ExceptionHandler(AuthenticationException.class)
	public ResponseEntity<APIErrorResponse> handleAuthenticationException(AuthenticationException ex) {
		log.info("Exception handled by AuthenticationException");

		HttpStatus status = HttpStatus.UNAUTHORIZED;
		APIErrorResponse errorResponse = new APIErrorResponse();
		errorResponse.setTimestamp(LocalDateTime.now());
		errorResponse.setError(status.getReasonPhrase());
		errorResponse.setStatus(status.value());
		errorResponse.setMessage(ex.getMessage());
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);

	}

	@ExceptionHandler(CustomException.class)
	public ResponseEntity<APIErrorResponse> handleCustomException(CustomException ex) {
		log.info("Exception handled by CustomException");

		HttpStatus status = HttpStatus.BAD_REQUEST;
		APIErrorResponse errorResponse = new APIErrorResponse();
		errorResponse.setTimestamp(LocalDateTime.now());
		errorResponse.setError(status.getReasonPhrase());
		errorResponse.setStatus(status.value());
		errorResponse.setMessage(ex.getMessage());
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);

	}

	@Override
	protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
			HttpHeaders headers, HttpStatus status, WebRequest request) {
		log.info("Exception handled by MethodArgumentNotValidException");
		Map<Object, Object> body = new HashMap<>();
		body.put("timestamp", LocalDateTime.now());
		body.put("status", status.value());
		body.put("error", status.getReasonPhrase());

		
		List<String> errors = ex.getBindingResult().getFieldErrors().stream()
				.map(DefaultMessageSourceResolvable::getDefaultMessage).collect(Collectors.toList());

		body.put("message", errors);

		return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
	}

}