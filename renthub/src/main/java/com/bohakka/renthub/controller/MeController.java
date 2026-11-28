package com.bohakka.renthub.controller;

import com.bohakka.renthub.dto.GeoConsentRequest;
import com.bohakka.renthub.dto.OnlineStatusRequest;
import com.bohakka.renthub.dto.PasswordConfirmRequest;
import com.bohakka.renthub.dto.ProfileUpdateRequest;
import com.bohakka.renthub.dto.UserResponse;
import com.bohakka.renthub.security.AppUserDetails;
import com.bohakka.renthub.service.AccountService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** /api/v1/users/me — profile, geolocation consent, online-status visibility (D-58), deactivation. */
@RestController
@RequestMapping("/api/v1/users/me")
@RequiredArgsConstructor
class MeController {

	private final AccountService accounts;

	@PutMapping
	UserResponse updateProfile(@AuthenticationPrincipal AppUserDetails principal,
			@Valid @RequestBody ProfileUpdateRequest request) {
		return UserResponse.from(accounts.updateProfile(principal.getId(), request));
	}

	@PutMapping("/geo-consent")
	UserResponse setGeoConsent(@AuthenticationPrincipal AppUserDetails principal,
			@Valid @RequestBody GeoConsentRequest request) {
		return UserResponse.from(accounts.setGeoConsent(principal.getId(), request.consent()));
	}

	@PutMapping("/online-status")
	UserResponse setShowOnlineStatus(@AuthenticationPrincipal AppUserDetails principal,
			@Valid @RequestBody OnlineStatusRequest request) {
		return UserResponse.from(accounts.setShowOnlineStatus(principal.getId(), request.visible()));
	}

	@PostMapping("/deactivate")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	void deactivate(@AuthenticationPrincipal AppUserDetails principal, @Valid @RequestBody PasswordConfirmRequest request,
			HttpServletRequest http) {
		accounts.deactivate(principal.getId(), request.password(), http);
	}
}
