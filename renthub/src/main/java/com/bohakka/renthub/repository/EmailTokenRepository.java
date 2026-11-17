package com.bohakka.renthub.repository;

import com.bohakka.renthub.entity.EmailToken;
import com.bohakka.renthub.entity.EmailTokenType;
import java.util.List;
import org.springframework.data.repository.CrudRepository;

/** Redis repository for e-mail link tokens; the id is the token's SHA-256. */
public interface EmailTokenRepository extends CrudRepository<EmailToken, String> {

	/** Issuing a new token voids the user's earlier unused tokens of the same type. */
	List<EmailToken> findByUserIdAndType(Long userId, EmailTokenType type);
}
