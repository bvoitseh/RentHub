package com.bohakka.renthub.service;

import com.bohakka.renthub.entity.EmailTokenType;
import com.bohakka.renthub.entity.User;

/**
 * Issues and consumes single-use e-mail link tokens (NFR-5): 256-bit random raw token, only its SHA-256 stored,
 * one usable token per user and type, consumed once before it expires.
 */
public interface EmailTokenService {

	/** Voids the user's earlier unused token of this type and returns a fresh raw token for the e-mail link. */
	String issue(User user, EmailTokenType type);

	/** Consumes the token and returns its user; rejects unknown, used or expired tokens. */
	User consume(String raw, EmailTokenType type);
}
