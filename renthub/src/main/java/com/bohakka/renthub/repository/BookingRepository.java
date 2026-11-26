package com.bohakka.renthub.repository;

import com.bohakka.renthub.entity.Booking;
import com.bohakka.renthub.entity.BookingStatus;
import com.bohakka.renthub.entity.CancelledBy;
import jakarta.persistence.LockModeType;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookingRepository extends JpaRepository<Booking, Long> {

	/** Bookings in {@code statuses} overlapping {@code [from, to]}: what occupies units on those days (FR-9). */
	@Query("""
		select b from Booking b
		where b.equipment.id = :equipmentId and b.status in :statuses and b.dateFrom <= :to and b.dateTo >= :from""")
	List<Booking> findOverlapping(@Param("equipmentId") Long equipmentId,
			@Param("statuses") Collection<BookingStatus> statuses, @Param("from") LocalDate from,
			@Param("to") LocalDate to);

	/** FR-5: active bookings of a listing — NEW or CONFIRMED, not yet over. */
	@Query("""
		select b from Booking b
		where b.equipment.id = :equipmentId and b.status in :statuses and b.dateTo >= :today
		order by b.dateFrom""")
	List<Booking> findActiveForEquipment(@Param("equipmentId") Long equipmentId,
			@Param("statuses") Collection<BookingStatus> statuses, @Param("today") LocalDate today);

	/** FR-3c: active bookings where the user is the renter or the equipment owner. */
	@EntityGraph(attributePaths = "equipment")
	@Query("""
		select b from Booking b
		where (b.renter.id = :userId or b.equipment.owner.id = :userId)
		  and b.status in :statuses and b.dateTo >= :today
		order by b.dateFrom""")
	List<Booking> findActiveForUser(@Param("userId") Long userId,
			@Param("statuses") Collection<BookingStatus> statuses, @Param("today") LocalDate today);

	boolean existsByEquipmentId(Long equipmentId);

	/** FR-20: ids of NEW/CONFIRMED bookings starting after {@code today} where the user is renter or owner. */
	@Query("""
		select b.id from Booking b
		where (b.renter.id = :userId or b.equipment.owner.id = :userId)
		  and b.status in :statuses and b.dateFrom > :today
		order by b.id""")
	List<Long> findFutureIdsForUser(@Param("userId") Long userId,
			@Param("statuses") Collection<BookingStatus> statuses, @Param("today") LocalDate today);

	/** FR-20: ids of NEW/CONFIRMED bookings of a listing starting after {@code today}. */
	@Query("""
		select b.id from Booking b
		where b.equipment.id = :equipmentId and b.status in :statuses and b.dateFrom > :today
		order by b.id""")
	List<Long> findFutureIdsForEquipment(@Param("equipmentId") Long equipmentId,
			@Param("statuses") Collection<BookingStatus> statuses, @Param("today") LocalDate today);

	/** FR-13: the renter's own bookings, newest first. */
	@EntityGraph(attributePaths = {"equipment", "equipment.owner", "renter"})
	Page<Booking> findByRenterIdOrderByCreatedAtDesc(Long renterId, Pageable pageable);

	/** FR-12: requests for the owner's listings, optionally only in some statuses, newest first. */
	@EntityGraph(attributePaths = {"equipment", "equipment.owner", "renter"})
	@Query("""
		select b from Booking b
		where b.equipment.owner.id = :ownerId and b.status in :statuses
		order by b.createdAt desc""")
	Page<Booking> findIncoming(@Param("ownerId") Long ownerId, @Param("statuses") Collection<BookingStatus> statuses,
			Pageable pageable);

	/**
	 * The booking row locked for writing (only this row, no joins), so status changes of one booking — owner,
	 * renter, scheduler — are serialised and each sees the previous one's result (FR-14). Taken after the listing
	 * lock when both are needed.
	 */
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select b from Booking b where b.id = :id")
	Optional<Booking> lockById(@Param("id") Long id);

	@Query("""
		select new com.bohakka.renthub.repository.BookingParties(b.renter.id, b.equipment.owner.id)
		from Booking b where b.id = :id""")
	Optional<BookingParties> findPartiesById(@Param("id") Long id);

	@Query("select b.equipment.id from Booking b where b.id = :id")
	Optional<Long> findEquipmentIdById(@Param("id") Long id);

	/** One booking with everything its response shows. */
	@EntityGraph(attributePaths = {"equipment", "equipment.owner", "renter"})
	@Query("select b from Booking b where b.id = :id")
	Optional<Booking> findWithPartiesById(@Param("id") Long id);

	/** FR-13a: bookings of the owner's listings that the owner cancelled (admin cancellations are not counted). */
	@Query("select count(b) from Booking b where b.equipment.owner.id = :ownerId and b.cancelledBy = :by")
	long countCancelledBy(@Param("ownerId") Long ownerId, @Param("by") CancelledBy by);

	/** FR-15: confirmed rentals whose last day is before {@code today}, locked like {@link #lockById}. */
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	List<Booking> findByStatusAndDateToBefore(BookingStatus status, LocalDate today);

	/** FR-15b: NEW requests made at or before {@code createdBefore}, or whose first day has arrived; locked. */
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("""
		select b from Booking b
		where b.status = com.bohakka.renthub.entity.BookingStatus.NEW
		  and (b.createdAt <= :createdBefore or b.dateFrom <= :today)""")
	List<Booking> findExpired(@Param("createdBefore") Instant createdBefore, @Param("today") LocalDate today);
}
