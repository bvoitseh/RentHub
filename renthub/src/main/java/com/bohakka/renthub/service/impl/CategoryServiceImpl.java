package com.bohakka.renthub.service.impl;

import com.bohakka.renthub.dto.CategoryResponse;
import com.bohakka.renthub.repository.CategoryRepository;
import com.bohakka.renthub.service.CategoryService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

	private final CategoryRepository categories;

	@Override
	@Transactional(readOnly = true)
	public List<CategoryResponse> list() {
		return categories.findAllByOrderByNameAsc().stream().map(CategoryResponse::from).toList();
	}
}
