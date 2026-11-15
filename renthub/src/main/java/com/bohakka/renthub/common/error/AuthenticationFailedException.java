package com.bohakka.renthub.common.error;

import org.springframework.http.HttpStatus;

/** 401: credentials are missing or wrong. */
public class AuthenticationFailedException extends DomainException {

	public AuthenticationFailedException(String code, String messageKey, Object... messageArgs) {
		super(HttpStatus.UNAUTHORIZED, code, messageKey, messageArgs);
	}
}
