package com.bohakka.renthub.common.error;

import java.util.LinkedHashMap;
import java.util.Map;
import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Base class for business-rule violations raised by the service/domain layer.
 * Carries the HTTP status, a machine-readable {@code code} (e.g. {@code booking.conflict}),
 * a key into {@code messages_uk.properties} with its arguments, and optional extra problem properties
 * (e.g. the list of conflicting days) that are rendered into the RFC 9457 response.
 */
@Getter
public abstract class DomainException extends RuntimeException {

	private final HttpStatus status;
	private final String code;
	private final String messageKey;
	private final transient Object[] messageArgs;
	private final transient Map<String, Object> properties = new LinkedHashMap<>();

	protected DomainException(HttpStatus status, String code, String messageKey, Object... messageArgs) {
		super(code + " (" + messageKey + ")");
		this.status = status;
		this.code = code;
		this.messageKey = messageKey;
		this.messageArgs = messageArgs;
	}

	/** Attaches an extra property to the problem response (fluent). */
	public DomainException withProperty(String name, Object value) {
		properties.put(name, value);
		return this;
	}
}
