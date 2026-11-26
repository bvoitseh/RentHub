package com.bohakka.renthub.service.impl;

import com.bohakka.renthub.dto.BookingReviewsResponse;
import com.bohakka.renthub.dto.PageResponse;
import com.bohakka.renthub.dto.ReviewRequest;
import com.bohakka.renthub.dto.ReviewResponse;
import com.bohakka.renthub.entity.Booking;
import com.bohakka.renthub.entity.BookingStatus;
import com.bohakka.renthub.entity.Review;
import com.bohakka.renthub.entity.User;
import com.bohakka.renthub.exception.BusinessRuleException;
import com.bohakka.renthub.exception.ConflictException;
import com.bohakka.renthub.exception.ForbiddenException;
import com.bohakka.renthub.exception.NotFoundException;
import com.bohakka.renthub.properties.ReviewProperties;
import com.bohakka.renthub.repository.BookingParties;
import com.bohakka.renthub.repository.BookingRepository;
import com.bohakka.renthub.repository.ReviewRepository;
import com.bohakka.renthub.repository.UserRepository;
import com.bohakka.renthub.service.BookingNotifier;
import com.bohakka.renthub.service.ReviewService;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.TreeSet;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * {@link ReviewService}. Submitting locks both sides' user rows (ascending id) and then the booking row (lock order
 * of D-40), so two sides submitting at the same moment cannot both miss each other and publication updates the
 * ratings without lost updates (FR-18, FR-19).
 */
@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

	private static final int PAGE_SIZE = 10;

	private final ReviewRepository reviews;
	private final BookingRepository bookings;
	private final UserRepository users;
	private final ReviewMapper mapper;
	private final BookingNotifier notifier;
	private final ReviewProperties properties;
	private final Clock clock;

	@Override
	@Transactional
	public ReviewResponse create(Long authorId, Long bookingId, ReviewRequest request) {
		BookingParties parties = bookings.findPartiesById(bookingId).orElseThrow(ReviewServiceImpl::bookingNotFound);
		if (!authorId.equals(parties.renterId()) && !authorId.equals(parties.ownerId())) {
			throw new ForbiddenException("booking.not_participant", "booking.not_participant");
		}
		for (Long userId : new TreeSet<>(List.of(parties.renterId(), parties.ownerId()))) {
			users.lockById(userId).orElseThrow(ReviewServiceImpl::bookingNotFound);
		}
		Booking booking = bookings.lockById(bookingId).orElseThrow(ReviewServiceImpl::bookingNotFound);
		Instant now = clock.instant();
		if (!isOpen(booking, now)) {
			throw booking.getStatus() == BookingStatus.COMPLETED
				? new BusinessRuleException("review.window_closed", "review.window_closed")
				: new ConflictException("review.booking_not_completed", "review.booking_not_completed");
		}
		List<Review> existing = reviews.findByBookingId(bookingId);
		if (existing.stream().anyMatch(r -> r.getAuthor().getId().equals(authorId))) {
			throw new ConflictException("review.already_exists", "review.already_exists");
		}
		User author = booking.isRenter(authorId) ? booking.getRenter() : booking.getEquipment().getOwner();
		User target = booking.isRenter(authorId) ? booking.getEquipment().getOwner() : booking.getRenter();
		Review review = reviews.save(new Review(booking, author, target, request.rating(), request.comment(), now));
		if (!existing.isEmpty()) {
			publish(review, now);
			existing.forEach(other -> publish(other, now));
		}
		return mapper.toResponse(review, authorId);
	}

	@Override
	@Transactional(readOnly = true)
	public BookingReviewsResponse forBooking(Long userId, Long bookingId) {
		Booking booking = bookings.findWithPartiesById(bookingId).orElseThrow(ReviewServiceImpl::bookingNotFound);
		if (!booking.isRenter(userId) && !booking.isOwner(userId)) {
			throw new ForbiddenException("booking.not_participant", "booking.not_participant");
		}
		List<Review> all = reviews.findByBookingId(bookingId);
		Review mine = all.stream().filter(r -> r.getAuthor().getId().equals(userId)).findFirst().orElse(null);
		Review theirs = all.stream().filter(r -> !r.getAuthor().getId().equals(userId) && r.isPublished())
			.findFirst().orElse(null);
		Instant until = booking.getCompletedAt() == null ? null : booking.getCompletedAt().plus(properties.window());
		boolean canReview = mine == null && isOpen(booking, clock.instant());
		return new BookingReviewsResponse(mine == null ? null : mapper.toResponse(mine, userId),
			theirs == null ? null : mapper.toResponse(theirs, userId), canReview, until);
	}

	@Override
	@Transactional(readOnly = true)
	public PageResponse<ReviewResponse> forUser(Long targetId, Long viewerId, int page) {
		return PageResponse.of(reviews.findByTargetIdAndPublishedAtIsNotNullOrderByPublishedAtDesc(targetId,
			PageRequest.of(page, PAGE_SIZE)), r -> mapper.toResponse(r, viewerId));
	}

	@Override
	@Transactional
	public ReviewResponse reply(Long userId, Long reviewId, String text) {
		Review review = reviews.lockById(reviewId)
			.orElseThrow(() -> new NotFoundException("review.not_found", "review.not_found"));
		review.reply(userId, text, clock.instant(), properties.replyWindow());
		notifier.reviewReplied(review);
		return mapper.toResponse(review, userId);
	}

	/** Publishes and notifies the target; the caller holds the target's user row lock. */
	private void publish(Review review, Instant now) {
		if (review.publish(now)) {
			notifier.reviewPublished(review);
		}
	}

	/** FR-18: reviews are accepted for a COMPLETED booking until completion + window. */
	private boolean isOpen(Booking booking, Instant now) {
		return booking.getStatus() == BookingStatus.COMPLETED && booking.getCompletedAt() != null
			&& now.isBefore(booking.getCompletedAt().plus(properties.window()));
	}

	private static NotFoundException bookingNotFound() {
		return new NotFoundException("booking.not_found", "booking.not_found");
	}
}
