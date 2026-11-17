package com.bohakka.renthub.service;

import com.bohakka.renthub.entity.EmailToken;
import com.bohakka.renthub.entity.EmailTokenType;
import com.bohakka.renthub.entity.User;

/**
 * Issues and consumes single-use e-mail tokens (NFR-5): 256-bit random raw token, only its SHA-256 stored,
 * one usable token per user and type, consumed exactly once before expiry.
 */
public interface EmailTokenService {

	/** Voids the user's earlier unused tokens of this type and returns a fresh raw token for the e-mail link. */
	String issue(User user, EmailTokenType type);

	/** Marks the token used and returns it (with its user); rejects unknown, used or expired tokens. */
	EmailToken consume(String raw, EmailTokenType type);
}
