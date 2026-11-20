package com.bohakka.renthub.service;

import com.bohakka.renthub.dto.CategoryResponse;
import java.util.List;

/** Equipment categories for filters and the listing form. */
public interface CategoryService {

	List<CategoryResponse> list();
}
