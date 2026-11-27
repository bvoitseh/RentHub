package com.bohakka.renthub.dto;

import com.bohakka.renthub.entity.OwnerType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Profile edit (name, phone, settlement — region + settlement only, NFR-5; owner type for owners — FR-1a "editable in the profile"). */
public record ProfileUpdateRequest(
		@NotBlank @Size(max = 100) String name,
		@NotBlank @Pattern(regexp = Validation.PHONE, message = "{validation.phone}") String phone,
		@NotNull Long settlementId,
		OwnerType ownerType,
		@Size(max = 200) String companyName,
		@Pattern(regexp = Validation.EDRPOU, message = "{validation.edrpou}") String edrpou) {
}
