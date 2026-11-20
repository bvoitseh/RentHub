package com.bohakka.renthub.repository;

import com.bohakka.renthub.entity.AvailabilityBlock;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AvailabilityBlockRepository extends JpaRepository<AvailabilityBlock, Long> {

	/** The owner's calendar: blocks that have not ended before {@code today}, in date order. */
	List<AvailabilityBlock> findByEquipmentIdAndDateToGreaterThanEqualOrderByDateFromAsc(Long equipmentId,
			LocalDate today);

	/** Blocks overlapping {@code [from, to]} (FR-9 overlap rule). */
	@Query("""
		select b from AvailabilityBlock b
		where b.equipment.id = :equipmentId and b.dateFrom <= :to and b.dateTo >= :from
		order by b.dateFrom""")
	List<AvailabilityBlock> findOverlapping(@Param("equipmentId") Long equipmentId, @Param("from") LocalDate from,
			@Param("to") LocalDate to);

	void deleteByEquipmentId(Long equipmentId);
}
