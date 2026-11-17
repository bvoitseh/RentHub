package com.bohakka.renthub.exception;

/** Machine-readable {@code code} values for infrastructure-level problems; features define their own (e.g. {@code booking.conflict}). */
public final class ErrorCodes {

	public static final String VALIDATION = "validation_failed";
	public static final String BAD_REQUEST = "bad_request";
	public static final String NOT_FOUND = "not_found";
	public static final String UNAUTHENTICATED = "unauthenticated";
	public static final String FORBIDDEN = "forbidden";
	public static final String METHOD_NOT_ALLOWED = "method_not_allowed";
	public static final String UNSUPPORTED_MEDIA_TYPE = "unsupported_media_type";
	public static final String PAYLOAD_TOO_LARGE = "payload_too_large";
	public static final String INTERNAL = "internal_error";

	private ErrorCodes() {
	}
}
