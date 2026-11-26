package com.bohakka.renthub.service;

import com.bohakka.renthub.RenthubApplication;
import com.bohakka.renthub.dto.EmailMessage;
import com.bohakka.renthub.entity.Handover;
import com.bohakka.renthub.event.BookingNotice;
import com.bohakka.renthub.properties.AppProperties;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Sends the FR-15a / FR-15b booking e-mails after the booking transaction commits, on the async pool, so they never
 * block or fail the request. Texts are {@code mail.booking.<type>.subject/body} in messages_uk.properties; the body
 * arguments are, in order: {0} recipient, {1} other party, {2} listing, {3} period, {4} length («3 доби»), {5} units,
 * {6} handover, {7} total, {8} answer deadline, {9} owner's phone, {10} cancellation reason, {11} link to the
 * caller's booking page, {12} catalogue link.
 */
@Component
@RequiredArgsConstructor
public class BookingMailListener {

	private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("d MMMM yyyy", RenthubApplication.UI_LOCALE);
	private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("d MMMM yyyy, HH:mm",
		RenthubApplication.UI_LOCALE);

	private final MailDelivery delivery;
	private final MessageSource messages;
	private final AppProperties app;

	@Async
	@TransactionalEventListener
	public void onBookingNotice(BookingNotice notice) {
		String prefix = "mail.booking." + notice.type().key();
		Object[] args = {notice.recipientName(), notice.otherPartyName(), notice.equipmentTitle(),
			period(notice.dateFrom(), notice.dateTo()), days(notice.days()), String.valueOf(notice.units()),
			handover(notice.handover()), money(notice.totalPrice()), deadline(notice.deadline()),
			orDash(notice.ownerPhone()), orDash(notice.cancelReason()),
			app.baseUrl() + (notice.type().toOwner() ? "/my/requests" : "/my/bookings"), app.baseUrl() + "/catalog"};
		EmailMessage message = new EmailMessage(notice.recipientEmail(), text(prefix + ".subject", args),
			text(prefix + ".body", args));
		delivery.deliver(message, "booking " + notice.bookingId() + " " + notice.type());
	}

	private String text(String key, Object... args) {
		return messages.getMessage(key, args, RenthubApplication.UI_LOCALE);
	}

	private static String period(LocalDate from, LocalDate to) {
		return from.equals(to) ? DATE.format(from) : DATE.format(from) + " — " + DATE.format(to);
	}

	/** Ukrainian plural of «доба»: 1, 21 доба; 2–4, 22–24 доби; 5–20, 25–30 діб. */
	private static String days(int n) {
		int lastTwo = n % 100;
		int last = n % 10;
		String word = lastTwo >= 11 && lastTwo <= 14 ? "діб" : last == 1 ? "доба" : last >= 2 && last <= 4 ? "доби" : "діб";
		return n + " " + word;
	}

	private static String handover(Handover handover) {
		return handover == Handover.PICKUP ? "самовивіз" : "доставка";
	}

	private static String money(BigDecimal value) {
		NumberFormat format = NumberFormat.getNumberInstance(RenthubApplication.UI_LOCALE);
		format.setMinimumFractionDigits(2);
		format.setMaximumFractionDigits(2);
		return format.format(value) + " ₴";
	}

	private String deadline(Instant instant) {
		return instant == null ? "—" : DATE_TIME.format(instant.atZone(ZoneId.of(app.timeZone())));
	}

	private static String orDash(String value) {
		return value == null || value.isBlank() ? "—" : value;
	}
}
