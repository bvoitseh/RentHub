package com.bohakka.renthub.dto;

import com.bohakka.renthub.entity.Category;

public record CategoryResponse(Long id, String name, int seasonFrom, int seasonTo) {

	public static CategoryResponse from(Category category) {
		return new CategoryResponse(category.getId(), category.getName(), category.getSeasonFrom(), category.getSeasonTo());
	}
}
