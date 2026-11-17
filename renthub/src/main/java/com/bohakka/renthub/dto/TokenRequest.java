package com.bohakka.renthub.dto;

import jakarta.validation.constraints.NotBlank;

/** The raw token from an e-mail link (verify e-mail). */
public record TokenRequest(@NotBlank String token) {
}
