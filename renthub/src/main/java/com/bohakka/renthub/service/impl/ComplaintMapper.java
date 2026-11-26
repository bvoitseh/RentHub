package com.bohakka.renthub.service.impl;

import com.bohakka.renthub.dto.ComplaintResponse;
import com.bohakka.renthub.entity.Complaint;
import com.bohakka.renthub.entity.User;
import org.springframework.stereotype.Component;

/** Complaint → API record, inside the service transaction. */
@Component
public class ComplaintMapper {

	public ComplaintResponse toResponse(Complaint c) {
		return new ComplaintResponse(c.getId(), person(c.getAuthor()), person(c.getAgainstUser()),
			c.getBooking() == null ? null : c.getBooking().getId(),
			c.getEquipment() == null ? null : new ComplaintResponse.Item(c.getEquipment().getId(), c.getEquipment().getTitle()),
			c.getDescription(), c.getStatus(), c.getCreatedAt(), c.getDecidedAt());
	}

	private static ComplaintResponse.Person person(User user) {
		return new ComplaintResponse.Person(user.getId(), user.getName());
	}
}
