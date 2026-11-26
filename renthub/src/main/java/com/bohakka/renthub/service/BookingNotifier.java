package com.bohakka.renthub.service;

import com.bohakka.renthub.entity.Booking;
import com.bohakka.renthub.event.BookingNoticeType;

/** Queues the FR-15a e-mail for a booking change; it is sent asynchronously after the current transaction commits. */
public interface BookingNotifier {

	/** Call inside the transaction that made the change; nothing is sent if it rolls back. */
	void notify(Booking booking, BookingNoticeType type);
}
