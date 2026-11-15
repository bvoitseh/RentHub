package com.bohakka.renthub.user.service;

import com.bohakka.renthub.common.error.BusinessRuleException;
import com.bohakka.renthub.user.domain.EmailToken;
import com.bohakka.renthub.user.domain.EmailTokenType;
import com.bohakka.renthub.user.domain.User;
import com.bohakka.renthub.user.repository.EmailTokenRepository;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Clock;
import java.util.Base64;
import java.util.HexFormat;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Issues and consumes single-use e-mail tokens (NFR-5): 256-bit random raw token, only its SHA-256 stored,
 * one usable token per user and type, consumed exactly once before expiry.
 */
@Service
@RequiredArgsConstructor
public class EmailTokenService {

	private static final int TOKEN_BYTES = 32;

	private final EmailTokenRepository tokens;
	private final Clock clock;
	private final SecureRandom random = new SecureRandom();

	/** Voids the user's earlier unused tokens of this type and returns a fresh raw token for the e-mail link. */
	@Transactional
	public String issue(User user, EmailTokenType type) {
		tokens.deleteByUserAndTypeAndUsedAtIsNull(user, type);
		byte[] bytes = new byte[TOKEN_BYTES];
		random.nextBytes(bytes);
		String raw = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
		tokens.save(new EmailToken(user, type, hash(raw), clock.instant()));
		return raw;
	}

	/** Marks the token used and returns it (with its user); rejects unknown, used or expired tokens. */
	@Transactional
	public EmailToken consume(String raw, EmailTokenType type) {
		EmailToken token = tokens.findByTokenHashAndType(hash(raw), type)
			.filter(t -> t.isUsable(clock.instant()))
			.orElseThrow(() -> new BusinessRuleException("token.invalid", "token.invalid"));
		token.markUsed(clock.instant());
		return token;
	}

	static String hash(String raw) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			return HexFormat.of().formatHex(digest.digest(raw.getBytes(StandardCharsets.US_ASCII)));
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException("SHA-256 not available", e);
		}
	}
}
