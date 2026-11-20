package com.bohakka.renthub.controller;

import com.bohakka.renthub.dto.CategoryResponse;
import com.bohakka.renthub.service.CategoryService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** /api/v1/categories — public list for filters and the listing form. */
@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
class CategoryController {

	private final CategoryService categories;

	@GetMapping
	List<CategoryResponse> list() {
		return categories.list();
	}
}
