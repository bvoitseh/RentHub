package com.bohakka.renthub.repository;

import com.bohakka.renthub.entity.CommissionRate;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommissionRateRepository extends JpaRepository<CommissionRate, Long> {

	/** The version in force at {@code moment}: the newest one that has already started (D-37). */
	Optional<CommissionRate> findFirstByValidFromLessThanEqualOrderByValidFromDesc(Instant moment);

	/** FR-22: the history shown to the admin, newest first. */
	List<CommissionRate> findTop20ByOrderByValidFromDesc();
}
