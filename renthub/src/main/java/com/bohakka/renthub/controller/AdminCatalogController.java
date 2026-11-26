package com.bohakka.renthub.controller;

import com.bohakka.renthub.dto.AdminCategoryResponse;
import com.bohakka.renthub.dto.CategoryRequest;
import com.bohakka.renthub.dto.CommissionRequest;
import com.bohakka.renthub.dto.CommissionResponse;
import com.bohakka.renthub.dto.StatisticsResponse;
import com.bohakka.renthub.service.CategoryAdminService;
import com.bohakka.renthub.service.CommissionService;
import com.bohakka.renthub.service.StatisticsService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** /api/v1/admin — categories, the commission rate (FR-22) and statistics (FR-23), ADMIN only (NFR-11). */
@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
class AdminCatalogController {

	private final CategoryAdminService categories;
	private final CommissionService commission;
	private final StatisticsService statistics;

	@GetMapping("/categories")
	List<AdminCategoryResponse> categories() {
		return categories.list();
	}

	@PostMapping("/categories")
	@ResponseStatus(HttpStatus.CREATED)
	AdminCategoryResponse createCategory(@Valid @RequestBody CategoryRequest request) {
		return categories.create(request);
	}

	@PutMapping("/categories/{id:\\d+}")
	AdminCategoryResponse updateCategory(@PathVariable Long id, @Valid @RequestBody CategoryRequest request) {
		return categories.update(id, request);
	}

	@DeleteMapping("/categories/{id:\\d+}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	void deleteCategory(@PathVariable Long id, @RequestParam(required = false) Long targetId) {
		categories.delete(id, targetId);
	}

	@GetMapping("/settings")
	CommissionResponse settings() {
		return commission.current();
	}

	@PutMapping("/settings")
	CommissionResponse changeCommission(@Valid @RequestBody CommissionRequest request) {
		return commission.change(request.rate());
	}

	@GetMapping("/statistics")
	StatisticsResponse statistics(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
		return statistics.forPeriod(from, to);
	}
}
