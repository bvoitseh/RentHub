package com.bohakka.renthub.user.web;

import com.bohakka.renthub.user.domain.OwnerType;
import com.bohakka.renthub.user.domain.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** FR-1 / FR-1a. Owner-type fields are required only when {@code role == OWNER} (checked in the domain). */
public record RegisterRequest(
		@NotBlank @Size(max = 100) String name,
		@NotBlank @Email @Size(max = 255) String email,
		@NotBlank @Pattern(regexp = Validation.PHONE, message = "{validation.phone}") String phone,
		@NotBlank @Size(min = Validation.PASSWORD_MIN, max = 72, message = "{validation.password}") String password,
		@NotBlank @Size(max = 100) String city,
		@NotNull Role role,
		OwnerType ownerType,
		@Size(max = 200) String companyName,
		@Pattern(regexp = Validation.EDRPOU, message = "{validation.edrpou}") String edrpou) {
}
