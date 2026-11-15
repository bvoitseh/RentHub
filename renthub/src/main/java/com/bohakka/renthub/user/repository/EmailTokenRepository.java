package com.bohakka.renthub.user.repository;

import com.bohakka.renthub.user.domain.EmailToken;
import com.bohakka.renthub.user.domain.EmailTokenType;
import com.bohakka.renthub.user.domain.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmailTokenRepository extends JpaRepository<EmailToken, Long> {

	@EntityGraph(attributePaths = "user")
	Optional<EmailToken> findByTokenHashAndType(String tokenHash, EmailTokenType type);

	/** Issuing a new token voids the user's earlier unused tokens of the same type. */
	long deleteByUserAndTypeAndUsedAtIsNull(User user, EmailTokenType type);
}
