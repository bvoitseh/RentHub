package com.bohakka.renthub.common.error;

import com.bohakka.renthub.common.error.ProblemDetailFactory.FieldViolation;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.validation.method.ParameterValidationResult;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * The single place where exceptions become HTTP responses (RFC 9457, {@code application/problem+json}, Ukrainian texts).
 * Domain exceptions map to their own status/code; framework exceptions handled by the base class are reshaped
 * in {@link #handleExceptionInternal}; anything unexpected becomes a logged 500 without leaking internals.
 */
@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {

	private final ProblemDetailFactory problems;

	@ExceptionHandler(DomainException.class)
	ProblemDetail handleDomain(DomainException ex) {
		return problems.fromDomain(ex);
	}

	@ExceptionHandler(AuthenticationException.class)
	ProblemDetail handleUnauthenticated(AuthenticationException ex) {
		return problems.fromKey(HttpStatus.UNAUTHORIZED, ErrorCodes.UNAUTHENTICATED, "error.unauthenticated");
	}

	@ExceptionHandler(AccessDeniedException.class)
	ProblemDetail handleForbidden(AccessDeniedException ex) {
		return problems.fromKey(HttpStatus.FORBIDDEN, ErrorCodes.FORBIDDEN, "error.forbidden");
	}

	@ExceptionHandler(ConstraintViolationException.class)
	ProblemDetail handleConstraintViolation(ConstraintViolationException ex) {
		List<FieldViolation> violations = ex.getConstraintViolations().stream()
			.map(cv -> new FieldViolation(leafName(cv), cv.getMessage()))
			.toList();
		return problems.validation(violations);
	}

	@ExceptionHandler(Exception.class)
	ProblemDetail handleUnexpected(Exception ex) {
		log.error("Unhandled exception", ex);
		return problems.fromKey(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCodes.INTERNAL, "error.internal");
	}

	@Override
	protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers,
			HttpStatusCode status, WebRequest request) {
		List<FieldViolation> violations = ex.getBindingResult().getAllErrors().stream()
			.map(error -> new FieldViolation(fieldName(error), error.getDefaultMessage()))
			.toList();
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problems.validation(violations));
	}

	@Override
	protected ResponseEntity<Object> handleHandlerMethodValidationException(HandlerMethodValidationException ex,
			HttpHeaders headers, HttpStatusCode status, WebRequest request) {
		List<FieldViolation> violations = ex.getParameterValidationResults().stream()
			.flatMap(result -> result.getResolvableErrors().stream()
				.map(error -> new FieldViolation(fieldName(result, error), error.getDefaultMessage())))
			.toList();
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problems.validation(violations));
	}

	@Override
	protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body, HttpHeaders headers,
			HttpStatusCode status, WebRequest request) {
		ProblemDetail problem = switch (ex) {
			case NoResourceFoundException ignored -> problems.fromKey(status, ErrorCodes.NOT_FOUND, "error.not_found");
			case NoHandlerFoundException ignored -> problems.fromKey(status, ErrorCodes.NOT_FOUND, "error.not_found");
			case HttpRequestMethodNotSupportedException ignored ->
				problems.fromKey(status, ErrorCodes.METHOD_NOT_ALLOWED, "error.method_not_allowed");
			case HttpMediaTypeNotSupportedException ignored ->
				problems.fromKey(status, ErrorCodes.UNSUPPORTED_MEDIA_TYPE, "error.unsupported_media_type");
			case HttpMessageNotReadableException ignored -> problems.fromKey(status, ErrorCodes.BAD_REQUEST, "error.malformed_body");
			case MissingServletRequestParameterException e ->
				problems.fromKey(status, ErrorCodes.BAD_REQUEST, "error.missing_parameter", e.getParameterName());
			case MethodArgumentTypeMismatchException e ->
				problems.fromKey(status, ErrorCodes.BAD_REQUEST, "error.type_mismatch", e.getName());
			case MaxUploadSizeExceededException ignored ->
				problems.fromKey(status, ErrorCodes.PAYLOAD_TOO_LARGE, "error.payload_too_large");
			default -> status.is5xxServerError()
				? problems.fromKey(status, ErrorCodes.INTERNAL, "error.internal")
				: problems.fromKey(status, ErrorCodes.BAD_REQUEST, "error.bad_request");
		};
		if (status.is5xxServerError()) {
			log.error("Request failed with {}", status, ex);
		}
		return ResponseEntity.status(status).headers(headers).body(problem);
	}

	private static String fieldName(ObjectError error) {
		return error instanceof FieldError fieldError ? fieldError.getField() : error.getObjectName();
	}

	private static String fieldName(ParameterValidationResult result, MessageSourceResolvable error) {
		if (error instanceof FieldError fieldError) {
			return fieldError.getField();
		}
		String name = result.getMethodParameter().getParameterName();
		return name != null ? name : "arg" + result.getMethodParameter().getParameterIndex();
	}

	private static String leafName(ConstraintViolation<?> violation) {
		String path = String.valueOf(violation.getPropertyPath());
		int dot = path.lastIndexOf('.');
		return dot >= 0 ? path.substring(dot + 1) : path;
	}
}
