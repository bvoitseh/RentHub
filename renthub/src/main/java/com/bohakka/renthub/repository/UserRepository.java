package com.bohakka.renthub.repository;

import com.bohakka.renthub.entity.Role;
import com.bohakka.renthub.entity.User;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Long> {

	/** Expects a normalised (lower-case, trimmed) e-mail, see {@link User#normalizeEmail}. */
	Optional<User> findByEmail(String email);

	boolean existsByEmail(String email);

	boolean existsByRole(Role role);

	/**
	 * The user row locked for writing until the transaction ends: serialises deactivation (FR-3c) with booking
	 * requests that involve the user. Lock order everywhere: listing, then users by ascending id, then booking.
	 */
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select u from User u where u.id = :id")
	Optional<User> lockById(@Param("id") Long id);
}
