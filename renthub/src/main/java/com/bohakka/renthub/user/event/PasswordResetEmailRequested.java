package com.bohakka.renthub.user.event;

/** Published (inside the transaction) when a password-reset link must be e-mailed; {@code rawToken} is never stored. */
public record PasswordResetEmailRequested(String email, String name, String rawToken) {
}
