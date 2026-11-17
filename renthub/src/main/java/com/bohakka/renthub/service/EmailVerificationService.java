package com.bohakka.renthub.service;

/** FR-1: e-mail verification by single-use link, and "resend verification" for the logged-in user. */
public interface EmailVerificationService {

	void verify(String rawToken);

	void resend(Long userId);
}
