package com.bohakka.renthub.event;

/** Published (inside the transaction) when a password-reset link must be e-mailed; {@code rawToken} is never stored. */
public record PasswordResetEmailRequested(String email, String name, String rawToken) {
}
