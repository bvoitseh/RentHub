package com.bohakka.renthub.service.impl;

import com.bohakka.renthub.entity.EmailToken;
import com.bohakka.renthub.entity.EmailTokenType;
import com.bohakka.renthub.entity.User;
import com.bohakka.renthub.exception.BusinessRuleException;
import com.bohakka.renthub.repository.EmailTokenRepository;
import com.bohakka.renthub.service.EmailTokenService;
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

/** Default implementation of {@link EmailTokenService}. */
@Service
@RequiredArgsConstructor
public class EmailTokenServiceImpl implements EmailTokenService {

	private static final int TOKEN_BYTES = 32;

	private final EmailTokenRepository tokens;
	private final Clock clock;
	private final SecureRandom random = new SecureRandom();

	@Override
	@Transactional
	public String issue(User user, EmailTokenType type) {
		tokens.deleteByUserAndTypeAndUsedAtIsNull(user, type);
		byte[] bytes = new byte[TOKEN_BYTES];
		random.nextBytes(bytes);
		String raw = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
		tokens.save(new EmailToken(user, type, hash(raw), clock.instant()));
		return raw;
	}

	@Override
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
