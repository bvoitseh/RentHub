package com.bohakka.renthub.controller;

import com.bohakka.renthub.dto.PhotoResponse;
import com.bohakka.renthub.security.AppUserDetails;
import com.bohakka.renthub.service.PhotoService;
import java.util.List;
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

/** /api/v1/equipment/{id}/photos — FR-4a upload (multipart field "file") and removal; both return all photos. */
@RestController
@RequestMapping("/api/v1/equipment/{equipmentId}/photos")
@RequiredArgsConstructor
class EquipmentPhotoController {

	private final PhotoService photos;

	@PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@ResponseStatus(HttpStatus.CREATED)
	List<PhotoResponse> upload(@AuthenticationPrincipal AppUserDetails principal, @PathVariable Long equipmentId,
			@RequestPart("file") MultipartFile file) {
		return photos.upload(principal.getId(), equipmentId, file);
	}

	@DeleteMapping("/{photoId}")
	List<PhotoResponse> delete(@AuthenticationPrincipal AppUserDetails principal, @PathVariable Long equipmentId,
			@PathVariable Long photoId) {
		return photos.delete(principal.getId(), equipmentId, photoId);
	}
}
