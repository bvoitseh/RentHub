package com.bohakka.renthub.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** FR-3a: the raw token from the reset link plus the new password. */
public record ResetPasswordRequest(
		@NotBlank String token,
		@NotBlank @Size(min = Validation.PASSWORD_MIN, max = 72, message = "{validation.password}") String password) {
}
