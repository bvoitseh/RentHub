package com.bohakka.renthub.service;

import com.bohakka.renthub.entity.Review;
import com.bohakka.renthub.properties.ReviewProperties;
import com.bohakka.renthub.repository.ReviewRepository;
import com.bohakka.renthub.repository.UserRepository;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * The scheduler actor for FR-18: a review whose window has ended without the other side's review is published on its
 * own. Each target's user row is locked before its rating changes (FR-19); the target is e-mailed after the commit.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReviewPublicationJob {

	private final ReviewRepository reviews;
	private final UserRepository users;
	private final BookingNotifier notifier;
	private final ReviewProperties properties;
	private final Clock clock;

	@Scheduled(cron = "${app.review.publish-cron}", zone = "${app.time-zone}")
	@Transactional
	public void publishExpiredWindows() {
		Instant now = clock.instant();
		List<Review> due = reviews.findUnpublishedCompletedBefore(now.minus(properties.window()));
		for (Review review : due) {
			users.lockById(review.getTarget().getId());
			if (review.publish(now)) {
				notifier.reviewPublished(review);
			}
		}
		if (!due.isEmpty()) {
			log.info("Published {} review(s) whose window ended", due.size());
		}
	}
}
