package com.bohakka.renthub.repository;

import com.bohakka.renthub.entity.Complaint;
import com.bohakka.renthub.entity.ComplaintStatus;
import java.util.Collection;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ComplaintRepository extends JpaRepository<Complaint, Long> {

	/** FR-21: one open complaint per author per booking or listing. */
	boolean existsByAuthorIdAndBookingIdAndStatus(Long authorId, Long bookingId, ComplaintStatus status);

	boolean existsByAuthorIdAndEquipmentIdAndStatus(Long authorId, Long equipmentId, ComplaintStatus status);

	/** FR-21 counter: complaints against the user (incl. their listings) except those rejected as unfounded. */
	long countByAgainstUserIdAndStatusNot(Long againstUserId, ComplaintStatus status);

	/** The same counter for a page of users at once: rows of [userId, count]. */
	@Query("""
		select c.againstUser.id, count(c) from Complaint c
		where c.againstUser.id in :userIds and c.status <> com.bohakka.renthub.entity.ComplaintStatus.REJECTED
		group by c.againstUser.id""")
	List<Object[]> countCountableByUser(@Param("userIds") Collection<Long> userIds);

	/** FR-22: the admin's list, newest first, optionally only some statuses. */
	@EntityGraph(attributePaths = {"author", "againstUser", "booking", "equipment"})
	@Query("select c from Complaint c where c.status in :statuses order by c.createdAt desc")
	Page<Complaint> findByStatuses(@Param("statuses") Collection<ComplaintStatus> statuses, Pageable pageable);
}
