package com.example.demo;

public class InvalidApiVersionException extends RuntimeException {

	public InvalidApiVersionException(String rawValue) {
		super("Unsupported API version: " + rawValue + ". Supported values: v1, v2");
	}

}
