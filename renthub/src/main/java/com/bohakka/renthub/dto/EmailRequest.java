package com.bohakka.renthub.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/** Forgot-password request (FR-3a); the response never reveals whether the e-mail exists. */
public record EmailRequest(@NotBlank @Email String email) {
}
