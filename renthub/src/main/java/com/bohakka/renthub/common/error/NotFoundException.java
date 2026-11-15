package com.bohakka.renthub.common.error;

import org.springframework.http.HttpStatus;

/** 404: the requested entity does not exist or is not visible to the caller. */
public class NotFoundException extends DomainException {

	public NotFoundException(String code, String messageKey, Object... messageArgs) {
		super(HttpStatus.NOT_FOUND, code, messageKey, messageArgs);
	}
}
