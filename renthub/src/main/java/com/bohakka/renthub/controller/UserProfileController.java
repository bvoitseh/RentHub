package com.bohakka.renthub.controller;

import com.bohakka.renthub.dto.PublicProfileResponse;
import com.bohakka.renthub.service.PublicProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** /api/v1/users/{id} — FR-19 public profile, open to everyone. */
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
class UserProfileController {

	private final PublicProfileService profiles;

	@GetMapping("/{id:\\d+}")
	PublicProfileResponse profile(@PathVariable Long id) {
		return profiles.get(id);
	}
}
