package com.bohakka.renthub.service.impl;

import com.bohakka.renthub.dto.AdminEquipmentResponse;
import com.bohakka.renthub.dto.AdminUserResponse;
import com.bohakka.renthub.dto.ComplaintResponse;
import com.bohakka.renthub.dto.ModerationResult;
import com.bohakka.renthub.dto.PageResponse;
import com.bohakka.renthub.entity.Booking;
import com.bohakka.renthub.entity.BookingStatus;
import com.bohakka.renthub.entity.Complaint;
import com.bohakka.renthub.entity.ComplaintStatus;
import com.bohakka.renthub.entity.Equipment;
import com.bohakka.renthub.entity.EquipmentStatus;
import com.bohakka.renthub.entity.Role;
import com.bohakka.renthub.entity.User;
import com.bohakka.renthub.exception.BusinessRuleException;
import com.bohakka.renthub.exception.NotFoundException;
import com.bohakka.renthub.repository.BookingRepository;
import com.bohakka.renthub.repository.ComplaintRepository;
import com.bohakka.renthub.repository.EquipmentRepository;
import com.bohakka.renthub.repository.UserRepository;
import com.bohakka.renthub.security.SessionTerminator;
import com.bohakka.renthub.service.BookingNotifier;
import com.bohakka.renthub.service.ModerationService;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * {@link ModerationService} (FR-20, FR-22). Blocking a user locks their listings, then the user row, then each affected
 * booking row (D-40 order: listing → users → booking), so no new booking of theirs can slip in concurrently; it then cancels every NEW/CONFIRMED booking that starts after today with {@code cancelledBy =
 * ADMIN} and e-mails the other side; rentals in progress continue. Unblocking restores nothing that was cancelled.
 */
@Service
@RequiredArgsConstructor
public class ModerationServiceImpl implements ModerationService {

	/** FR-20: the cancellation reason shown to the other side. */
	public static final String BLOCK_REASON = "Заблоковано адміністратором";
	private static final int PAGE_SIZE = 20;

	private final ComplaintRepository complaints;
	private final UserRepository users;
	private final EquipmentRepository equipment;
	private final BookingRepository bookings;
	private final ComplaintMapper complaintMapper;
	private final BookingNotifier notifier;
	private final SessionTerminator sessions;
	private final Clock clock;

	@Override
	@Transactional(readOnly = true)
	public PageResponse<ComplaintResponse> complaints(Set<ComplaintStatus> statuses, int page) {
		Set<ComplaintStatus> filter = statuses == null || statuses.isEmpty() ? EnumSet.allOf(ComplaintStatus.class) : statuses;
		return PageResponse.of(complaints.findByStatuses(filter, PageRequest.of(page, PAGE_SIZE)), complaintMapper::toResponse);
	}

	@Override
	@Transactional
	public ComplaintResponse resolve(Long adminId, Long complaintId) {
		Complaint complaint = complaint(complaintId);
		complaint.resolve(users.getReferenceById(adminId), clock.instant());
		return complaintMapper.toResponse(complaint);
	}

	@Override
	@Transactional
	public ComplaintResponse reject(Long adminId, Long complaintId) {
		Complaint complaint = complaint(complaintId);
		complaint.reject(users.getReferenceById(adminId), clock.instant());
		return complaintMapper.toResponse(complaint);
	}

	@Override
	@Transactional(readOnly = true)
	public PageResponse<AdminUserResponse> users(String query, int page) {
		Page<User> found = users.searchUsers(normalize(query), PageRequest.of(page, PAGE_SIZE));
		Map<Long, Long> counts = complaints.countCountableByUser(found.map(User::getId).getContent()).stream()
			.collect(Collectors.toMap(r -> (Long) r[0], r -> (Long) r[1]));
		return PageResponse.of(found, u -> new AdminUserResponse(u.getId(), u.getName(), u.getEmail(), u.getPhone(),
			u.getCity(), u.getOwnerType(), u.isEmailVerified(), u.isBlocked(), u.isDeactivated(),
			counts.getOrDefault(u.getId(), 0L), u.getCreatedAt()));
	}

	@Override
	@Transactional
	public ModerationResult blockUser(Long userId) {
		equipment.lockByOwnerId(userId);
		User user = lockUser(userId);
		user.block();
		sessions.expireAllSessions(userId);
		int cancelled = cancelFuture(bookings.findFutureIdsForUser(userId, BookingStatus.HOLDING_UNITS, today()),
			b -> !b.isOwner(userId));
		return new ModerationResult(userId, true, cancelled);
	}

	@Override
	@Transactional
	public ModerationResult unblockUser(Long userId) {
		lockUser(userId).unblock();
		return new ModerationResult(userId, false, 0);
	}

	@Override
	@Transactional(readOnly = true)
	public PageResponse<AdminEquipmentResponse> equipment(String query, int page) {
		return PageResponse.of(equipment.searchAll(normalize(query), PageRequest.of(page, PAGE_SIZE)),
			e -> new AdminEquipmentResponse(e.getId(), e.getTitle(), e.getCategory().getName(), e.getPricePerDay(),
				e.getStatus(), e.getOwner().getId(), e.getOwner().getName(), e.getOwner().isBlocked(), e.getCreatedAt()));
	}

	@Override
	@Transactional
	public ModerationResult blockEquipment(Long equipmentId) {
		Equipment listing = lockListing(equipmentId);
		listing.block(clock.instant());
		int cancelled = cancelFuture(bookings.findFutureIdsForEquipment(equipmentId, BookingStatus.HOLDING_UNITS, today()),
			b -> false);
		return new ModerationResult(equipmentId, true, cancelled);
	}

	@Override
	@Transactional
	public ModerationResult unblockEquipment(Long equipmentId) {
		Equipment listing = lockListing(equipmentId);
		listing.unblock(clock.instant());
		return new ModerationResult(equipmentId, listing.getStatus() == EquipmentStatus.BLOCKED, 0);
	}

	/**
	 * Cancels the bookings (each row locked) and tells the other side; {@code notifyOwner} decides per booking whether
	 * that is the owner (the blocked user was the renter) or the renter.
	 */
	private int cancelFuture(List<Long> ids, Predicate<Booking> notifyOwner) {
		LocalDate today = today();
		Instant now = clock.instant();
		int cancelled = 0;
		for (Long id : ids) {
			Booking booking = bookings.lockById(id).orElse(null);
			if (booking == null || !BookingStatus.HOLDING_UNITS.contains(booking.getStatus())
					|| !today.isBefore(booking.getDateFrom())) {
				continue;
			}
			booking.cancelByAdmin(BLOCK_REASON, today, now);
			notifier.adminCancelled(booking, notifyOwner.test(booking));
			cancelled++;
		}
		return cancelled;
	}

	private User lockUser(Long userId) {
		User user = users.lockById(userId)
			.orElseThrow(() -> new NotFoundException("user.not_found", "user.not_found"));
		if (user.getRole() != Role.USER) {
			throw new BusinessRuleException("admin.cannot_block_admin", "admin.cannot_block_admin");
		}
		return user;
	}

	private Equipment lockListing(Long equipmentId) {
		return equipment.lockById(equipmentId)
			.orElseThrow(() -> new NotFoundException("equipment.not_found", "equipment.not_found"));
	}

	private Complaint complaint(Long id) {
		return complaints.findById(id)
			.orElseThrow(() -> new NotFoundException("complaint.not_found", "complaint.not_found"));
	}

	private LocalDate today() {
		return LocalDate.now(clock);
	}

	private static String normalize(String query) {
		return query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
	}
}
