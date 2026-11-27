package com.bohakka.renthub.service;

import com.bohakka.renthub.dto.VideoResponse;
import org.springframework.web.multipart.MultipartFile;

/** FR-4a (D-52): the owner uploads, replaces or removes the listing's video. */
public interface VideoService {

	VideoResponse upload(Long ownerId, Long equipmentId, MultipartFile file);

	void delete(Long ownerId, Long equipmentId);
}
