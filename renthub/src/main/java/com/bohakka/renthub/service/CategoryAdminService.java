package com.bohakka.renthub.service;

import com.bohakka.renthub.dto.AdminCategoryResponse;
import com.bohakka.renthub.dto.CategoryRequest;
import java.util.List;

/** FR-22: the admin manages categories; callers are ADMIN only (NFR-11). */
public interface CategoryAdminService {

	List<AdminCategoryResponse> list();

	AdminCategoryResponse create(CategoryRequest request);

	AdminCategoryResponse update(Long id, CategoryRequest request);

	/** Deletes the category; one with listings needs {@code targetId}, where its listings move first (D-46). */
	void delete(Long id, Long targetId);
}
