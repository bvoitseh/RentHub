package com.bohakka.renthub.service.impl;

import com.bohakka.renthub.dto.RegionResponse;
import com.bohakka.renthub.dto.SettlementResponse;
import com.bohakka.renthub.entity.Region;
import com.bohakka.renthub.entity.Settlement;
import com.bohakka.renthub.exception.BusinessRuleException;
import com.bohakka.renthub.repository.RegionRepository;
import com.bohakka.renthub.repository.SettlementRepository;
import com.bohakka.renthub.service.AddressService;
import java.text.Collator;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** {@link AddressService} over the regions / settlements tables (D-48). */
@Service
@RequiredArgsConstructor
public class AddressServiceImpl implements AddressService {

	public static final int MIN_QUERY = 2;
	private static final int LIMIT = 20;
	/** The database orders by code point (І before А); lists are shown in Ukrainian alphabetical order. */
	private static final Collator UKRAINIAN = Collator.getInstance(Locale.forLanguageTag("uk"));

	private final RegionRepository regions;
	private final SettlementRepository settlements;

	@Override
	@Transactional(readOnly = true)
	public List<RegionResponse> regions() {
		return regions.findByActiveTrueOrderByNameAsc().stream()
			.sorted(Comparator.comparing(Region::getName, UKRAINIAN))
			.map(RegionResponse::from).toList();
	}

	@Override
	@Transactional(readOnly = true)
	public List<SettlementResponse> search(Long regionId, String query) {
		String prefix = Settlement.searchKey(query == null ? "" : query);
		if (prefix.length() < MIN_QUERY) {
			return List.of();
		}
		String escaped = prefix.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
		return settlements.search(regionId, escaped, PageRequest.of(0, LIMIT)).stream()
			.sorted(Comparator.comparingInt(AddressServiceImpl::statusRank)
				.thenComparing(Settlement::getName, UKRAINIAN).thenComparing(Settlement::getLabel, UKRAINIAN))
			.map(SettlementResponse::from).toList();
	}

	private static int statusRank(Settlement s) {
		return s.getStatus() == null ? 3 : s.getStatus().ordinal();
	}

	@Override
	@Transactional(readOnly = true)
	public Settlement settlement(Long id) {
		return settlements.findByIdAndActiveTrue(id)
			.orElseThrow(() -> new BusinessRuleException("settlement.invalid", "settlement.invalid"));
	}
}
