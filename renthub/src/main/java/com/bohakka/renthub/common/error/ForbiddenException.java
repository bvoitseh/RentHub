package com.bohakka.renthub.common.error;

import org.springframework.http.HttpStatus;

/** 403: the caller is authenticated but is not allowed to act on this resource (ownership/role check). */
public class ForbiddenException extends DomainException {

	public ForbiddenException(String code, String messageKey, Object... messageArgs) {
		super(HttpStatus.FORBIDDEN, code, messageKey, messageArgs);
	}
}
