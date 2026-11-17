package com.bohakka.renthub.service.impl;

import com.bohakka.renthub.entity.User;
import com.bohakka.renthub.exception.BusinessRuleException;
import com.bohakka.renthub.repository.UserRepository;
import com.bohakka.renthub.service.EmailTokenService;
import com.bohakka.renthub.service.EmailTokenType;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;

/**
 * Redis-backed {@link EmailTokenService}. {@code email-token:{TYPE}:{sha256}} holds the user id and
 * {@code email-token:{TYPE}:user:{id}} the user's current hash; both expire after {@link EmailTokenType#validity()}.
 * Expiry is Redis's TTL; single use is guaranteed by the atomic GETDEL on consumption.
 */
@Service
@RequiredArgsConstructor
public class EmailTokenServiceImpl implements EmailTokenService {

	private static final int TOKEN_BYTES = 32;

	/** Atomically voids the user's previous token of this type and stores the new one, both with the same TTL. */
	private static final RedisScript<Long> STORE = RedisScript.of("""
		local previous = redis.call('GET', KEYS[2])
		if previous then
			redis.call('DEL', ARGV[3] .. previous)
		end
		redis.call('SET', KEYS[1], ARGV[1], 'PX', ARGV[2])
		redis.call('SET', KEYS[2], ARGV[4], 'PX', ARGV[2])
		return 1
		""", Long.class);

	private final StringRedisTemplate redis;
	private final UserRepository users;
	private final SecureRandom random = new SecureRandom();

	@Override
	public String issue(User user, EmailTokenType type) {
		byte[] bytes = new byte[TOKEN_BYTES];
		random.nextBytes(bytes);
		String raw = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
		String hash = hash(raw);
		redis.execute(STORE, List.of(tokenKey(type, hash), userKey(type, user.getId())),
			String.valueOf(user.getId()), String.valueOf(type.validity().toMillis()), tokenKey(type, ""), hash);
		return raw;
	}

	@Override
	public User consume(String raw, EmailTokenType type) {
		String hash = hash(raw);
		String userId = redis.opsForValue().getAndDelete(tokenKey(type, hash));
		if (userId == null) {
			throw invalid();
		}
		redis.delete(userKey(type, Long.valueOf(userId)));
		return users.findById(Long.valueOf(userId)).orElseThrow(EmailTokenServiceImpl::invalid);
	}

	private static String tokenKey(EmailTokenType type, String hash) {
		return "email-token:" + type + ":" + hash;
	}

	private static String userKey(EmailTokenType type, Long userId) {
		return "email-token:" + type + ":user:" + userId;
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
