package com.bohakka.renthub.user.repository;

import com.bohakka.renthub.user.domain.Role;
import com.bohakka.renthub.user.domain.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

	/** Expects a normalised (lower-case, trimmed) e-mail, see {@link User#normalizeEmail}. */
	Optional<User> findByEmail(String email);

	boolean existsByEmail(String email);

	boolean existsByRole(Role role);
}
