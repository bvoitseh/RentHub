package com.bohakka.renthub.service;

import com.bohakka.renthub.dto.ProfileUpdateRequest;
import com.bohakka.renthub.entity.User;
import jakarta.servlet.http.HttpServletRequest;

/** The caller's own account: profile (FR-1a), geolocation consent (FR-3), deactivation (FR-3c). */
public interface AccountService {

	User get(Long userId);

	User updateProfile(Long userId, ProfileUpdateRequest request);

	User setGeoConsent(Long userId, boolean consent);

	/** D-58: show or hide the user's online status and last-seen time in chats. */
	User setShowOnlineStatus(Long userId, boolean show);

	/** FR-3c: password confirmation, no active bookings, then every session of the user is ended. */
	void deactivate(Long userId, String password, HttpServletRequest http);
}
