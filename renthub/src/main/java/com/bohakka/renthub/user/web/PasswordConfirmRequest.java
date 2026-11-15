package com.bohakka.renthub.user.web;

import jakarta.validation.constraints.NotBlank;

/** FR-3c: deactivation requires the current password. */
public record PasswordConfirmRequest(@NotBlank String password) {
}
