package com.bohakka.renthub.repository;

import com.bohakka.renthub.entity.Equipment;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface EquipmentRepository extends JpaRepository<Equipment, Long>, JpaSpecificationExecutor<Equipment> {

	/** Catalogue page: owner and category are fetched with the listings (no N+1 on the cards). */
	@Override
	@EntityGraph(attributePaths = {"owner", "category"})
	Page<Equipment> findAll(Specification<Equipment> spec, Pageable pageable);

	/** Equipment card and editing: everything the card shows in one query. */
	@EntityGraph(attributePaths = {"owner", "category", "photos"})
	Optional<Equipment> findWithDetailsById(Long id);

	/** The owner's dashboard: own listings in every status, newest first. */
	@EntityGraph(attributePaths = {"category"})
	List<Equipment> findByOwnerIdOrderByCreatedAtDesc(Long ownerId);
}
