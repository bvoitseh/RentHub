package com.bohakka.renthub.service;

import com.bohakka.renthub.dto.CatalogQuery;
import com.bohakka.renthub.dto.EquipmentDetailsResponse;
import com.bohakka.renthub.dto.EquipmentSummaryResponse;
import com.bohakka.renthub.dto.PageResponse;

/** The public catalogue (FR-6, FR-7): search with filters, sorting and paging, and the equipment card. */
public interface CatalogService {

	PageResponse<EquipmentSummaryResponse> search(CatalogQuery query);

	/** The card; a listing outside the catalogue (archived, blocked, no photo yet) is visible to its owner only. */
	EquipmentDetailsResponse card(Long equipmentId, Long viewerId);
}
