package com.bohakka.renthub.repository;

import com.bohakka.renthub.entity.Region;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RegionRepository extends JpaRepository<Region, Long> {

	List<Region> findByActiveTrueOrderByNameAsc();
}
