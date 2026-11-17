package com.bohakka.renthub.service;

/** FR-3a: password reset by a single-use 1-hour link; old sessions are invalidated after the reset. */
public interface PasswordResetService {

	/** Always completes silently: the caller never learns whether the e-mail exists (NFR-14). */
	void requestReset(String email);

	void reset(String rawToken, String newPassword);
}
