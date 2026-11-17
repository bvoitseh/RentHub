package com.bohakka.renthub.dto;

/** Shared validation constants for account DTOs (Ukrainian mobile phone, 8-digit ЄДРПОУ, password length). */
final class Validation {

	static final String PHONE = "^\\+380\\d{9}$";
	static final String EDRPOU = "^\\d{8}$";
	static final int PASSWORD_MIN = 8;

	private Validation() {
	}
}
