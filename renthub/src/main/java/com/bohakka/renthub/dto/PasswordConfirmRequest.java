package com.bohakka.renthub.dto;

import jakarta.validation.constraints.NotBlank;

/** FR-3c: deactivation requires the current password. */
public record PasswordConfirmRequest(@NotBlank String password) {
}
