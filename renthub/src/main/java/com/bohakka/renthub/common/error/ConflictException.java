package com.bohakka.renthub.common.error;

import org.springframework.http.HttpStatus;

/** 409: the operation conflicts with the current state (e.g. no free units, duplicate e-mail). */
public class ConflictException extends DomainException {

	public ConflictException(String code, String messageKey, Object... messageArgs) {
		super(HttpStatus.CONFLICT, code, messageKey, messageArgs);
	}
}
