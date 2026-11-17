package com.bohakka.renthub.service.impl;

import com.bohakka.renthub.entity.EmailToken;
import com.bohakka.renthub.entity.EmailTokenType;
import com.bohakka.renthub.entity.User;
import com.bohakka.renthub.exception.BusinessRuleException;
import com.bohakka.renthub.repository.EmailTokenRepository;
import com.bohakka.renthub.repository.UserRepository;
import com.bohakka.renthub.service.EmailTokenService;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/** {@link EmailTokenService} over the Redis {@link EmailTokenRepository}; expiry is the entry's Redis TTL. */
@Service
@RequiredArgsConstructor
public class EmailTokenServiceImpl implements EmailTokenService {

	private static final int TOKEN_BYTES = 32;

	private final EmailTokenRepository tokens;
	private final UserRepository users;
	private final SecureRandom random = new SecureRandom();

	@Override
	public String issue(User user, EmailTokenType type) {
		tokens.deleteAll(tokens.findByUserIdAndType(user.getId(), type));
		byte[] bytes = new byte[TOKEN_BYTES];
		random.nextBytes(bytes);
		String raw = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
		tokens.save(new EmailToken(hash(raw), user.getId(), type));
		return raw;
	}

	/**
	 * Find-then-delete is not atomic: two simultaneous requests with the same link could both pass. Both would come
	 * from the link holder and do the same thing (verify, or set a password), so this is accepted (DECISIONS.md D-28).
	 */
	@Override
	public User consume(String raw, EmailTokenType type) {
		EmailToken token = tokens.findById(hash(raw))
			.filter(t -> t.getType() == type)
			.orElseThrow(EmailTokenServiceImpl::invalid);
		tokens.delete(token);
		return users.findById(token.getUserId()).orElseThrow(EmailTokenServiceImpl::invalid);
	}

	private static BusinessRuleException invalid() {
		return new BusinessRuleException("token.invalid", "token.invalid");
	}

	private static String hash(String raw) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			return HexFormat.of().formatHex(digest.digest(raw.getBytes(StandardCharsets.US_ASCII)));
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException("SHA-256 not available", e);
		}
	}
}
