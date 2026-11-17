package com.bohakka.renthub.exception;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.stereotype.Component;

/**
 * Builds RFC 9457 {@link ProblemDetail} bodies in the project's shape:
 * {@code status}, {@code title}, Ukrainian {@code detail}, machine-readable {@code code},
 * and for validation failures an {@code errors} list of {@link FieldViolation}.
 */
@Component
@RequiredArgsConstructor
public class ProblemDetailFactory {

	public static final String CODE_PROPERTY = "code";
	public static final String ERRORS_PROPERTY = "errors";

	private final MessageSource messages;

	/** Resolves a Ukrainian message by key; falls back to the key itself so a missing text never breaks a response. */
	public String text(String key, Object... args) {
		return messages.getMessage(key, args, key, LocaleContextHolder.getLocale());
	}

	public ProblemDetail create(HttpStatusCode status, String code, String detail) {
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
		problem.setProperty(CODE_PROPERTY, code);
		return problem;
	}

	public ProblemDetail fromKey(HttpStatusCode status, String code, String messageKey, Object... args) {
		return create(status, code, text(messageKey, args));
	}

	public ProblemDetail validation(List<FieldViolation> violations) {
		ProblemDetail problem = fromKey(HttpStatusCode.valueOf(400), ErrorCodes.VALIDATION, "error.validation");
		problem.setProperty(ERRORS_PROPERTY, violations);
		return problem;
	}

	public ProblemDetail fromDomain(DomainException ex) {
		ProblemDetail problem = fromKey(ex.getStatus(), ex.getCode(), ex.getMessageKey(), ex.getMessageArgs());
		ex.getProperties().forEach(problem::setProperty);
		return problem;
	}

	/** One invalid field or parameter: {@code field} (dotted path) and a Ukrainian {@code message}. */
	public record FieldViolation(String field, String message) {
	}
}
