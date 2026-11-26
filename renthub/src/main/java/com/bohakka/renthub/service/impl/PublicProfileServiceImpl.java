package com.bohakka.renthub.service.impl;

import com.bohakka.renthub.dto.PublicProfileResponse;
import com.bohakka.renthub.entity.CancelledBy;
import com.bohakka.renthub.entity.ComplaintStatus;
import com.bohakka.renthub.entity.Role;
import com.bohakka.renthub.entity.User;
import com.bohakka.renthub.exception.NotFoundException;
import com.bohakka.renthub.repository.BookingRepository;
import com.bohakka.renthub.repository.ComplaintRepository;
import com.bohakka.renthub.repository.UserRepository;
import com.bohakka.renthub.service.PublicProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * {@link PublicProfileService}: name, city, account type and FR-19 figures; no e-mail or phone (NFR-5). The admin has
 * no public profile. Rating and review count from published reviews (FR-19); complaints exclude rejected ones (FR-21).
 */
@Service
@RequiredArgsConstructor
public class PublicProfileServiceImpl implements PublicProfileService {

	private final UserRepository users;
	private final BookingRepository bookings;
	private final ComplaintRepository complaints;

	@Override
	@Transactional(readOnly = true)
	public PublicProfileResponse get(Long userId) {
		User user = users.findById(userId)
			.filter(u -> u.getRole() == Role.USER)
			.orElseThrow(() -> new NotFoundException("user.not_found", "user.not_found"));
		return new PublicProfileResponse(user.getId(), user.getName(), user.getCity(), user.getOwnerType(),
			user.getCompanyName(), user.isDeactivated(), EquipmentMapper.rating(user), user.getReviewCount(),
			Math.toIntExact(bookings.countCancelledBy(user.getId(), CancelledBy.OWNER)),
			Math.toIntExact(complaints.countByAgainstUserIdAndStatusNot(user.getId(), ComplaintStatus.REJECTED)),
			user.getCreatedAt());
	}
}
