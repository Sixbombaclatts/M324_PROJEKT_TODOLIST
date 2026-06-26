package com.example.demo;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiVersionExceptionHandler {

	@ExceptionHandler(InvalidApiVersionException.class)
	public ResponseEntity<String> handleInvalidApiVersion(InvalidApiVersionException exception) {
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exception.getMessage());
	}

}
