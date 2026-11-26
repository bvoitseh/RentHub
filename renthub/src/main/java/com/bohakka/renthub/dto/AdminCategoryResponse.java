package com.bohakka.renthub.dto;

/** FR-22: a category in the admin list with the number of its listings in any status. */
public record AdminCategoryResponse(Long id, String name, int seasonFrom, int seasonTo, long listingCount) {
}
