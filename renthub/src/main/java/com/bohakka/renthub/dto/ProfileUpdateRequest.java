package com.bohakka.renthub.dto;

import com.bohakka.renthub.entity.OwnerType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Profile edit (name, phone, city; owner type for owners — FR-1a "editable in the profile"). */
public record ProfileUpdateRequest(
		@NotBlank @Size(max = 100) String name,
		@NotBlank @Pattern(regexp = Validation.PHONE, message = "{validation.phone}") String phone,
		@NotBlank @Size(max = 100) String city,
		OwnerType ownerType,
		@Size(max = 200) String companyName,
		@Pattern(regexp = Validation.EDRPOU, message = "{validation.edrpou}") String edrpou) {
}
