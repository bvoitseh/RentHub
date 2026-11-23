package com.bohakka.renthub.service.impl;

import com.bohakka.renthub.entity.Booking;
import com.bohakka.renthub.entity.User;
import com.bohakka.renthub.event.BookingNotice;
import com.bohakka.renthub.event.BookingNoticeType;
import com.bohakka.renthub.service.BookingNotifier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * {@link BookingNotifier}: snapshots the booking into a {@link BookingNotice} event (Observer). A deactivated recipient
 * gets no e-mail (FR-3c).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BookingNotifierImpl implements BookingNotifier {

	private final ApplicationEventPublisher events;
	private final BookingDeadline deadline;

	@Override
	public void notify(Booking booking, BookingNoticeType type) {
		User owner = booking.getEquipment().getOwner();
		User renter = booking.getRenter();
		User recipient = type.toOwner() ? owner : renter;
		User other = type.toOwner() ? renter : owner;
		if (recipient.isDeactivated()) {
			log.debug("No '{}' e-mail for booking {}: recipient deactivated", type, booking.getId());
			return;
		}
		events.publishEvent(new BookingNotice(type, booking.getId(), recipient.getEmail(), recipient.getName(),
			other.getName(), booking.getEquipment().getTitle(), booking.getDateFrom(), booking.getDateTo(),
			booking.period().days(), booking.getUnits(), booking.getHandover(), booking.getTotalPrice(),
			type == BookingNoticeType.REQUESTED ? deadline.respondBy(booking) : null,
			type == BookingNoticeType.CONFIRMED ? owner.getPhone() : null,
			type == BookingNoticeType.CANCELLED_BY_OWNER ? booking.getCancelReason() : null));
	}
}
