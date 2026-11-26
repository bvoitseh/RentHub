package com.bohakka.renthub.service;

import com.bohakka.renthub.entity.Booking;
import com.bohakka.renthub.entity.BookingStatus;
import com.bohakka.renthub.event.BookingNoticeType;
import com.bohakka.renthub.properties.BookingProperties;
import com.bohakka.renthub.repository.BookingRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * The scheduler actor (spec §4): completes finished rentals daily (FR-15) and expires unanswered requests hourly
 * (FR-15b). Transitions go through {@link Booking}'s methods, so the FR-14 rules apply to the jobs as well.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BookingLifecycleJobs {

	private final BookingRepository bookings;
	private final BookingProperties properties;
	private final BookingNotifier notifier;
	private final Clock clock;

	/** FR-15: CONFIRMED bookings whose last day is before today become COMPLETED. */
	@Scheduled(cron = "${app.booking.complete-cron}", zone = "${app.time-zone}")
	@Transactional
	public void completeFinished() {
		Instant now = clock.instant();
		List<Booking> finished = bookings.findByStatusAndDateToBefore(BookingStatus.CONFIRMED, LocalDate.now(clock));
		finished.forEach(b -> b.complete(now));
		if (!finished.isEmpty()) {
			log.info("Completed {} finished booking(s)", finished.size());
		}
	}

	/** FR-15b: NEW requests older than the TTL, or whose first day has arrived, become REJECTED (expired); the renter
	 * is e-mailed after the commit. */
	@Scheduled(cron = "${app.booking.expire-cron}", zone = "${app.time-zone}")
	@Transactional
	public void expireUnanswered() {
		Instant now = clock.instant();
		List<Booking> expired = bookings.findExpired(now.minus(properties.requestTtl()), LocalDate.now(clock));
		expired.forEach(b -> {
			b.expire(now);
			notifier.notify(b, BookingNoticeType.EXPIRED);
		});
		if (!expired.isEmpty()) {
			log.info("Expired {} unanswered booking request(s)", expired.size());
		}
	}
}
