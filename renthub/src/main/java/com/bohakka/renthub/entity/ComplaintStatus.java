package com.bohakka.renthub.entity;

/** FR-21, FR-22: a complaint is NEW until the admin resolves it or rejects it as unfounded. */
public enum ComplaintStatus {
	NEW,
	RESOLVED,
	REJECTED
}
