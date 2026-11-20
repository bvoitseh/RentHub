package com.bohakka.renthub.service.impl;

import com.bohakka.renthub.dto.PhotoResponse;
import com.bohakka.renthub.entity.Equipment;
import com.bohakka.renthub.event.StoredObjectUploaded;
import com.bohakka.renthub.event.StoredObjectsRemoved;
import com.bohakka.renthub.exception.BusinessRuleException;
import com.bohakka.renthub.repository.EquipmentRepository;
import com.bohakka.renthub.service.ObjectStorage;
import com.bohakka.renthub.service.PhotoService;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.Clock;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.unit.DataSize;
import org.springframework.web.multipart.MultipartFile;

/**
 * {@link PhotoService}: the file is checked by its content (JPEG/PNG/WebP) and size, attached to the listing
 * (which enforces the 1-5 rule), then stored; storage clean-up after commit or rollback is event-driven.
 */
@Service
@RequiredArgsConstructor
public class PhotoServiceImpl implements PhotoService {

	private static final long MAX_BYTES = DataSize.ofMegabytes(5).toBytes();

	private final EquipmentRepository equipment;
	private final EquipmentAccess access;
	private final ObjectStorage storage;
	private final EquipmentMapper mapper;
	private final ApplicationEventPublisher events;
	private final Clock clock;

	@Override
	@Transactional
	public List<PhotoResponse> upload(Long ownerId, Long equipmentId, MultipartFile file) {
		Equipment listing = access.owned(ownerId, equipmentId);
		if (file == null || file.isEmpty()) {
			throw new BusinessRuleException("photo.empty", "photo.empty");
		}
		if (file.getSize() > MAX_BYTES) {
			throw new BusinessRuleException("photo.too_large", "photo.too_large", 5);
		}
		byte[] content = bytes(file);
		ImageFormat format = ImageFormat.detect(content)
			.orElseThrow(() -> new BusinessRuleException("photo.unsupported_type", "photo.unsupported_type"));
		String key = "equipment/%d/%s.%s".formatted(listing.getId(), UUID.randomUUID(), format.extension());
		listing.addPhoto(key, clock.instant());
		storage.put(key, content, format.contentType());
		events.publishEvent(new StoredObjectUploaded(key));
		equipment.flush();
		return mapper.photos(listing);
	}

	@Override
	@Transactional
	public List<PhotoResponse> delete(Long ownerId, Long equipmentId, Long photoId) {
		Equipment listing = access.owned(ownerId, equipmentId);
		String key = listing.removePhoto(photoId, clock.instant());
		events.publishEvent(new StoredObjectsRemoved(List.of(key)));
		equipment.flush();
		return mapper.photos(listing);
	}

	private static byte[] bytes(MultipartFile file) {
		try {
			return file.getBytes();
		} catch (IOException ex) {
			throw new UncheckedIOException(ex);
		}
	}
}
