package com.bohakka.renthub.common.error;

import org.springframework.http.HttpStatus;

/** 422: the request is well-formed but violates a business rule (e.g. forbidden booking transition). */
public class BusinessRuleException extends DomainException {

	public BusinessRuleException(String code, String messageKey, Object... messageArgs) {
		super(HttpStatus.UNPROCESSABLE_CONTENT, code, messageKey, messageArgs);
	}
}
