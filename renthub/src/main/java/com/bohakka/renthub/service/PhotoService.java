package com.bohakka.renthub.service;

import com.bohakka.renthub.dto.PhotoResponse;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

/** FR-4a: listing photos (JPEG/PNG/WebP, at most 5 MB, 1-5 per listing); both calls return the listing's photos. */
public interface PhotoService {

	List<PhotoResponse> upload(Long ownerId, Long equipmentId, MultipartFile file);

	List<PhotoResponse> delete(Long ownerId, Long equipmentId, Long photoId);
}
