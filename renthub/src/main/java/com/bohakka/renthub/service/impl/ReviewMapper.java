package com.bohakka.renthub.service.impl;

import com.bohakka.renthub.dto.ReviewResponse;
import com.bohakka.renthub.entity.Equipment;
import com.bohakka.renthub.entity.Review;
import com.bohakka.renthub.entity.User;
import com.bohakka.renthub.properties.ReviewProperties;
import java.time.Clock;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Maps reviews to API records; {@code canReply} is evaluated for the viewer (FR-18). */
@Component
@RequiredArgsConstructor
public class ReviewMapper {

	private final ReviewProperties properties;
	private final Clock clock;

	public ReviewResponse toResponse(Review r, Long viewerId) {
		Equipment e = r.getBooking().getEquipment();
		boolean canReply = viewerId != null && r.canReply(viewerId, clock.instant(), properties.replyWindow());
		return new ReviewResponse(r.getId(), r.getBooking().getId(), new ReviewResponse.Item(e.getId(), e.getTitle()),
			person(r.getAuthor()), person(r.getTarget()), r.getRating(), r.getComment(), r.getCreatedAt(),
			r.isPublished(), r.getPublishedAt(), r.getReply(), r.getRepliedAt(), canReply);
	}

	private static ReviewResponse.Person person(User user) {
		return new ReviewResponse.Person(user.getId(), user.getName(), user.isDeactivated());
	}
}
