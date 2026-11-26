package com.bohakka.renthub.service.impl;

import com.bohakka.renthub.dto.ComplaintRequest;
import com.bohakka.renthub.dto.ComplaintResponse;
import com.bohakka.renthub.entity.Booking;
import com.bohakka.renthub.entity.Complaint;
import com.bohakka.renthub.entity.ComplaintStatus;
import com.bohakka.renthub.entity.Equipment;
import com.bohakka.renthub.entity.Role;
import com.bohakka.renthub.entity.User;
import com.bohakka.renthub.exception.BusinessRuleException;
import com.bohakka.renthub.exception.ConflictException;
import com.bohakka.renthub.exception.ForbiddenException;
import com.bohakka.renthub.exception.NotFoundException;
import com.bohakka.renthub.repository.BookingRepository;
import com.bohakka.renthub.repository.ComplaintRepository;
import com.bohakka.renthub.repository.EquipmentRepository;
import com.bohakka.renthub.repository.UserRepository;
import com.bohakka.renthub.service.ComplaintService;
import java.time.Clock;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * {@link ComplaintService} (FR-21). Only an active user with a verified e-mail may complain (D-45); a complaint about
 * a booking comes from one of its sides and is against the other, one about a listing is against its owner. The
 * author's row is locked first, so two clicks cannot open two complaints about the same subject.
 */
@Service
@RequiredArgsConstructor
public class ComplaintServiceImpl implements ComplaintService {

	private final ComplaintRepository complaints;
	private final UserRepository users;
	private final BookingRepository bookings;
	private final EquipmentRepository equipment;
	private final ComplaintMapper mapper;
	private final Clock clock;

	@Override
	@Transactional
	public ComplaintResponse create(Long authorId, ComplaintRequest request) {
		if ((request.bookingId() == null) == (request.equipmentId() == null)) {
			throw new BusinessRuleException("complaint.subject_required", "complaint.subject_required");
		}
		User author = users.lockById(authorId)
			.orElseThrow(() -> new NotFoundException("user.not_found", "user.not_found"));
		if (author.getRole() != Role.USER || !author.isActive()) {
			throw new ForbiddenException("complaint.forbidden", "complaint.forbidden");
		}
		if (!author.isEmailVerified()) {
			throw new ForbiddenException("complaint.email_not_verified", "complaint.email_not_verified");
		}
		Complaint complaint = request.bookingId() != null ? aboutBooking(author, request) : aboutListing(author, request);
		return mapper.toResponse(complaints.save(complaint));
	}

	private Complaint aboutBooking(User author, ComplaintRequest request) {
		Booking booking = bookings.findWithPartiesById(request.bookingId())
			.orElseThrow(() -> new NotFoundException("booking.not_found", "booking.not_found"));
		User other;
		if (booking.isRenter(author.getId())) {
			other = booking.getEquipment().getOwner();
		} else if (booking.isOwner(author.getId())) {
			other = booking.getRenter();
		} else {
			throw new ForbiddenException("booking.not_participant", "booking.not_participant");
		}
		if (complaints.existsByAuthorIdAndBookingIdAndStatus(author.getId(), booking.getId(), ComplaintStatus.NEW)) {
			throw new ConflictException("complaint.already_open", "complaint.already_open");
		}
		return Complaint.aboutBooking(author, booking, other, request.description(), clock.instant());
	}

	private Complaint aboutListing(User author, ComplaintRequest request) {
		Equipment listing = equipment.findById(request.equipmentId())
			.orElseThrow(() -> new NotFoundException("equipment.not_found", "equipment.not_found"));
		if (listing.isOwnedBy(author.getId())) {
			throw new BusinessRuleException("complaint.own_listing", "complaint.own_listing");
		}
		if (complaints.existsByAuthorIdAndEquipmentIdAndStatus(author.getId(), listing.getId(), ComplaintStatus.NEW)) {
			throw new ConflictException("complaint.already_open", "complaint.already_open");
		}
		return Complaint.aboutListing(author, listing, request.description(), clock.instant());
	}
}
