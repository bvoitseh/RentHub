package com.bohakka.renthub.service.impl;

import com.bohakka.renthub.entity.Booking;
import com.bohakka.renthub.entity.Review;
import com.bohakka.renthub.entity.User;
import com.bohakka.renthub.event.BookingNotice;
import com.bohakka.renthub.event.BookingNoticeType;
import com.bohakka.renthub.service.BookingNotifier;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * {@link BookingNotifier}: snapshots the booking (and review) into a {@link BookingNotice} event (Observer). A
 * deactivated recipient gets no e-mail (FR-3c).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BookingNotifierImpl implements BookingNotifier {

	private final ApplicationEventPublisher events;
	private final BookingDeadline deadline;

	@Override
	public void notify(Booking booking, BookingNoticeType type) {
		Instant answerBy = type == BookingNoticeType.REQUESTED ? deadline.respondBy(booking) : null;
		String reason = type == BookingNoticeType.CANCELLED_BY_OWNER ? booking.getCancelReason() : null;
		publish(booking, type, type.toOwner(), answerBy, reason, null, null);
	}

	@Override
	public void remindToReview(Booking booking, Instant reviewUntil) {
		publish(booking, BookingNoticeType.REVIEW_REMINDER, false, reviewUntil, null, null, null);
		publish(booking, BookingNoticeType.REVIEW_REMINDER, true, reviewUntil, null, null, null);
	}

	@Override
	public void reviewPublished(Review review) {
		Booking booking = review.getBooking();
		publish(booking, BookingNoticeType.REVIEW_PUBLISHED, booking.isOwner(review.getTarget().getId()), null, null,
			review.getRating(), review.getComment());
	}

	@Override
	public void reviewReplied(Review review) {
		Booking booking = review.getBooking();
		publish(booking, BookingNoticeType.REVIEW_REPLIED, booking.isOwner(review.getAuthor().getId()), null, null,
			review.getRating(), review.getReply());
	}

	private void publish(Booking booking, BookingNoticeType type, boolean toOwner, Instant deadlineAt, String reason,
			Integer rating, String text) {
		User owner = booking.getEquipment().getOwner();
		User renter = booking.getRenter();
		User recipient = toOwner ? owner : renter;
		User other = toOwner ? renter : owner;
		if (recipient.isDeactivated()) {
			log.debug("No '{}' e-mail for booking {}: recipient deactivated", type, booking.getId());
			return;
		}
		events.publishEvent(new BookingNotice(type, booking.getId(), recipient.getId(), toOwner, recipient.getEmail(),
			recipient.getName(), other.getId(), other.getName(), booking.getEquipment().getTitle(), booking.getDateFrom(),
			booking.getDateTo(), booking.period().days(), booking.getUnits(), booking.getHandover(),
			booking.getTotalPrice(), deadlineAt, type == BookingNoticeType.CONFIRMED ? owner.getPhone() : null, reason,
			rating, text));
	}
}
