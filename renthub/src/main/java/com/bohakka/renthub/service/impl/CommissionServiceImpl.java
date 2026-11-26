package com.bohakka.renthub.service.impl;

import com.bohakka.renthub.dto.CommissionResponse;
import com.bohakka.renthub.entity.CommissionRate;
import com.bohakka.renthub.exception.NotFoundException;
import com.bohakka.renthub.repository.CommissionRateRepository;
import com.bohakka.renthub.service.CommissionService;
import java.math.BigDecimal;
import java.time.Clock;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** {@link CommissionService}: every change is a new {@link CommissionRate} row valid from now (D-37, D-46). */
@Service
@RequiredArgsConstructor
public class CommissionServiceImpl implements CommissionService {

	private final CommissionRateRepository rates;
	private final Clock clock;

	@Override
	@Transactional(readOnly = true)
	public CommissionResponse current() {
		CommissionRate now = rates.findFirstByValidFromLessThanEqualOrderByValidFromDesc(clock.instant())
			.orElseThrow(() -> new NotFoundException("commission.not_configured", "commission.not_configured"));
		return new CommissionResponse(now.getRate(), now.getValidFrom(), rates.findTop20ByOrderByValidFromDesc().stream()
			.map(r -> new CommissionResponse.Version(r.getRate(), r.getValidFrom()))
			.toList());
	}

	@Override
	@Transactional
	public CommissionResponse change(BigDecimal rate) {
		rates.saveAndFlush(new CommissionRate(rate, clock.instant()));
		return current();
	}
}
