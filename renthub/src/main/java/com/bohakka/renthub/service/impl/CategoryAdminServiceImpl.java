package com.bohakka.renthub.service.impl;

import com.bohakka.renthub.dto.AdminCategoryResponse;
import com.bohakka.renthub.dto.CategoryRequest;
import com.bohakka.renthub.entity.Category;
import com.bohakka.renthub.entity.Equipment;
import com.bohakka.renthub.exception.BusinessRuleException;
import com.bohakka.renthub.exception.ConflictException;
import com.bohakka.renthub.exception.NotFoundException;
import com.bohakka.renthub.repository.CategoryRepository;
import com.bohakka.renthub.repository.EquipmentRepository;
import com.bohakka.renthub.service.CategoryAdminService;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * {@link CategoryAdminService} (FR-22, D-46). Names are unique regardless of case. Deleting locks the category, then
 * its listings, so no listing can be added to it meanwhile; listings move to the target in the same transaction.
 */
@Service
@RequiredArgsConstructor
public class CategoryAdminServiceImpl implements CategoryAdminService {

	private final CategoryRepository categories;
	private final EquipmentRepository equipment;
	private final Clock clock;

	@Override
	@Transactional(readOnly = true)
	public List<AdminCategoryResponse> list() {
		Map<Long, Long> counts = equipment.countByCategory().stream()
			.collect(Collectors.toMap(r -> (Long) r[0], r -> (Long) r[1]));
		return categories.findAllByOrderByNameAsc().stream()
			.map(c -> toResponse(c, counts.getOrDefault(c.getId(), 0L)))
			.toList();
	}

	@Override
	@Transactional
	public AdminCategoryResponse create(CategoryRequest request) {
		if (categories.existsByNameIgnoreCase(request.name().trim())) {
			throw nameTaken();
		}
		Category category = categories.save(new Category(request.name(), request.seasonFrom(), request.seasonTo()));
		return toResponse(category, 0);
	}

	@Override
	@Transactional
	public AdminCategoryResponse update(Long id, CategoryRequest request) {
		Category category = lock(id);
		if (categories.existsByNameIgnoreCaseAndIdNot(request.name().trim(), id)) {
			throw nameTaken();
		}
		category.change(request.name(), request.seasonFrom(), request.seasonTo());
		long count = equipment.countByCategory().stream().filter(r -> id.equals(r[0])).mapToLong(r -> (Long) r[1]).sum();
		return toResponse(category, count);
	}

	@Override
	@Transactional
	public void delete(Long id, Long targetId) {
		Category category = lock(id);
		List<Equipment> listings = equipment.lockByCategoryId(id);
		if (!listings.isEmpty()) {
			if (targetId == null) {
				throw new ConflictException("category.has_listings", "category.has_listings", listings.size());
			}
			if (targetId.equals(id)) {
				throw new BusinessRuleException("category.target_invalid", "category.target_invalid");
			}
			Category target = categories.findById(targetId)
				.orElseThrow(() -> new BusinessRuleException("category.target_invalid", "category.target_invalid"));
			Instant now = clock.instant();
			listings.forEach(listing -> listing.moveTo(target, now));
			equipment.flush();
		}
		categories.delete(category);
	}

	private Category lock(Long id) {
		return categories.lockById(id).orElseThrow(() -> new NotFoundException("category.not_found", "category.not_found"));
	}

	private static ConflictException nameTaken() {
		return new ConflictException("category.name_taken", "category.name_taken");
	}

	private static AdminCategoryResponse toResponse(Category c, long listingCount) {
		return new AdminCategoryResponse(c.getId(), c.getName(), c.getSeasonFrom(), c.getSeasonTo(), listingCount);
	}
}
