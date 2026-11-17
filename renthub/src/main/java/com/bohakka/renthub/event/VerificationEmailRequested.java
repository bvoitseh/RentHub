package com.bohakka.renthub.event;

/** Published (inside the transaction) when a verification link must be e-mailed; {@code rawToken} is never stored. */
public record VerificationEmailRequested(String email, String name, String rawToken) {
}
