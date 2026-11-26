package com.bohakka.renthub.repository;

import com.bohakka.renthub.entity.Category;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CategoryRepository extends JpaRepository<Category, Long> {

	List<Category> findAllByOrderByNameAsc();

	/** FR-22: names are unique regardless of case. */
	boolean existsByNameIgnoreCase(String name);

	boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);

	/** FR-22: the category row locked while it is changed or deleted. */
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select c from Category c where c.id = :id")
	Optional<Category> lockById(@Param("id") Long id);
}
