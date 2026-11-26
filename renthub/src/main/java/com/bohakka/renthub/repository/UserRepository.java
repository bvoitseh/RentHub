package com.bohakka.renthub.repository;

import com.bohakka.renthub.entity.Role;
import com.bohakka.renthub.entity.User;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Long> {

	/** Expects a normalised (lower-case, trimmed) e-mail, see {@link User#normalizeEmail}. */
	Optional<User> findByEmail(String email);

	boolean existsByEmail(String email);

	boolean existsByRole(Role role);

	/** FR-22: users (not the admin) whose name or e-mail contains {@code q} (lower case), newest first. */
	@Query("""
		select u from User u
		where u.role = com.bohakka.renthub.entity.Role.USER
		  and (:q = '' or lower(u.name) like concat('%', :q, '%') or lower(u.email) like concat('%', :q, '%'))
		order by u.createdAt desc, u.id desc""")
	Page<User> searchUsers(@Param("q") String q, Pageable pageable);

	/**
	 * The user row locked for writing until the transaction ends: serialises deactivation (FR-3c) with booking
	 * requests that involve the user. Lock order everywhere: listing, then users by ascending id, then booking.
	 */
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select u from User u where u.id = :id")
	Optional<User> lockById(@Param("id") Long id);
}
