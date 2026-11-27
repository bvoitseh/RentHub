package com.bohakka.renthub.controller;

import com.bohakka.renthub.dto.VideoResponse;
import com.bohakka.renthub.security.AppUserDetails;
import com.bohakka.renthub.service.VideoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/** /api/v1/equipment/{id}/video — FR-4a (D-52) upload or replace (multipart field "file") and removal; owner only. */
@RestController
@RequestMapping("/api/v1/equipment/{equipmentId}/video")
@RequiredArgsConstructor
class EquipmentVideoController {

	private final VideoService videos;

	@PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@ResponseStatus(HttpStatus.CREATED)
	VideoResponse upload(@AuthenticationPrincipal AppUserDetails principal, @PathVariable Long equipmentId,
			@RequestPart("file") MultipartFile file) {
		return videos.upload(principal.getId(), equipmentId, file);
	}

	@DeleteMapping
	@ResponseStatus(HttpStatus.NO_CONTENT)
	void delete(@AuthenticationPrincipal AppUserDetails principal, @PathVariable Long equipmentId) {
		videos.delete(principal.getId(), equipmentId);
	}
}
