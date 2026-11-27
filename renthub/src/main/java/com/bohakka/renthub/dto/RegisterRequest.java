package com.bohakka.renthub.dto;

import com.bohakka.renthub.entity.OwnerType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** FR-1 / FR-1a. No role is chosen: every account can rent and rent out. Account type is optional (default PRIVATE). */
public record RegisterRequest(
		@NotBlank @Size(max = 100) String name,
		@NotBlank @Email @Size(max = 255) String email,
		@NotBlank @Pattern(regexp = Validation.PHONE, message = "{validation.phone}") String phone,
		@NotBlank @Size(min = Validation.PASSWORD_MIN, max = 72, message = "{validation.password}") String password,
		@NotNull Long settlementId,
		OwnerType ownerType,
		@Size(max = 200) String companyName,
		@Pattern(regexp = Validation.EDRPOU, message = "{validation.edrpou}") String edrpou) {
}
