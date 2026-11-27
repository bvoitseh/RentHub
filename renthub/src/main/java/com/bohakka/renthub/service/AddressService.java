package com.bohakka.renthub.service;

import com.bohakka.renthub.dto.RegionResponse;
import com.bohakka.renthub.dto.SettlementResponse;
import com.bohakka.renthub.entity.Settlement;
import java.util.List;

/** Regions and settlements of Ukraine from our own reference data (D-48); never calls an external service. */
public interface AddressService {

	List<RegionResponse> regions();

	/** Prefix search, case- and apostrophe-insensitive, at most 20 results. */
	List<SettlementResponse> search(Long regionId, String query);

	/** An active settlement chosen in a form; 422 when it does not exist or is no longer active. */
	Settlement settlement(Long id);
}
