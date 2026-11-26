package com.bohakka.renthub.repository;

/** The two sides of a booking by id, read without loading the users, so their rows can be locked first (D-40). */
public record BookingParties(Long renterId, Long ownerId) {
}
