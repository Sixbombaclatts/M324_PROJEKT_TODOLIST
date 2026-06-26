package com.example.demo;

public enum ApiVersion {

	V1("v1"),
	V2("v2");

	private final String value;

	ApiVersion(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	public static ApiVersion from(String rawValue) {
		if (rawValue == null || rawValue.isBlank()) {
			return V1;
		}
		for (ApiVersion version : values()) {
			if (version.value.equalsIgnoreCase(rawValue.trim())) {
				return version;
			}
		}
		throw new InvalidApiVersionException(rawValue);
	}

}
