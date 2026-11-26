package com.bohakka.renthub.repository;

import com.bohakka.renthub.entity.Review;
import jakarta.persistence.LockModeType;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReviewRepository extends JpaRepository<Review, Long> {

	/** FR-23 E3: average rating and number of reviews published in [from, to): one row [avg, count]. */
	@Query("select avg(r.rating), count(r) from Review r where r.publishedAt >= :from and r.publishedAt < :to")
	List<Object[]> ratingPublishedBetween(@Param("from") java.time.Instant from, @Param("to") java.time.Instant to);

	/** Both sides' reviews of one booking (0–2). */
	@EntityGraph(attributePaths = {"author", "target"})
	List<Review> findByBookingId(Long bookingId);

	/** FR-19: a user's published reviews, newest first, with what the card shows. */
	@EntityGraph(attributePaths = {"author", "target", "booking", "booking.equipment"})
	Page<Review> findByTargetIdAndPublishedAtIsNotNullOrderByPublishedAtDesc(Long targetId, Pageable pageable);

	/** FR-18: unpublished reviews of bookings completed at or before {@code completedBefore} (window over). */
	@Query("""
		select r from Review r
		where r.publishedAt is null and r.booking.completedAt <= :completedBefore
		order by r.id""")
	List<Review> findUnpublishedCompletedBefore(@Param("completedBefore") Instant completedBefore);

	/** The review row locked for writing, so a reply cannot be stored twice (FR-18: one reply). */
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select r from Review r where r.id = :id")
	Optional<Review> lockById(@Param("id") Long id);
}
