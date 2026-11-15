package com.bohakka.renthub.user.web;

import jakarta.validation.constraints.NotNull;

/** FR-3: give or withdraw consent to process geolocation. */
public record GeoConsentRequest(@NotNull Boolean consent) {
}
