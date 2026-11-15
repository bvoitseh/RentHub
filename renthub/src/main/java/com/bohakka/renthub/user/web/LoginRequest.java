package com.bohakka.renthub.user.web;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/** FR-2 login and FR-3c restore both take e-mail + password. */
public record LoginRequest(@NotBlank @Email String email, @NotBlank String password) {
}
