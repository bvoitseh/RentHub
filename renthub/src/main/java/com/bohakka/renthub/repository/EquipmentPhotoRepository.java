package com.bohakka.renthub.repository;

import com.bohakka.renthub.entity.EquipmentPhoto;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EquipmentPhotoRepository extends JpaRepository<EquipmentPhoto, Long> {

	/** Cover photos (position 0) of a page of listings, loaded in one query. */
	@Query("select p from EquipmentPhoto p join fetch p.equipment e where e.id in :equipmentIds and p.position = 0")
	List<EquipmentPhoto> findCovers(@Param("equipmentIds") Collection<Long> equipmentIds);
}
