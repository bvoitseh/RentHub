package com.bohakka.renthub.controller;

import com.bohakka.renthub.dto.EmailRequest;
import com.bohakka.renthub.dto.LoginRequest;
import com.bohakka.renthub.dto.RegisterRequest;
import com.bohakka.renthub.dto.ResetPasswordRequest;
import com.bohakka.renthub.dto.TokenRequest;
import com.bohakka.renthub.dto.UserResponse;
import com.bohakka.renthub.entity.User;
import com.bohakka.renthub.security.AppUserDetails;
import com.bohakka.renthub.service.AccountService;
import com.bohakka.renthub.service.EmailVerificationService;
import com.bohakka.renthub.service.LoginService;
import com.bohakka.renthub.service.PasswordResetService;
import com.bohakka.renthub.service.RegistrationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** /api/v1/auth — registration, login, restore, e-mail verification and password reset. Logout is handled by Spring Security. */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
class AuthController {

	private final RegistrationService registration;
	private final LoginService login;
	private final AccountService accounts;
	private final EmailVerificationService verification;
	private final PasswordResetService passwordReset;

	@PostMapping("/register")
	@ResponseStatus(HttpStatus.CREATED)
	UserResponse register(@Valid @RequestBody RegisterRequest request, HttpServletRequest http,
			HttpServletResponse response) {
		User user = registration.register(request, http.getRemoteAddr());
		return UserResponse.from(login.startSession(user, http, response));
	}

	@PostMapping("/login")
	UserResponse login(@Valid @RequestBody LoginRequest request, HttpServletRequest http, HttpServletResponse response) {
		return UserResponse.from(login.login(request, http, response));
	}

	@PostMapping("/restore")
	UserResponse restore(@Valid @RequestBody LoginRequest request, HttpServletRequest http,
			HttpServletResponse response) {
		return UserResponse.from(login.restore(request, http, response));
	}

	@GetMapping("/me")
	UserResponse me(@AuthenticationPrincipal AppUserDetails principal) {
		return UserResponse.from(accounts.get(principal.getId()));
	}

	@PostMapping("/verify-email")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	void verifyEmail(@Valid @RequestBody TokenRequest request) {
		verification.verify(request.token());
	}

	@PostMapping("/resend-verification")
	@ResponseStatus(HttpStatus.ACCEPTED)
	void resendVerification(@AuthenticationPrincipal AppUserDetails principal) {
		verification.resend(principal.getId());
	}

	@PostMapping("/forgot-password")
	@ResponseStatus(HttpStatus.ACCEPTED)
	void forgotPassword(@Valid @RequestBody EmailRequest request) {
		passwordReset.requestReset(request.email());
	}

	@PostMapping("/reset-password")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	void resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
		passwordReset.reset(request.token(), request.password());
	}
}
