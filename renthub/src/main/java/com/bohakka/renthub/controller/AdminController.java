package com.bohakka.renthub.controller;

import com.bohakka.renthub.dto.AdminEquipmentResponse;
import com.bohakka.renthub.dto.AdminUserResponse;
import com.bohakka.renthub.dto.ComplaintResponse;
import com.bohakka.renthub.dto.ModerationResult;
import com.bohakka.renthub.dto.PageResponse;
import com.bohakka.renthub.entity.ComplaintStatus;
import com.bohakka.renthub.security.AppUserDetails;
import com.bohakka.renthub.service.ModerationService;
import jakarta.validation.constraints.Min;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** /api/v1/admin — complaints and moderation (FR-20, FR-22), ADMIN only (NFR-11). */
@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
class AdminController {

	private final ModerationService moderation;

	@GetMapping("/complaints")
	PageResponse<ComplaintResponse> complaints(@RequestParam(required = false) Set<ComplaintStatus> status,
			@RequestParam(defaultValue = "0") @Min(0) int page) {
		return moderation.complaints(status, page);
	}

	@PostMapping("/complaints/{id:\\d+}/resolve")
	ComplaintResponse resolve(@AuthenticationPrincipal AppUserDetails principal, @PathVariable Long id) {
		return moderation.resolve(principal.getId(), id);
	}

	@PostMapping("/complaints/{id:\\d+}/reject")
	ComplaintResponse reject(@AuthenticationPrincipal AppUserDetails principal, @PathVariable Long id) {
		return moderation.reject(principal.getId(), id);
	}

	@GetMapping("/users")
	PageResponse<AdminUserResponse> users(@RequestParam(defaultValue = "") String q,
			@RequestParam(defaultValue = "0") @Min(0) int page) {
		return moderation.users(q, page);
	}

	@PostMapping("/users/{id:\\d+}/block")
	ModerationResult blockUser(@PathVariable Long id) {
		return moderation.blockUser(id);
	}

	@PostMapping("/users/{id:\\d+}/unblock")
	ModerationResult unblockUser(@PathVariable Long id) {
		return moderation.unblockUser(id);
	}

	@GetMapping("/equipment")
	PageResponse<AdminEquipmentResponse> equipment(@RequestParam(defaultValue = "") String q,
			@RequestParam(defaultValue = "0") @Min(0) int page) {
		return moderation.equipment(q, page);
	}

	@PostMapping("/equipment/{id:\\d+}/block")
	ModerationResult blockEquipment(@PathVariable Long id) {
		return moderation.blockEquipment(id);
	}

	@PostMapping("/equipment/{id:\\d+}/unblock")
	ModerationResult unblockEquipment(@PathVariable Long id) {
		return moderation.unblockEquipment(id);
	}
}
