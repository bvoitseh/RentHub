package com.bohakka.renthub.entity;

import com.bohakka.renthub.exception.BusinessRuleException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Equipment category with its rental season in months 1-12; the season may wrap over New Year (e.g. 11 to 3). */
@Entity
@Table(name = "categories")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Category {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true, length = 100)
	private String name;

	@Column(name = "season_from", nullable = false)
	private int seasonFrom;

	@Column(name = "season_to", nullable = false)
	private int seasonTo;

	public Category(String name, int seasonFrom, int seasonTo) {
		change(name, seasonFrom, seasonTo);
	}

	/** FR-22: name and season (months 1-12; may wrap over New Year). */
	public void change(String name, int seasonFrom, int seasonTo) {
		if (seasonFrom < 1 || seasonFrom > 12 || seasonTo < 1 || seasonTo > 12) {
			throw new BusinessRuleException("category.season_invalid", "category.season_invalid");
		}
		this.name = name.trim();
		this.seasonFrom = seasonFrom;
		this.seasonTo = seasonTo;
	}

	/** FR-17: whether {@code month} (1-12) falls in the season, also when it wraps over New Year (11 -> 2). */
	public boolean isInSeason(int month) {
		return seasonFrom <= seasonTo ? month >= seasonFrom && month <= seasonTo : month >= seasonFrom || month <= seasonTo;
	}
}
