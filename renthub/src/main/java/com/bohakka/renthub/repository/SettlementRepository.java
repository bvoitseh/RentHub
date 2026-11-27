package com.bohakka.renthub.repository;

import com.bohakka.renthub.entity.Settlement;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SettlementRepository extends JpaRepository<Settlement, Long> {

	/** Autocomplete (D-48): active settlements of the region whose search key starts with {@code prefix}; cities first. */
	@Query("""
		select s from Settlement s
		where s.region.id = :regionId and s.active = true and s.searchName like concat(:prefix, '%')
		order by case s.status when com.bohakka.renthub.entity.SettlementStatus.CITY then 0
		                       when com.bohakka.renthub.entity.SettlementStatus.TOWN then 1 else 2 end,
		         s.name, s.label""")
	List<Settlement> search(@Param("regionId") Long regionId, @Param("prefix") String prefix, Pageable pageable);

	Optional<Settlement> findByIdAndActiveTrue(Long id);
}
